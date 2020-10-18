package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.addons.PlayerPermissions;
import alemiz.bettersurvival.commands.SellAllCommand;
import alemiz.bettersurvival.commands.SellCommand;
import alemiz.bettersurvival.commands.SellHandCommand;
import alemiz.bettersurvival.commands.ShopCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntitySpawnEvent;
import cn.nukkit.event.inventory.StartBrewEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.onebone.economyapi.EconomyAPI;

import java.text.DecimalFormat;
import java.util.*;


public class SurvivalShop extends Addon {

    public Map<String, ShopCategory> categories = new HashMap<>();
    private Map<String, JsonObject> subCategories = new HashMap<>();

    private Position shopSpawn;
    private SellManager sellManager;

    private List<String> npcRemovers = new ArrayList<>();

    private SmithShop smithShop = null;

    public SurvivalShop(String path){
        super("survivalshop", path);
    }

    @Override
    public void postLoad() {
        JsonElement json = this.getJsonResource("shop.json");
        if (!json.isJsonObject()){
            this.setEnabled(false);
            return;
        }
        JsonObject shopData = json.getAsJsonObject();

        if (shopData.has("subcategories") && shopData.get("subcategories").isJsonObject()){
            JsonObject subCategories = shopData.getAsJsonObject("subcategories");
            for (Map.Entry<String, JsonElement> entry : subCategories.entrySet()){
                if (!entry.getValue().isJsonObject()) continue;
                this.subCategories.put(entry.getKey().toLowerCase(), (JsonObject) entry.getValue());
            }
        }

        JsonObject categories = shopData.getAsJsonObject("categories");
        for (Map.Entry<String, JsonElement> entry: categories.entrySet()){
            String categoryName = entry.getKey();
            JsonElement category = entry.getValue();

            if (category.isJsonObject()){
                ShopCategory shopCategory = new ShopCategory(categoryName, (JsonObject) category, this);
                this.categories.put(categoryName.toLowerCase(), shopCategory);
            }
        }

        this.sellManager = new SellManager(this);
        this.shopSpawn = this.plugin.getServer().getDefaultLevel().getSafeSpawn();

        if (configFile.exists("shopSpawn")){
            String[] data = configFile.getString("shopSpawn").split(",");

            Level level = this.plugin.getServer().getLevelByName(data[3]);
            this.shopSpawn = new Position(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), level);
        }

        if (configFile.getBoolean("enableSmith")){
            this.smithShop = new SmithShop(this);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("enableSmith", true);

            configFile.set("signTitle", "§f[Shop]");

            configFile.set("shopCreatePermission", "bettersurvival.shop.create");
            configFile.set("shopRemovePermission", "bettersurvival.shop.remove");
            configFile.set("shopVipPermission", "bettersurvival.shop.vip");
            configFile.set("shopManagePermission", "bettersurvival.shop.manage");
            configFile.set("enchantPermission", "bettersurvival.shop.enchant");

            configFile.set("wrongFormat", "§c»§7Shop was not created! Use following format: Line 1: shop Line 2: category");
            configFile.set("shopSet", "§6»§7Shop was created successfully!");

            configFile.set("messageSuccess", "§a»§7You have successfully bought §6{item}§7!");
            configFile.set("messageFail", "§c»§7You dont have §e{money}§7 coins to buy §6{item}§7!");

            configFile.set("sellAllMessage", "§6»§7All your inventory was sold. Total income: §e{money}§7!");
            configFile.set("sellHandMessage", "§6»§7All items same as item in your hand were sold. Total income: §e{money}§7!");

            configFile.set("allowBrewing", false);
            configFile.save();
        }

        this.saveFromResources("shop.json");
    }

    @Override
    public void registerCommands() {
        this.registerCommand("shop", new ShopCommand("shop", this));
        this.registerCommand("sell", new SellCommand("sell", this));
        this.registerCommand("sellall", new SellAllCommand("sellall", this));
        this.registerCommand("sellhand", new SellHandCommand("sellhand", this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if (Addon.getAddon(PlayerPermissions.class) != null && (Addon.getAddon(PlayerPermissions.class) instanceof PlayerPermissions)){
            ((PlayerPermissions) Addon.getAddon(PlayerPermissions.class)).addPermission(player, configFile.getString("enchantPermission"));
        }else {
            player.addAttachment(plugin, configFile.getString("enchantPermission"), true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if (entity.namedTag.getByte("shop_npc") == 0) return;

        if ((event instanceof EntityDamageByEntityEvent) && (((EntityDamageByEntityEvent) event).getDamager() instanceof Player)){
            Player player = (Player) ((EntityDamageByEntityEvent) event).getDamager();

            if (this.npcRemovers.contains(player.getName())){
                this.npcRemovers.remove(player.getName());

                player.sendMessage("§6»§7Shop npc removed successfully!");
                entity.close();
                event.setCancelled(true);
                return;
            }

            if (this.smithShop != null) this.smithShop.onDamage((EntityDamageByEntityEvent) event);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event){
        Entity entity = event.getEntity();

        if (entity.namedTag.getByte("shop_npc") == 0) return;
        entity.setNameTagAlwaysVisible(true);

        if (this.smithShop != null) this.smithShop.onSpawn(entity);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if ((block.getId() != Block.WALL_SIGN && block.getId() != Block.SIGN_POST) || !(player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)) return;
        BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);
        String[] oldLines = sign.getText();

        if (!this.hasCategory(oldLines[1])){
            return;
        }

        if (this.isShopSign(sign)){
            if (!player.hasPermission(configFile.getString("shopRemovePermission"))){
                event.setCancelled(true);
            }
            return;
        }

        if (!oldLines[0].equals("shop") || !player.hasPermission(configFile.getString("shopCreatePermission"))){
            player.sendMessage("§c»§7You dont have permission to create shop!");
            return;
        }

        if (oldLines[1].equals("")){
            player.sendMessage(this.messageFormat(player, "wrongFormat"));
            return;
        }

        String[] lines = new String[]{configFile.getString("signTitle"), "§e"+oldLines[1], "", ""};
        if (oldLines[2].equals("vip")){
            lines[2] = "§5VIP Only";
        }

        event.setCancelled(true);
        sign.setText(lines);

        player.sendMessage(this.messageFormat(player, "shopSet"));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if (!this.configFile.getBoolean("allowBrewing") && event.getBlock().getId() == Block.BREWING_STAND_BLOCK){
            event.setCancelled(true);
        }

        if (this.smithShop != null){
            if (event.getBlock().getId() == Block.ANVIL){
                event.setCancelled(true);
            }

            if (event.getItem() != null){
                CompoundTag namedTag = event.getItem().getNamedTag();
                if (namedTag != null && namedTag.getByte("enchant_orb") == 1){
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getBlock();

        if ((block.getId() != Block.WALL_SIGN && block.getId() != Block.SIGN_POST) || !(player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)) return;
        BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);

        if (!this.isShopSign(sign)) return;
        event.setCancelled(true);

        String[] lines = sign.getText();
        if (lines[2].equals("§5VIP Only") && !player.hasPermission(configFile.getString("shopVipPermission"))){
            player.sendMessage("§c»§7This shop is for VIP members only!");
            return;
        }

        ShopCategory category = this.getCategory(lines[1].substring(2).toLowerCase());
        if (category == null) return;

        category.sendForm(player);
    }

    @EventHandler
    public void onBrew(StartBrewEvent event){
        if (!this.configFile.getBoolean("allowBrewing")){
            event.setCancelled(true);
        }
    }

    public void setShopSpawn(Position pos){
        DecimalFormat format = new DecimalFormat("0.0");

        String[] data = new String[]{format.format(pos.getX()), format.format(pos.getY()), format.format(pos.getZ()), pos.getLevel().getFolderName()};
        this.shopSpawn = pos.clone();

        this.configFile.set("shopSpawn", String.join(",", data));
        this.configFile.save();
    }

    public Position getShopSpawn() {
        return this.shopSpawn;
    }

    public void teleportToSpawn(Player player){
        if (player == null) return;
        player.teleport(this.shopSpawn.add(0, 0.5));
    }

    public boolean isShopSign(BlockEntitySign sign){
        String[] lines = sign.getText();
        return lines[0].equals(configFile.getString("signTitle")) && lines.length >= 2 && this.hasCategory(lines[1].substring(2).toLowerCase());
    }

    public boolean hasCategory(String category){
        return this.getCategory(category) != null;
    }

    public ShopCategory getCategory(String category){
        return this.categories.getOrDefault(category.toLowerCase(), null);
    }

    public JsonObject getSubCategoryJson(String subName){
        return this.subCategories.get(subName.toLowerCase());
    }

    public Map<String, ShopCategory> getCategories() {
        return this.categories;
    }

    public SellManager getSellManager() {
        return this.sellManager;
    }

    public SmithShop getSmithShop() {
        return smithShop;
    }

    public void addRemover(Player player){
        if (player == null) return;

        player.sendMessage("§6»§7Hit shop npc to remove!");
        this.npcRemovers.add(player.getName());
    }

    public void removeRemover(Player player){
        if (player == null) return;
        this.npcRemovers.remove(player.getName());
    }

    public List<String> getNpcRemovers() {
        return npcRemovers;
    }

    public String messageFormat(Player player, String messageKey){
        return this.messageFormat(player, messageKey, (int) EconomyAPI.getInstance().myMoney(player));
    }

    public String messageFormat(Player player, String messageKey, int money){
        if (player == null || messageKey == null || configFile.getString(messageKey).equals("")) return "";

        String message = configFile.getString(messageKey);
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{money}", TextUtils.formatBigNumber(money));
        return message;
    }
}

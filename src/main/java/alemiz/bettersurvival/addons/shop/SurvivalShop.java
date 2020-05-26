package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.commands.SellCommand;
import alemiz.bettersurvival.commands.ShopCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntitySpawnEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cubemc.nukkit.connector.modules.Money;
import net.minidev.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;


public class SurvivalShop extends Addon {

    public Map<String, ShopCategory> categories = new HashMap<>();

    private Position shopSpawn;
    private SellManager sellManager;

    private List<String> npcRemovers = new ArrayList<>();

    private SmithShop smithShop = null;

    public SurvivalShop(String path){
        super("survivalshop", path);

        JSONObject shopData = ConfigManager.getInstance().loadJson(ConfigManager.getInstance().ADDONS_PATH+"/shop.json");
        Set<String> categories = shopData.keySet();

        for (String category : categories){
            if (!(shopData.get(category) instanceof JSONObject)) continue;

            ShopCategory shopCategory = new ShopCategory(category, (JSONObject) shopData.get(category), this);
            this.categories.put(category.toLowerCase(), shopCategory);
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

            configFile.set("shopRemovePermission", "bettersurvival.shop.remove");
            configFile.set("shopVipPermission", "bettersurvival.shop.vip");
            configFile.set("shopManagePermission", "bettersurvival.shop.manage");
            configFile.set("enchantPermission", "bettersurvival.shop.enchant");

            configFile.set("wrongFormat", "§c»§7Shop was not created! Use following format: Line 1: shop Line 2: category");
            configFile.set("shopSet", "§6»§7Shop was created successfully!");

            configFile.set("messageSuccess", "§a»§7You have successfully bought §6{item}§7!");
            configFile.set("messageFail", "§e»§7You dont have §e{money}§7 coins to buy §6{item}§7!");
            configFile.save();
        }

        this.saveFromResources("shop.json");
    }

    protected void saveFromResources(String fileName){
        try {
            File shopFile = new File(ConfigManager.getInstance().ADDONS_PATH+"/"+fileName);
            if (!shopFile.exists()){
                shopFile.createNewFile();

                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                OutputStream outputStream = new FileOutputStream(shopFile);
                outputStream.write(buffer);
                inputStream.close();
            }
        }catch (Exception e){
            this.plugin.getLogger().info("§eUnable to save "+fileName+" from resources!");
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("shop", new ShopCommand("shop", this));
        registerCommand("sell", new SellCommand("sell", this));
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

        switch (oldLines[2]){
            case "vip":
                lines[2] = "§5VIP Only";
                break;
            case "subscriber":
                lines[2] = "§bSubscriber Only";
            case "":
                break;
            default:
                //TODO: shop by rank
                break;
        }

        event.setCancelled(true);
        sign.setText(lines);

        player.sendMessage(this.messageFormat(player, "shopSet"));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if (this.smithShop != null && event.getItem() != null){
            CompoundTag namedTag = event.getItem().getNamedTag();
            if (namedTag != null && namedTag.getByte("enchant_orb") == 1){
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getBlock();

        if ((block.getId() != Block.WALL_SIGN && block.getId() != Block.SIGN_POST) || !(player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)) return;
        BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);

        if (!this.isShopSign(sign)) return;
        event.setCancelled(true);

        String[] lines = sign.getText();
        switch (lines[2]){
            case "§5VIP Only":
                if (player.hasPermission(configFile.getString("shopVipPermission"))) break;
                player.sendMessage("§c»§7This shop is for VIP members only!");
                return;
            case "§bSubscriber Only":
                if (player.hasPermission("cube.subscriber")) break;
                player.sendMessage("§c»§7This shop is for Subscriber members only!");
                return;
        }

        ShopCategory category = this.getCategory(lines[1].substring(2).toLowerCase());
        if (category == null) return;

        category.sendForm(player);
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        if ((event.getWindow() instanceof FormWindowCustom)){
            String title = ((FormWindowCustom) event.getWindow()).getTitle();

            if ("§l§8Rename Item".equals(title) && this.smithShop != null) {
                this.smithShop.handleRenameForm((FormWindowCustom) event.getWindow(), event.getPlayer());
                return;
            }

            if (title.startsWith("§l§8Sell")){
                this.sellManager.handleSellForm((FormWindowCustom) event.getWindow(), event.getPlayer());
            }
            return;
        }

        if (event.getWindow() instanceof FormWindowModal){
            String title = ((FormWindowModal) event.getWindow()).getTitle();

            if ("§l§8Smith Repair".equals(title) && this.smithShop != null) {
                this.smithShop.handleRepairForm((FormWindowModal) event.getWindow(), event.getPlayer());
                return;
            }
        }

        if (!(event.getWindow() instanceof FormWindowSimple)) return;

        Player player = event.getPlayer();
        FormWindowSimple form = (FormWindowSimple) event.getWindow();
        FormResponseSimple response = (FormResponseSimple) event.getResponse();

        switch (form.getTitle()){
            case "§l§8Smith the Man":
                if (this.smithShop != null) this.smithShop.handleMenu(form, event.getPlayer());
                return;
            case "§l§8Enchants Shop":
                if (this.smithShop != null) this.smithShop.handleEnchantsForm(form, event.getPlayer());
                return;
            case "§l§8Sell Items":
                this.sellManager.handleForm(form, player);
                return;

        }

        if (form.getTitle().startsWith("§l§8Levels of ") && this.smithShop != null){
            this.smithShop.handleEnchantLevelForm(form, event.getPlayer());
            return;
        }

        if (!form.getTitle().startsWith("§l§8Shop")){
            return;
        }

        if (response == null) return;

        ShopCategory category = this.getCategory(form.getTitle().substring(9).toLowerCase());
        ShopItem item = category.getItem(response.getClickedButtonId());

        if (item == null) return;
        boolean success = item.buyItem(player);

        String message = this.messageFormat(player, (success? "messageSuccess" : "messageFail"));
        message = message.replace("{item}", item.getName());
        message = message.replace("{money}", String.valueOf(item.getPrice()));
        player.sendMessage(message);
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

    protected String messageFormat(Player player, String messageKey){
        if (player == null || messageKey == null || configFile.getString(messageKey).equals("")) return "";

        String message = configFile.getString(messageKey);
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", String.valueOf(Money.getInstance().getMoney(player, false)));
        return message;
    }
}

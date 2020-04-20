package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.commands.ShopCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import net.minidev.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SurvivalShop extends Addon {

    public Map<String, ShopCategory> categories = new HashMap<>();
    protected Position shopSpawn;

    public SurvivalShop(String path){
        super("survivalshop", path);

        JSONObject shopData = ConfigManager.getInstance().loadJson(ConfigManager.getInstance().ADDONS_PATH+"/shop.json");
        Set<String> categories = shopData.keySet();

        for (String category : categories){
            if (!(shopData.get(category) instanceof JSONObject)) continue;

            ShopCategory shopCategory = new ShopCategory(category, (JSONObject) shopData.get(category), this);
            this.categories.put(category, shopCategory);
        }


        this.shopSpawn = this.plugin.getServer().getDefaultLevel().getSafeSpawn();
        if (configFile.exists("shopSpawn")){
            System.out.println("loading");

            String[] data = configFile.getString("shopSpawn").split(",");

            Level level = this.plugin.getServer().getLevelByName(data[3]);
            this.shopSpawn = new Position(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), level);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("signTitle", "§f[Shop]");

            configFile.set("shopCreatePermission", "bettersurvival.shop.create");
            configFile.set("shopRemovePermission", "bettersurvival.shop.remove");
            configFile.set("shopVipPermission", "bettersurvival.shop.remove");

            configFile.set("netEnoughMoney", "§c»§7You do not have enough coins to buy {item}!");
            configFile.set("wrongFormat", "§c»§7Shop was not created! Use following format: Line 1: shop Line 2: category");
            configFile.set("shopSet", "§6»§7Shop was created successfully!");

            configFile.set("messageSuccess", "§a»§7You have successfully bought §6{item}§7!");
            configFile.set("messageFail", "§e»§7You dont have §e{money}§7 coins to buy §6{item}§7!");
            configFile.save();
        }

        try {
            File shopFile = new File(ConfigManager.getInstance().ADDONS_PATH+"/shop.json");
            if (!shopFile.exists()){
                shopFile.createNewFile();

                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("shop.json");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                OutputStream outputStream = new FileOutputStream(shopFile);
                outputStream.write(buffer);
                inputStream.close();
            }
        }catch (Exception e){
            this.plugin.getLogger().info("§eUnable to save shop.json from resources!");
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("shop", new ShopCommand("shop", this));
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
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if ((block.getId() != Block.WALL_SIGN && block.getId() != Block.SIGN_POST) || !(player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)) return;
        BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);

        if (!this.isShopSign(sign)) return;

        String[] lines = sign.getText();
        ShopCategory category = this.getCategory(lines[1].substring(2).toLowerCase());
        if (category == null) return;

        category.sendForm(player);
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        if (!(event.getWindow() instanceof FormWindowSimple) || !((FormWindowSimple) event.getWindow()).getTitle().startsWith("§l§8Shop")) return;

        Player player = event.getPlayer();
        FormWindowSimple form = (FormWindowSimple) event.getWindow();
        FormResponseSimple response = (FormResponseSimple) event.getResponse();

        if (response == null) return;

        ShopCategory category = this.getCategory(form.getTitle().substring(9).toLowerCase());
        ShopItem item = category.getItem(response.getClickedButtonId());

        if (item == null) return;
        boolean success = item.buyItem(player);

        String message = this.messageFormat(player, (success? "messageSuccess" : "messageFail"));
        message = message.replace("{item}", item.getName());
        message = message.replace("{money}", item.getName());
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
        return this.categories.getOrDefault(category, null);
    }

    private String messageFormat(Player player, String messageKey){
        if (player == null || messageKey == null || configFile.getString(messageKey).equals("")) return "";

        String message = configFile.getString(messageKey);
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", "0"); //TODO: use EconomyAPI
        return message;
    }
}

package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.commands.LandCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;

import java.util.*;


public class MyLandProtect extends Addon {

    private Map<String, List<Block>> selectors = new HashMap<>();
    private Map<String, LandRegion> lands = new HashMap<>();

    public static String WAND = "§6LandWand";

    public MyLandProtect(String path){
        super("mylandprotect", path);

        WAND = configFile.getString("wandName");

        for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
            loadLand(config);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("wandName", "§6LandWand");

            configFile.set("landsLimit", 2);
            configFile.set("landsLimitSize", 50);
            configFile.set("landsLimitSizeVip", 100);

            configFile.set("landNotExists", "§6»§7Land §6{land}§7 not found!");
            configFile.set("landWarn", "§6»§7Hey {player}, this is not your region! Ask §6{owner} §7to access §6{land}§7!");

            configFile.set("landCreate", "§6»§7You have created new land §6{land}§7!");
            configFile.set("landRemove", "§6»§7You have removed your land §6{land}§7!");
            configFile.set("landSetPos", "§6»§7Break 2 blocks with wand to select border positions§7!");
            configFile.set("landPosSelected", "§6»§7Successfully selected position at §6{pos}§7!");

            configFile.set("landWhitelistAdd", "§6»§7You gain access §6{player}§7 to your land §6{land}§7!");
            configFile.set("landWhitelistRemove", "§6»§7You restrict §6{player}§7's access to your land §6{land}§7!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        if (configFile.getBoolean("enable", true)){
            plugin.getServer().getCommandMap().register("land", new LandCommand("land", this));
        }
    }

    @EventHandler
    public void onBlockTouch(PlayerInteractEvent event){
        Player player = event.getPlayer();

        String item = player.getInventory().getItemInHand().getName();

        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && item.equals(WAND)){
            List<Block> blocks = new ArrayList<>();

            if (selectors.containsKey(player.getName().toLowerCase())){
                blocks = selectors.get(player.getName().toLowerCase());
            }

            if (blocks.size() >= 2) blocks.clear();

            blocks.add(event.getBlock());
            selectors.put(player.getName(), blocks);

            String message = configFile.getString("landPosSelected");
            message = message.replace("{pos}", event.getBlock().x +", "+ event.getBlock().y +", "+ event.getBlock().z);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            event.setCancelled();
        }

        LandRegion region = getLandByBlock(event.getBlock());
        if (!interact(player, region)) event.setCancelled();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LandRegion region = getLandByBlock(block);
        if (!interact(player, region)) event.setCancelled();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LandRegion region = getLandByBlock(block);
        if (!interact(player, region)) event.setCancelled();
    }

    public boolean interact(Player player, LandRegion region){
        if (region == null) return true;
        if (region.owner.equals(player.getName().toLowerCase()) || region.whitelist.contains(player.getName().toLowerCase())) return true;

        String message = configFile.getString("landWarn");
        message = message.replace("{land}", region.land);
        message = message.replace("{player}", player.getName());
        message = message.replace("{owner}", region.owner);
        player.sendMessage(message);
        return false;
    }

    public LandRegion getLandByBlock(Block block){
        for (LandRegion region : this.lands.values()){
            if (Math.min(region.pos1.x, region.pos2.x) <= block.x && Math.max(region.pos1.x, region.pos2.x) >= block.x) {
                if (Math.min(region.pos1.y, region.pos2.y) <= block.y && Math.max(region.pos1.y, region.pos2.y) >= block.y) {
                    if (Math.min(region.pos1.z, region.pos2.z) <= block.z && Math.max(region.pos1.z, region.pos2.z) >= block.x) {
                        return region;
                    }
                }
            }
        }
        return null;
    }

    public void loadLand(SuperConfig config){
       String owner = config.getName().substring(0, config.getName().lastIndexOf("."));

       for (String land : config.getSection("land").getKeys(false)){
           LandRegion region = new LandRegion(owner, land);

           List<Integer> data = config.getIntegerList("land."+land+".pos0");
           region.pos1 = new Vector3f(data.get(0), data.get(1), data.get(2));

           data = config.getIntegerList("land."+land+".pos1");
           region.pos2 = new Vector3f(data.get(0), data.get(1), data.get(2));

           region.whitelist = config.getStringList("land."+land+".whitelist");
           this.lands.put(land, region);
       }
    }

    public void createLand(Player player, String land){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = configFile.getInt("landsLimit");

        Set<String> lands = config.getSection("land").getKeys();
        if (lands != null && lands.size() >= limit && !player.isOp()){
            player.sendMessage("§6»§7Lands limit reached!");
            return;
        }

        if (!this.selectors.containsKey(player.getName().toLowerCase())){
            player.sendMessage(configFile.getString("landSetPos"));
            return;
        }

        List<Block> blocks = this.selectors.get(player.getName().toLowerCase());
        if (blocks.size() < 2){
            selectors.remove(player.getName().toLowerCase());
            player.sendMessage(configFile.getString("landSetPos"));
            return;
        }

        for (int i = 0; i < blocks.size(); i++){
            Block block = blocks.get(i);
            Double[] pos = {block.getX(), block.getY(), block.getZ()};
            config.set("land."+land.toLowerCase()+".pos"+i, pos);
        }
        config.set("land."+land.toLowerCase()+".whitelist", new String[0]);

        String message = configFile.getString("landCreate");
        message = message.replace("{land}", land);
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);

        this.selectors.remove(player.getName().toLowerCase());
        config.save();

        LandRegion region = new LandRegion(player.getName().toLowerCase(), land.toLowerCase());
        region.pos1 = blocks.get(0).asVector3f();
        region.pos2 = blocks.get(1).asVector3f();
        this.lands.put(land.toLowerCase(), region);
    }

    public void removeLand(Player player, String land){
        if (player == null || !player.isConnected()) return;

        this.lands.remove(land);

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("land."+land.toLowerCase())){
            String message = configFile.getString("landNotExists");
            message = message.replace("{land}", land);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        ((Map) config.get("land")).remove(land.toLowerCase());
        config.save();

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", land);
        player.sendMessage(message);
    }
}

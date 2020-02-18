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
    public static String PERM_VIP = "bettersurvival.land.vip";
    public static String PERM_ACCESS = "bettersurvival.land.access";

    public MyLandProtect(String path){
        super("mylandprotect", path);

        WAND = configFile.getString("wandName");
        PERM_VIP = configFile.getString("landsVipPermission");
        PERM_ACCESS = configFile.getString("landsAccessPermission");

        for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
            loadLand(config);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("wandName", "§6Land§eWand");

            configFile.set("landsLimit", 2);
            configFile.set("landsLimitSize", 50);
            configFile.set("landsLimitSizeVip", 100);

            configFile.set("landsVipPermission", "bettersurvival.land.vip");
            configFile.set("landsAccessPermission", "bettersurvival.land.access");

            configFile.set("landNotExists", "§6»§7Land §6{land}§7 not found!");
            configFile.set("landWithNameExists", "§6»§7Land §6{land}§7 already exists§7!");
            configFile.set("landWarn", "§6»§7Hey §6@{player}§7, this is not your region! Ask §6@{owner} §7to access §6{land}§7!");
            configFile.set("landTooBig", "§6»§7Selected land is bigger than maximum allowed limit §6{limit} blocks§7!");
            configFile.set("landPosSelected", "§6»§7Successfully selected {select} position at §6{pos}§7!");
            configFile.set("landLimitWarn", "§6»§7Lands limit reached!");
            configFile.set("landHereNotFound", "§6»§7This land is free§7!");

            configFile.set("landCreate", "§6»§7You have created new land §6{land}§7!");
            configFile.set("landRemove", "§6»§7You have removed your land §6{land}§7!");
            configFile.set("landSetPos", "§6»§7Break 2 blocks with wand to select border positions§7!");
            configFile.set("landWhitelist", "§6»§7Whitelist for §6{land}§7 saved§7!");
            configFile.set("landWhitelistList", "§6»{land}§7 access: {players}");
            configFile.set("landHere", "§6»§7The land §6{land}§7 is owned by §6{owner}§7!");
            configFile.set("landList", "§6»§7Your lands: {lands}");

            configFile.set("landWhitelistAdd", "§6»§7You gain access §6@{player}§7 to your land §6{land}§7!");
            configFile.set("landWhitelistRemove", "§6»§7You restrict §6@{player}§7's access to your land §6{land}§7!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        if (configFile.getBoolean("enable", true)){
            registerCommand("land", new LandCommand("land", this));
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
            selectors.put(player.getName().toLowerCase(), blocks);

            String message = configFile.getString("landPosSelected");
            message = message.replace("{pos}", event.getBlock().x +", "+ event.getBlock().y +", "+ event.getBlock().z);
            message = message.replace("{player}", player.getName());
            message = message.replace("{select}", (blocks.size() == 1)? "first" : "second");
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
        if (!interact(player, region)){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LandRegion region = getLandByBlock(block);
        if (!interact(player, region)){
            event.setCancelled();
        }
    }

    public boolean interact(Player player, LandRegion region){
        if (region == null) return true;
        if (region.owner.equals(player.getName().toLowerCase()) || region.whitelist.contains(player.getName().toLowerCase())) return true;
        if (player.isOp() || player.hasPermission(PERM_ACCESS)) return true;

        String message = configFile.getString("landWarn");
        message = message.replace("{land}", region.land);
        message = message.replace("{player}", player.getName());
        message = message.replace("{owner}", region.owner);
        player.sendMessage(message);
        return false;
    }

    public LandRegion getLandByBlock(Block block){
        for (LandRegion region : this.lands.values()){
            if (block.x >= Math.min(region.pos1.x, region.pos2.x) && block.x <= Math.max(region.pos1.x, region.pos2.x)
                    && block.y >= Math.min(region.pos1.y, region.pos2.y) && block.y <= Math.max(region.pos1.y, region.pos2.y)
                    && block.z >= Math.min(region.pos1.z, region.pos2.z) && block.z <= Math.max(region.pos1.z, region.pos2.z))
                return region;
        }
        return null;
    }

    public Set<String> getLands(Player player){
        return getLands(player.getName());
    }

    public Set<String> getLands(String player){
        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("land").getKeys(false);
    }

    public boolean validateLand(List<Block> blocks){
        return validateLand(blocks, null);
    }

    public boolean validateLand(List<Block> blocks, Player player){
        if (blocks == null || blocks.isEmpty()) return false;

        if (blocks.size() < 2){
            if (player != null){
                player.sendMessage(configFile.getString("landSetPos"));
            }
            return false;
        }

        int landSize = configFile.getInt("landsLimitSize");
        if (player != null && player.hasPermission(PERM_VIP)){
            landSize = configFile.getInt("landsLimitSizeVip");
        }

        if (player != null && player.isOp()) return true;

        if ((Math.max(blocks.get(0).x, blocks.get(1).x) - Math.min(blocks.get(0).x, blocks.get(1).x)) > landSize ||
                (Math.max(blocks.get(0).y, blocks.get(1).y) - Math.min(blocks.get(0).y, blocks.get(1).y)) > landSize ||
                (Math.max(blocks.get(0).z, blocks.get(1).z) - Math.min(blocks.get(0).z, blocks.get(1).z)) > landSize){

            if (player != null){
                String message = configFile.getString("landTooBig");
                message = message.replace("{player}", player.getName());
                message = message.replace("{limit}", String.valueOf(landSize));
                player.sendMessage(message);
            }
            return false;
        }
        return true;
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
           this.lands.put(owner.toLowerCase()+"-"+land, region);
       }
    }

    public void createLand(Player player, String land){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = configFile.getInt("landsLimit");

        Set<String> lands = config.getSection("land").getKeys();
        if (lands != null && lands.size() >= limit && !player.isOp()){
            String message = configFile.getString("landLimitWarn");
            message = message.replace("{land}", land);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        if (lands != null && lands.contains(land.toLowerCase())){
            String message = configFile.getString("landWithNameExists");
            message = message.replace("{land}", land);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        if (!this.selectors.containsKey(player.getName().toLowerCase())){
            player.sendMessage(configFile.getString("landSetPos"));
            return;
        }

        List<Block> blocks = this.selectors.get(player.getName().toLowerCase());
        if (!validateLand(blocks, player)){
            selectors.remove(player.getName().toLowerCase());
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
        this.lands.put(player.getName().toLowerCase()+"-"+land.toLowerCase(), region);
    }

    public void removeLand(Player player, String land){
        if (player == null || !player.isConnected()) return;

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

        this.lands.remove(player.getName().toLowerCase()+"-"+land);

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", land);
        player.sendMessage(message);
    }

    public void findLand(Player player){
        Block block = player.getLevel().getBlock(player.add(0, -1));
        LandRegion land = null;

        if (block != null && (land = getLandByBlock(block)) != null){
            String message = configFile.getString("landHere");
            message = message.replace("{owner}", land.owner);
            message = message.replace("{land}", land.land);
            player.sendMessage(message);
            return;
        }
        String message = configFile.getString("landHereNotFound");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void whitelist(Player owner, String player, String land, String action){
        LandRegion region = this.lands.get(owner.getName().toLowerCase()+"-"+land.toLowerCase());

        if (region == null){
            String message = configFile.getString("landNotExists");
            message = message.replace("{land}", land);
            message = message.replace("{player}", owner.getName());
            owner.sendMessage(message);
            return;
        }

        if (!region.owner.equals(owner.getName().toLowerCase())){
            String message = configFile.getString("landWarn");
            message = message.replace("{land}", region.land);
            message = message.replace("{player}", owner.getName());
            message = message.replace("{owner}", region.owner);
            owner.sendMessage(message);
            return;
        }

        switch (action){
            case LandRegion.WHITELIST_ADD:
                region.addWhitelist(player);
                break;
            case LandRegion.WHITELIST_REMOVE:
                region.whitelistRemove(player);
                break;
            case LandRegion.WHITELIST_LIST:
                String players = String.join(", ", region.whitelist);

                String message = configFile.getString("landWhitelistList");
                message = message.replace("{land}", region.land);
                message = message.replace("{player}", owner.getName());
                message = message.replace("{players}", players);
                owner.sendMessage(message);
                return; //exit
        }

        String message = configFile.getString("landWhitelist");
        message = message.replace("{land}", region.land);
        message = message.replace("{player}", owner.getName());
        owner.sendMessage(message);
    }

    public void listLands(Player player){
        Set<String> lands = this.getLands(player);

        String message = configFile.getString("landList");
        message = message.replace("{lands}", String.join(", ", lands));
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }
}

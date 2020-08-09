package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.commands.*;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.*;

public class MyHomes extends Addon {

    private final Map<String, Position> playerWarps = new HashMap<>();
    private final Map<String, WarpCategory> warpCategories = new HashMap<>();

    public MyHomes(String path){
        super("myhome", path);
    }

    @Override
    public void postLoad() {
        List<String> categories = configFile.getStringList("warpCategories");
        for (String category : categories){
            this.warpCategories.put(category.toLowerCase(), new WarpCategory(category));
        }
        this.warpCategories.put("other", new WarpCategory("Other"));

        for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
            try {
                this.loadWarps(config);
            }catch (Exception e){
                this.plugin.getLogger().error("Unable to load warp for "+config.getName()+"!", e);
            }
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("homeLimit", 3);

            configFile.set("homeLimitVip", 5);
            configFile.set("permission-vip", "bettersurvival.home.vip");

            configFile.set("homeTeleport", "§6»§7Woosh! Welcome at {home} §6@{player}!");
            configFile.set("homeSet", "§6»§7Your home §6{home}§7 has been saved! You have §6{limit}§7 free homes!");
            configFile.set("homeDel", "§6»§7Your home §6{home}§7 was deleted!");

            configFile.set("warpCategories", new ArrayList<>(Arrays.asList("Shop", "Farm", "Social", "Storage", "Game", "Guild", "Creations")));
            configFile.set("warpCreate", "§6»§7Your warp §6{warp}§7 has been created! You have §6{limit}§7 free public warps!");
            configFile.set("warpDelete", "§6»§7Your warp §6{warp}§7 was deleted!");
            configFile.set("warpTeleport", "§6»§7Welcome to §6{warp}§7 destination!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        if (configFile.getBoolean("enable", true)){
            registerCommand("home", new HomeCommand("home", this));
            registerCommand("sethome", new SetHomeCommand("sethome", this));
            registerCommand("gethome", new GetHomeCommand("gethome", this));
            registerCommand("delhome", new DelHomeCommand("delhome", this));
            registerCommand("pwarp", new PlayerWarpCommand("pwarp", this));
        }
    }

    private void loadWarps(SuperConfig config){
        if (config == null) return;
        String owner = config.getName().substring(0, config.getName().lastIndexOf("."));

        ConfigSection section = config.getSection("pwarps");
        Set<String> warps = section.getKeys(false);
        for (String warp : warps){
            ConfigSection data = section.getSection(warp);

            String name = data.getString("name");
            String categoryName = data.getString("category");
            String rawPos = data.getString("pos");
            String levelName = data.getString("level");

            WarpCategory category = this.getWarpCategory(categoryName);
            Level level = this.plugin.getServer().getLevelByName(levelName);
            if (category == null || level == null) continue;

            String[] posData = rawPos.split(",");
            Position pos = new Position(Integer.parseInt(posData[0]), Integer.parseInt(posData[1]), Integer.parseInt(posData[2]), level);

            PlayerWarp playerWarp = new PlayerWarp(name, categoryName, owner, pos);
            category.addWarp(playerWarp);
        }
    }


    public void setHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = player.hasPermission(configFile.getString("permission-vip"))? configFile.getInt("homeLimitVip") : configFile.getInt("homeLimit");

        Set<String> homes = config.getSection("home").getKeys(false);
        if (homes.size() >= limit && !player.isOp()){
            player.sendMessage("§6»§7Home limit reached!");
            return;
        }

        Double[] pos = {player.getX(), player.getY(), player.getZ()};
        config.set("home."+home.toLowerCase()+".pos", pos);
        config.set("home."+home.toLowerCase()+".level", player.getLevel().getFolderName());
        config.save();

        String message = configFile.getString("homeSet");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        message = message.replace("{limit}", player.isOp()? "unlimited" : String.valueOf(limit - homes.size()));
        player.sendMessage(message);
    }

    public void delHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("home."+home.toLowerCase())){
            player.sendMessage("§6»§7Home §6"+home+"§7 doesnt exist!");
            return;
        }

        ((Map) config.get("home")).remove(home.toLowerCase());
        config.save();

        String message = configFile.getString("homeDel");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }

    public void teleportToHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("home."+home.toLowerCase())){
            player.sendMessage("§6»§7Home §6"+home+"§7 doesnt exist!");
            return;
        }

        List<Integer> data = config.getIntegerList("home."+home.toLowerCase()+".pos");
        if (data == null || data.size() < 3){
            player.sendMessage("§eError occurs while teleporting to home!");
            return;
        }
        Level level = this.plugin.getServer().getLevelByName(config.getString("home."+home.toLowerCase()+".level"));
        level = (level == null)? this.plugin.getServer().getDefaultLevel() : level;

        player.teleport(new Position(data.get(0), data.get(1), data.get(2), level));

        String message = configFile.getString("homeTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }


    public void createWarp(Player player, String name, WarpCategory category){
        if (player == null || name == null) return;

        if (category == null){
            player.sendMessage("§c»§7Unknown warp category. Can not register warp!!");
            return;
        }

        if (category.getWarp(name) != null){
            player.sendMessage("§c»§7Warp with this name has been already registered!");
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = player.hasPermission(configFile.getString("permission-vip"))? configFile.getInt("homeLimitVip") : configFile.getInt("homeLimit");

        Set<String> warps = config.getSection("pwarps").getKeys(false);
        if (warps.size() >= limit && !player.isOp()){
            player.sendMessage("§c»§7Player Warps limit reached!");
            return;
        }

        PlayerWarp playerWarp = new PlayerWarp(name, category.getName(), player.getName(), player.clone());
        config.set("pwarps."+playerWarp.getRawName(), playerWarp.save());
        config.save();

        category.addWarp(playerWarp);

        String message = configFile.getString("warpCreate");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        message = message.replace("{limit}", player.isOp()? "unlimited" : String.valueOf(limit - warps.size()));
        player.sendMessage(message);
    }

    public void deleteWarp(Player player, String name){
        if (player == null || name == null) return;

        PlayerWarp playerWarp = this.getWarp(name);
        if (playerWarp == null){
            player.sendMessage("§c»§7Warp §6"+name+"§7 doesnt exist!");
            return;
        }

        boolean isOwner = playerWarp.getOwner().equalsIgnoreCase(player.getName());
        if (!isOwner && !player.isOp()){
            player.sendMessage("§c»§7You are not owner of §6"+name+"§7 warp!");
            return;
        }

        WarpCategory category = this.getWarpCategory(playerWarp.getCategory());
        category.removeWarp(playerWarp);

        Config config = ConfigManager.getInstance().loadPlayer(isOwner? player.getName() : playerWarp.getOwner());
        if (config == null) return;

        ((Map) config.get("pwarps")).remove(playerWarp.getRawName());
        config.save();

        String message = configFile.getString("warpDelete");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        player.sendMessage(message);
    }

    public void teleportToWarp(Player player, String name){
        if (player == null || name == null) return;

        PlayerWarp playerWarp = this.getWarp(name);
        if (playerWarp == null){
            player.sendMessage("§c»§7Warp §6"+name+"§7 doesnt exist!");
            return;
        }

        playerWarp.teleport(player);

        String message = configFile.getString("warpTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        player.sendMessage(message);
    }

    public void showWarpMenu(Player player){
        if (player == null) return;
        new WarpMenu(player, this).buildForm().sendForm();
    }

    public Map<String, WarpCategory> getWarpCategories() {
        return this.warpCategories;
    }

    public WarpCategory getWarpCategory(String name){
        return this.warpCategories.get(name.toLowerCase());
    }

    public PlayerWarp getWarp(String name){
        for (WarpCategory category : this.warpCategories.values()){
            PlayerWarp warp = category.getWarp(name);
            if (warp != null) return warp;
        }
        return null;
    }

    public Set<String> getHomes(Player player){
        if (player == null || !player.isConnected()) return null;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("home").getKeys(false);
    }
}

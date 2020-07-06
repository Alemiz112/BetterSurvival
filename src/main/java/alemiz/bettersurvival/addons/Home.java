package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.*;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Home extends Addon {

    private final Map<String, Position> playerWarps = new HashMap<>();

    public Home(String path){
        super("home", path);

        for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
            this.loadWarps(config);
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

        Set<String> warps = config.getSection("warps").getKeys(false);
        for (String warp : warps){
            String rawPos = config.getString("warps."+warp);
            String[] data = rawPos.split(",");

            Level level = this.plugin.getServer().getLevelByName(data[3]);
            if (level == null) continue;

            try {
                Position pos = new Position(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), level);
                this.playerWarps.put(warp.toLowerCase(), pos);
            }catch (NumberFormatException e){
                this.plugin.getLogger().info("§eUnable to load player warp §6"+warp+"§e for player §6"+config.getName()+"§e!");
            }
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


    public void createWarp(Player player, String name){
        if (player == null || name == null) return;

        if (this.playerWarps.get(name.toLowerCase()) != null){
            player.sendMessage("§c»§7Warp with this name has been already registered!");
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = player.hasPermission(configFile.getString("permission-vip"))? configFile.getInt("homeLimitVip") : configFile.getInt("homeLimit");

        Set<String> warps = config.getSection("warps").getKeys(false);
        if (warps.size() >= limit && !player.isOp()){
            player.sendMessage("§c»§7Player Warps limit reached!");
            return;
        }

        String rawPos = (int) player.getX()+","+ (int) player.getY()+","+ (int) player.getZ()+","+player.getLevel().getFolderName();
        config.set("warps."+name.toLowerCase(), rawPos);
        config.save();

        this.playerWarps.put(name.toLowerCase(), player.clone());

        String message = configFile.getString("warpCreate");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        message = message.replace("{limit}", player.isOp()? "unlimited" : String.valueOf(limit - warps.size()));
        player.sendMessage(message);
    }

    public void deleteWarp(Player player, String name){
        if (player == null || name == null) return;

        if (!this.playerWarps.containsKey(name.toLowerCase())){
            player.sendMessage("§c»§7Warp §6"+name+"§7 doesnt exist!");
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("warp."+name.toLowerCase())){
            player.sendMessage("§c»§7You are not owner of §6"+name+"§7 warp!");
            return;
        }

        this.playerWarps.remove(name.toLowerCase());

        ((Map) config.get("warps")).remove(name.toLowerCase());
        config.save();

        String message = configFile.getString("warpDelete");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        player.sendMessage(message);
    }

    public void teleportToWarp(Player player, String name){
        if (player == null || name == null) return;

        Position warp = this.playerWarps.get(name.toLowerCase());
        if (warp == null){
            player.sendMessage("§c»§7Warp §6"+name+"§7 doesnt exist!");
            return;
        }

        player.teleport(warp);

        String message = configFile.getString("warpTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", name);
        player.sendMessage(message);
    }

    public void listWarps(Player player){
        if (player == null) return;

        String message = "§6Available Warps:\n§7"+String.join(", ",this.playerWarps.keySet());
        player.sendMessage(message);
    }

    public Set<String> getHomes(Player player){
        if (player == null || !player.isConnected()) return null;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("home").getKeys(false);
    }
}

package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.commands.DelCommand;
import alemiz.bettersurvival.commands.GetHomeCommand;
import alemiz.bettersurvival.commands.HomeCommand;
import alemiz.bettersurvival.commands.SetHomeCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Home extends Addon {

    public Home(String path){
        super("home", path);
        registerCommands();
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("homeLimit", 3);
            configFile.set("homeTeleport", "§6»§7Woosh! Welcome at {home} §6@{player}!");
            configFile.set("homeSet", "§6»§7Your home §6{home}§7 has been saved!");
            configFile.set("homeDel", "§6»§7Your home §6{home}§7 was deleted!");
            configFile.save();
        }
    }

    public void registerCommands(){
        if (configFile.getBoolean("enable", true)){
            plugin.getServer().getCommandMap().register("home", new HomeCommand("home", this));
            plugin.getServer().getCommandMap().register("sethome", new SetHomeCommand("sethome", this));
            plugin.getServer().getCommandMap().register("gethome", new GetHomeCommand("gethome", this));
            plugin.getServer().getCommandMap().register("delhome", new DelCommand("delhome", this));
        }
    }

    public void setHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = configFile.getInt("homeLimit");

        Set<String> homes = config.getSection("home").getKeys();
        if (homes != null && homes.size() >= limit && !player.isOp()){
            player.sendMessage("§6»§7Home limit reached!");
            return;
        }

        Double[] pos = {player.getX(), player.getY(), player.getZ()};
        config.set("home."+home.toLowerCase(), pos);
        config.save();

        String message = configFile.getString("homeSet");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
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

        List<Integer> data = config.getIntegerList("home."+home.toLowerCase());
        if (data == null || data.size() < 3){
            player.sendMessage("§eError occurs while teleporting to home!");
            return;
        }
        player.teleport(new Vector3((double) data.get(0), (double) data.get(1), (double) data.get(2)));

        String message = configFile.getString("homeTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }



    public Set<String> getHomes(Player player){
        if (player == null || !player.isConnected()) return null;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("home").getKeys();
    }
}

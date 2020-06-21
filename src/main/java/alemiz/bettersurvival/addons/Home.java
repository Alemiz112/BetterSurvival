package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.DelHomeCommand;
import alemiz.bettersurvival.commands.GetHomeCommand;
import alemiz.bettersurvival.commands.HomeCommand;
import alemiz.bettersurvival.commands.SetHomeCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Home extends Addon {

    public Home(String path){
        super("home", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("homeLimit", 3);
            configFile.set("homeTeleport", "§6»§7Woosh! Welcome at {home} §6@{player}!");
            configFile.set("homeSet", "§6»§7Your home §6{home}§7 has been saved! You have §6{limit}§7 free homes!");
            configFile.set("homeDel", "§6»§7Your home §6{home}§7 was deleted!");
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
        }
    }

    public void setHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = configFile.getInt("homeLimit");

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



    public Set<String> getHomes(Player player){
        if (player == null || !player.isConnected()) return null;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("home").getKeys(false);
    }
}

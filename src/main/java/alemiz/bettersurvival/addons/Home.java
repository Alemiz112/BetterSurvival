package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.List;
import java.util.Set;

public class Home {

    public static void setHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = BetterSurvival.getInstance().loadPlayer(player);
        if (config == null) return;
        int limit = BetterSurvival.getInstance().cfg.getInt("homes.homeLimit");

        Set<String> homes = config.getSection("home").getKeys();
        if (homes != null && homes.size() >= limit && !player.isOp()){
            player.sendMessage("§6»§7Home limit reached!");
            return;
        }

        Double[] pos = {player.getX(), player.getY(), player.getZ()};
        config.set("home."+home, pos);
        config.save();

        String message = BetterSurvival.getInstance().cfg.getString("homes.homeSet");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }



    public static void delHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = BetterSurvival.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("home."+home.toLowerCase())){
            player.sendMessage("§6»§7Home §6"+home+"§7 doesnt exist!");
            return;
        }

        config.remove("home."+home.toLowerCase());
        config.save();

        String message = BetterSurvival.getInstance().cfg.getString("homes.homeDel");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }



    public static void teleportToHome(Player player, String home){
        if (player == null || !player.isConnected()) return;

        Config config = BetterSurvival.getInstance().loadPlayer(player);
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

        String message = BetterSurvival.getInstance().cfg.getString("homes.homeTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{home}", home);
        player.sendMessage(message);
    }



    public static Set<String> getHomes(Player player){
        if (player == null || !player.isConnected()) return null;

        Config config = BetterSurvival.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("home").getKeys();
    }
}

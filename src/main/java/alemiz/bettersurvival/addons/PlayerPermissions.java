package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerPermissions extends Addon {

    private final List<String> defaultPermissions;

    /*
     TODO: Create permission groups
     */
    public PlayerPermissions(String path){
        super("playerpermissions", path);
        this.defaultPermissions = configFile.getStringList("defaultPermissions");
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("defaultPermissions", new ArrayList<>());
            configFile.save();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        checkExpiredPermissions(player);
        loadDefaultPermissions(player);
    }

    public void checkExpiredPermissions(Player player){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null || (config.get("expired-permissions") instanceof ArrayList)) return;

        ConfigSection permissions = config.getSection("expired-permissions");
        permissions.getAllMap().forEach((String permission, Object expiry)->{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            try {
                if (!sdf.parse((String) expiry).before(new Date())) return;
            }catch (Exception e){
                plugin.getLogger().alert("Error while parsing date in permissions module! Date: "+expiry+" Right format: yyyy-MM-dd");
                return;
            }

            permission = permission.replace("-", ".");
            removePermission(player, permission);
            removeFromExpiryList(player, permission);
        });
    }

    public void loadDefaultPermissions(Player player){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        //Permissions columns wasn't set yet
        if (config.getStringList("permissions").isEmpty()){
            config.set("permissions", new ArrayList<>());
            config.set("expired-permissions", new ArrayList<>());
            config.save();
            return;
        }

        for (String permission : config.getStringList("permissions")){
            player.addAttachment(plugin, permission, (!permission.startsWith("!")));
        }

        for (String permission : this.defaultPermissions){
            player.addAttachment(plugin, permission, (!permission.startsWith("!")));
        }
    }

    public void addPermission(Player player, String permission){
        addPermission(player, permission, "");
    }

    /**
     * Date format "yyyy-MM-dd"
     */
    public void addPermission(Player player, String permission, String expiry){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        List<String> permissions = config.getStringList("permissions");
        if (!permissions.contains(permission)) permissions.add(permission);

        if (expiry != null && !expiry.equals("")){
            config.set("expired-permissions."+permission.replace(".", "-"), expiry);
        }

        config.set("permissions", permissions);
        config.save();

        player.addAttachment(plugin, permission, (!permission.startsWith("!")));
    }

    public void removePermission(Player player, String permission){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        List<String> permissions = new ArrayList<>(config.getStringList("permissions"));
        permissions.remove(permission);

        config.set("permissions", permissions);
        config.save();

        player.addAttachment(plugin, permission, false);
    }

    /**
     * Date format "yyyy-MM-dd"
     */
    public void setExpiryPermission(Player player, String permission, String date){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        config.set("expired-permissions."+permission.replace(".", "-"), date);
        config.save();
    }

    public void removeFromExpiryList(Player player, String permission){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        config.remove("expired-permissions."+permission.replace(".", "-"));
        config.save();
    }

    public void addDefaultPermission(String permission){
        this.defaultPermissions.add(permission);
    }

    public void removeDefaultPermission(String permission){
        this.defaultPermissions.remove(permission);
    }

    public List<String> getDefaultPermissions() {
        return this.defaultPermissions;
    }
}

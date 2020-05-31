package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cubemc.nukkit.connector.events.CubePlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class CubeBridge extends Addon {

    public static String VIP_PERM = "cube.vip";
    public static String PLUS_PERM = "cube.plus";
    public static String SUBSCRIBER_PERM = "cube.subscriber";
    public static String STAFF_PERM = "cube.staff";


    public CubeBridge(String path){
        super("cubebridge", path);

        VIP_PERM = configFile.getString("vip-rank");
        PLUS_PERM = configFile.getString("plus-rank");
        SUBSCRIBER_PERM = configFile.getString("subscriber-rank");
        STAFF_PERM = configFile.getString("staff-rank");
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("vip-rank", "cube.vip");
            configFile.set("plus-rank", "cube.plus");
            configFile.set("subscriber-rank", "cube.subscriber");
            configFile.set("staff-rank", "cube.staff");
            configFile.save();
        }
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event){
        Player player = event.getPlayer();
        if (player == null) return;

        List<String> perms = new ArrayList<>();

        boolean isStaff = player.hasPermission(STAFF_PERM);

        if (player.hasPermission(SUBSCRIBER_PERM) || isStaff){
            //Subscriber Features
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));
            //perms.add(Addon.getAddon("levelvote").configFile.getString("permission-vote"));
        }

        if (player.hasPermission(PLUS_PERM) || isStaff){
            //Cube+ Freatures
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-heal"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));
        }

        if (player.hasPermission(VIP_PERM) || isStaff){
            //VIP Perms
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-heal"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-fly"));

            perms.add(Addon.getAddon("mylandprotect").configFile.getString("landsVipPermission"));
            perms.add(Addon.getAddon("survivalshop").configFile.getString("shopVipPermission"));
        }

        if (player.hasPermission(STAFF_PERM)){
            //STAFF Perms here
            perms.add(Addon.getAddon("troller").configFile.getString("permission-vanish"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-block"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-troll"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-troll-advanced"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-invsee"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-mute"));
        }

        for (String perm : perms){
            player.addAttachment(plugin, perm, true);
        }
    }
}

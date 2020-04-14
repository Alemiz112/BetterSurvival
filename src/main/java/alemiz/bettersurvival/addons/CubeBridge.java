package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.CustomListener;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cubemc.nukkit.connector.events.CubePlayerJoinEvent;
import cubemc.nukkit.connector.modules.staff.StaffManagePacket;
import cubemc.nukkit.connector.modules.staff.StaffModule;
import cubemc.nukkit.cubeanticheat.events.PlayerCheatActionEvent;
import io.pocketvote.event.VoteDispatchEvent;
import io.pocketvote.event.VoteEvent;

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

    @Override
    public void loadListeners() {
        if (plugin.getServer().getPluginManager().getPlugin("CubeAntiCheat") != null){
            CustomListener listener = new CustomListener(this){
                @EventHandler
                public void onCheatAction(PlayerCheatActionEvent event){
                    if (event.getPlayer() == null) return;
                    Player player = event.getPlayer();

                    String message = "You was suspected from cheating! Hackers are no tolerated!";
                    StaffModule.getInstance().operate("console", player.getName(), message, StaffManagePacket.STAFF_BAN);
                }
            };
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event){
        Player player = event.getPlayer();
        List<String> perms = new ArrayList<>();

        boolean isStaff = player.hasPermission(STAFF_PERM);

        if (player.hasPermission(SUBSCRIBER_PERM) || isStaff){
            //Subscriber Features
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));
            perms.add(Addon.getAddon("levelvote").configFile.getString("permission-vote"));
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
        }

        if (player.hasPermission(STAFF_PERM)){
            //STAFF Perms here
            perms.add(Addon.getAddon("troller").configFile.getString("permission-vanish"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-block"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-troll"));
            perms.add(Addon.getAddon("troller").configFile.getString("permission-troll-advanced"));
        }

        for (String perm : perms){
            player.addAttachment(plugin, perm, true);
        }
    }
}

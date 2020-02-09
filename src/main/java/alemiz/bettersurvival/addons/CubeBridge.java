package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.network.protocol.GameRulesChangedPacket;
import cubemc.connector.events.CubePlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class CubeBridge extends Addon {

    public static String VIP_PERM = "cube.vip";
    public static String PLUS_PERM = "cube.plus";
    public static String SUBSCRIBER_PERM = "cube.subscriber";


    public CubeBridge(String path){
        super("cubebridge", path);

        VIP_PERM = configFile.getString("vip-rank");
        PLUS_PERM = configFile.getString("plus-rank");
        SUBSCRIBER_PERM = configFile.getString("subscriber-rank");
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("vip-rank", "cube.vip");
            configFile.set("plus-rank", "cube.plus");
            configFile.set("subscriber-rank", "cube.subscriber");

            configFile.save();
        }
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event){
        Player player = event.getPlayer();
        List<String> perms = new ArrayList<>();

        if (player.hasPermission(SUBSCRIBER_PERM)){
            //Subscriber Features
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
        }

        if (player.hasPermission(PLUS_PERM)){
            //Cube+ Freatures
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-heal"));
        }

        if (player.hasPermission(VIP_PERM)){
            //VIP Perms
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-fly"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-heal"));
            perms.add(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            perms.add(Addon.getAddon("mylandprotect").configFile.getString("landsVipPermission"));
        }

        for (String perm : perms){
            player.addAttachment(plugin, perm, true);
        }

        /* Send new GameRules to player after player changes server*/
        GameRulesChangedPacket packet = new GameRulesChangedPacket();
        packet.gameRules = player.getLevel().getGameRules();
        player.dataPacket(packet);
    }
}

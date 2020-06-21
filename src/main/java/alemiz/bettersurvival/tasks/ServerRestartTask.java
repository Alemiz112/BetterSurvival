package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cubemc.nukkit.connector.network.LobbyJoinPacket;

public class ServerRestartTask implements Runnable{

    private final BetterSurvival plugin;

    public ServerRestartTask(BetterSurvival plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int restartIn = this.plugin.getRestartTime();

        if (restartIn > 0){
            this.plugin.setRestartTime((restartIn -= 10));
            if (restartIn < 20) this.plugin.getServer().broadcastMessage("§c»§7Server restarts in "+restartIn+" minutes!");
            return;
        }

        for (Player player : this.plugin.getServer().getOnlinePlayers().values()){
            LobbyJoinPacket packet = new LobbyJoinPacket();
            packet.player = player.getName();
            packet.putPacket();
        }
        this.plugin.getServer().shutdown();
    }
}

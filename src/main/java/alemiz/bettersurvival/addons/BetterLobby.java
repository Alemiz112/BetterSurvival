package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.LevelDecoration;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.scheduler.Task;

import java.util.ArrayList;
import java.util.List;

public class BetterLobby extends Addon {

    private String joinMessage = "";
    private String quitMessage = "";
    private int broadcastInterval = 1200;
    private List<String> broadcastMessages = new ArrayList<>();

    private int nextMessage = 0;

    public BetterLobby(String path){
        super("betterlobby", path);

        loadConfig();
        loadBroadcaster();
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("broadcast", new String[]{"§eDid you find hacker? Use §b/report§e to report him!", "§eDo people actually read these?", "§aCheck out our youtube channel §cCubeMC Official§a!", "§bVote for us and get §eSubscriber §brank!"});
            configFile.set("broadcastInterval", 1200);
            configFile.set("joinMessage", "§6»§7Be careful, §6@{player}§7 joined!");
            configFile.set("quitMessage", "§6»§7Oops §6@{player}§7 left!");
            configFile.save();
        }

        this.broadcastMessages = configFile.getStringList("broadcast");
        this.broadcastInterval = configFile.getInt("broadcastInterval");
        this.joinMessage = configFile.getString("joinMessage");
        this.quitMessage = configFile.getString("quitMessage");
    }
    public void loadBroadcaster(){
        Task task = new Task() {
            @Override
            public void onRun(int i) {
                String message = broadcastMessages.get(nextMessage);

                for (Player player : Server.getInstance().getOnlinePlayers().values()){
                    player.sendMessage(message.replace("{player}", player.getName()));
                }

                if (nextMessage >= (broadcastMessages.size() - 1)){
                    nextMessage = 0;
                }else nextMessage++;
            }
        };
        plugin.getServer().getScheduler().scheduleRepeatingTask(task, broadcastInterval);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        AddEntityPacket packet = new AddEntityPacket();
        packet.type = 93;
        packet.entityRuntimeId = Entity.entityCount++;
        packet.yaw = (float) player.getYaw();
        packet.pitch = (float) player.getPitch();
        packet.x = (float) player.getX();
        packet.y = (float) player.getY();
        packet.z = (float) player.getZ();

        for (Player pplayer : player.getLevel().getPlayers().values()){
            pplayer.dataPacket(packet);
        }

        String message = joinMessage.replace("{player}", player.getName());
        event.setJoinMessage(message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        LevelDecoration[] decorations = {
                new LevelDecoration(player.clone(), LevelEventPacket.EVENT_SOUND_ENDERMAN_TELEPORT),
                new LevelDecoration(player.clone(), LevelEventPacket.EVENT_PARTICLE_ENDERMAN_TELEPORT)
        };
        LevelDecoration.sendDecoration(decorations, player.getLevel().getPlayers());

        String message = quitMessage.replace("{player}", player.getName());
        event.setQuitMessage(message);
    }

}

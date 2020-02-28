package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Command;
import alemiz.bettersurvival.utils.LevelDecoration;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.DummyBossBar;

import java.util.*;

public class BetterLobby extends Addon {

    private String joinMessage = "";
    private String quitMessage = "";
    private int broadcastInterval = 1200;
    private List<String> broadcastMessages = new ArrayList<>();

    private int nextMessage = 0;

    private Map<String, Long> bossBars = new HashMap<>();
    private List<FloatingTextParticle> particles = new ArrayList<>();

    private boolean protectSpawn = true;

    public BetterLobby(String path){
        super("betterlobby", path);

        this.broadcastMessages = configFile.getStringList("broadcast");
        this.broadcastInterval = configFile.getInt("broadcastInterval");
        this.joinMessage = configFile.getString("joinMessage");
        this.quitMessage = configFile.getString("quitMessage");

        this.protectSpawn = configFile.getBoolean("safeSpawn", true);

        this.particles = createHelpParticles();
        loadBroadcaster();
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("broadcast", Arrays.asList("§eDid you find hacker? Use §b/report§e to report him!", "§eDo people actually read these?", "§aCheck out our youtube channel §cCubeMC Official§a!", "§bVote for us and get §eSubscriber §brank!", "§2Tips for commands can be found on §a/spawn§2!"));
            configFile.set("broadcastInterval", 1800);
            configFile.set("joinMessage", "§6»§7Be careful, §6@{player}§7 joined!");
            configFile.set("quitMessage", "§6»§7Oops §6@{player}§7 left!");

            configFile.set("bossBar", true);
            configFile.set("bossBarText", "§bCube§eMC §cSurvival");
            configFile.set("bossBarSize", 50);

            configFile.set("helpParticlePos", new ArrayList<>());
            configFile.set("helpParticleMaxLines", 10);
            configFile.set("helpParticleTitle", "§d<-- §5Available Commands §d-->");
            configFile.set("helpParticleIncludedCommands", Arrays.asList("§7/kill : Kill yourself", "§7/lobby : Go back to server lobby", "§7/spawn : Go to spawn"));

            configFile.set("safeSpawn", true);
            configFile.save();
        }
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


        if (configFile.getBoolean("bossBar")){
            this.bossBars.remove(player.getName());
        }

        String message = quitMessage.replace("{player}", player.getName());
        event.setQuitMessage(message);
    }

    @EventHandler
    public void onInitialize(DataPacketReceiveEvent event){
        if (!(event.getPacket() instanceof SetLocalPlayerAsInitializedPacket)) return;
        Player player = event.getPlayer();

        if (configFile.getBoolean("bossBar")){
            player.createBossBar(buildBossBar(player));
        }

        sendParticles(player);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        if (this.protectSpawn){
            Position explodePos = event.getEntity().getPosition();
            if (!isSafeSpawn(explodePos)) return;

            event.setBlockList(new ArrayList<Block>());
        }
    }

    public boolean isSafeSpawn(Position position){
        if (position.level != plugin.getServer().getDefaultLevel()) return false;

        Position spawn = plugin.getServer().getDefaultLevel().getSpawnLocation();
        int radius = plugin.getServer().getSpawnRadius();

        return position.x >= (spawn.x - radius) && position.x <= (spawn.x + radius) &&
                position.z >= (spawn.z - radius) && position.z <= (spawn.z + radius);
    }

    public DummyBossBar buildBossBar(Player player){
        if (!configFile.getBoolean("bossBar")){
            return new DummyBossBar.Builder(player).build();
        }

        DummyBossBar.Builder builder = new DummyBossBar.Builder(player);
        builder.text(configFile.getString("bossBarText"));
        builder.length(configFile.getInt("bossBarSize"));
        builder.color(BlockColor.RED_BLOCK_COLOR);

        DummyBossBar bossBar = builder.build();
        this.bossBars.put(player.getName(), bossBar.getBossBarId());
        return bossBar;
    }

    public void setHelpParticlesCoords(List<String> positions){
        this.configFile.set("helpParticlePos", positions);
        this.configFile.save();
    }

    public List<String> generateHelpParticleTexts(){
        List<String> commands = new ArrayList<>(this.configFile.getStringList("helpParticleIncludedCommands"));

        for (Addon addon : Addon.getAddons().values()){
            for (Command command : addon.getCommands().values()){
                commands.addAll(Arrays.asList(command.usage.split("\n")));
            }
        }
        return commands;
    }

    public List<FloatingTextParticle> createHelpParticles(){
        List<Vector3> positions = new ArrayList<>();
        for (String pos : this.configFile.getStringList("helpParticlePos")){
            String[] data = pos.split(",");
            positions.add(new Vector3(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
        }

        List<String> helptexts = generateHelpParticleTexts();
        int maxLines = this.configFile.getInt("helpParticleMaxLines", 10);
        String title = this.configFile.getString("helpParticleTitle");

        List<FloatingTextParticle> particles = new ArrayList<FloatingTextParticle>();
        int lastLine = 0;

        for (Vector3 pos : positions){
            FloatingTextParticle particle = new FloatingTextParticle(pos, title);

            List<String> particleText = new ArrayList<>();
            for (int i = ((lastLine == 0)?0 : lastLine+1); i <= (lastLine+maxLines); i++){
                particleText.add(helptexts.get(i));
            }
            lastLine = lastLine+10;

            particle.setText(String.join("\n", particleText));
            particles.add(particle);
        }
        return particles;
    }

    public void sendParticles(Player player){
        if (player == null) return;

        for (FloatingTextParticle particle : this.particles){
            player.getLevel().addParticle(particle, player);
        }
    }


    public Map<String, Long> getBossBars() {
        return bossBars;
    }
}

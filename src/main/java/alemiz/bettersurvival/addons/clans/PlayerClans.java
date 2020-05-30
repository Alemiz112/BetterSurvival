package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerClans extends Addon {

    private final Map<String, Clan> clans = new HashMap<>();

    public PlayerClans(String path) {
        super("playerclans", path);

        for (SuperConfig config : ConfigManager.getInstance().loadAllFromFolder(ConfigManager.PATH+"/clans")){
            //REGISTER CLAN
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("clanCreateMessage", "§6»§7New clan §6@{clan}§7 was created successfully!");
            configFile.save();
        }

        File folder = new File(ConfigManager.PATH+"/clans");
        if (!folder.isDirectory()) folder.mkdirs();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        Runnable task = () -> {
            if (player == null) return;

            Config config = ConfigManager.getInstance().loadPlayer(player);
            List<String> pendingInvites = config.getStringList("clanInvites");

            if (pendingInvites.isEmpty()) return;
            StringBuilder builder = new StringBuilder("§3»§7You have new pending invites to this clans:");

            for (String invite : pendingInvites){
                builder.append("§6").append(pendingInvites).append("§7,");
            }

            player.sendMessage(builder.substring(0, builder.length()-1)+"!");
            player.sendMessage("§6»§7TIP: Use §6/clan accept <name> §7or§6 /clan deny <name> §7to manage your invites!");
        };
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, task, 20*4);
    }

    private void loadClan(SuperConfig config){
        this.plugin.getLogger().info("§eLoading clan "+config.getName()+"§e!");
        this.clans.put(config.getName(), new Clan(config.getName(), config.getString("formattedName"), config, this));
    }

    public Clan createClan(Player player, String name){
        if (player == null || name.isEmpty()) return null;

        if (this.getClan(player) != null){
            player.sendMessage("§c»§7You are already in clan. Please leave it first. TIP: Use §6/clan leave§7 to leave clan.");
            return null;
        }

        name = TextFormat.clean(name);
        String rawName = name.toLowerCase().replace(" ", "_");
        Config config = new Config(ConfigManager.PATH+"/clans/"+rawName+".yml", Config.YAML);

        config.set("formattedName", name);
        config.set("owner", player.getName());
        config.set("players", new ArrayList<>());
        config.set("money", 0);
        config.set("maxMoney", 400000);
        config.set("playerLimit", 10);
        config.save();

        Clan clan = new Clan(rawName, name, config, this);
        this.clans.put(rawName, clan);

        String message = configFile.getString("clanCreateMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{clan}", name);
        player.sendMessage(message);
        return clan;
    }

    public Clan getClan(Player player){
        if (player == null) return null;

        for (Clan clan : this.clans.values()){
            if (!clan.getPlayers().contains(player.getName())) continue;
            return clan;
        }
        return null;
    }

    public boolean isOwner(Player player, String clanName){
        if (player == null) return false;

        for (Clan clan : this.clans.values()){
            if (!clan.getName().equals(clanName) || !clan.getRawName().equals(clanName)) continue;

            if (clan.getOwner().equals(player.getName())){
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Player player, Clan clan){
        if (player == null || clan == null) return false;
        return clan.getOwner().equals(player.getName());
    }
}

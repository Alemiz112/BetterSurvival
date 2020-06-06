package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.commands.ClanCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.*;

public class PlayerClans extends Addon {

    private final Map<String, Clan> clans = new HashMap<>();

    public PlayerClans(String path) {
        super("playerclans", path);

        for (SuperConfig config : ConfigManager.getInstance().loadAllFromFolder(ConfigManager.PATH+"/clans")){
            this.loadClan(config);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("playerLimit", 10);
            configFile.set("moneyLimit", 400000);

            configFile.set("clanCreateMessage", "§6»§7New clan §6@{clan}§7 was created successfully!");
            configFile.save();
        }

        File folder = new File(ConfigManager.PATH+"/clans");
        if (!folder.isDirectory()) folder.mkdirs();
    }

    @Override
    public void registerCommands() {
        registerCommand("clan", new ClanCommand("clan", this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, () -> this.sendInvitationsMessage(player), 20*4);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event){
        String message = event.getMessage();
        if (!message.startsWith("%")) return;

        Clan clan = this.getClan(event.getPlayer());
        if (clan == null) return;

        clan.chat(message.substring(1), event.getPlayer());
        event.setCancelled(true);
    }

    private void loadClan(SuperConfig config){
        String rawName = config.getName().split("\\.")[0];
        this.plugin.getLogger().info("§eLoading clan §3"+rawName+"§e!");

        this.clans.put(rawName, new Clan(rawName, config.getString("formattedName"), config, this));
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
        config.set("players", new ArrayList<>(Collections.singletonList(player.getName())));
        config.set("money", 0);
        config.set("maxMoney", configFile.getInt("moneyLimit", 400000));
        config.set("playerLimit", configFile.getInt("playerLimit", 10));
        config.save();

        Clan clan = new Clan(rawName, name, config, this);
        this.clans.put(rawName, clan);

        String message = configFile.getString("clanCreateMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{clan}", name);
        player.sendMessage(message);
        return clan;
    }

    public void invite(String playerName, Player executor){
        if (playerName == null || executor == null) return;

        Clan clan = this.getClan(executor);
        if (clan == null){
            executor.sendMessage("§c»§7You are not in any clan!");
            return;
        }

        Player player = Server.getInstance().getPlayer(playerName);
        if (player ==  null){
            executor.sendMessage("§c»§7Player §6@"+playerName+"§7 is not online!");
            return;
        }

        clan.invitePlayer(player, executor);
    }

    public void sendInvitationsMessage(Player player){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        List<String> pendingInvites = config.getStringList("clanInvites");

        if (pendingInvites.isEmpty()) return;
        StringBuilder builder = new StringBuilder("§3»§7You have new pending invites to this clans: ");

        for (String invite : pendingInvites){
            builder.append("§6").append(invite).append("§7,");
        }

        player.sendMessage(builder.substring(0, builder.length()-1)+"!");
        player.sendMessage("§6»§7TIP: Use §6/clan accept <name> §7or§6 /clan deny <name> §7to manage your invites!");
    }

    public void clearInvitations(Player player){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        config.set("clanInvites", new ArrayList<>());
        config.save();
    }

    public List<String> getInvitations(Player player){
        if (player == null) return null;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        return config.getStringList("clanInvites");
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

    public Map<String, Clan> getClans() {
        return this.clans;
    }
}

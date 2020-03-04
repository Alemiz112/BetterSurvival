package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.LevelVoteCommand;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.Task;

import java.util.*;

public class LevelVote extends Addon {

    public Map<String, Map<String, Integer>> votes = new HashMap<>();
    public List<String> votedPlayers = new ArrayList<>();

    public List<String> voteTopics;

    public LevelVote(String path){
        super("levelvote", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("defaultPermission", false);
            configFile.set("permission-vote", "bettersurvival.vote.normal");
            configFile.set("voteCooldown", 120);

            configFile.set("newVote", "§a»Player §6@{player}§7 started new vote about §6{vote}§7!");
            configFile.set("voteMessage", "§6»§7Thanks for voting! You will see vote result of §6{vote}§7 soon...");
            configFile.set("alreadyVoted", "§c»§7You have already voted for §6{vote}§7!");
            configFile.set("voteTopicNotFound", "§c»§7Topic §6{vote}§7 does not exists!");

            configFile.set("voteWinMessage", "§a»§7Voting about §6{vote}§7 has ended! Result is §6{result}.");
            configFile.set("permissionMessage", "§c»§7You dont have permission to vote about §6{vote}§7!");


            configFile.set("topics.weather.command", "weather {state}");
            configFile.set("topics.weather.parameters", new ArrayList<>(Arrays.asList("rain", "thunder", "clear")));
            configFile.set("topics.weather.usage", "Vote for ideal weather");

            configFile.set("topics.time.command", "time set {state}");
            configFile.set("topics.time.parameters", new ArrayList<>(Arrays.asList("day", "night")));
            configFile.set("topics.time.usage", "Vote for current time");


            configFile.save();
        }
    }

    @Override
    public boolean preLoad() {
        this.voteTopics = new ArrayList<>(configFile.getSection("topics").getKeys(false));
        return true;
    }

    @Override
    public void registerCommands() {
        registerCommand("levelvote", new LevelVoteCommand("levelvote", this, prepareUsage()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if (configFile.getBoolean("defaultPermission")){
            if (Addon.getAddon("playerpermissions") != null && (Addon.getAddon("playerpermissions") instanceof PlayerPermissions)){
                ((PlayerPermissions) Addon.getAddon("playerpermissions")).addPermission(player, configFile.getString("permission-vote"));
            }else {
                player.addAttachment(plugin, configFile.getString("permission-vote"), true);
            }
        }
    }

    public String prepareUsage(){
        String usage = "";

        for (String topic : this.voteTopics){
            String args = String.join("|", configFile.getStringList("topics."+topic+".parameters"));
            String topicUsage = configFile.getString("topics."+topic+".usage", "Custom topic");

            usage += "§7/levelvote "+topic+" <"+args+"> : "+topicUsage+"\n";
        }

        return usage.substring(0, usage.length()-1);
    }

    public void manageVote(String voteAbout){
        Map<String, Integer> result = this.votes.get(voteAbout);
        if (result == null) return;

        String preferredValue = getPreferredValue(result);
        if (preferredValue == null) return;

        String command = configFile.getString("topics."+voteAbout+".command");

        if (!command.equals("")){
            command = command.replace("{state}", preferredValue);
            this.plugin.getServer().dispatchCommand(new ConsoleCommandSender(), command);
        }

        this.votes.remove(voteAbout);
        this.votedPlayers.removeIf(vote -> (vote.startsWith(vote.toLowerCase())));

        String message = configFile.getString("voteWinMessage");
        message = message.replace("{vote}", voteAbout);
        message = message.replace("{result}", preferredValue);
        this.plugin.getServer().broadcastMessage(message);
    }

    public String getPreferredValue(Map<String, Integer> result){
        Integer max = Collections.max(result.values());

        for (Map.Entry<String, Integer> entry : result.entrySet()){
            if (entry.getValue() == max) return entry.getKey();
        }
        return null;
    }

    public void vote(Player player, String key, String value){
        if (player == null || !player.isConnected()) return;

        if (!player.hasPermission(configFile.getString("permission-vote")) &&
                !player.hasPermission(configFile.getString("permission-vote")+"."+key.toLowerCase())){
            String message = configFile.getString("permissionMessage");
            message = message.replace("{vote}", key);
            player.sendMessage(message);
            return;
        }

        if (this.votedPlayers.contains(key.toLowerCase()+"_"+player.getName().toLowerCase())){
            String message = configFile.getString("alreadyVoted");
            message = message.replace("{vote}", key);
            player.sendMessage(message);
            return;
        }

        if (!this.voteTopics.contains(key.toLowerCase())){
            String message = configFile.getString("voteTopicNotFound");
            message = message.replace("{vote}", key);
            player.sendMessage(message);
            return;
        }

        Map<String, Integer> voteData = this.votes.get(key);
        boolean newVote = (voteData == null);

        if (newVote){
            voteData = new HashMap<>();
            voteData.put(value.toLowerCase(), 1);
        }else {
            Integer votes = voteData.getOrDefault(value, 0);
            voteData.put(value, ++votes);
        }

        this.votes.put(key, voteData);
        this.votedPlayers.add(key.toLowerCase()+"_"+player.getName().toLowerCase());

        if (newVote){
            this.plugin.getServer().getScheduler().scheduleDelayedTask(new Task() {
                @Override
                public void onRun(int i) {
                    manageVote(key);
                }
            }, 20*configFile.getInt("voteCooldown"));

            this.plugin.getServer().broadcastMessage(configFile.getString("newVote"));
        }

        String message = configFile.getString("voteMessage");
        message = message.replace("{vote}", key);
        player.sendMessage(message);
    }
}

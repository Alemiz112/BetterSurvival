package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    private final PlayerClans loader;

    private final String rawName;
    private final String name;

    private Config config;

    private final String owner;
    private final List<String> players = new ArrayList<>();

    private int money;

    public Clan(String rawName, String name, Config config, PlayerClans loader){
        this.rawName = rawName;
        this.name = name;
        this.owner = config.getString("owner");
        this.players.addAll(config.getStringList("players"));
        this.money = config.getInt("money");

        this.loader = loader;
        this.config = config;
    }

    public void setMoney(int value){
        this.money = value;
        config.set("money", value);
        config.save();
    }

    public void addMoney(int value){
        int balance = this.money+value;

        if (balance > this.config.getInt("maxMoney")) return;
        this.setMoney(balance);
    }

    public boolean reduceMoney(int value){
        if ((this.money - value) < 0) return false;

        this.setMoney(this.money - value);
        return true;
    }

    private void savePlayerList(){
        config.set("players", players);
        config.save();
    }

    //TODO: do not forget to clear all other pending invitations if any is accepted
    //TODO: create and allow admins to invite player
    public void invitePlayer(Player player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7You do not have permission to invite player to clan!");
            return;
        }

        if (this.players.contains(player.getName())){
            if (executor != null) executor.sendMessage("§c»§7Player is already in your clan!");
            return;
        }

        if (this.loader.getClan(player) != null){
            if (executor != null) executor.sendMessage("§c»§7Player is already member of other clan!");
            return;
        }

        int limit = config.getInt("playerLimit");
        if (this.players.size() >= limit){
            if (executor != null) executor.sendMessage("§c»§7Your clan player limit is §6"+limit+"§7 players. You can not add another player! " +
                    "TIP: Upgrade your clan and get more player slots!");
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        List<String> pendingInvites = config.getStringList("clanInvites");

        pendingInvites.add(this.rawName);
        config.set("clanInvites", pendingInvites);
        config.save();

        player.sendMessage("§6»§7You was invited to join §6@"+this.name+" Clan! Use §6/clan accept <name> §7or§6 /clan deny <name> §7to manage your invite!");
    }

    public void addPlayer(Player player){
        if (player == null) return;

        this.sendMessage("Player @6"+player.getName()+" joined your clan!");
        this.players.add(player.getName());
        this.savePlayerList();

        player.sendMessage("§6»§7You joined §6@"+this.name+"§7 Clan! Welcome to your new Home!");
    }

    //TODO: create and allow admins to kick player
    public void kickPlayer(String playerName, Player executor){
        if (playerName == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7You do not have permission to invite player to clan!");
            return;
        }

        if (!this.players.contains(playerName)){
            if (executor != null) executor.sendMessage("§c»§7Player §6@"+playerName+" is not member in your clan!");
            return;
        }

        this.sendMessage("Player @6"+playerName+" was kicked from your clan!");
        this.players.remove(playerName);
        this.savePlayerList();

        Player player = Server.getInstance().getPlayer(playerName);
        if (player != null) player.sendMessage("§c»§7You was kicked from §6@"+this.name+"§7 Clan!");
    }

    public void removePlayer(Player player){
        if (player == null) return;

        this.sendMessage("Player @6"+player.getName()+" leaved your clan!");
        this.players.remove(player.getName());
        this.savePlayerList();

        player.sendMessage("§6»§7You leaved §6@"+this.name+"§7 Clan! Welcome to your new Home!");
    }

    public void chat(String message, Player player){
        if (message == null || message.isEmpty() || player == null) return;
        this.sendMessage(message, player.getName());
    }

    public void sendMessage(String message){
        this.sendMessage(message, null);
    }

    public void sendMessage(String message, String author){
        String formattedMessage = "§f[§a"+this.name+"§f]"+(author == null? "" : author)+": §7"+message;

        for (String playerName : this.players){
            Player member = Server.getInstance().getPlayer(playerName);

            if (member == null) continue;
            member.sendMessage(formattedMessage);
        }
    }

    public String getRawName() {
        return this.rawName;
    }

    public String getName() {
        return this.name;
    }

    public String getOwner() {
        return this.owner;
    }

    public List<String> getPlayers() {
        return this.players;
    }

    public int getMoney() {
        return this.money;
    }

    public Config getConfig(){
        return config;
    }
}

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
        this.setMoney(this.money + value);
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
    public void invitePlayer(Player player, Player executor){
        if (player == null) return;

        if (this.loader.getClan(player) != null){
            executor.sendMessage("§c»§7Player is already member of other clan!");
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

        for (String playerName : this.players){
            Player member = Server.getInstance().getPlayer(playerName);
            if (member == null) continue;
            member.sendMessage("§6»§7Player @6"+player.getName()+" joined your clan!");
        }

        this.players.add(player.getName());
        this.savePlayerList();

        player.sendMessage("§6»§7You joined §6@"+this.name+" Clan! Welcome to your new Home!");
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

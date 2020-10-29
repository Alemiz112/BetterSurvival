/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.myland.LandRegion;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clan {

    private final PlayerClans loader;

    private final String rawName;
    private final String name;

    private Config config;

    private final String owner;
    private final List<String> players = new ArrayList<>();
    private final List<String> admins = new ArrayList<>();

    private final Map<String, Position> homes = new HashMap<>();

    private int money;

    public Clan(String rawName, String name, Config config, PlayerClans loader){
        this.rawName = rawName;
        this.name = name;
        this.owner = config.getString("owner");
        this.players.addAll(config.getStringList("players"));
        this.admins.addAll(config.getStringList("admins"));
        this.money = config.getInt("money");

        this.loader = loader;
        this.config = config;

        this.loadHomes();
    }

    public void setMoney(int value){
        this.money = value;
        config.set("money", value);
        config.save();
    }

    public boolean addMoney(int value){
        int balance = this.money+value;

        if (balance > this.config.getInt("maxMoney")) return false;
        this.setMoney(balance);
        return true;
    }

    public boolean reduceMoney(int value){
        if ((this.money - value) < 0) return false;

        this.setMoney(this.money - value);
        return true;
    }

    private void savePlayerList(){
        config.set("players", this.players);
        config.set("admins", this.admins);
        config.save();
    }

    private void saveHomes(){
        Map<String, String> homeMap = new HashMap<>();
        for (String homeName : this.homes.keySet()){
            Position home = this.homes.get(homeName);
            String homeString = home.getX()+","+home.getY()+","+home.getZ()+","+home.getLevel().getFolderName();
            homeMap.put(homeName, homeString);
        }
        config.set("home", homeMap);
        config.save();
    }

    private void loadHomes(){
        ConfigSection section = config.getSection("home");
        for (String home : section.getKeys(false)){
            String homeString = section.getString(home);
            String[] data = homeString.split(",");

            Level level = Server.getInstance().getLevelByName(data[3]);
            if (level == null) continue;

            try {
                this.homes.put(home.toLowerCase(), new Position(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), level));
            }catch (Exception e){
                BetterSurvival.getInstance().getLogger().warning("§cUnable to load home §4"+home+"§c for clan §4"+this.rawName);
            }
        }
    }

    public boolean isMember(Player player){
        return player != null && this.isMember(player.getName());
    }

    public boolean isMember(String player){
        return this.players.contains(player) || this.owner.equalsIgnoreCase(player);
    }

    public boolean isAdmin(Player player){
        return player != null && this.isAdmin(player.getName());
    }

    public boolean isAdmin(String player){
        return this.admins.contains(player.toLowerCase());
    }

    public void addAdmin(String player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7You do not have permission to manage clan admins!");
            return;
        }

        if (this.isAdmin(player)){
            if (executor != null) executor.sendMessage("§c»§7Player is already admin of your clan!");
            return;
        }

        if (executor != null && executor.getName().equalsIgnoreCase(player)){
            executor.sendMessage("§c»§7You can not add yourself as clan admin!");
            return;
        }

        if (this.owner.equalsIgnoreCase(player)){
            if (executor != null) executor.sendMessage("§c»§7You are clan owner! You can not be clan admin.!");
            return;
        }

        this.admins.add(player.toLowerCase());
        this.savePlayerList();
        this.sendMessage("§2Player §6@"+player+"§2 was promoted to clan admin!");
    }

    public void removeAdmin(String player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7You do not have permission to manage clan admins!");
            return;
        }

        if (!this.isAdmin(player.toLowerCase())){
            if (executor != null) executor.sendMessage("§c»§7Player is not admin of your clan!");
            return;
        }

        this.admins.remove(player.toLowerCase());
        this.savePlayerList();
        this.sendMessage("§4Player §6@"+player+"§4 was demoted to clan member!");
    }

    public void invitePlayer(Player player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName()) && !this.isAdmin(executor)){
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

        int limit = this.config.getInt("playerLimit");
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
        if (executor != null) executor.sendMessage("§6»§7You have invited §6@"+player.getDisplayName()+"§7 to your clan!");
    }

    public boolean addPlayer(Player player){
        if (player == null) return false;

        int limit = this.config.getInt("playerLimit");
        if (this.players.size() >= limit){
            player.sendMessage("§c»§7Clan §6@"+this.name+"§7 is full at the moment! You can not join!");
            return false;
        }

        this.sendMessage("Player §6@"+player.getDisplayName()+" joined your Clan!");
        this.players.add(player.getName());
        this.savePlayerList();

        player.sendMessage("§6»§7You joined §6@"+this.name+"§7 Clan! Welcome to your new Home!");
        return true;
    }

    public void kickPlayer(String playerName, Player executor){
        if (playerName == null) return;

        boolean admin = executor != null && this.isAdmin(executor);
        if (executor != null && !this.owner.equals(executor.getName()) && !admin){
            executor.sendMessage("§c»§7You do not have permission to kick player from clan!");
            return;
        }

        if (this.isAdmin(playerName) && admin){
            executor.sendMessage("§c»§7You do not have permission to kick clan admin!");
            return;
        }

        if (executor != null && executor.getName().equalsIgnoreCase(playerName)){
            executor.sendMessage("§c»§7You can not kick yourself from clan!");
            return;
        }

        if (playerName.equalsIgnoreCase(this.owner)){
            if (executor != null) executor.sendMessage("§c»§7You can not kick owner of clan!");;
            return;
        }

        if (!this.players.contains(playerName)){
            if (executor != null) executor.sendMessage("§c»§7Player §6@"+playerName+" is not member in your clan!");
            return;
        }

        this.sendMessage("Player §6@"+playerName+" was kicked from your clan!");
        this.players.remove(playerName);
        this.savePlayerList();

        Player player = Server.getInstance().getPlayer(playerName);
        if (player != null) player.sendMessage("§c»§7You was kicked from §6@"+this.name+"§7 Clan!");
    }

    public void removePlayer(Player player){
        if (player == null) return;

        if (this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7You are owner of this clan, you can not leave. If you want to cancel you clan use §e/clan destroy§7.");
            return;
        }

        this.players.remove(player.getName());
        this.sendMessage("Player @6"+player.getDisplayName()+" left your Clan!");
        this.savePlayerList();

        player.sendMessage("§6»§7You left §6@"+this.name+"§7 Clan!");
    }

    public void createBankNote(Player player, int value){
        if (player == null || value == 0) return;

        if (!this.owner.equalsIgnoreCase(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7You do not have permission to create clan bank note!");
            return;
        }

        if (Addon.getAddon(BetterEconomy.class) == null){
            player.sendMessage("§c»§7Economy addon is not enabled!");
            return;
        }

        BetterEconomy economy = (BetterEconomy) Addon.getAddon(BetterEconomy.class);
        economy.createNote(player, value, true);
    }

    public void applyBankNote(Player player){
        if (Addon.getAddon(BetterEconomy.class) == null){
            player.sendMessage("§c»§7Economy addon is not enabled!");
            return;
        }

        BetterEconomy economy = (BetterEconomy) Addon.getAddon(BetterEconomy.class);
        Item item = player.getInventory().getItemInHand();

        if (item.getId() == Item.AIR){
            player.sendMessage("§c»§r§7You must hold Bank Note item!");
            return;
        }
        Item result = economy.applyNote(player, item, true);
        if (result != null) player.getInventory().setItemInHand(result);
    }

    public void createLand(Player player){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7You do not have permission to create clan land!");
            return;
        }

        if (Addon.getAddon(MyLandProtect.class) == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon(MyLandProtect.class);
        landProtect.createLand(player, "", true);
    }

    public void removeLand(Player player){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7You do not have permission to remove clan land!");
            return;
        }

        if (Addon.getAddon(MyLandProtect.class) == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon(MyLandProtect.class);
        landProtect.removeClanLand(player);
    }

    public void landWhitelist(Player player, String action, String[] args){
        ClanLand land = this.getLand();
        if (land == null){
            player.sendMessage("§c»§7Your clan has not land!");
            return;
        }

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7Land settings can be configured by clan owner only!");
            return;
        }

        if (Addon.getAddon(MyLandProtect.class) == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon(MyLandProtect.class);

        if (!action.equals("on") && !action.equals("off")){
            if (args.length < 1 && !action.equals(LandRegion.WHITELIST_LIST)){
                player.sendMessage("§c»§7Command can not be proceed! Please provide additional value to complete this command!");
                return;
            }

            landProtect.whitelist(player, String.join(" ", args), land, action);
            return;
        }

        boolean state = action.equalsIgnoreCase("on");
        land.setWhitelistEnabled(state);
        player.sendMessage("§a»§7Land whitelist has been turned §6"+(state? "on" : "off")+"§7!");
    }

    public ClanLand getLand(){
        if (Addon.getAddon(MyLandProtect.class) == null){
            return null;
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon(MyLandProtect.class);
        LandRegion land = landProtect.getLands().get(this.rawName);
        return (land instanceof ClanLand)? (ClanLand) land : null;
    }

    public void createHome(Player player, String name){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7You do not have permission to create clan home!");
            return;
        }

        int homeLimit = this.config.getInt("homeLimit", 10);
        if (this.homes.size() >= homeLimit){
            player.sendMessage("§c»§7Your clan has passed home limit which is §6"+homeLimit+"§7 homes!");
            return;
        }

        if (this.homes.containsKey(name.toLowerCase())){
            player.sendMessage("§c»§7Your clan has already home with same name!");
            return;
        }

        this.homes.put(name.toLowerCase(), player.clone());
        this.saveHomes();
        player.sendMessage("§6»§7Your have successfully created clan home!");
    }

    public void removeHome(Player player, String home){
        if (player == null) return;

        if (!this.owner.equals(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7You do not have permission to remove clan home!");
            return;
        }

        if (!this.homes.containsKey(home.toLowerCase())){
            player.sendMessage("§c»§7Clan home with name §6"+home+"§7 was not found!");
            return;
        }

        this.homes.remove(home.toLowerCase());
        this.saveHomes();
        player.sendMessage("§6»§7Your have successfully removed clan home!");
    }

    public void teleportToHome(Player player, String home){
        if (player == null) return;

        if (!this.homes.containsKey(home.toLowerCase())){
            player.sendMessage("§c»§7Clan home with name §6"+home+"§7 was not found!");
            return;
        }

        player.teleport(this.homes.get(home.toLowerCase()));
        player.sendMessage("§6»§7Woosh! Welcome at clan home §6"+home+" @"+player.getDisplayName()+"§7!");
    }

    //May be useful in feature
    public void onApplyNote(Player player, int value){
        this.onDonate(player, value);
    }

    public void onDonate(Player player, int value){
        this.sendMessage("Player §6@"+player.getDisplayName()+"§f donated to clan bank value of §e"+TextUtils.formatBigNumber(value)+"$§f!");
    }

    public void chat(String message, Player player){
        if (message == null || message.isEmpty() || player == null) return;
        this.sendMessage(message, player.getName());
    }

    public void sendMessage(String message){
        this.sendMessage(message, null);
    }

    public void sendMessage(String message, String author){
        String formattedMessage = "§f[§a"+this.name+"§f] §7"+(author == null? "" : author)+": §f"+message;

        for (String playerName : this.players){
            Player member = Server.getInstance().getPlayer(playerName);

            if (member == null) continue;
            member.sendMessage(formattedMessage);
        }
    }

    public String buildTextInfo(){
        int moneyLimit = this.config.getInt("maxMoney");
        int playerLimit = this.config.getInt("playerLimit");
        int homeLimit = this.config.getInt("homeLimit", 10);

        return "§a"+this.name+"§a Clan:\n" +
                "§3»§7 Owner: "+this.owner+"\n" +
                "§3»§7 Money: §e"+this.money+"§7/§6"+moneyLimit+"$\n" +
                "§3»§7 Land: §e"+(this.hasLand()? "Yes" : "No")+"\n" +
                "§3»§7 Admin List: §e"+(this.admins.size() == 0? "None" : String.join(", ", this.admins))+"\n" +
                "§3»§7 Players: §c"+this.players.size()+"§7/§4"+playerLimit+"\n" +
                "§3»§7 Player List: §e"+String.join(", ", this.players)+"\n" +
                "§3»§7 Homes: §a"+this.homes.size()+"§7/§2"+homeLimit+"\n" +
                "§3»§7 Home List: §e"+(this.homes.size() == 0? "None" : String.join(", ", this.homes.keySet()));
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

    public int getMaxMoney(){
        return this.config.getInt("maxMoney");
    }

    public Config getConfig(){
        return config;
    }

    public boolean hasLand(){
        return this.config.exists("land");
    }

    public Map<String, Position> getHomes() {
        return this.homes;
    }
}

/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.commands.ClanCommand;
import alemiz.bettersurvival.commands.ClanLevelsCommand;
import alemiz.bettersurvival.commands.ClanTopCommand;
import alemiz.bettersurvival.commands.ClanWarCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.util.*;

public class PlayerClans extends Addon {

    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<Integer, ClanLevelInfo> clanLevels = new HashMap<>();
    private ClanLevelInfo lowestLevel;

    public PlayerClans(String path) {
        super("playerclans", path);
    }

    @Override
    public void postLoad() {
        ConfigSection section = this.configFile.getSection("clanLevels");
        for (String levelText : section.getKeys(false)) {
            ConfigSection levelData = section.getSection(levelText);
            int level = levelData.getInt("level");
            int requiredPoints = levelData.getInt("points");

            ClanLevelInfo levelInfo = new ClanLevelInfo(level, requiredPoints);
            if (levelData.exists("moneyLimit")) {
                levelInfo.setMoneyLimit(levelData.getInt("moneyLimit"));
            }
            if (levelData.exists("playerLimit")) {
                levelInfo.setPlayerLimit(levelData.getInt("playerLimit"));
            }
            if (levelData.exists("homeLimit")) {
                levelInfo.setHomeLimit(levelData.getInt("homeLimit"));
            }
            if (levelData.exists("landSize")) {
                levelInfo.setMaxLandSize(levelData.getInt("landSize"));
            }

            this.clanLevels.put(level, levelInfo);
            if (this.lowestLevel == null || this.lowestLevel.getLevel() > level) {
                this.lowestLevel = levelInfo;
            }
        }

        int failedAttempts = 0;
        for (SuperConfig config : ConfigManager.getInstance().loadAllFromFolder(ConfigManager.PATH+"/clans")){
            String rawName = config.getName().split("\\.")[0];
            try {
                this.loadClan(rawName, config);
            }catch (Exception e){
                failedAttempts++;
                this.plugin.getLogger().error("§cFailed to load clan §4"+rawName+"§c!", e);
            }
        }

        this.plugin.getLogger().info("§eAll clans loaded! Entries: §3"+this.clans.size());
        this.plugin.getLogger().info("§eFailed: §c"+failedAttempts);

        for (Clan clan : this.clans.values()) {
            clan.loadWarClans();
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("clanCreatePayment", 80000);

            configFile.set("playerLimit", 10);
            configFile.set("homeLimit", 10);
            configFile.set("moneyLimit", 400000);
            configFile.set("maxLandSize", 375);
            configFile.set("warKillPoints", 5);

            configFile.set("clanCreateMessage", "§6»§7New clan §6@{clan}§7 was created successfully!");
            configFile.set("clanWarInviteFormMessage", "§fYou can invite any of clans to war. Fighting with players from the clan will increase your clan points!\n§7Some server rules during clan war may be different!");
            configFile.set("clanWarInvitesFormMessage", "§fThis is the list of pending clan war invites. Would you like to join the war?\n§7Click the clan to manage it.");
            configFile.set("clanWarJoinFormMessage", "§fWould you like to join in war with clan §e{clan}§f?\n§7Keep in mind that some pvp rules may be different when you are in clan war!");
            configFile.set("clanWarLeaveFormMessage", "§fYou are in war with clan §e{clan}!\n§7Would you like to leave war?");

            configFile.set("warKillMessage", "§eKilled {target}! §7[+{points}XP]");

            ConfigSection clanLevels = new ConfigSection();
            ConfigSection level1 = new ConfigSection();
            level1.set("level", 1);
            level1.set("moneyLimit", 600000);
            level1.set("playerLimit", 12);
            level1.set("homeLimit", 12);
            level1.set("landSize", 400);
            clanLevels.set("level1", level1);

            ConfigSection level2 = new ConfigSection();
            level2.set("level", 2);
            level2.set("moneyLimit", 800000);
            level2.set("playerLimit", 16);
            level2.set("homeLimit", 16);
            level2.set("landSize", 450);
            clanLevels.set("level2", level2);

            configFile.set("clanLevels", clanLevels);
            configFile.save();
        }

        File folder = new File(ConfigManager.PATH+"/clans");
        if (!folder.isDirectory()) folder.mkdirs();
    }

    @Override
    public void registerCommands() {
        this.registerCommand("clan", new ClanCommand("clan", this));
        this.registerCommand("clanwar", new ClanWarCommand("clanwar", this));
        this.registerCommand("topclans", new ClanTopCommand("topclans", this));
        this.registerCommand("clanlevels", new ClanLevelsCommand("clanlevels", this));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, () -> this.onPlayerInitialize(player), 80);
    }

    private void onPlayerInitialize(Player player) {
        if (player == null || !player.isConnected()) {
            return;
        }

        this.sendInvitationsMessage(player);

        Clan clan = this.getClan(player);
        if (clan == null || (!clan.isAdmin(player) && !clan.isOwner(player)) || clan.getWarClanInvites().isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder("§3»§7Your clan was invited to war by this clans:");
        for (Clan warClan : clan.getWarClanInvites()) {
            builder.append("\n§7- §e").append(warClan.getName());
        }
        player.sendMessage(builder.toString());
    }

    @EventHandler
    public void onChat(PlayerChatEvent event){
        String message = event.getMessage();
        if (!message.startsWith("%")) {
            return;
        }

        Clan clan = this.getClan(event.getPlayer());
        if (clan != null) {
            clan.chat(message.substring(1), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getEntity();
        if ((target.getHealth() - event.getFinalDamage()) > 0.5){
            return;
        }

        Player player = null;
        if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            player = (Player) ((EntityDamageByEntityEvent) event).getDamager();
        } else if (target.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent cause = (EntityDamageByEntityEvent) target.getLastDamageCause();
            if (cause.getDamager() instanceof Player) {
                player = (Player) cause.getDamager();
            }
        }

        if (player == null) {
            return;
        }

        Clan clan;
        Clan targetClan;
        if ((clan = this.getClan(player)) != null && (targetClan = this.getClan(target)) != null) {
            clan.onEnemyKilled(player, target, targetClan);
        }
    }

    private void loadClan(String rawName, SuperConfig config){
        Clan clan = new Clan(rawName, config.getString("formattedName"), config, this);
        this.clans.put(rawName, clan);

        if (config.exists("land") && Addon.getAddon(MyLandProtect.class) != null){
            Addon.getAddon(MyLandProtect.class).loadClanLand(clan);
        }
    }

    public Clan createClan(Player player, String name){
        if (player == null || name.isEmpty()) return null;

        if (this.getClan(player) != null){
            player.sendMessage("§c»§7You are already in clan. Please leave it first. TIP: Use §6/clan leave§7 to leave clan.");
            return null;
        }

        int clanPrice = configFile.getInt("clanCreatePayment");
        int clanRevert = (clanPrice / 100) * 75;

        boolean success = EconomyAPI.getInstance().reduceMoney(player, clanPrice) >= 1;
        if (!success){
            player.sendMessage("§c»§7You do not have enough coins to pay clan fee." +
                    " Clan fee consists of §e"+ TextUtils.formatBigNumber(clanPrice)+"$§7,\n" +
                    "§c75%§f of this fee will be return to clan bank.");
            return null;
        }

        name = TextFormat.clean(name);
        String rawName = name.toLowerCase().replace(" ", "_");
        Config config = new Config(ConfigManager.PATH+"/clans/"+rawName+".yml", Config.YAML);

        config.set("formattedName", name);
        config.set("owner", player.getName());
        config.set("players", new ArrayList<>(Collections.singletonList(player.getName())));
        config.set("money", clanRevert);
        config.set("maxMoney", configFile.getInt("moneyLimit", 400000));
        config.set("playerLimit", configFile.getInt("playerLimit", 10));
        config.set("homeLimit", configFile.getInt("homeLimit", 10));
        config.set("maxLandSize", configFile.getInt("maxLandSize", 375));
        config.save();

        Clan clan = new Clan(rawName, name, config, this);
        this.clans.put(rawName, clan);

        String message = configFile.getString("clanCreateMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{clan}", name);
        player.sendMessage(message);
        return clan;
    }

    public void destroyClan(Player player, Clan clan){
        if (player == null || clan == null) return;

        if (!clan.getOwner().equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7This command can be run only by Clan owner!");
            return;
        }

        if (clan.hasLand()){
            clan.removeLand(player);
        }

        clan.sendMessage("§cYour clan was destroyed!");
        this.clans.remove(clan.getRawName());

        File file = new File(ConfigManager.PATH+"/clans/"+clan.getRawName()+".yml");
        if (file.isFile()) file.delete();

        player.sendMessage("§6»§7You have successfully closed your clan.");
    }

    public void invite(String playerName, Player executor){
        if (playerName == null || executor == null) return;

        Clan clan = this.getClan(executor);
        if (clan == null){
            executor.sendMessage("§c»§7You are not in any Clan!");
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

    public Clan getClanByName(String clanRawName) {
        return this.clans.get(clanRawName);
    }

    public boolean isOwner(Player player, String clanName){
        if (player == null) {
            return false;
        }

        for (Clan clan : this.clans.values()){
            if (!clan.getName().equals(clanName) || !clan.getRawName().equals(clanName)) continue;

            if (clan.getOwner().equals(player.getName())){
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Player player, Clan clan){
        if (player == null || clan == null) {
            return false;
        }
        return clan.getOwner().equals(player.getName());
    }

    public ClanLevelInfo getLevelFromPoints(int points, ClanLevelInfo oldLevel) {
        ClanLevelInfo levelInfo = this.clanLevels.get(oldLevel.getLevel()+ 1);
        if (levelInfo == null) {
            return oldLevel;
        }
        return points >= levelInfo.getRequiredPoints()? levelInfo : oldLevel;
    }

    public int getLevelMinPoints(int level) {
        ClanLevelInfo levelInfo = this.clanLevels.get(level);
        return levelInfo == null ? 0 : levelInfo.getRequiredPoints();
    }

    public ClanLevelInfo getLevel(int level) {
        ClanLevelInfo levelInfo = this.clanLevels.get(level);
        if (levelInfo == null) {
            return this.lowestLevel;
        }
        return levelInfo;
    }

    public ClanLevelInfo getLowestLevel() {
        return this.lowestLevel;
    }

    public Collection<ClanLevelInfo> getClanLevels() {
        return this.clanLevels.values();
    }

    public Map<String, Clan> getClans() {
        return this.clans;
    }
}

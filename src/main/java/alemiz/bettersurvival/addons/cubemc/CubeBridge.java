package alemiz.bettersurvival.addons.cubemc;

import alemiz.bettersurvival.addons.BetterLobby;
import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.addons.Troller;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.plugin.PluginEnableEvent;
import cubemc.commons.nukkit.events.CubePlayerJoinEvent;
import cubemc.commons.nukkit.events.RanksLoadEvent;
import cubemc.commons.nukkit.utils.scoreboard.AdvancedScoreboard;
import cubemc.commons.nukkit.utils.scoreboard.ScoreLineEntry;
import cubemc.commons.ranks.Rank;
import cubemc.nukkit.connector.CubeConnector;

import java.util.*;

public class CubeBridge extends Addon {

    private final Map<String, RankData> rankDataMap = new HashMap<>();
    private AdvancedScoreboard scoreboard = null;

    public CubeBridge(String path){
        super("cubebridge", path);
    }

    @Override
    public void postLoad() {
        this.scoreboard = new AdvancedScoreboard("survival", "survival");

        ScoreLineEntry playerCountLine = new ScoreLineEntry("\uE180§e {count}");
        playerCountLine.addTranslation((text, player) ->
                text.replace("{count}", String.valueOf(this.plugin.getServer().getOnlinePlayersCount())));
        this.scoreboard.addLine(playerCountLine, false);

        ScoreLineEntry mainEntry = new ScoreLineEntry("§bCube§6MC §aSurvival{vanish}");
        mainEntry.addTranslation(this::translateMainScore);
        this.scoreboard.addLine(mainEntry, false);
        this.scoreboard.updateBoard();
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("rank.subscriber", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.cube+", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.vip", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.moderator", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.helper", new ArrayList<>(Collections.emptyList()));
            configFile.save();
        }
    }

    @EventHandler
    public void onNetworkLoad(PluginEnableEvent event) {
        if (!(event.getPlugin() instanceof CubeConnector)) {
            return;
        }

        CubeConnector connector = (CubeConnector) event.getPlugin();
        BetterLobby betterLobby = Addon.getAddon(BetterLobby.class);
        if (betterLobby != null && betterLobby.isEnabled()) {
            connector.getPlayerManager().getRegisterHelper().setJoinMessage(betterLobby.getJoinMessage());
        }
    }

    @EventHandler
    public void onRanksLoad(RanksLoadEvent event){
        this.loadRanks(new ArrayList<>(event.getRanks().values()));
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event){
        if (event.getPlayer() == null){
            return;
        }
        Player player = event.getPlayer();
        this.scoreboard.addPlayer(player);
        this.scoreboard.updateBoard();

        for (Rank rank : event.getPlayerEntry().getRanks()){
            RankData rankData = this.getRankData(rank.getName());
            if (rankData != null){
                rankData.assignPermissions(player, this.plugin);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.scoreboard.removePlayer(player);
        this.plugin.getServer().getScheduler().scheduleDelayedTask(() ->
                this.scoreboard.updateBoard(), 10);
    }

    public void loadRanks(List<Rank> ranks){
        for (Rank rank : ranks){
            RankData rankData = new RankData(rank);

            List<String> permissions = configFile.getStringList("rank."+rank.getName().toLowerCase());
            if (!permissions.isEmpty()){
                rankData.addPermissions(permissions);
            }
            this.rankDataMap.put(rank.getName().toLowerCase(), rankData);
        }
        this.initAddonPermissions();
    }

    private void initAddonPermissions(){
        MoreVanilla moreVanilla = Addon.getAddon(MoreVanilla.class);
        MyHomes myHomes = Addon.getAddon(MyHomes.class);
        MyLandProtect myLandProtect = Addon.getAddon(MyLandProtect.class);
        SurvivalShop survivalShop = Addon.getAddon(SurvivalShop.class);
        Troller troller = Addon.getAddon(Troller.class);

        RankData subscriber = this.getRankData("subscriber");
        if (subscriber != null){
            if (moreVanilla != null) {
                subscriber.addPermission(moreVanilla.configFile.getString("permission-feed"));
                subscriber.addPermission(moreVanilla.configFile.getString("permission-jump"));
            }
        }

        RankData cubePlus = this.getRankData("cube+");
        if (cubePlus != null){
            if (moreVanilla != null) {
                cubePlus.addPermission(moreVanilla.configFile.getString("permission-feed"));
                cubePlus.addPermission(moreVanilla.configFile.getString("permission-near"));
                cubePlus.addPermission(moreVanilla.configFile.getString("permission-keepInvAll"));
            }
            if (myHomes != null) {
                cubePlus.addPermission(myHomes.configFile.getString("permission-vip"));
            }
        }

        RankData vip = this.getRankData("vip");
        if (vip != null){
            if (moreVanilla != null) {
                vip.addPermission(moreVanilla.configFile.getString("permission-heal"));
                vip.addPermission(moreVanilla.configFile.getString("permission-fly"));
                vip.addPermission(moreVanilla.configFile.getString("permission-near"));
                vip.addPermission(moreVanilla.configFile.getString("permission-jump"));
            }
            if (myLandProtect != null) {
                vip.addPermission(myLandProtect.configFile.getString("landsVipPermission"));
            }
            if (survivalShop  != null) {
                vip.addPermission(survivalShop.configFile.getString("shopVipPermission"));
            }
        }

        RankData staffRank = this.getRankData("moderator");
        if (staffRank != null){
            if (troller != null) {
                staffRank.addPermission(troller.configFile.getString("permission-vanish"));
                staffRank.addPermission(troller.configFile.getString("permission-troll"));
                staffRank.addPermission(troller.configFile.getString("permission-troll-advanced"));
                staffRank.addPermission(troller.configFile.getString("permission-invsee"));
            }
            if (moreVanilla != null) {
                staffRank.addPermission(moreVanilla.configFile.getString("permission-mute"));
                staffRank.addPermission(moreVanilla.configFile.getString("permission-manage"));
                staffRank.addPermission(moreVanilla.configFile.getString("permission-keepInvAll"));
            }
            if (myLandProtect != null) {
                staffRank.addPermission(myLandProtect.configFile.getString("chestsAccessPermission"));
                staffRank.addPermission(myLandProtect.configFile.getString("landsAccessPermission"));
            }
        }

        RankData helperRank = this.getRankData("helper");
        if (helperRank != null){
            if (moreVanilla != null) {
                helperRank.addPermission(moreVanilla.configFile.getString("permission-near"));
                helperRank.addPermission(moreVanilla.configFile.getString("permission-mute"));
                helperRank.addPermission(moreVanilla.configFile.getString("permission-fly"));
            }
            if (troller != null) {
                helperRank.addPermission(troller.configFile.getString("permission-invsee"));
            }
        }
    }

    private String translateMainScore(String text, Player player) {
        Troller troller = Addon.getAddon(Troller.class);
        if (troller == null || !troller.isEnabled()) {
            return text.replace("{vanish}", "");
        }

        boolean vanished = troller.getVanishPlayers().contains(player.getName());
        return text.replace("{vanish}", vanished? "§7 - §cVanished": "");
    }

    public RankData getRankData(String rankName){
        return this.rankDataMap.get(rankName.toLowerCase());
    }

    public Map<String, RankData> getRankDataMap() {
        return this.rankDataMap;
    }

    public AdvancedScoreboard getScoreboard() {
        return this.scoreboard;
    }
}

package alemiz.bettersurvival.addons.cubemc;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.addons.Troller;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cubemc.commons.nukkit.events.CubePlayerJoinEvent;
import cubemc.commons.nukkit.events.RanksLoadEvent;
import cubemc.commons.ranks.Rank;
import cubemc.nukkit.connector.CubeConnector;

import java.util.*;

public class CubeBridge extends Addon {

    private final Map<String, RankData> rankDataMap = new HashMap<>();

    public CubeBridge(String path){
        super("cubebridge", path);
    }

    @Override
    public void postLoad() {
        this.loadRanks(new ArrayList<>(CubeConnector.getInstance().rankManager.getRanks().values()));
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("rank.subscriber", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.cube+", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.vip", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.staff", new ArrayList<>(Collections.emptyList()));
            configFile.set("rank.helper", new ArrayList<>(Collections.emptyList()));
            configFile.save();
        }
    }

    @EventHandler
    public void onRanksLoad(RanksLoadEvent event){
        this.loadRanks(new ArrayList<>(event.getRanks().values()));
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event){
        Player player = event.getPlayer();
        if (player == null) return;

        for (Rank rank : event.getRanks()){
            RankData rankData = this.getRankData(rank.getName());
            if (rankData != null){
                rankData.assignPermissions(player, this.plugin);
            }
        }
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
        RankData subscriber = this.getRankData("subscriber");
        if (subscriber != null){
            subscriber.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-feed"));
            subscriber.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-jump"));
        }

        RankData cubePlus = this.getRankData("cube+");
        if (cubePlus != null){
            cubePlus.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-feed"));
            cubePlus.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-near"));
            cubePlus.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-keepInvAll"));
            cubePlus.addPermission(Addon.getAddon(MyHomes.class).configFile.getString("permission-vip"));
        }

        RankData vip = this.getRankData("vip");
        if (vip != null){
            vip.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-heal"));
            vip.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-fly"));
            vip.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-near"));
            vip.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-jump"));

            vip.addPermission(Addon.getAddon(MyLandProtect.class).configFile.getString("landsVipPermission"));
            vip.addPermission(Addon.getAddon(SurvivalShop.class).configFile.getString("shopVipPermission"));
        }

        RankData staffRank = this.getRankData("staff");
        if (staffRank != null){
            staffRank.addPermission(Addon.getAddon(Troller.class).configFile.getString("permission-vanish"));
            staffRank.addPermission(Addon.getAddon(Troller.class).configFile.getString("permission-troll"));
            staffRank.addPermission(Addon.getAddon(Troller.class).configFile.getString("permission-troll-advanced"));
            staffRank.addPermission(Addon.getAddon(Troller.class).configFile.getString("permission-invsee"));

            staffRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-mute"));
            staffRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-manage"));
            staffRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-keepInvAll"));

            staffRank.addPermission(Addon.getAddon(MyLandProtect.class).configFile.getString("chestsAccessPermission"));
            staffRank.addPermission(Addon.getAddon(MyLandProtect.class).configFile.getString("landsAccessPermission"));
        }

        RankData helperRank = this.getRankData("helper");
        if (helperRank != null){
            helperRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-near"));
            helperRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-mute"));
            helperRank.addPermission(Addon.getAddon(Troller.class).configFile.getString("permission-invsee"));
            helperRank.addPermission(Addon.getAddon(MoreVanilla.class).configFile.getString("permission-fly"));
        }
    }

    public RankData getRankData(String rankName){
        return this.rankDataMap.get(rankName.toLowerCase());
    }

    public Map<String, RankData> getRankDataMap() {
        return this.rankDataMap;
    }
}

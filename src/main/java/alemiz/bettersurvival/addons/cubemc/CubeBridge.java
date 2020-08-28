package alemiz.bettersurvival.addons.cubemc;

import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cubemc.commons.ranks.Rank;
import cubemc.nukkit.connector.CubeConnector;
import cubemc.nukkit.connector.events.CubePlayerJoinEvent;
import cubemc.nukkit.connector.events.RanksLoadEvent;

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
            subscriber.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            subscriber.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));
        }

        RankData cubePlus = this.getRankData("cube+");
        if (cubePlus != null){
            cubePlus.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-feed"));
            cubePlus.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            cubePlus.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-keepInvAll"));
            cubePlus.addPermission(Addon.getAddon("myhome").configFile.getString("permission-vip"));
        }

        RankData vip = this.getRankData("vip");
        if (vip != null){
            vip.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-heal"));
            vip.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-fly"));
            vip.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-near"));
            vip.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-jump"));

            vip.addPermission(Addon.getAddon("mylandprotect").configFile.getString("landsVipPermission"));
            vip.addPermission(Addon.getAddon("survivalshop").configFile.getString("shopVipPermission"));
        }

        RankData staffRank = this.getRankData("staff");
        if (staffRank != null){
            staffRank.addPermission(Addon.getAddon("troller").configFile.getString("permission-vanish"));
            staffRank.addPermission(Addon.getAddon("troller").configFile.getString("permission-block"));
            staffRank.addPermission(Addon.getAddon("troller").configFile.getString("permission-troll"));
            staffRank.addPermission(Addon.getAddon("troller").configFile.getString("permission-troll-advanced"));
            staffRank.addPermission(Addon.getAddon("troller").configFile.getString("permission-invsee"));

            staffRank.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-mute"));
            staffRank.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-manage"));
            staffRank.addPermission(Addon.getAddon("morevanilla").configFile.getString("permission-keepInvAll"));

            staffRank.addPermission(Addon.getAddon("mylandprotect").configFile.getString("chestsAccessPermission"));
        }
    }

    public RankData getRankData(String rankName){
        return this.rankDataMap.get(rankName.toLowerCase());
    }

    public Map<String, RankData> getRankDataMap() {
        return this.rankDataMap;
    }
}

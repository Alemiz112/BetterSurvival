package alemiz.bettersurvival.addons.cubemc;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cubemc.commons.ranks.Rank;

import java.util.ArrayList;
import java.util.List;

public class RankData {

    private final Rank rank;
    private final List<String> permissions = new ArrayList<>();

    public RankData(Rank rank){
        this.rank = rank;
    }

    public void assignPermissions(Player player, BetterSurvival loader){
        if (player == null) return;

        for (String permission : this.permissions){
            player.addAttachment(loader, permission, true);
        }
    }

    public String getName() {
        return this.rank.getName();
    }

    public void addPermissions(List<String> permissions){
        this.permissions.addAll(permissions);
    }

    public void addPermission(String permission){
        this.permissions.add(permission);
    }

    public boolean removePermission(String permission){
        return this.permissions.remove(permission);
    }

    public List<String> getPermissions() {
        return this.permissions;
    }
}

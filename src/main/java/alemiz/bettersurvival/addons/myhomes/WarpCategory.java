package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class WarpCategory {

    private final String name;

    private final Map<String, PlayerWarp> warps = new HashMap<>();

    public WarpCategory(String name){
        this.name = name;
    }

    public PlayerWarp getWarp(String name){
        return this.warps.get(name.toLowerCase().replace(" ", "_"));
    }

    public boolean addWarp(PlayerWarp warp){
        return this.warps.putIfAbsent(warp.getRawName(), warp) == null;
    }

    public void removeWarp(PlayerWarp warp){
        this.warps.remove(warp.getRawName());
    }

    public String getName() {
        return this.name;
    }

    public String getFormattedName(){
        return TextUtils.headerFormat(this.name);
    }

    public Map<String, PlayerWarp> getWarps() {
        return this.warps;
    }
}

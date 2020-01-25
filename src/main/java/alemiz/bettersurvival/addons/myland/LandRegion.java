package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;

import java.util.List;

public class LandRegion {

    public String owner = "";
    public String land = "";

    public List<String> whitelist;

    public Vector3f pos1;
    public Vector3f pos2;

    public LandRegion(String owner, String name){
        this.owner = owner;
        this.land = name;
    }

    public void addWhitelist(String player){
        this.whitelist.add(player.toLowerCase());
        this.save();
    }

    public void whitelistRemove(String player){
        this.whitelist.remove(player.toLowerCase());
        this.save();
    }

    public void save(){
        Config config = ConfigManager.getInstance().loadPlayer(owner);
        if (config == null) return;

        config.set("land."+land.toLowerCase()+".pos1", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land."+land.toLowerCase()+".pos2", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});

        config.set("land."+land.toLowerCase()+".whitelist", whitelist);
        config.save();
    }
}

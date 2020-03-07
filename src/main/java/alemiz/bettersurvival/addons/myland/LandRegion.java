package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class LandRegion {

    public static final String WHITELIST_ADD = "add";
    public static final String WHITELIST_REMOVE = "remove";
    public static final String WHITELIST_LIST = "list";


    public String owner = "";
    public String land = "";

    public List<String> whitelist = new ArrayList<>();

    public Level level = null;
    public Vector3f pos1;
    public Vector3f pos2;

    public LandRegion(String owner, String name){
        this(owner, name, null);
    }

    public LandRegion(String owner, String name, Level level){
        this.owner = owner;
        this.land = name;

        if (level == null){
            level = Server.getInstance().getDefaultLevel();
        }
        this.level = level;
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

        config.set("land."+land.toLowerCase()+".level", level.getFolderName().toLowerCase());
        config.set("land."+land.toLowerCase()+".pos0", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land."+land.toLowerCase()+".pos1", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});

        config.set("land."+land.toLowerCase()+".whitelist", whitelist);
        config.save();
    }
}

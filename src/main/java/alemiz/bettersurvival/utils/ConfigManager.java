package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    protected static ConfigManager instance;
    public BetterSurvival plugin;

    public String PATH = null;
    public String PLAYER_PATH;
    public String ADDONS_PATH;

    public Config cfg;

    public ConfigManager(BetterSurvival loader){
        instance = this;
        plugin = loader;

        PATH = loader.getDataFolder().getPath();
        PLAYER_PATH = PATH + "/players";
        ADDONS_PATH = PATH + "/addons";

        loader.saveDefaultConfig();
        cfg = loader.getConfig();
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public Config loadPlayer(Player player){
        return new Config(PLAYER_PATH+"/"+player.getName().toLowerCase()+".yml", Config.YAML);
    }

    public Config loadPlayer(String player){
        return new Config(PLAYER_PATH+"/"+player.toLowerCase()+".yml", Config.YAML);
    }

    public Config loadAddon(Addon addon){
        return new Config(ADDONS_PATH+"/"+addon.PATH,Config.YAML);
    }

    public List<SuperConfig> loadAllPlayers(){
        File folder = new File(PLAYER_PATH);
        File[] listFiles = folder.listFiles();

        List<SuperConfig> configs = new ArrayList<>();
        if (listFiles != null){
            for (File file: listFiles){
                configs.add(new SuperConfig(file, Config.YAML));
            }
        }
        return configs;
    }
}

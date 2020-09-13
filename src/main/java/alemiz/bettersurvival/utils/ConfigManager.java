package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    protected static ConfigManager instance;
    public BetterSurvival plugin;

    public static String PATH = null;
    public static String PLAYER_PATH;
    public static String ADDONS_PATH;

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
        return this.loadAllFromFolder(PLAYER_PATH);
    }

    public List<SuperConfig> loadAllFromFolder(String path){
        File folder = new File(path);
        File[] listFiles = folder.listFiles();

        List<SuperConfig> configs = new ArrayList<>();
        if (listFiles != null){
            for (File file: listFiles){
                try {
                    configs.add(new SuperConfig(file, Config.YAML));
                }catch (Exception e){
                    BetterSurvival.getInstance().getLogger().error("Unable to load '"+file.getName()+"' file!", e);
                }
            }
        }
        return configs;
    }

    public JsonElement loadJson(String path){
        try {
            return new JsonParser().parse(new FileReader(path));
        }catch (Exception e){
        }
        return null;
    }
}

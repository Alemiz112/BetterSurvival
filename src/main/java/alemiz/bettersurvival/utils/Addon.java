package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class Addon implements Listener{
    public String PATH = null;
    public Config configFile = null;
    protected boolean enabled = false;
    protected Map<String, Command> commands = new HashMap<>();

    protected static Map<String, Addon> addons = new HashMap<>();

    public static Map<String, Addon> getAddons() {
        return addons;
    }

    public static void loadAddon(Addon addon) {
        Addon.addons.put(addon.name, addon);
    }

    public static Addon getAddon(String name) {
        return Addon.addons.getOrDefault(name, null);
    }



    public String name;
    public BetterSurvival plugin;

    public Addon(String name, String path){
        this.PATH = path;
        this.name = name;
        this.plugin = BetterSurvival.getInstance();
        this.configFile = ConfigManager.getInstance().loadAddon(this);
        loadConfig();

        this.enabled = configFile.getBoolean("enable", false);

        if (this.enabled && preLoad()){
            plugin.getLogger().info("§eLoading BetterSurvival addon: §3"+name);
            Server.getInstance().getPluginManager().registerEvents(this, plugin);

            loadListeners();
            registerCommands();
        }
        postLoad();
    }

    public abstract void loadConfig();

    public void registerCommands(){
        //Should be implemented
    }

    public void registerCommand(String fallbackPrefix, Command command){
        registerCommand(fallbackPrefix, command, true);
    }

    public boolean registerCommand(String fallbackPrefix, Command command, boolean map){
        boolean registered = plugin.getServer().getCommandMap().register(fallbackPrefix, command);;

        if (registered && map) this.commands.put(fallbackPrefix, command);
        return registered;
    }

    public void loadListeners(){
        //Implemented by parent
    }


    public boolean preLoad(){
        //Implemented by parent
        //true = load
        return true;
    }

    public void postLoad(){
        //Implemented by parent
    }


    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}

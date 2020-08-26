package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;

import java.lang.reflect.Constructor;
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

    public static void loadAddon(Class<?> clazz, String configName){
        try {
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Addon addon = (Addon) constructor.newInstance(configName);
            addon.setEnabled(addon.configFile.getBoolean("enable", false));
            Addon.addons.put(addon.name, addon);
        }catch (Exception e){
            BetterSurvival.getInstance().getLogger().error("Unable to enable addon: §3"+clazz.getSimpleName(), e);
        }
    }

    public static Addon getAddon(String name){
        return Addon.addons.get(name);
    }

    public static void disableAddons(){
        for (Addon addon : Addon.addons.values()){
            addon.setEnabled(false);
        }
    }


    public String name;
    public BetterSurvival plugin;

    public Addon(String name, String path){
        this.PATH = path;
        this.name = name;
        this.plugin = BetterSurvival.getInstance();
        this.configFile = ConfigManager.getInstance().loadAddon(this);
        this.loadConfig();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (this.enabled && this.preLoad()){
            this.plugin.getLogger().info("§eLoading BetterSurvival addon: §3"+name);
            Server.getInstance().getPluginManager().registerEvents(this, this.plugin);

            this.postLoad();
            this.loadListeners();
            this.registerCommands();
        }else {
            this.plugin.getLogger().info("§eUnloading BetterSurvival addon: §3"+name);
            this.onUnload();
        }
    }

    public abstract void loadConfig();

    public void registerCommands(){
        //Should be implemented
    }

    public void registerCommand(String fallbackPrefix, Command command){
        this.registerCommand(fallbackPrefix, command, true);
    }

    public boolean registerCommand(String fallbackPrefix, Command command, boolean map){
        boolean registered = this.plugin.getServer().getCommandMap().register(fallbackPrefix, command);;

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

    public void onUnload(){
        //Implemented by parent
    }


    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}

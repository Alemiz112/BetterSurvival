package alemiz.bettersurvival;

import alemiz.bettersurvival.commands.DelCommand;
import alemiz.bettersurvival.commands.GetHomeCommand;
import alemiz.bettersurvival.commands.HomeCommand;
import alemiz.bettersurvival.commands.SetHomeCommand;
import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;

public class BetterSurvival extends PluginBase {

    public static String PATH = "/players";
    public Config cfg;

    protected static BetterSurvival instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        cfg = getConfig();

        registerCommands();

        getLogger().info("§aEnabling BetterSurvival by §6Alemiz!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§aDisabling BetterSurvival by §6Alemiz!");
    }

    public static BetterSurvival getInstance() {
        return instance;
    }

    public Config loadPlayer(Player player){
        return new Config(getDataFolder().getPath()+PATH+"/"+player.getName().toLowerCase()+".yml", Config.YAML);
    }

    public void registerCommands(){
        if (cfg.getBoolean("homes.enable", true)){
            getServer().getCommandMap().register("home", new HomeCommand("home"));
            getServer().getCommandMap().register("sethome", new SetHomeCommand("sethome"));
            getServer().getCommandMap().register("gethome", new GetHomeCommand("gethome"));
            getServer().getCommandMap().register("delhome", new DelCommand("delhome"));
        }
    }
}

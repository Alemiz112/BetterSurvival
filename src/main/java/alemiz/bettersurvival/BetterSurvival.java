package alemiz.bettersurvival;

import alemiz.bettersurvival.addons.Home;
import alemiz.bettersurvival.addons.BetterLobby;
import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.addons.Troller;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;

public class BetterSurvival extends PluginBase implements Listener {

    protected static BetterSurvival instance;
    protected ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);

        loadAddons();

        getLogger().info("§aEnabling BetterSurvival by §6Alemiz!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§aDisabling BetterSurvival by §6Alemiz!");
    }

    public static BetterSurvival getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void loadAddons(){
        Addon.loadAddon(new Home("homes.yml"));
        Addon.loadAddon(new MoreVanilla("more_vanilla.yml"));
        Addon.loadAddon(new MyLandProtect("my_land_protect.yml"));
        Addon.loadAddon(new BetterLobby("better_lobby.yml"));
        Addon.loadAddon(new Troller("troll_addon.yml"));
    }
}

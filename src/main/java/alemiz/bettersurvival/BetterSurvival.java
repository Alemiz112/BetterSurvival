package alemiz.bettersurvival;

import alemiz.bettersurvival.addons.*;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.tasks.ServerRestartTask;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.enitity.FakeHuman;
import alemiz.bettersurvival.utils.fakeChest.FakeInventory;
import alemiz.bettersurvival.utils.fakeChest.FakeInventoryManager;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BetterSurvival extends PluginBase implements Listener {

    private static BetterSurvival instance;
    private ConfigManager configManager;

    private boolean autoRestart;
    /**
     * Time in minutes until restart
     */
    private int restartTime;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);

        this.autoRestart = this.configManager.cfg.getBoolean("auto-restart");
        this.restartTime = this.configManager.cfg.getInt("restartInterval", 120);
        this.getServer().getScheduler().scheduleRepeatingTask(this, new ServerRestartTask(this), 20*60*10);

        this.loadAddons();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("§aEnabled BetterSurvival by §6Alemiz!");
    }

    @Override
    public void onLoad() {
        Entity.registerEntity("FakeHuman", FakeHuman.class);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("§aDisabling BetterSurvival by §6Alemiz!");
        Addon.disableAddons();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        FakeInventoryManager.removeInventory(event.getPlayer());
    }

    @EventHandler
    public void onInventoryTranslation(InventoryTransactionEvent event){
        Player player = event.getTransaction().getSource();
        Map<FakeInventory, List<SlotChangeAction>> actions = new HashMap<>();

        for (InventoryAction action : event.getTransaction().getActions()){
            if (!(action instanceof SlotChangeAction)) continue;

            SlotChangeAction slotChange = (SlotChangeAction) action;
            if (!(slotChange.getInventory() instanceof FakeInventory)) continue;

            FakeInventory inventory = (FakeInventory) slotChange.getInventory();
            List<SlotChangeAction> slotChanges = actions.computeIfAbsent(inventory, fakeInventory -> new ArrayList<>());
            slotChanges.add(slotChange);
        }

        AtomicBoolean cancel = new AtomicBoolean(false);
        actions.forEach((FakeInventory inv, List<SlotChangeAction> aactions)->{
            if (inv.getInventoryFlag(FakeInventory.Flags.IS_LOCKED)){
                cancel.set(true);
                return;
            }

            for (SlotChangeAction action : aactions){
                if (!inv.slotChange(player, action)) cancel.set(true);
            }
        });

        if (cancel.get()) event.setCancelled(true);
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        if (!(event.getWindow() instanceof Form)) return;
        Form form = (Form) event.getWindow();
        form.handle(event.getPlayer());
    }

    public void loadAddons(){
        //Load permissions as first
        Addon.loadAddon(PlayerPermissions.class, "player_permissions.yml");

        Addon.loadAddon(MyHomes.class, "my_homes.yml");
        Addon.loadAddon(MoreVanilla.class, "more_vanilla.yml");
        Addon.loadAddon(MyLandProtect.class, "my_land_protect.yml");
        Addon.loadAddon(Troller.class, "troll_addon.yml");
        Addon.loadAddon(BetterVoting.class, "better_voting.yml");
        Addon.loadAddon(LevelVote.class, "level_vote.yml");
        Addon.loadAddon(SurvivalShop.class, "survival_shop.yml");
        Addon.loadAddon(BetterEconomy.class, "better_economy.yml");
        Addon.loadAddon(PlayerClans.class, "player_clans.yml");

        //CubeMC addons
        Addon.loadAddon(EasterAddon.class, "easter_addon.yml");

        //This must be last addon loaded
        Addon.loadAddon(CubeBridge.class, "cube_bridge.yml");
        Addon.loadAddon(BetterLobby.class, "better_lobby.yml");
    }

    public static BetterSurvival getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isAutoRestartEnabled() {
        return this.autoRestart;
    }

    public void setRestartTime(int restartTime) {
        this.restartTime = restartTime;
    }

    public int getRestartTime() {
        return this.restartTime;
    }
}

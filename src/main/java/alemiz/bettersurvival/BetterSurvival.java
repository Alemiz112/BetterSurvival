package alemiz.bettersurvival;

import alemiz.bettersurvival.addons.*;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.fakeChest.FakeInventory;
import alemiz.bettersurvival.utils.fakeChest.FakeInventoryManager;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BetterSurvival extends PluginBase implements Listener {

    protected static BetterSurvival instance;
    protected ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);

        this.loadAddons();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("§aEnabling BetterSurvival by §6Alemiz!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§aDisabling BetterSurvival by §6Alemiz!");
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

        event.setCancelled(cancel.get());
    }

    public static BetterSurvival getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void loadAddons(){
        //Load permissions as first
        Addon.loadAddon(new PlayerPermissions("player_permissions.yml"));

        Addon.loadAddon(new Home("homes.yml"));
        Addon.loadAddon(new MoreVanilla("more_vanilla.yml"));
        Addon.loadAddon(new MyLandProtect("my_land_protect.yml"));
        Addon.loadAddon(new Troller("troll_addon.yml"));
        Addon.loadAddon(new BetterVoting("better_voting.yml"));
        Addon.loadAddon(new LevelVote("level_vote.yml"));
        Addon.loadAddon(new SurvivalShop("survival_shop.yml"));

        //CubeMC addons
        Addon.loadAddon(new EasterAddon("easter_addon.yml"));

        //This must be last addon loaded
        Addon.loadAddon(new CubeBridge("cube_bridge.yml"));
        Addon.loadAddon(new BetterLobby("better_lobby.yml"));
    }
}

/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival;

import alemiz.bettersurvival.addons.*;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.cubemc.CubeBridge;
import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.addons.quests.SurvivalQuests;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.tasks.ServerRestartTask;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.enitity.FakeHuman;
import alemiz.bettersurvival.utils.fakeChest.FakeInventory;
import alemiz.bettersurvival.utils.fakeChest.FakeInventoryManager;
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
import cubemc.commons.nukkit.modules.npc.NpcModule;

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
        this.getServer().getScheduler().scheduleRepeatingTask(this, new ServerRestartTask(this), 1200);

        this.loadAddons();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("§aEnabled BetterSurvival by §6Alemiz!");
    }

    @Override
    public void onLoad() {
        NpcModule.registerEntity("FakeHuman", FakeHuman.class);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("§aDisabling BetterSurvival by §6Alemiz!");
        Addon.disableAddons();
    }

    @EventHandler
    public void onInventoryTranslation(InventoryTransactionEvent event){
        Player player = event.getTransaction().getSource();
        for (InventoryAction action : event.getTransaction().getActions()){
            if (!(action instanceof SlotChangeAction)) {
                continue;
            }

            SlotChangeAction slotChange = (SlotChangeAction) action;
            if (!(slotChange.getInventory() instanceof FakeInventory)) {
                continue;
            }

            FakeInventory inventory = (FakeInventory) slotChange.getInventory();
            if (inventory.getInventoryFlag(FakeInventory.Flags.IS_LOCKED) || !inventory.slotChange(player, slotChange)) {
                event.setCancelled(true);
                return;
            }
        }
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
        Addon.loadAddon(SurvivalQuests.class, "survival_quests.yml");

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

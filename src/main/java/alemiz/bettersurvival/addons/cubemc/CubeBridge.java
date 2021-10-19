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

package alemiz.bettersurvival.addons.cubemc;

import alemiz.bettersurvival.addons.BetterLobby;
import alemiz.bettersurvival.addons.Troller;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.plugin.PluginEnableEvent;
import cubemc.commons.data.CoinsEnum;
import cubemc.commons.nukkit.events.CubePlayerJoinEvent;
import cubemc.commons.nukkit.utils.scoreboard.AdvancedScoreboard;
import cubemc.commons.nukkit.utils.scoreboard.ScoreLineEntry;
import cubemc.commons.structure.Rank;
import cubemc.nukkit.connector.CubeConnector;
import cubemc.nukkit.connector.player.PlayerManager;

import java.util.*;

public class CubeBridge extends Addon {

    public static final CoinsEnum DEFAULT_COINS = CoinsEnum.SURVIVAL_COINS;

    private final Map<String, RankData> rankDataMap = new HashMap<>();
    private AdvancedScoreboard scoreboard;
    private boolean initialized;

    public CubeBridge(String path){
        super("cubebridge", path);
    }

    @Override
    public void postLoad() {
        this.scoreboard = new AdvancedScoreboard("survival", "survival");

        ScoreLineEntry playerCountLine = new ScoreLineEntry("\uE180§e {count}");
        playerCountLine.addTranslation((text, player) ->
                text.replace("{count}", String.valueOf(this.plugin.getServer().getOnlinePlayersCount())));
        this.scoreboard.addLine(playerCountLine, false);

        ScoreLineEntry mainEntry = new ScoreLineEntry("§bCube§6MC §aSurvival{vanish}");
        mainEntry.addTranslation(this::translateMainScore);
        this.scoreboard.addLine(mainEntry, false);
        this.scoreboard.updateBoard();

        CubeConnector connector = CubeConnector.getInstance();
        if (connector != null) {
            this.onCubeInit(connector);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.save();
        }
    }

    @EventHandler
    public void onNetworkLoad(PluginEnableEvent event) {
        if (event.getPlugin() instanceof CubeConnector) {
            this.onCubeInit((CubeConnector) event.getPlugin());
        }
    }

    private void onCubeInit(CubeConnector connector) {
        if (this.initialized) {
            return;
        }
        this.initialized = true;

        BetterLobby betterLobby = Addon.getAddon(BetterLobby.class);
        if (betterLobby != null && betterLobby.isEnabled()) {
            connector.getPlayerManager().setJoinMessage(betterLobby.getJoinMessage());
        }
        this.loadRanks(connector.getRankManager().getCachedRanks().values());
    }

    @EventHandler
    public void onNetworkJoin(CubePlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.scoreboard.addPlayer(player);
        this.scoreboard.updateBoard();

        for (String rankName : event.getLoginData().getRanks()) {
            RankData rankData = this.getRankData(rankName);
            if (rankData != null) {
                rankData.assignPermissions(player, this.plugin);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.scoreboard.removePlayer(player);
        this.plugin.getServer().getScheduler().scheduleDelayedTask(() ->
                this.scoreboard.updateBoard(), 10);
    }

    public void loadRanks(Collection<Rank> ranks) {
        for (Rank rank : ranks) {
            RankData rankData = new RankData(rank);
            List<String> permissions = configFile.getStringList("rank." + rank.getName().toLowerCase());
            if (!permissions.isEmpty()) {
                rankData.addPermissions(permissions);
            }
            this.rankDataMap.put(rank.getName().toLowerCase(), rankData);
        } }

    private String translateMainScore(String text, Player player) {
        Troller troller = Addon.getAddon(Troller.class);
        if (troller == null || !troller.isEnabled()) {
            return text.replace("{vanish}", "");
        }

        boolean vanished = troller.getVanishPlayers().contains(player.getName());
        return text.replace("{vanish}", vanished? "§7 - §cVanished": "");
    }

    public RankData getRankData(String rankName) {
        return this.rankDataMap.get(rankName.toLowerCase());
    }

    public AdvancedScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public static PlayerManager playerManager() {
        return CubeConnector.getInstance().getPlayerManager();
    }
}

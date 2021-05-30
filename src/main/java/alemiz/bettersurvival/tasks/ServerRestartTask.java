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

package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cubemc.nukkit.connector.CubeConnector;

import java.util.Collection;

public class ServerRestartTask implements Runnable{

    private final BetterSurvival plugin;

    public ServerRestartTask(BetterSurvival plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int restartIn = this.plugin.getRestartTime();
        if (restartIn <= 0) {
            this.onRestart();
            return;
        }

        this.plugin.setRestartTime(restartIn - 1);
        if (restartIn <= 10) {
            this.plugin.getServer().broadcastMessage("§c»§7Server restarts in "+restartIn+" minutes!");
        }
    }

    private void onRestart() {
        CubeConnector connector = CubeConnector.getInstance();
        Collection<Player> players = this.plugin.getServer().getOnlinePlayers().values();
        for (Player player : players) {
            connector.getPlayerManager().sendToLobby(player, true);
        }
        this.plugin.getServer().getScheduler().scheduleDelayedTask(() -> this.plugin.getServer().shutdown(), 20);
    }
}

/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cubemc.nukkit.connector.network.LobbyJoinPacket;

public class ServerRestartTask implements Runnable{

    private final BetterSurvival plugin;

    public ServerRestartTask(BetterSurvival plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int restartIn = this.plugin.getRestartTime();

        if (restartIn > 0){
            if (restartIn <= 20) this.plugin.getServer().broadcastMessage("§c»§7Server restarts in "+restartIn+" minutes!");
            this.plugin.setRestartTime(restartIn-10);
            return;
        }

        for (Player player : this.plugin.getServer().getOnlinePlayers().values()){
            LobbyJoinPacket packet = new LobbyJoinPacket();
            packet.player = player.getName();
            packet.putPacket();
        }
        this.plugin.getServer().shutdown();
    }
}

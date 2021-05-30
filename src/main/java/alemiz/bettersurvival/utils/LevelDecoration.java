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

package alemiz.bettersurvival.utils;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelEventPacket;

import java.util.Map;

public class LevelDecoration extends Vector3{

    protected final int id;
    protected float pitch;

    public LevelDecoration(Vector3 pos, int id) {
        this(pos, id, 0);
    }

    public LevelDecoration(Vector3 pos, int id, float pitch) {
        super(pos.x, pos.y, pos.z);
        this.id = id;
        this.pitch = pitch * 1000f;
    }

    public void sendDecoration(Player player){
        sendDecoration(this, new Player[]{player});
    }

    public void sendDecoration(Map<Long, Player> players){
        for (Player player : players.values()){
            sendDecoration(player);
        }
    }


    public static void sendDecoration(LevelDecoration decoration, Player player){
        sendDecoration(decoration, new Player[]{player});
    }

    public static void sendDecoration(LevelDecoration decoration, Map<Long, Player> players){
        for (Player player : players.values()){
            sendDecoration(decoration, player);
        }
    }



    public static void sendDecoration(LevelDecoration[] decorations, Player player){
        for (LevelDecoration decoration: decorations){
            sendDecoration(decoration, new Player[]{player});
        }
    }

    public static void sendDecoration(LevelDecoration[] decorations, Map<Long, Player> players){
        for (LevelDecoration decoration: decorations){
            for (Player player : players.values()){
                sendDecoration(decoration, player);
            }
        }
    }

    public void sendDecoration(LevelDecoration[] decorations, Player[] players){
        for (LevelDecoration decoration: decorations){
            sendDecoration(decoration, players);
        }
    }


    public static void sendDecoration(LevelDecoration decoration, Player[] players){
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = decoration.id;
        pk.x = (float) decoration.x;
        pk.y = (float) decoration.y;
        pk.z = (float) decoration.z;
        pk.data = (int) decoration.pitch;

        for (Player player: players){
            player.dataPacket(pk);
        }
    }
}

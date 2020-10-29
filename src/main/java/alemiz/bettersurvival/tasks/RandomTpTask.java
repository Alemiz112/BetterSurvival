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
import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.scheduler.Task;

import java.util.concurrent.ThreadLocalRandom;

public class RandomTpTask extends Task {

    private final Player player;
    private final String message;

    private int x;
    private int y;
    private int z;

    private int countdown = 5;
    private boolean found = false;

    public RandomTpTask(Player player, String message){
        this.player = player;
        this.message = message;

        this.getRandomPos();
    }

    @Override
    public void onRun(int i) {
        if (player == null) return;

        if (!this.found){
            Level level = player.getLevel();
            BaseFullChunk chunk = level.getChunk(this.x >> 4, this.z >> 4);
            boolean nether = level.getDimension() == Level.DIMENSION_NETHER;

            for (this.y = 0; this.y <= (nether? 124 : 250); this.y++){
                if (nether && this.y > 122){
                    this.clearSpawn(new Position(this.x, this.y, this.z, player.getLevel()));
                    this.found = true;
                    break;
                }

                int xx = this.x & 15;
                int zz = this.z & 15;

                if (MoreVanilla.UNSAFE_BLOCKS.get(chunk.getBlockId(xx, this.y & 255, zz)) ||
                        chunk.getBlockId(xx, (this.y & 255)+1, zz) != BlockID.AIR ||
                        chunk.getBlockId(xx, (this.y & 255)+2, zz) != BlockID.AIR) continue;

                this.found = true;
                break;
            }
        }

        if (!this.found){
            this.getRandomPos();
            BetterSurvival.getInstance().getServer().getScheduler().scheduleDelayedTask(this, 40);
            return;
        }

        if (this.countdown > 0){
            player.sendMessage("§6»§7Teleporting in §6"+this.countdown+"§7!");

            this.countdown--;
            BetterSurvival.getInstance().getServer().getScheduler().scheduleDelayedTask(this, 20);
            return;
        }


        player.teleport(new Location(this.x, y+1, this.z, player.getLevel()));
        player.sendMessage(this.message);
    }

    private void getRandomPos(){
        Level level = player.getLevel();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        boolean invertX = rand.nextBoolean();
        boolean invertZ = rand.nextBoolean();

        this.x = rand.nextInt(invertX? -50000 : +20000, invertX? -20000 : +50000);
        this.z = rand.nextInt(invertZ? -50000 : +20000, invertZ? -20000 : +50000);
        level.generateChunk(x >> 4, z >> 4, true);
    }

    private void clearSpawn(Position pos){
        Level level = pos.getLevel();
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        int z = (int) pos.getZ();

        for (int i = 0; i < 2; i++){
            level.setBlockIdAt(x, y+i, z, Block.AIR);

            level.setBlockIdAt(x+1, y+i, z, Block.AIR);
            level.setBlockIdAt(x-1, y+i, z, Block.AIR);

            level.setBlockIdAt(x, y+i, z+1, Block.AIR);
            level.setBlockIdAt(x, y+i, z-1, Block.AIR);

            level.setBlockIdAt(x+1, y+i, z+1, Block.AIR);
            level.setBlockIdAt(x+1, y+i, z-1, Block.AIR);
            level.setBlockIdAt(x-1, y, z+1, Block.AIR);
            level.setBlockIdAt(x-1, y+i, z-1, Block.AIR);
        }
    }
}

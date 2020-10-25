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

import cn.nukkit.Player;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.math.Vector3;;
import cn.nukkit.scheduler.Task;

public class QuestParticleTask extends Task {

    private final Player player;
    private final int finishTick;

    public QuestParticleTask(Player player, int tickTime){
        this.player = player;
        this.finishTick = player.getServer().getTick()+tickTime;
        player.getServer().getScheduler().scheduleRepeatingTask(this, 10);
    }

    @Override
    public void onRun(int currentTick) {
        if (this.player == null || currentTick > this.finishTick){
            this.cancel();
            return;
        }

        Vector3 direction = this.player.getDirection().getOpposite().getUnitVector().multiply(0.8).add(0, 1);
        this.player.getLevel().addParticle(new HeartParticle(this.player.add(direction)));
    }
}

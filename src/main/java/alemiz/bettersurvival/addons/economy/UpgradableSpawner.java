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

package alemiz.bettersurvival.addons.economy;

import cn.nukkit.blockentity.BlockEntitySpawner;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ShortTag;

public class UpgradableSpawner extends BlockEntitySpawner {

    public UpgradableSpawner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if (!this.namedTag.contains("spawner_level") || !(this.namedTag.get("spawner_level") instanceof ShortTag)){
            this.setSpawnerLevel(EconomySpawners.DEFAULT_LEVEL);
        }

        super.initBlockEntity();
    }

    public void setSpawnerLevel(SpawnerLevel level){
        this.namedTag.putShort("spawner_level", level.getLevel());
        this.namedTag.putShort(TAG_MIN_SPAWN_DELAY, level.getMinSpawnDelay());
        this.namedTag.putShort(TAG_MAX_SPAWN_DELAY, level.getMaxSpawnDelayTicked());
        this.setMinSpawnDelay(level.getMinSpawnDelay());
        this.setMinSpawnDelay(level.getMaxSpawnDelayTicked());
    }

    public int getSpawnerLevel(){
        return this.namedTag.getInt("spawner_level");
    }
}

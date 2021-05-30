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

public class SpawnerLevel {

    private final int level;
    private final int minSpawnDelay;
    private final int maxSpawnDelay;
    private final int price;

    public SpawnerLevel(int level, int minSpawnDelay, int maxSpawnDelay, int price){
        this.level = level;
        this.minSpawnDelay = minSpawnDelay;
        this.maxSpawnDelay = maxSpawnDelay;
        this.price = price;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMinSpawnDelay() {
        return this.minSpawnDelay;
    }

    public int getMaxSpawnDelay() {
        return this.maxSpawnDelay;
    }

    public int getMinSpawnDelayTicked(){
        return this.minSpawnDelay*20;
    }

    public int getMaxSpawnDelayTicked(){
        return this.maxSpawnDelay*20;
    }

    public int getAverageDelay(){
        return (this.minSpawnDelay + this.maxSpawnDelay) / 2;
    }

    public int getPrice() {
        return this.price;
    }
}

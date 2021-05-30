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

package alemiz.bettersurvival.addons.clans;

public class ClanLevelInfo {

    private final int level;
    private final int requiredPoints;
    private int moneyLimit;
    private int playerLimit;
    private int homeLimit;
    private int maxLandSize;

    public ClanLevelInfo(int level, int requiredPoints) {
        this.level = level;
        this.requiredPoints = requiredPoints;
    }


    public int getLevel() {
        return this.level;
    }

    public int getRequiredPoints() {
        return this.requiredPoints;
    }

    public int getMoneyLimit() {
        return this.moneyLimit;
    }

    public void setMoneyLimit(int moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public int getPlayerLimit() {
        return this.playerLimit;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public int getHomeLimit() {
        return this.homeLimit;
    }

    public void setHomeLimit(int homeLimit) {
        this.homeLimit = homeLimit;
    }

    public int getMaxLandSize() {
        return this.maxLandSize;
    }

    public void setMaxLandSize(int maxLandSize) {
        this.maxLandSize = maxLandSize;
    }
}

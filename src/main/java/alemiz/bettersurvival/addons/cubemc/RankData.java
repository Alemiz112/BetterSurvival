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

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cubemc.commons.ranks.Rank;

import java.util.ArrayList;
import java.util.List;

public class RankData {

    private final Rank rank;
    private final List<String> permissions = new ArrayList<>();

    public RankData(Rank rank){
        this.rank = rank;
    }

    public void assignPermissions(Player player, BetterSurvival loader){
        if (player == null) return;

        for (String permission : this.permissions){
            player.addAttachment(loader, permission, true);
        }
    }

    public String getName() {
        return this.rank.getName();
    }

    public void addPermissions(List<String> permissions){
        this.permissions.addAll(permissions);
    }

    public void addPermission(String permission){
        this.permissions.add(permission);
    }

    public boolean removePermission(String permission){
        return this.permissions.remove(permission);
    }

    public List<String> getPermissions() {
        return this.permissions;
    }
}

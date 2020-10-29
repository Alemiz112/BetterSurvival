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

package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class WarpCategory {

    private final String name;

    private final Map<String, PlayerWarp> warps = new HashMap<>();

    public WarpCategory(String name){
        this.name = name;
    }

    public PlayerWarp getWarp(String name){
        return this.warps.get(name.toLowerCase().replace(" ", "_"));
    }

    public boolean addWarp(PlayerWarp warp){
        return this.warps.putIfAbsent(warp.getRawName(), warp) == null;
    }

    public void removeWarp(PlayerWarp warp){
        this.warps.remove(warp.getRawName());
    }

    public String getName() {
        return this.name;
    }

    public String getFormattedName(){
        return TextUtils.headerFormat(this.name);
    }

    public Map<String, PlayerWarp> getWarps() {
        return this.warps;
    }
}

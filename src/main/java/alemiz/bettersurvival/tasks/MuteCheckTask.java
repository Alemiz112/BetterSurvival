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

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.scheduler.Task;

import java.util.Date;
import java.util.Map;

public class MuteCheckTask extends Task {

    private MoreVanilla loader;

    public MuteCheckTask(MoreVanilla loader){
        this.loader = loader;
    }

    @Override
    public void onRun(int i) {
        Map<String, Date> muted = this.loader.getMutedPlayers();
        Date now = new Date();

        for (String playerName : muted.keySet()) {
            Date muteTill = muted.get(playerName);

            if (!now.after(muteTill) || !muteTill.before(now)) return;
            this.loader.unmute(playerName, "console");
        }
    }
}

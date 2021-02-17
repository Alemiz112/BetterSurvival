/*
 * Copyright 2021 Alemiz
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

package alemiz.bettersurvival.addons.clans.forms;

import alemiz.bettersurvival.addons.clans.ClanLevelInfo;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.utils.form.CustomForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementLabel;
import cubemc.commons.nukkit.utils.forms.Form;

import java.util.ArrayList;
import java.util.List;

public class ClanLevelsForm extends CustomForm {

    private final transient PlayerClans loader;

    public ClanLevelsForm(Player player, PlayerClans loader) {
        super("§l§8Clan Levels");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addElement(new ElementLabel("§fCollect clan points and boost your clan!\n§7With every level you can improve the clan even more!"));

        List<ClanLevelInfo> levels = new ArrayList<>(this.loader.getClanLevels());
        for (ClanLevelInfo levelInfo : levels) {
            String text = "§3Level "+levelInfo.getLevel()+":";
            if (levelInfo.getPlayerLimit() > 0) {
                text += "\n§7- "+levelInfo.getPlayerLimit()+" players";
            }
            if (levelInfo.getMoneyLimit() > 0) {
                text += "\n§7- "+levelInfo.getMoneyLimit()+" max coins";
            }
            if (levelInfo.getHomeLimit() > 0) {
                text += "\n§7- "+levelInfo.getHomeLimit()+" homes";
            }
            if (levelInfo.getMaxLandSize() > 0) {
                text += "\n§7- "+levelInfo.getMaxLandSize()+" blocks land size";
            }
            this.addElement(new ElementLabel(text));
        }
        return this;
    }
}

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

package alemiz.bettersurvival.addons.clans.forms;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class ClanTopForm extends SimpleForm {

    private final transient PlayerClans loader;

    public ClanTopForm(Player player, PlayerClans loader) {
        super("§l§8Top Clans", "");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.setContent("§fYou can find here ordered top clans:");

        List<Clan> clans = new ArrayList<>(this.loader.getClans().values());
        clans.sort((clan1, clan2) -> Integer.compare(clan2.getClanPoints(), clan1.getClanPoints()));

        int count = 1;
        for (Clan clan : clans) {
            String text = "§b" + clan.getName() + " §3#" + count++ +"\n§fLevel: §e"+ clan.getClanLevel()+" §fXP: §e"+clan.getClanPoints();
            this.addButton(new ElementButton(text));
        }
        return this;
    }
}

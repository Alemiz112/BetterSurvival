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

package alemiz.bettersurvival.addons.quests.forms;

import alemiz.bettersurvival.addons.quests.Quest;
import alemiz.bettersurvival.addons.quests.SurvivalQuests;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

public class QuestMenuForm extends SimpleForm {

    private final transient SurvivalQuests loader;

    public QuestMenuForm(Player player, SurvivalQuests loader){
        super("§l§8Quest Master", "");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        String message = this.loader.configFile.getString("questMenuMessage");
        message = message.replace("{player}", player.getDisplayName());
        this.setContent(message);

        this.addButton(new ElementButton("§aDaily Quest\n§7»Click to open"));
        this.addButton(new ElementButton("§eQuest Profile\n§7»Click to open"));
        this.addButton(new ElementButton("§fClose"));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null){
            return;
        }

        int response = this.getResponse().getClickedButtonId();
        switch (response){
            case 0:
                Quest quest = this.loader.getActualQuest();
                new QuestInfoForm(player, quest, this.loader).buildForm().sendForm();
                break;
            case 1:
                new QuestPlayerInfoForm(player, this.loader).buildForm().sendForm();
                break;
        }

    }
}

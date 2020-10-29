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

package alemiz.bettersurvival.addons.quests.forms;

import alemiz.bettersurvival.addons.quests.Quest;
import alemiz.bettersurvival.addons.quests.QuestIngredient;
import alemiz.bettersurvival.addons.quests.SurvivalQuests;
import alemiz.bettersurvival.utils.form.ModalForm;
import cn.nukkit.Player;
import cubemc.commons.nukkit.utils.forms.Form;

public class QuestInfoForm extends ModalForm {

    private final transient SurvivalQuests loader;
    private final transient Quest quest;

    private transient boolean completed = false;

    public QuestInfoForm(Player player, Quest quest, SurvivalQuests loader) {
        super("§l§8Today Quest", "", "", "Exit");
        this.player = player;
        this.quest = quest;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        StringBuilder builder = new StringBuilder("§f"+this.quest.getQuestName()+":\n§7"+this.quest.getDescription());
        this.completed = this.loader.isCompletedQuest(this.player, this.quest);

        if (this.completed){
            builder.append("\n§fGreat job! You have§a completed§f this quest!\n§7Now you can wait till I will have new quest.");
            this.setContent(builder.toString());
            return this;
        }

        builder.append("\n§fBring me this ingredients to complete quest:");
        for (QuestIngredient ingredient : this.quest.getIngredients()){
            builder.append("\n§7- ").append(ingredient.getInfoString());
        }
        builder.append("\n§fAnd what is the reward?").append("\n§7- ").append(this.quest.getRewardValue()).append(" coins");

        this.setContent(builder.toString());
        this.setButton1("§aComplete");
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.completed || this.getResponse() == null || this.getResponse().getClickedButtonId() == 1){
            return;
        }

        boolean success = true;
        StringBuilder builder = new StringBuilder("§c»§7You forgot some ingredients:");
        for (QuestIngredient ingredient : this.quest.getIngredients()){
            if (!ingredient.checkIngredient(player, this.loader)){
                builder.append("\n§7- ").append(ingredient.getInfoString());
                success = false;
            }
        }

        if (!success){
            player.sendMessage(builder.toString());
            return;
        }
        this.quest.onComplete(player, this.loader);
    }
}

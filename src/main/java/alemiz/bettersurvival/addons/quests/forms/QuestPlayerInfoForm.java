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

import alemiz.bettersurvival.addons.quests.PlayerQuestData;
import alemiz.bettersurvival.addons.quests.SurvivalQuests;
import alemiz.bettersurvival.utils.TextUtils;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.item.Item;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestPlayerInfoForm extends SimpleForm {

    private final transient SurvivalQuests loader;

    public QuestPlayerInfoForm(Player player, SurvivalQuests loader){
        super("§l§8Quest Profile", "");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        StringBuilder builder = new StringBuilder("§fYou can find here progress of quest important items. QuestMaster may ask you to get more!");
        PlayerQuestData questData = this.loader.getQuestData(player);

        builder.append("\n\n§eCrafted Items:");
        for (Int2ObjectMap.Entry<AtomicInteger> entry : questData.getCraftCountMap().int2ObjectEntrySet()){
            Item item = Item.get(entry.getIntKey());
            builder.append("\n§7- ").append(item.getName()).append(" x ").append(entry.getValue().get());
        }

        builder.append("\n\n§eKilled Entities:");
        for (Map.Entry<String, AtomicInteger> entry : questData.getKillCountMap().entrySet()){
            builder.append("\n§7- ").append(TextUtils.headerFormat(entry.getKey())).append(" x ").append(entry.getValue().get());
        }

        this.setContent(builder.toString());
        this.addButton(new ElementButton("Exit"));
        return this;
    }

    @Override
    public void handle(Player player) {
        //NOOP
    }
}

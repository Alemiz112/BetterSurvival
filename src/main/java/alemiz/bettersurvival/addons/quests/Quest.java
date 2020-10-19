/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival.addons.quests;

import alemiz.bettersurvival.tasks.QuestParticleTask;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.event.Event;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cubemc.nukkit.connector.modules.Money;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quest {

    public static Quest fromJson(String questId, JsonObject jsonObject, SurvivalQuests loader){
        if (!jsonObject.has("description") || !jsonObject.has("reward") || !jsonObject.has("ingredients")){
            loader.plugin.getLogger().warning("§cWrong configuration for quest "+jsonObject+"!");
            return null;
        }

        String questName = jsonObject.has("name")? jsonObject.get("name").getAsString() : TextUtils.headerFormat(questId.replace("_", " "));
        String description = jsonObject.get("description").getAsString();
        int reward = jsonObject.get("reward").getAsInt();

        List<QuestIngredient> ingredients = new ArrayList<>();
        for (JsonElement element : jsonObject.getAsJsonArray("ingredients")){
            QuestIngredient ingredient = loader.getIngredient(element.getAsString());
            if (ingredient != null){
                ingredients.add(ingredient);
            }
        }

        if (ingredients.size() < 1){
            loader.plugin.getLogger().warning("§cNo ingredients found for quest "+questId+"!");
            return null;
        }
        return new Quest(questId, questName, description, ingredients, reward);
    }

    private final String questId;
    private final String questName;
    private final String description;

    private final List<QuestIngredient> ingredients;
    private final int rewardValue;

    private ZonedDateTime validTime;

    public Quest(String questId, String questName, String description, List<QuestIngredient> ingredients, int rewardValue){
        this.questId = questId;
        this.questName = questName;
        this.description = description;
        this.ingredients = ingredients;
        this.rewardValue = rewardValue;
    }

    public void onComplete(Player player, SurvivalQuests loader){
        if (player == null || loader == null){
            return;
        }

        if (loader.useParticles()){
            new QuestParticleTask(player, loader.getParticleDelay());
        }

        for (QuestIngredient ingredient : this.ingredients){
            ingredient.onComplete(player, loader);
        }

        loader.setQuestCompleted(player, this);
        Event event = new QuestCompletedEvent(player, this);
        player.getServer().getPluginManager().callEvent(event);

        Money.getInstance().addMoney(player, this.rewardValue);

        String message = loader.configFile.getString("completedQuestMessage");
        message = message.replace("{quest}", this.questName);
        message = message.replace("{player}", player.getDisplayName());
        player.sendMessage(message);
    }

    public String getQuestId() {
        return this.questId;
    }

    public String getQuestName() {
        return this.questName;
    }

    public String getDescription() {
        return this.description;
    }

    public List<QuestIngredient> getIngredients() {
        return this.ingredients;
    }

    public int getRewardValue() {
        return this.rewardValue;
    }

    public void setValidTime(ZonedDateTime validTime) {
        this.validTime = validTime;
    }

    public ZonedDateTime getValidTime() {
        return this.validTime;
    }
}

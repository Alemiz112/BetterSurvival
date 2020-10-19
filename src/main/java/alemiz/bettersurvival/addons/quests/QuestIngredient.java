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

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.utils.Items;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import com.google.gson.JsonObject;

public class QuestIngredient {

    public static QuestIngredient fromJson(String ingredientId, JsonObject jsonObject){
        if (!jsonObject.has("type") || !jsonObject.has("count")){
            return null;
        }

        String stringType = jsonObject.get("type").getAsString();
        TYPE type;
        try {
            type = TYPE.valueOf(stringType.toUpperCase());
        }catch (Exception e){
            BetterSurvival.getInstance().getLogger().warning("§eUnknown QuestIngredient type "+stringType+"!");
            return null;
        }

        QuestIngredient ingredient =  new QuestIngredient(ingredientId, type, jsonObject.get("count").getAsInt());
        switch (type){
            case ITEM:
            case CRAFT:
                if (!jsonObject.has("item_id")){
                    return null;
                }
                int itemMeta = jsonObject.has("item_meta")? jsonObject.get("item_meta").getAsInt() : 0;
                Item item = Item.get(jsonObject.get("item_id").getAsInt(), itemMeta);
                ingredient.setItem(item);
                break;
            case KILL:
                if (!jsonObject.has("entityName")){
                    return null;
                }
                ingredient.setEntityName(jsonObject.get("entityName").getAsString());
                break;
        }
        return ingredient;
    }

    private final String ingredientId;
    private final TYPE type;

    private final int count;
    private Item item;
    private String entityName;

    private String infoString;

    public QuestIngredient(String ingredientId, TYPE type, int count){
        this.ingredientId = ingredientId;
        this.type = type;
        this.count = count;
    }

    public void onComplete(Player player, SurvivalQuests loader){
        if (this.type == TYPE.ITEM){
            Item item = this.getSampleItem();
            item.setCount(this.count);
            player.getInventory().removeItem(item);
        }
    }

    public boolean checkIngredient(Player player, SurvivalQuests loader){
        if (player == null || loader == null){
            return false;
        }

        switch (this.type){
            case ITEM:
                return this.checkInventory(player);
            case CRAFT:
                return this.checkCraft(player, loader);
            case KILL:
                return this.checkKill(player, loader);
        }
        return false;
    }

    private boolean checkInventory(Player player){
        Item item = this.getSampleItem();
        item.setCount(this.count);
        return player.getInventory().contains(item);
    }

    private boolean checkCraft(Player player, SurvivalQuests loader){
        PlayerQuestData questData = loader.getQuestData(player);
        if (questData != null){
            return questData.getCraftCount(this.item.getId()) >= this.count;
        }
        return false;
    }

    private boolean checkKill(Player player, SurvivalQuests loader){
        PlayerQuestData questData = loader.getQuestData(player);
        if (questData != null){
            return questData.getEntityKillCount(this.entityName.toLowerCase()) >= this.count;
        }
        return false;
    }

    private void buildInfoString(){
        switch (this.type){
            case ITEM:
                this.infoString = "Bring "+this.item.getName()+" x "+this.count;
                break;
            case CRAFT:
                this.infoString = "Craft "+this.item.getName()+" x "+this.count;
                break;
            case KILL:
                this.infoString = "Kill "+this.entityName+" x "+this.count;
                break;
        }
    }

    public String getIngredientId() {
        return this.ingredientId;
    }

    public TYPE getType() {
        return this.type;
    }

    public int getCount() {
        return this.count;
    }

    public void setItem(Item item) {
        this.item = item;
        this.buildInfoString();
    }

    public Item getItem() {
        return this.item;
    }

    public Item getSampleItem(){
        return Items.deepCopy(this.item);
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
        this.buildInfoString();
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getInfoString(){
        return this.infoString;
    }

    public enum TYPE {
        ITEM,
        CRAFT,
        KILL
    }

}

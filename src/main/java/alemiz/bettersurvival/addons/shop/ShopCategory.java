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

package alemiz.bettersurvival.addons.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCategory extends ShopCategoryElement{

    public Map<String, ShopSubCategory> subCategories = new HashMap<>();
    private List<ShopItem> allItems = null;

    public ShopCategory(String categoryName, JsonObject data, SurvivalShop loader) {
        super(categoryName, data, loader);

        try {
            this.loadCategories(data);
        }catch (Exception e){
            loader.plugin.getLogger().error("§cCan not load subcategories for shop category '"+categoryName+"'!");
        }
    }

    private void loadCategories(JsonObject jsonObject) throws Exception{
        if (!jsonObject.has("subcategories") || !jsonObject.get("subcategories").isJsonArray()){
            return;
        }

        JsonArray categories = jsonObject.getAsJsonArray("subcategories");
        for (JsonElement jsonElement : categories){
            String subName = jsonElement.getAsString();
            JsonObject json = this.loader.getSubCategoryJson(subName);

            if (json == null) continue;
            ShopSubCategory subCategory = new ShopSubCategory(subName, json, this.loader);
            this.subCategories.put(subName.toLowerCase(), subCategory);
        }
    }

    public boolean hasSubCategories(){
        return !this.subCategories.isEmpty();
    }

    public Map<String, ShopSubCategory> getSubCategories() {
        return this.subCategories;
    }

    public void resetAllItems(){
        this.allItems = null;
    }

    public List<ShopItem> getAllItems(){
        if (this.allItems == null){
            List<ShopItem> items = new ArrayList<>(this.items);
            if (this.hasSubCategories()){
                for (ShopSubCategory subCategory : this.subCategories.values()){
                    items.addAll(subCategory.getItems());
                }
            }
            this.allItems = items;
        }
        return this.allItems;
    }
}

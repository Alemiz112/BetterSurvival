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

package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.ShopCategory;
import alemiz.bettersurvival.addons.shop.ShopCategoryElement;
import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.addons.shop.ShopSubCategory;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import me.onebone.economyapi.EconomyAPI;

import java.util.ArrayList;

public class ShopItemListForm extends SimpleForm {

    private final transient ShopCategoryElement category;

    private final transient ArrayList<Object> elements = new ArrayList<>();

    public ShopItemListForm(Player player, ShopCategoryElement category) {
        super();
        this.player = player;
        this.category = category;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Shop " + this.category.getFormattedName());
        this.setContent("§7Pickup item by your own choice. Your coins: §8" + EconomyAPI.getInstance().myMoney(player) + "$");

        if ((category instanceof ShopCategory) && ((ShopCategory) category).hasSubCategories()){
            ShopCategory shopCategory = (ShopCategory) category;
            this.insertCategories(shopCategory);
        }

        this.insertItems();
        return this;
    }

    private void insertCategories(ShopCategory shopCategory){
        for (ShopSubCategory subCategory : shopCategory.getSubCategories().values()){
            ElementButton button = new ElementButton("§5" + subCategory.getFormattedName()+"\n§7Click to browse!");

            if (subCategory.canUseImage()){
                String image = subCategory.getCustomImage();
                button.addImage(new ElementButtonImageData(subCategory.getImageType(), image));
            }

            this.addButton(button);
            this.elements.add(subCategory);
        }
    }

    private void insertItems(){
        for (ShopItem item : this.category.getItems()) {
            ElementButton button = new ElementButton("§5" + item.getFormattedName() + "\n§7Package: §8" + item.getCount() + " §7Price: §8" + item.getPrice() + "$");

            if (item.canUseImage()) {
                String image = item.getCustomImage();
                if (image.equals("")) {
                    image = "textures/items/" + item.buildItem().getName().replace(" ", "_").toLowerCase();
                }
                button.addImage(new ElementButtonImageData(item.getImageType(), image));
            }
            this.addButton(button);
            this.elements.add(item);
        }
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        int index = this.getResponse().getClickedButtonId();

        if (this.elements.size() <= index){
            return;
        }

        Object element = this.elements.get(index);
        if (element instanceof ShopItem){
            ShopItem item = (ShopItem) element;
            new ShopItemForm(player, item, this.category.getLoader()).buildForm().sendForm();
            return;
        }

        if (element instanceof ShopSubCategory){
            ShopSubCategory subCategory = (ShopSubCategory) element;
            subCategory.sendForm(player);
        }
    }
}

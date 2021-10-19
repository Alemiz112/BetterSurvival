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

package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.cubemc.CubeBridge;
import alemiz.bettersurvival.addons.shop.ShopCategoryElement;
import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.utils.form.CustomForm;
import cubemc.commons.nukkit.utils.forms.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemSellForm extends CustomForm {

    private final transient ShopCategoryElement category;
    private final transient List<ShopItem> items = new ArrayList<>();

    public ItemSellForm(Player player, ShopCategoryElement category){
        super();
        this.player = player;
        this.category = category;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Sell "+category.getCategoryName().toUpperCase());
        this.addElement(new ElementLabel("§7Please choose item you want to sell."));

        ElementDropdown dropdown = new ElementDropdown("");
        for (ShopItem item : category.getItems()){
            if (!item.canBeSold()) continue;
            dropdown.addOption("§5"+item.getFormattedName()+" §8Offer: §l"+item.getSellPrice()+"$");
            this.items.add(item);
        }
        this.addElement(dropdown);
        this.addElement(new ElementStepSlider("§7Item count", Arrays.asList("1", "2", "5", "10", "20", "40", "64")));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;

        int itemIndex = this.getResponse().getDropdownResponse(1).getElementID();
        if (this.items.size() <= itemIndex) return;

        int count = Integer.parseInt(this.getResponse().getStepSliderResponse(2).getElementContent());

        ShopItem shopItem = this.items.get(itemIndex);
        if (shopItem == null || !shopItem.canBeSold()){
            player.sendMessage("§c»§7Unable to sell item!");
            return;
        }

        Item item = shopItem.getItemSample();
        item.setCount(count);

        if (!player.getInventory().contains(item)){
            player.sendMessage("§c»§7Unable to sell item. You do not own this item!");
            return;
        }

        player.getInventory().removeItem(item);

        CubeBridge.playerManager().addCoins(player, count * shopItem.getSellPrice(), CubeBridge.DEFAULT_COINS);
        player.sendMessage("§a»§7"+item.getName()+" was sold successfully!");
    }
}

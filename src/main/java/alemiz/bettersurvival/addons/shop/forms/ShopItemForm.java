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

import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.form.CustomForm;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;

import java.util.Arrays;

public class ShopItemForm extends CustomForm {

    private final transient SurvivalShop loader;
    private final transient ShopItem item;

    public ShopItemForm(Player player, ShopItem item, SurvivalShop loader){
        super("§l§8Buy "+item.getFormattedName());
        this.player = player;
        this.item = item;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addElement(new ElementLabel("§7One package contains §8"+this.item.getCount()+"x "+this.item.getFormattedName()+"§r§7!"));
        this.addElement(new ElementStepSlider("§7Select package count", Arrays.asList("1", "2", "3","4", "6", "8", "10", "12", "16", "32"), 0));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        int stackCount = Integer.parseInt(this.getResponse().getStepSliderResponse(1).getElementContent());

        boolean success = item.buyItem(player, stackCount);
        String message = this.loader.messageFormat(player, (success? "messageSuccess" : "messageFail"), item.getStackPrice(stackCount));
        message = message.replace("{item}", item.getName());
        player.sendMessage(message);
    }
}

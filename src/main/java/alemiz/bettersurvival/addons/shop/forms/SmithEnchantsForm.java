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

import alemiz.bettersurvival.addons.shop.Enchant;
import alemiz.bettersurvival.addons.shop.SmithShop;
import cubemc.commons.nukkit.utils.forms.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

public class SmithEnchantsForm extends SimpleForm {

    private final transient SmithShop loader;

    public SmithEnchantsForm(Player player, SmithShop loader){
        super("§l§8Enchants Shop", "§7You can add special powers to items by enchanting them. TIP: Click on enchant to buy it.");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        for (Enchant enchant : this.loader.getEnchants()){
            this.addButton(new ElementButton("§5"+enchant.getFormattedName()+"\n§7Starting at: §8"+enchant.getBasePrice()+"$"));
        }
        this.addButton(new ElementButton("Exit"));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        int response = this.getResponse().getClickedButtonId();

        if (response >= this.loader.getEnchants().size()) return;

        Enchant enchant = this.loader.getEnchants().get(response);
        if (enchant == null){
            player.sendMessage("§c»§7Enchantment was not found!");
            return;
        }
        this.loader.sendEnchantLevelForm(player, enchant);
    }
}

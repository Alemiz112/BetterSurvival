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

import alemiz.bettersurvival.addons.shop.Enchant;
import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

public class SmithEnchantLevelForm extends SimpleForm {

    private final transient SmithShop loader;
    private final transient Enchant enchant;

    public SmithEnchantLevelForm(Player player, Enchant enchant, SmithShop loader){
        super();
        this.player = player;
        this.enchant = enchant;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Levels of "+this.enchant.getFormattedName());
        this.setContent("§7Please select enchantment level.");

        for (int i = 0; i < this.enchant.prices.length; i++){
            int level = i+1;
            this.addButton(new ElementButton("§fLevel: "+level+"§7 Price: §8"+this.enchant.getPrice(level)+"$\n§7»Click to Buy!"));
        }

        this.addButton(new ElementButton("Exit"));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null || this.getResponse().getClickedButton().getText().equals("Exit")) return;
        int level = this.getResponse().getClickedButtonId()+1;

        if (level > this.enchant.prices.length){
            player.sendMessage("§c»§7You chose highest level than maximum allowed!");
            return;
        }

        int price = enchant.getPrice(level);
        boolean success = EconomyAPI.getInstance().reduceMoney(player, price) >= 1;

        if (success){
            Item item = enchant.getEnchantItem(level);
            player.getInventory().addItem(item);
        }

        String message = this.loader.getLoader().messageFormat(player, (success? "messageSuccess" : "messageFail"), price);
        message = message.replace("{item}", this.enchant.getFormattedName()+" Enchant");
        player.sendMessage(message);
    }
}

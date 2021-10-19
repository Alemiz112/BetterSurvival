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

package alemiz.bettersurvival.addons.economy;

import alemiz.bettersurvival.addons.cubemc.CubeBridge;
import alemiz.bettersurvival.utils.TextUtils;
import alemiz.bettersurvival.utils.form.ModalForm;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cubemc.commons.nukkit.utils.forms.Form;

public class SpawnerBreakForm extends ModalForm {

    private final transient EconomySpawners loader;
    private final transient Block block;

    public SpawnerBreakForm(Player player, Block block, EconomySpawners loader) {
        super("", "");
        this.player = player;
        this.block = block;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Break The Spawner");
        this.setContent("Spawners are rare blocks. To break the spawner you must pay §e"+ TextUtils.formatBigNumber(this.loader.getSpawnerPrice()) +"$\n" +
                "§rYou can also leave the spawner here and upgrade it using §6/spawnerup§r command!");

        this.setButton1("§aBuy Spawner");
        this.setButton2("Exit");
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.block == null || this.getResponse() == null || this.getResponse().getClickedButtonId() == 1){
            return;
        }

        boolean success = CubeBridge.playerManager().canReduceCoins(player, this.loader.getSpawnerPrice(), CubeBridge.DEFAULT_COINS);
        if (!success){
            player.sendMessage("§c»§7You do not have enough coins to buy mob spawner!");
            return;
        }
        CubeBridge.playerManager().reduceCoins(player, this.loader.getSpawnerPrice(), CubeBridge.DEFAULT_COINS);

        Level level = this.block.getLevel();
        Item item = player.getInventory().getItemInHand();
        level.useBreakOn(this.block, item, null, true);

        boolean silkTouch = item.getEnchantment(Enchantment.ID_SILK_TOUCH) != null;
        if (!silkTouch){
            level.dropItem(this.block.add(0.5, 0.5, 0.5), Item.get(Item.MONSTER_SPAWNER, 0, 1));
        }
        player.sendMessage("§a»§7You have successfully bought spawner!");
    }
}

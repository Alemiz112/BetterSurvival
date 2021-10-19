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
import alemiz.bettersurvival.utils.form.CustomForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.item.Item;
import cubemc.commons.nukkit.utils.forms.Form;

import java.util.ArrayList;
import java.util.List;

public class SpawnerUpgradeForm extends CustomForm {

    private final transient EconomySpawners loader;
    private final transient List<SpawnerLevel> levels = new ArrayList<>();

    public SpawnerUpgradeForm(Player player, EconomySpawners loader){
        super("§l§8Spawner Upgrades");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        StringBuilder builder = new StringBuilder("Upgrade your spawner and decrease mob spawn time!\n" +
                "Default interval: §l§a"+EconomySpawners.DEFAULT_LEVEL.getAverageDelay()+" §rseconds\n");
        List<String> levels = new ArrayList<>();

        for (SpawnerLevel level : this.loader.getSpawnerLevels().values()){
            if (level == EconomySpawners.DEFAULT_LEVEL){
                continue;
            }

            builder.append("Level ").append(level.getLevel()).append(": §l§e").append(level.getAverageDelay()).append(" §rseconds\n");
            levels.add("§5Level "+level.getLevel()+" §7Price: §8"+level.getPrice()+"$");
            this.levels.add(level);
        }
        this.addElement(new ElementLabel(builder.toString()));
        this.addElement(new ElementDropdown("Select spawner level:", levels));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null || this.getResponse().getDropdownResponse(1) == null) return;
        int levelId = this.getResponse().getDropdownResponse(1).getElementID();

        if (this.levels.size() <= levelId) return;
        SpawnerLevel level = this.levels.get(levelId);

        boolean success = CubeBridge.playerManager().reduceCoins(player, level.getPrice(), CubeBridge.DEFAULT_COINS) != null;
        if (!success){
            player.sendMessage("§c»§7You dont have §e"+level.getPrice()+"§7 coins to buy §6spawner upgrade§7!");
            return;
        }

        Item item = this.loader.buildSpawnerUpgrade(player.getName(), level.getLevel());
        if (player.getInventory().canAddItem(item)){
            player.getInventory().addItem(item);
        }else {
            player.getLevel().dropItem(player, item);
        }

        String message = this.loader.loader.configFile.getString("spawnerUpgradeBuyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{level}", String.valueOf(level.getLevel()));
        player.sendMessage(message);
    }
}

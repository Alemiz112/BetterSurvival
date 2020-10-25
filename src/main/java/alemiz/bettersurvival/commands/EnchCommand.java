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

package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;

public class EnchCommand extends Command {

    public SmithShop loader;

    public EnchCommand(String name, SmithShop loader) {
        super(name, "Enchant your item", "");

        this.usage = "§7/ench : Apply all possible enchants";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.getLoader().configFile.getString("enchantPermission"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be run only in game!");
            return true;
        }

        Player player = (Player) sender;
        Item inHand = player.getInventory().getItemInHand();
        if (inHand.getId() == Item.AIR){
            player.sendMessage("§c»§r§7You must hold item!");
            return true;
        }

        Item item = this.loader.enchantItem(player, inHand);
        if (item != null) player.getInventory().setItemInHand(item);
        return true;
    }
}

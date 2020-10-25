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

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class ShopCommand extends Command {

    public SurvivalShop loader;

    public ShopCommand(String name, SurvivalShop loader) {
        super(name, "Teleports to shop", "");

        this.usage = "§7/shop : Teleports to shop";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
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

        if (args.length <= 0){
            this.loader.teleportToSpawn(player);
            player.sendMessage("§6»§7You was teleported to shop!");
            return true;
        }

        if (!player.hasPermission(this.loader.configFile.getString("shopManagePermission"))){
            player.sendMessage("§c»§7You dont have permission to create shop!");
            return true;
        }

        switch (args[0]){
            case "set":
                this.loader.setShopSpawn(player);
                player.sendMessage("§6»§7Shop spawn was saved!");
                break;
            case "smith":
                if (this.loader.getSmithShop() == null){
                    player.sendMessage("§c»§7Smith addon is not enabled. Please enable it in SurvivalShop config!");
                    break;
                }

                this.loader.getSmithShop().createSmith(player);
                break;
            case "clear":
                if (this.loader.getNpcRemovers().contains(player.getName())){
                    this.loader.removeRemover(player);
                    break;
                }
                this.loader.addRemover(player);
                break;
            default:
                player.sendMessage("§6Shop Commands:\n§7/shop : Teleports to shop\n"+
                        "§7/shop set : Set shop spawn\n" +
                        "§7/shop smith : Creates new smith npc\n" +
                        "§7/shop clear : Hit any shop npc to remove");
                break;
        }
        return true;
    }
}

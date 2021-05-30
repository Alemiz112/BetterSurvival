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

package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.BetterVoting;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;

public class CrateCommand extends Command {

    public BetterVoting loader;

    public CrateCommand(String name, BetterVoting loader) {
        super(name, "Manage crates", "");

        this.usage = "§7/crate give <player> <count>: Give player crate key\n"+
                "§7/crate set: Touch crate chest to get coords\n";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-crateCommand"));
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

        if (args.length < 1){
            if (!this.loader.enableVoteCrate) return true;

            if (!player.getInventory().getItemInHand().getCustomName().equals(this.loader.voteKey.getCustomName())){
                player.sendMessage("§c»§7You must have crate key. Vote to get one!");
                return true;
            }

            this.loader.sendCrateMenu(player);
            return true;
        }

        switch (args[0]){
            case "give":
                if (args.length < 3){
                    sender.sendMessage(getUsageMessage());
                    break;
                }
                try {
                    this.loader.givekey((Player) sender, args[1], Integer.parseInt(args[2]));
                }catch (Exception e){
                    sender.sendMessage("§cPlease enter count as a number!");
                }
                return true;
            case "set":
                this.loader.getCratePos((Player) sender);
                return true;
        }


        return true;
    }
}

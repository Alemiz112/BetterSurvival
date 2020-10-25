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

import alemiz.bettersurvival.addons.myhomes.AddWarpForm;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.Arrays;


public class PlayerWarpCommand extends Command {

    private final MyHomes loader;

    public PlayerWarpCommand(String name, MyHomes loader) {
        super(name, "Visit public player warps", "");
        this.commandParameters.clear();

        this.usage = "§7/pwarp add : Creates warp with unique name\n" +
                "§7/pwarp <remove> <name> : Removes your unique warp\n" +
                "§7/pwarp <name> : Teleports to warp\n" +
                "§7/pwarp list : Lists all available warps";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", true)
        });

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
            this.loader.showWarpMenu(player);
            return true;
        }

        switch (args[0]){
            case "add":
                new AddWarpForm(player, this.loader).buildForm().sendForm();
                break;
            case "remove":
                this.loader.deleteWarp(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                break;
            case "list":
                this.loader.showWarpMenu(player);
                break;
            default:
                this.loader.teleportToWarp(player, String.join(" ", args));
                break;
        }
        return true;
    }
}

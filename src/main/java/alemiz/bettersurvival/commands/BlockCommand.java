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

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class BlockCommand extends Command {

    public Troller loader;

    public BlockCommand(String name, Troller loader) {
        super(name, "Troll player and spawn blocks around him", "");

        this.usage = "§7/block <player> <block id>: Troll player and spawn blocks around him";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET,false),
                new CommandParameter("block", CommandParamType.TEXT, false)
        });

        this.setPermission(loader.configFile.getString("permission-block"));
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

        if (args.length < 2){
            sender.sendMessage(getUsageMessage());
            return true;
        }

        this.loader.block((Player) sender, args[0], args[1]);
        return true;
    }
}

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

import alemiz.bettersurvival.addons.myhomes.MyHomes;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class DelHomeCommand extends Command {

    public MyHomes loader;

    public DelHomeCommand(String name, MyHomes loader) {
        super(name, "Remove your home", "");

        this.usage = "§7/delhome <home - optional> : Remove your home";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
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
            loader.delHome(player, "default");
            return true;
        }

        loader.delHome(player, args[0]);
        return true;
    }
}

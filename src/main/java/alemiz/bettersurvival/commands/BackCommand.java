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

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class BackCommand extends Command {

    public MoreVanilla loader;

    public BackCommand(String name, MoreVanilla loader) {
        super(name, "Teleports to your death point", "");

        this.usage = "§7/back : Teleports to your death point";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-back"));
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
        this.loader.back((Player) sender);
        return true;
    }
}

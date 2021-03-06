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

import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.clans.forms.ClanLevelsForm;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class ClanLevelsCommand extends Command {

    public PlayerClans loader;

    public ClanLevelsCommand(String name, PlayerClans loader) {
        super(name, "Clan levels info", "");
        this.usage = "§7/clanlevels : Show clan levels info";
        this.setUsage(this.getUsageMessage());

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

        new ClanLevelsForm((Player) sender, this.loader).buildForm().sendForm();
        return true;
    }
}

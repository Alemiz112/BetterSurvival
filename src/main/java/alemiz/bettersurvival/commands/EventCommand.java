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

import alemiz.bettersurvival.addons.BetterLobby;
import alemiz.bettersurvival.utils.Command;
import alemiz.sgu.nukkit.StarGateUniverse;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class EventCommand extends Command {

    public BetterLobby loader;

    public EventCommand(String name, BetterLobby loader) {
        super(name, "Teleports to event server", "");

        this.usage = "§7/event : Teleports to event server";
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

        StarGateUniverse.getInstance().transferPlayer(((Player) sender), "event");
        return true;
    }
}

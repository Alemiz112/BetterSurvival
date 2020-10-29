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

import alemiz.bettersurvival.addons.BetterVoting;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;

public class VoteCommand extends Command {

    public BetterVoting loader;

    public VoteCommand(String name, BetterVoting loader) {
        super(name, "Vote command", "/vote");

        this.usage = "§7/vote : Vote command";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.loader = loader;
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
       sender.sendMessage("§a»§7Vote and get awesome reward. Vote 20x and get subscriber rank! Visit: §acubedmc.eu/vote§7!");
       return true;
    }
}

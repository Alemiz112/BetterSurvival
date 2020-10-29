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

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.Server;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class HealCommand extends Command {

    public MoreVanilla loader;

    public HealCommand(String name, MoreVanilla loader) {
        super(name, "Heal player or yourself", "");

        this.usage ="§7/heal <player - optional> : Heal player or yourself";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });

        this.setPermission(loader.configFile.getString("permission-heal"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player) && args.length < 1){
            sender.sendMessage("§cThis command can be run only in game!");
            return true;
        }

        if (!(sender instanceof Player)){
            if (args.length < 1){
                sender.sendMessage(getUsageMessage());
                return true;
            }

            Player player = Server.getInstance().getPlayer(args[0]);
            this.loader.heal(player, "console");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1){
            this.loader.heal(player, player.getName());
            return true;
        }

        player = Server.getInstance().getPlayer(args[0]);
        this.loader.heal(player, ((Player) sender).getName());
        return true;
    }
}

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

import java.util.Set;

public class GetHomeCommand extends Command {

    public MyHomes loader;

    public GetHomeCommand(String name, MyHomes loader) {
        super(name, "Prints your homes", "", new String[]{"listhome"});
        this.commandParameters.clear();

        this.usage = "§7/gethome: Prints your homes";
        this.setUsage(getUsageMessage());

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

        Set<String> homes = loader.getHomes(player);
        if (homes.isEmpty()){
            player.sendMessage("§6»§7You dont have any homes yet!");
            return true;
        }

        StringBuilder format = new StringBuilder();
        for (String home : homes){
            format.append(home).append(", ");
        }

        player.sendMessage("§6»§7Your homes: "+format.substring(0, format.length()-2));
        return true;
    }
}

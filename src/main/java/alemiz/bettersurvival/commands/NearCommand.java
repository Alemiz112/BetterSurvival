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
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;

import java.util.List;

public class NearCommand extends Command {

    public MoreVanilla loader;

    public NearCommand(String name, MoreVanilla loader) {
        super(name, "List players who are nearby", "");
        this.commandParameters.clear();

        this.usage = "§7/near <radius - optional>: List players who are nearby";
        this.setUsage(getUsageMessage());

        this.setPermission(loader.configFile.getString("permission-near"));
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

        int radius = 8;
        if (args.length >= 1){
            try {
                radius = Integer.parseInt(args[0]);
            }catch (Exception e){
                player.sendMessage("§cPlease enter numerical radius!");
                return true;
            }
        }

        List<Player> players = this.loader.getNearPlayers(player.clone(), radius);
        players.remove(player);

        String pplayers = "";
        for (Player pplayer : players){
            if (pplayer.getName().equals(player.getName())) continue;
            pplayers += pplayer.getDisplayName() + ", ";
        }

        String message = this.loader.configFile.getString("nearMessage");
        message = message.replace("{players}", (pplayers.equals("")? "Not found" : pplayers.substring(0, pplayers.length()-2)));
        player.sendMessage(message);
        return true;
    }
}

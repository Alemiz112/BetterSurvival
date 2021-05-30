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

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class TradeCommand extends Command {

    public BetterEconomy loader;

    public TradeCommand(String name, BetterEconomy loader) {
        super(name, "Trade with players", "");

        this.usage = "§7/trade setup <price> : Setup trade ItemFrame\n" +
                "§7/trade <on|off> : Enable trade mode";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, false),
                new CommandParameter("value", CommandParamType.STRING, false)
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
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "setup":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    break;
                }

                try {
                    int value = Integer.parseInt(args[1]);
                    this.loader.addTraderCreator(player, value);
                }catch (NumberFormatException e){
                    player.sendMessage("§c»§r§7Please provide numerical value!");
                    break;
                }
                break;
            case "on":
                this.loader.addTrader(player);
                player.sendMessage("§6»§7Trade mode has been enabled!");
                break;
            case "off":
                this.loader.removeTrader(player);
                player.sendMessage("§6»§7Trade mode has been disabled!");
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }
}

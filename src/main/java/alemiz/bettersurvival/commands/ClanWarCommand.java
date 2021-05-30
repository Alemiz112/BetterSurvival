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

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.clans.forms.ClanWarInviteForm;
import alemiz.bettersurvival.addons.clans.forms.ClanWarForm;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class ClanWarCommand extends Command {

    public PlayerClans loader;

    public ClanWarCommand(String name, PlayerClans loader) {
        super(name, "Manage clan wars", "");

        this.usage = "§7/clanwar invite : Invite clan to war\n" +
                "§7/clanwar manage : Manage your clan war invites";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, false),
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

        Clan clan = this.loader.getClan(player);
        if (clan == null){
            player.sendMessage("§c»§7You are not in any clan!");
            return true;
        }

        switch (args[0]) {
            case "invite":
                if (!clan.isAdmin(player) && !clan.isOwner(player)) {
                    player.sendMessage("§c»§7Only clan admin can invite clan to war!");
                    return true;
                }
                new ClanWarInviteForm(player, this.loader).buildForm().sendForm();
                return true;
            case "manage":
                new ClanWarForm(player, this.loader).buildForm().sendForm();
                return true;

        }

        player.sendMessage(this.getUsageMessage());
        return true;
    }
}

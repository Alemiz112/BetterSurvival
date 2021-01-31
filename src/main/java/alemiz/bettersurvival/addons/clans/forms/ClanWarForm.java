/*
 * Copyright 2021 Alemiz
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

package alemiz.bettersurvival.addons.clans.forms;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class ClanWarForm extends SimpleForm {

    private final transient PlayerClans loader;
    private final transient List<Clan> clans = new ArrayList<>();

    public ClanWarForm(Player player, PlayerClans loader) {
        super("§l§8Clan War", "");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        Clan clan = this.loader.getClan(this.player);
        if (clan == null) {
            this.setContent("§cYou aren't in any clan!");
            return this;
        }

        String message = this.loader.configFile.getString("clanWarInvitesFormMessage");
        this.setContent(message);

        for (Clan warClan : clan.getWarClanInvites()) {
            this.addButton(new ElementButton("§3"+warClan.getName()+ " §7[§eInvite§7]"));
            this.clans.add(warClan);
        }

        for (Clan warClan : clan.getClansInWar()) {
            this.addButton(new ElementButton("§3"+warClan.getName()+ " §7[§cWar§7]"));
            this.clans.add(warClan);
        }

        if (this.clans.isEmpty()) {
            this.setContent("§fYour clan has no pending war invites and is currently not in war with any clan!");
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null){
            return;
        }

        int response = this.getResponse().getClickedButtonId();
        if (response >= this.clans.size()) {
            return;
        }

        Clan clan = this.loader.getClan(player);
        if (!clan.isAdmin(player) && !clan.isOwner(player)) {
            player.sendMessage("§c»§7Only clan admin can manage clan wars!");
            return;
        }

        Clan warClan = this.clans.get(response);
        new ClanWarManageForm(player, clan, warClan, this.loader).buildForm().sendForm();
    }
}

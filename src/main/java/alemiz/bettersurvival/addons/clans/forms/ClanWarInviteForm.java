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

package alemiz.bettersurvival.addons.clans.forms;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.utils.form.CustomForm;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseData;

import java.util.ArrayList;
import java.util.List;

public class ClanWarInviteForm extends CustomForm {

    private final transient PlayerClans loader;
    private final transient List<Clan> clans = new ArrayList<>();

    public ClanWarInviteForm(Player player, PlayerClans loader) {
        super("§l§8Clan Wars");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        String message = this.loader.configFile.getString("clanWarInviteFormMessage");
        this.addElement(new ElementLabel(message));

        List<String> clans = new ArrayList<>();
        for (Clan clan : this.loader.getClans().values()) {
            if (!clan.isMember(this.player)) {
                clans.add(clan.getName());
                this.clans.add(clan);
            }
        }

        ElementDropdown dropdown = new ElementDropdown("Choose clan:", clans);
        this.addElement(dropdown);
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null){
            return;
        }

        FormResponseData response = this.getResponse().getDropdownResponse(1);
        if (response == null || response.getElementID() >= this.clans.size()) {
            return;
        }

        Clan clan = this.loader.getClan(player);
        if (clan == null) {
            player.sendMessage("§c»§7You are not in clan!");
            return;
        }

        if (!clan.isAdmin(player) && !clan.isOwner(player)) {
            player.sendMessage("§c»§7Only clan admin can invite clan to war!");
            return;
        }

        Clan targetClan = this.clans.get(response.getElementID());
        if (targetClan == null) {
            player.sendMessage("§c»§7Clan " + response.getElementContent() + "was not found!");
            return;
        }

        if (!targetClan.addClanWarInvite(clan)) {
            player.sendMessage("§c»§7Your clan is already in war with "+targetClan.getName()+"!");
            return;
        }

        player.sendMessage("§a»§7You have invited clan "+targetClan.getName()+" to war!");
    }
}

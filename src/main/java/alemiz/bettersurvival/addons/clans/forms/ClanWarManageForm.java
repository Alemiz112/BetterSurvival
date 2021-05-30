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
import alemiz.bettersurvival.utils.form.ModalForm;
import cn.nukkit.Player;
import cubemc.commons.nukkit.utils.forms.Form;

public class ClanWarManageForm extends ModalForm {

    private final transient PlayerClans loader;
    private final transient Clan playerClan;
    private final transient Clan warClan;

    public ClanWarManageForm(Player player, Clan playerClan, Clan warClan, PlayerClans loader) {
        super("§l§8Clan War Manage", "", "", "");
        this.player = player;
        this.playerClan = playerClan;
        this.warClan = warClan;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        if (!this.playerClan.isOwner(this.player) && ! this.playerClan.isAdmin(this.player)) {
            this.setContent("§cOnly clan admin can manage clan wars!");
            return this;
        }

        String message;
        if (this.playerClan.isInWarWith(this.warClan)) {
             message = this.loader.configFile.getString("clanWarLeaveFormMessage");
        } else {
            message = this.loader.configFile.getString("clanWarJoinFormMessage");
        }

        message = message.replace("{clan}", this.warClan.getName());
        this.setContent(message);

        this.setButton1("§aYes");
        this.setButton2("§cNo");
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) {
            return;
        }

        if (!this.playerClan.isMember(player)) {
            player.sendMessage("§c»§You are no longer clan member!");
            return;
        }

        if (this.getResponse().getClickedButtonId() == 1) {
            this.playerClan.onClanWarRejected(this.warClan);
            return;
        }

        if (this.playerClan.isInWarWith(this.warClan)) {
            this.playerClan.onClanWarLeft(this.warClan);
        } else {
            this.playerClan.onClanWarAccepted(this.warClan);
        }
    }
}

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

package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.addons.myland.LandRegion;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.exception.CancelException;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

public class ClanLand extends LandRegion {

    private final Clan clan;

    private boolean restriction = false;
    private boolean whitelistEnabled = false;

    public ClanLand(Clan clan) {
        super(clan.getName(), clan.getName()+" land");
        this.clan = clan;
    }

    @Override
    public boolean canManage(String player) {
        return this.clan.getOwner().equalsIgnoreCase(player) || this.clan.isAdmin(player);
    }

    @Override
    public boolean onInteract(Player player, Block block) throws CancelException {
        boolean isMember = this.clan.isMember(player);
        if (!isMember) {
            return false;
        }

        boolean isAdmin = this.clan.isAdmin(player);
        if (isAdmin) {
            return true;
        }

        if (this.whitelistEnabled && !this.whitelist.contains(player.getName().toLowerCase())){
            player.sendMessage("§c»§7Your clan land has restricted access to whitelisted members only!");
            throw new CancelException(false);
        }

        if (block != null && this.restriction && MyLandProtect.INTERACT_BLOCKS.get(block.getId())){
            player.sendMessage("§c»§7Your clan restricted access to some blocks in the clan land!");
            throw new CancelException(false);
        }
        return true;
    }

    @Override
    public void load(ConfigSection config) {
       super.load(config);

       this.restriction = config.getBoolean("playerRestriction", false);
       this.whitelistEnabled = config.getBoolean("whitelistEnabled", false);
    }

    @Override
    public void save() {
        Config config = this.clan.getConfig();
        if (config == null) {
            return;
        }

        config.set("land.level", level.getFolderName().toLowerCase());
        config.set("land.pos0", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land.pos1", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});
        config.set("land.restriction", this.restriction);
        config.set("land.whitelistEnabled", this.whitelistEnabled);
        config.set("land.whitelist", this.whitelist);
        config.set("land.liquidFlow", this.liquidFlow);
        config.set("land.pistonMovement", this.pistonMovement);
        config.save();
    }

    @Override
    public boolean validate() {
        return super.validate() && this.clan != null;
    }

    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
        this.save();
    }

    public boolean isRestrictionEnabled() {
        return this.restriction;
    }

    public boolean isWhitelistEnabled() {
        return this.whitelistEnabled;
    }

    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
        this.save();
    }

    public Clan getClan() {
        return this.clan;
    }
}

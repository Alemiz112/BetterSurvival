package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.addons.myland.LandRegion;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.exception.CancelException;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

public class ClanLand extends LandRegion {

    private final Clan clan;

    private boolean restriction = false;
    private boolean whitelistEnabled = false;

    public ClanLand(Clan clan, Level level) {
        super(clan.getName(), clan.getName()+" land", level);
        this.clan = clan;
    }

    @Override
    public boolean canManage(String player) {
        return this.clan.getOwner().equalsIgnoreCase(player) || this.clan.isAdmin(player);
    }

    @Override
    public boolean onInteract(Player player, Block block) throws CancelException {
        boolean isMember = this.clan.isMember(player);
        if (!isMember) return false;

        boolean isAdmin = this.clan.isAdmin(player);
        if (isAdmin) return true;

        if (whitelistEnabled && !this.whitelist.contains(player.getName().toLowerCase())){
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
    public void save() {
        Config config = this.clan.getConfig();
        if (config == null) return;

        config.set("land.level", level.getFolderName().toLowerCase());
        config.set("land.pos0", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land.pos1", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});
        config.set("land.restriction", this.restriction);
        config.set("land.whitelistEnabled", this.whitelistEnabled);
        config.set("land.whitelist", this.whitelist);
        config.save();
    }

    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
    }

    public boolean isRestrictionEnabled() {
        return this.restriction;
    }

    public boolean isWhitelistEnabled() {
        return this.whitelistEnabled;
    }

    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }

    public Clan getClan() {
        return this.clan;
    }
}

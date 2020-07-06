package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.addons.myland.LandRegion;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

public class ClanLand extends LandRegion {

    private final Clan clan;

    private boolean restriction = false;

    public ClanLand(Clan clan, Level level) {
        super(clan.getName(), clan.getName()+" land", level);
        this.clan = clan;
    }

    @Override
    public void addWhitelist(String player) {
        //NOOP
    }

    @Override
    public void whitelistRemove(String player) {
        //NOOP
    }

    @Override
    public void save() {
        Config config = clan.getConfig();
        if (config == null) return;

        config.set("land.level", level.getFolderName().toLowerCase());
        config.set("land.pos0", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land.pos1", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});
        config.set("land.restriction", this.restriction);
        config.save();
    }

    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
    }

    public boolean isRestrictionEnabled() {
        return this.restriction;
    }

    public Clan getClan() {
        return this.clan;
    }
}

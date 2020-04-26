package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.scheduler.Task;

import java.util.Date;
import java.util.Map;

public class MuteCheckTask extends Task {

    private MoreVanilla loader;

    public MuteCheckTask(MoreVanilla loader){
        this.loader = loader;
    }

    @Override
    public void onRun(int i) {
        Map<String, Date> muted = this.loader.getMutedPlayers();
        Date now = new Date();

        for (String playerName : muted.keySet()) {
            Date muteTill = muted.get(playerName);

            if (!now.after(muteTill) || !muteTill.before(now)) return;
            this.loader.unmute(playerName, "console");
        }
    }
}

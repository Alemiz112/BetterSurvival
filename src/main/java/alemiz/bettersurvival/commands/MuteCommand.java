package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class MuteCommand extends Command {

    public MoreVanilla loader;

    public MuteCommand(String name, MoreVanilla loader) {
        super(name, "Mute player", "");

        this.usage = "§7/mute <player> <time - optional>: Mute player - default for an hour";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("time", CommandParamType.STRING, true)
        });

        this.setPermission(loader.configFile.getString("permission-mute"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length < 1){
            sender.sendMessage(getUsageMessage());
            return true;
        }

        if (!(sender instanceof Player)){
            Player player = Server.getInstance().getPlayer(args[0]);
            this.loader.mute(player, "console", (args.length < 2? "60:00": args[1]));
            return true;
        }


        Player player = Server.getInstance().getPlayer(args[0]);
        this.loader.mute(player, sender.getName(), (args.length < 2? "60:00": args[1]));
        return true;
    }
}

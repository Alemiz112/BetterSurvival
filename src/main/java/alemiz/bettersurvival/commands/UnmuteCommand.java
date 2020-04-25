package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class UnmuteCommand extends Command {

    public MoreVanilla loader;

    public UnmuteCommand(String name, MoreVanilla loader) {
        super(name, "Unmute player", "");

        this.usage = "§7/unmute <player>: Unmute player";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
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

        this.loader.unmute(args[0], ((sender instanceof Player)? sender.getName() : "console"));
        return true;
    }
}

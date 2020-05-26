package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.BetterVoting;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;

public class VoteCommand extends Command {

    public BetterVoting loader;

    public VoteCommand(String name, BetterVoting loader) {
        super(name, "Vote command", "/vote");

        this.usage = "§7/vote : Vote command";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.loader = loader;
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
       sender.sendMessage("§a»§7Vote and get awesome reward. Vote 20x and get subscriber rank! Visit: §acubedmc.eu/vote§7!");
       return true;
    }
}

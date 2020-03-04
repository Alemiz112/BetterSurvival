package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.LevelVote;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class LevelVoteCommand extends Command {

    public LevelVote loader;

    public LevelVoteCommand(String name, LevelVote loader, String usage) {
        super(name, "Vote for level events", "");

        System.out.println(usage);

        this.usage = usage;
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-vote"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be used only in game!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2){
            player.sendMessage(getUsageMessage());
            return true;
        }

        this.loader.vote(player, args[0], args[1]);
        return true;
    }
}

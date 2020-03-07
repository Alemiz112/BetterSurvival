package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.BetterVoting;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.task.TopVoterTask;

public class VoteCommand extends Command implements PluginIdentifiableCommand {

    /* TODO: Implement vote command without PocketVote command*/

    public BetterVoting loader;
    private PocketVote plugin;

    public VoteCommand(String name, BetterVoting loader) {
        super(name, "Vote command", "/vote [top]", new String[]{"v"});

        this.usage = "§7/vote : Vote command";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.loader = loader;
        this.plugin = PocketVote.getPlugin();
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!sender.hasPermission("pocketvote.vote")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to use /vote.");
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("TOP")) {
            this.plugin.getServer().getScheduler().scheduleAsyncTask(this.plugin, new TopVoterTask(this.plugin, sender.getName()));
            return true;
        } else {
            String link = this.loader.configFile.getString("customVoteCommandLink");
            if (link == null) {
                if (sender.hasPermission("pocketvote.admin")) {
                    sender.sendMessage(TextFormat.YELLOW + "You can add a link by typing /guadd");
                    sender.sendMessage(TextFormat.YELLOW + "See /guru for help!");
                } else {
                    sender.sendMessage(TextFormat.YELLOW + "The server operator has not added any voting sites.");
                }

                return true;
            } else {
                if (sender.hasPermission("pocketvote.admin")) {
                    sender.sendMessage(TextFormat.YELLOW + "Use /guru to manage this link.");
                }

                sender.sendMessage(TextFormat.AQUA + "You can vote at " + TextFormat.YELLOW + link);
                return true;
            }
        }
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}

package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.Server;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class FeedCommand extends Command {

    public MoreVanilla loader;

    public FeedCommand(String name, MoreVanilla loader) {
        super(name, "Load full food bar", "");

        this.usage = "§7/feed <player - optional> : Load full food bar";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", true)
        });

        this.setPermission(loader.configFile.getString("permission-feed"));
        this.loader = loader;
        this.setAliases(new String[]{"eat", "food"});
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player) && args.length < 1){
            sender.sendMessage("§cThis command can be run only in game!");
            return true;
        }

        if (!(sender instanceof Player)){
            if (args.length < 1){
                sender.sendMessage(getUsageMessage());
                return true;
            }

            Player player = Server.getInstance().getPlayer(args[0]);
            this.loader.feed(player, "console");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1){
            this.loader.feed(player, player.getName());
            return true;
        }

        player = Server.getInstance().getPlayer(args[0]);
        this.loader.feed(player, ((Player) sender).getName());
        return true;
    }
}

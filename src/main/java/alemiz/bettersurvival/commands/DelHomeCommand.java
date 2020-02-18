package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class DelHomeCommand extends Command {

    public Home loader;

    public DelHomeCommand(String name, Home loader) {
        super(name, "Remove your home", "");

        this.usage = "§7/delhome <home - optional> : Remove your home";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });

        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be run only in game!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1){
            loader.delHome(player, "default");
            return true;
        }

        loader.delHome(player, args[0]);
        return true;
    }
}

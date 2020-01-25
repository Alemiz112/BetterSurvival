package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;


public class HomeCommand extends Command {

    protected static final String usage = "§6Home Command:\n"+
            "§7/home <home - optional> : Teleport to your home";


    public Home loader;

    public HomeCommand(String name, Home loader) {
        super(name, "Teleport to home", usage);
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
            loader.teleportToHome(player, "default");
            return true;
        }

        loader.teleportToHome(player, args[0]);
        return true;
    }
}

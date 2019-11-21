package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.List;

public class HomeCommand extends Command {

    protected static final String usage = "§6Home Command:\n"+
            "§7/home <home - optional> : Teleport to your home";

    public HomeCommand(String name) {
        super(name, "Teleport to home", usage);
        this.commandParameters.clear();

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be run only in game!");
        }

        Player player = (Player) sender;

        if (args.length < 1){
            Home.teleportToHome(player, "default");
            return true;
        }

        Home.teleportToHome(player, args[0]);
        return true;
    }
}

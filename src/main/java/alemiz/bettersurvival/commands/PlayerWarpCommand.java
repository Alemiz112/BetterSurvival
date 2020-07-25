package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.myhomes.AddWarpForm;
import alemiz.bettersurvival.addons.myhomes.MyHomes;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.Arrays;


public class PlayerWarpCommand extends Command {

    private final MyHomes loader;

    public PlayerWarpCommand(String name, MyHomes loader) {
        super(name, "Visit public player warps", "");
        this.commandParameters.clear();

        this.usage = "§7/pwarp add : Creates warp with unique name\n" +
                "§7/pwarp <remove> <name> : Removes your unique warp\n" +
                "§7/pwarp <name> : Teleports to warp\n" +
                "§7/pwarp list : Lists all available warps";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", true)
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
            this.loader.showWarpMenu(player);
            return true;
        }

        switch (args[0]){
            case "add":
                new AddWarpForm(player, this.loader).buildForm().sendForm();
                break;
            case "remove":
                this.loader.deleteWarp(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                break;
            case "list":
                this.loader.showWarpMenu(player);
                break;
            default:
                this.loader.teleportToWarp(player, String.join(" ", args));
                break;
        }
        return true;
    }
}

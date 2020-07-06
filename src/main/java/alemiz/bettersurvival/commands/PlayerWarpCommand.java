package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;


public class PlayerWarpCommand extends Command {

    private final Home loader;

    public PlayerWarpCommand(String name, Home loader) {
        super(name, "Visit public player warps", "");
        this.commandParameters.clear();

        this.usage = "§7/pwarp <add> <name> : Creates warp with unique name\n" +
                "§7/pwarp <remove> <name> : Removes your unique warp\n" +
                "§7/pwarp <name> : Teleports to warp\n" +
                "§7/pwarp list : Lists all available warps";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", false)
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
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "add":
                this.loader.createWarp(player, args[1]);
                break;
            case "remove":
                this.loader.deleteWarp(player, args[1]);
                break;
            case "list":
                this.loader.listWarps(player);
                break;
            default:
                this.loader.teleportToWarp(player, args[0]);
                break;
        }
        return true;
    }
}

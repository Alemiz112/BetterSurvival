package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class TpaCommand extends Command {

    public MoreVanilla loader;

    public TpaCommand(String name, MoreVanilla loader) {
        super(name, "Player Teleportation", "");

        this.usage = "§7/tpa <player> : Send request to player\n" +
                "§7/tpa <a|accept> : Accept request and teleport player\n" +
                "§7/tpa <d|denny> : Deny players request";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player|accept|deny", false)
        });

        this.setPermission(loader.configFile.getString("permission-tpa"));
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
            player.sendMessage(getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "a":
            case "accept":
                loader.tpaAccept(player);
                break;
            case "d":
            case "denny":
                loader.tpaDenny(player);
                break;
            default:
                loader.tpa(player, args[0]);
        }
        return true;
    }
}

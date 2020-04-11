package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.EasterAddon;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class EggCommand extends Command {

    public EasterAddon loader;

    public EggCommand(String name, EasterAddon loader) {
        super(name, "Manage Easter Eggs", "");

        this.usage = "§7/egg spawn : Allows you to spawn egg when you place block\n" +
                "§7/egg remove : Removes all eater eggs from the world\n" +
                "§7/egg clear : Removes all found easter eggs from player config";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("option", CommandParamType.STRING, false)
        });

        this.setPermission(loader.configFile.getString("eggCommandPermission"));
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

        if (args.length < 1){
            sender.sendMessage(getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "spawn":
                this.loader.addSetter((Player) sender);
                break;
            case "remove":
                this.loader.removeAllEggs((Player) sender);
                break;
            case "clear":
                if (!((Player) sender).isOp()){
                    sender.sendMessage("§c»§7This command can be run only by OP user!");
                    break;
                }

                this.loader.clearFoundEggs();
                sender.sendMessage("§6»§7All found Easter Eggs were removed!");
                break;
            default:
                sender.sendMessage(getUsageMessage());
                break;
        }
        return true;
    }
}

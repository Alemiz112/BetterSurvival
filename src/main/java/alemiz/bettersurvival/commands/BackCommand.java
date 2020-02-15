package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class BackCommand extends Command {

    protected static final String usage = "§6Back Command:\n"+
            "§7/back : Teleports to your death point";


    public MoreVanilla loader;

    public BackCommand(String name, MoreVanilla loader) {
        super(name, "Back command", usage);
        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-back"));
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
        this.loader.back((Player) sender);
        return true;
    }
}

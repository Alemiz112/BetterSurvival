package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class VanishCommand extends Command {

    protected static final String usage = "§6Vanish Command:\n"+
            "§7/vanish : Turn on/off ";


    public Troller loader;

    public VanishCommand(String name, Troller loader) {
        super(name, "Allows the player to vanish", usage);
        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-vanish"));
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
        this.loader.vanish((Player) sender);
        return true;
    }
}

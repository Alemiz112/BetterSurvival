package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class KeepInvCommand extends Command {

    public MoreVanilla loader;

    public KeepInvCommand(String name, MoreVanilla loader) {
        super(name, "Turn on|off keep inventory", "");

        this.usage = "§7/keepinv : Turn on|off keep inventory";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
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
        boolean keep = this.loader.keepInventory(player);
        this.loader.setKeepInventory(player, !keep);
        return true;
    }
}

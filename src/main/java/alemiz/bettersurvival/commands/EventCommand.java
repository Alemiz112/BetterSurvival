package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.BetterLobby;
import alemiz.bettersurvival.utils.Command;
import alemiz.sgu.StarGateUniverse;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class EventCommand extends Command {

    public BetterLobby loader;

    public EventCommand(String name, BetterLobby loader) {
        super(name, "Teleports to event server", "");

        this.usage = "§7/event : Teleports to event server";
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

        StarGateUniverse.getInstance().transferPlayer((Player) sender, "event");
        return true;
    }
}

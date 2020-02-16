package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class JumpCommand extends Command {

    protected static final String usage = "§6Jump Command:\n"+
            "§7/jump : Super power jump";


    public MoreVanilla loader;

    public JumpCommand(String name, MoreVanilla loader) {
        super(name, "Jump command", usage);
        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-jump"));
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
        this.loader.jump((Player) sender);
        return true;
    }
}

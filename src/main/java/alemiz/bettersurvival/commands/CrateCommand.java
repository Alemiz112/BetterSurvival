package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.BetterVoting;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;

public class CrateCommand extends Command {

    public BetterVoting loader;

    public CrateCommand(String name, BetterVoting loader) {
        super(name, "Manage crates", "");

        this.usage = "§7/crate give <player> <count>: Give player crate key\n"+
                "§7/crate set: Touch crate chest to get coords\n";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-crateCommand"));
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
            case "give":
                if (args.length < 3){
                    sender.sendMessage(getUsageMessage());
                    break;
                }
                try {
                    this.loader.givekey((Player) sender, args[1], Integer.parseInt(args[2]));
                }catch (Exception e){
                    sender.sendMessage("§cPlease enter count as a number!");
                }
                break;
            case "set":
                this.loader.getCratePos((Player) sender);
                break;
        }
        return true;
    }
}

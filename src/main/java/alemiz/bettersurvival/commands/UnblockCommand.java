package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class UnblockCommand extends Command {

    public Troller loader;

    public UnblockCommand(String name, Troller loader) {
        super(name, "Release player from spawned blocks", "", new String[]{"free"});

        this.usage = "§7/unblock <player> : Release player from spawned blocks";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
        });


        this.setPermission(loader.configFile.getString("permission-block"));
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

        this.loader.unblock((Player) sender, args[0]);
        return true;
    }
}

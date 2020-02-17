package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class UnblockCommand extends Command {

    protected static final String usage = "§6Unblock Command:\n" +
            "§7/unblock <player> : Release player from spawned blocks\n"+
            "§7/block <player> <block id>: Troll player and spawn blocks around him";


    public Troller loader;

    public UnblockCommand(String name, Troller loader) {
        super(name, "Unblock command", usage, new String[]{"free"});
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
            sender.sendMessage(usage);
            return true;
        }

        this.loader.unblock((Player) sender, args[0]);
        return true;
    }
}

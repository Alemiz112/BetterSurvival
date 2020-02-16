package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class BlockCommand extends Command {

    protected static final String usage = "§6Block Command:\n"+
            "§7/block <player> <block id>: Troll player and spawn blocks around him\n" +
            "§7/unblock <player> : Release player from spawned blocks";


    public Troller loader;

    public BlockCommand(String name, Troller loader) {
        super(name, "Block command", usage);
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET,false),
                new CommandParameter("block", CommandParamType.INT, false)
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

        if (args.length < 2){
            sender.sendMessage(usage);
            return true;
        }

        int blockId = 0;
        try {
            blockId = Integer.parseInt(args[1]);
        }catch (Exception e){
            sender.sendMessage("§cPlease enter numerical block id!");
            return true;
        }
        this.loader.block((Player) sender, args[0], blockId);
        return true;
    }
}

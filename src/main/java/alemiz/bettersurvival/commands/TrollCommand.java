package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrollCommand extends Command {

    public Troller loader;

    public TrollCommand(String name, Troller loader) {
        super(name, "Collection of troll commands", "");

        this.usage = "§7/vanish : Allows the player to vanish\n"+
                "§7/block <player> <block id>: Troll player and spawn blocks around him\n" +
                "§7/unblock <player> : Release player from spawned blocks\n" +
                "§7/troll anvil <player> : Now who could have put that up there?\n" +
                "§7/troll chat <player> <message> : Chat or run a command as a player using their own permissions\n" +
                "§7/troll rainbow <player> : Spawn chaotic wool around player!\n" +
                "§7/troll lava <player> : Learn your friend to swim in lava!";
        this.setUsage(getUsageMessage());

        this.ignoreInHelpTexts = true;

        this.commandParameters.clear();
        this.setPermission(loader.configFile.getString("permission-troll"));
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

        Player player = (Player) sender;
        switch (args[0].toLowerCase()){
            case "anvil":
                if (args.length < 2){
                    player.sendMessage("§7/troll anvil <player> : Now who could have put that up there?");
                    break;
                }
                this.loader.anvil(player, args[1]);
                break;
            case "chat":
                if (args.length < 3){
                    player.sendMessage("§7/troll chat <player> <message> : Chat or run a command as a player using their own permissions");
                    break;
                }

                List<String> messageArgs = new ArrayList<>(Arrays.asList(args));
                messageArgs.remove(args[0]);
                messageArgs.remove(args[1]);

                this.loader.chat(player, args[1], String.join(" ", messageArgs));
                break;
            case "rainbow":
                if (args.length < 2){
                    player.sendMessage("§7/troll rainbow <player> : Spawn chaotic wool around player!");
                    break;
                }
                this.loader.rainbowFloor(player, args[1]);
                break;
            case "lava":
                if (args.length < 2){
                    player.sendMessage("§7/troll lava <player> : Learn your friend to swim in lava!");
                    break;
                }
                this.loader.fakeLavaHole(player, args[1]);
                break;
            default:
                sender.sendMessage(getUsageMessage());
                break;
        }
        return true;
    }
}

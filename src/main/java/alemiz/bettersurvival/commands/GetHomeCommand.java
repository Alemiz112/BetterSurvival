package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.Set;

public class GetHomeCommand extends Command {

    public Home loader;

    public GetHomeCommand(String name, Home loader) {
        super(name, "Prints your homes", "", new String[]{"listhome"});
        this.commandParameters.clear();

        this.usage = "§7/gethome: Prints your homes";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });

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

        Set<String> homes = loader.getHomes(player);
        if (homes.isEmpty()){
            player.sendMessage("§6»§7You dont have any homes yet!");
            return true;
        }

        String format = "";
        for (String home : homes){
            format += home+", ";
        }

        player.sendMessage("§6»§7Your homes: "+format.substring(0, format.length()-2));
        return true;
    }
}

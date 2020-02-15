package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import java.util.List;

public class NearCommand extends Command {

    protected static final String usage = "§6Near Command:\n"+
            "§7/near <radius - optional>: List players who are nearby";


    public MoreVanilla loader;

    public NearCommand(String name, MoreVanilla loader) {
        super(name, "Near command", usage);
        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-near"));
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

        int radius = 8;
        if (args.length >= 1){
            try {
                radius = Integer.parseInt(args[0]);
            }catch (Exception e){
                player.sendMessage("§cPlease enter numerical radius!");
                return true;
            }
        }

        List<Player> players = this.loader.getNearPlayers(player.clone(), radius);
        players.remove(player);

        String pplayers = "";
        for (Player pplayer : players){
            if (pplayer.getName().equals(player.getName())) continue;
            pplayers += pplayer.getName() + ", ";
        }

        String message = this.loader.configFile.getString("nearMessage");
        message = message.replace("{players}", (pplayers.equals("")? "Not found" : pplayers.substring(0, pplayers.length()-2)));
        player.sendMessage(message);
        return true;
    }
}

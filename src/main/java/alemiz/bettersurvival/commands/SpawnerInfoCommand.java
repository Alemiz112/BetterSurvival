package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SpawnerInfoCommand extends Command {

    public BetterEconomy loader;

    public SpawnerInfoCommand(String name, BetterEconomy loader) {
        super(name, "Shows spawner info", "");

        this.usage = "§7/spawnerinfo : Shows spawner info";
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
        player.sendMessage("§6»§7Touch spawner to get its info!");
        this.loader.economySpawners.addSpawnInfoPlayer(player);
        return true;
    }
}

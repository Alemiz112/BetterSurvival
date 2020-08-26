package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SpawnerCommand extends Command {

    public BetterEconomy loader;

    public SpawnerCommand(String name, BetterEconomy loader) {
        super(name, "Buy spawner upgrades", "");

        this.usage = "§7/spawnerup : Buy spawner upgrades";
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

        this.loader.economySpawners.sendSpawnerShopForm((Player) sender);
        return true;
    }
}

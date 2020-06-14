package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SellAllCommand extends Command {

    public SurvivalShop loader;

    public SellAllCommand(String name, SurvivalShop loader) {
        super(name, "Sell all items in your inventory", "");

        this.usage = "§7/sellall : Sell all items in your inventory";
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

        this.loader.getSellManager().sellAll((Player) sender);
        return true;
    }
}

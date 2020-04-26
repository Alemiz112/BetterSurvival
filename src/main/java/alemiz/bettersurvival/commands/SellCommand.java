package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SellCommand extends Command {

    public SurvivalShop loader;

    public SellCommand(String name, SurvivalShop loader) {
        super(name, "Opens sell menu", "");

        this.usage = "§7/sell : Open sell menu";
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

        this.loader.getSellManager().sendForm((Player) sender);
        return true;
    }
}

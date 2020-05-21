package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;

public class EnchCommand extends Command {

    public SmithShop loader;

    public EnchCommand(String name, SmithShop loader) {
        super(name, "Enchant your item", "");

        this.usage = "§7/ench : Apply all possible enchants";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.getLoader().configFile.getString("enchantPermission"));
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
        Item item = this.loader.enchantItem(player, player.getInventory().getItemInHand());
        if (item != null) player.getInventory().setItemInHand(item);
        return true;
    }
}

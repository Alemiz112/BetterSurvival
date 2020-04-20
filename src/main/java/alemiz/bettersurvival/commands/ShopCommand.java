package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class ShopCommand extends Command {

    public SurvivalShop loader;

    public ShopCommand(String name, SurvivalShop loader) {
        super(name, "Teleports to shop", "");

        this.usage = "§7/shop : Teleports to shop";
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

        if (args.length <= 0){
            loader.teleportToSpawn(player);
            player.sendMessage("§6»§7You was teleported to shop!");
            return true;
        }

        if (!player.hasPermission(this.loader.configFile.getString("shopCreatePermission"))){
            player.sendMessage("§c»§7You dont have permission to create shop!");
            return true;
        }

        switch (args[0]){
            case "set":
                this.loader.setShopSpawn(player);
                player.sendMessage("§6»§7Shop spawn was saved!");
                break;
            default:
                player.sendMessage("§7/shop : Teleports to shop\n§7/shop set : set shop spawn");
                break;
        }
        return true;
    }
}

package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.myland.LandRegion;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;

public class LandCommand extends Command {

    public MyLandProtect loader;

    public LandCommand(String name, MyLandProtect loader) {
        super(name, "Protect your area", "");

        this.usage = "§7/land <wand> : Get wand to select positions\n" +
                "§7/land <create|add> <land>: Create land after selected positions\n" +
                "§7/land <remove|del> <land>: Deny players request\n"+
                "§7/land <whitelist> <add|remove|list> <land> <player> : Manage lands whitelist\n" +
                "§7/land <flow> <land> <on|off> : Allow water and lava flow in land\n"+
                "§7/land <here> : Shows area where you are\n" +
                "§7/land <list> : Shows your lands";
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

        if (args.length < 1){
            player.sendMessage(getUsageMessage());
            return true;
        }

        LandRegion region;

        switch (args[0]){
            case "wand":
                Item item = new Item(Item.WOODEN_AXE, 0, 1);
                item.setCustomName(MyLandProtect.WAND);
                item.addEnchantment(Enchantment.get(Enchantment.ID_EFFICIENCY));

                player.getInventory().addItem(item);
                player.sendMessage(loader.configFile.getString("landSetPos"));
                break;
            case "create":
            case "add":
                if (args.length < 2){
                    player.sendMessage(getUsageMessage());
                    break;
                }

                this.loader.createLand(player, args[1]);
                break;
            case "remove":
            case "del":
                if (args.length < 2){
                    player.sendMessage(getUsageMessage());
                    break;
                }

                this.loader.removeLand(player, args[1]);
                break;
            case "flow":
                if (args.length < 3){
                    player.sendMessage(getUsageMessage());
                    break;
                }

                region = this.loader.getLand(player, args[1]);
                if (region == null){
                    this.loader.regionNotFound(player);
                    break;
                }

                boolean state = args[2].equalsIgnoreCase("on");
                region.setLiquidFlow(state);
                this.loader.waterFlowMessage(player, region.getName(), state);
                break;
            case "here":
                this.loader.findLand(player);
                break;
            case "whitelist":
                if (args.length >= 4){
                    region = this.loader.getLand(player, args[2]);
                    this.loader.whitelist(player, args[3], region, args[1]);
                    break;
                }
                if (args.length == 3 && args[1].equals(LandRegion.WHITELIST_LIST)){
                    region = this.loader.getLand(player, args[2]);
                    this.loader.whitelist(player, "", region, args[1]);
                }else {
                    player.sendMessage(getUsageMessage());
                }
                break;
            case "list":
                this.loader.listLands(player);
                break;
            default:
                player.sendMessage(getUsageMessage());
                break;
        }
        return true;
    }
}

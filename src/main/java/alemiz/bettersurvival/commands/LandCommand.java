package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.myland.MyLandProtect;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;

public class LandCommand extends Command {

    protected static final String usage = "§6Land Command:\n"+
            "§7/land <wand> : Get wand to select positions\n" +
            "§7/land <create|add> : Create land after selected positions\n" +
            "§7/land <remove|del> : Deny players request";


    public MyLandProtect loader;

    public LandCommand(String name, MyLandProtect loader) {
        super(name, "Protect your home", usage);
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
            player.sendMessage(usage);
            return true;
        }

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
                    player.sendMessage(usage);
                    return true;
                }

                this.loader.createLand(player, args[1]);
                break;
            case "remove":
            case "del":
                if (args.length < 2){
                    player.sendMessage(usage);
                    return true;
                }

                this.loader.removeLand(player, args[1]);
                break;
        }
        return true;
    }
}

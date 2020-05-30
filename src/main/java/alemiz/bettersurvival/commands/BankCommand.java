package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;

public class BankCommand extends Command {

    public BetterEconomy loader;

    public BankCommand(String name, BetterEconomy loader) {
        super(name, "Manage your bank account", "");

        this.usage = "§7/bank note <price> : Creates bank note with own money value\n" +
                "§7/bank apply : Applies note from your hand to bank balance";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, true)
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

        if (args.length < 1){
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "note":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    break;
                }

                try {
                    int value = Integer.parseInt(args[1]);
                    this.loader.createNote(player, value);
                }catch (NumberFormatException e){
                    player.sendMessage("§c»§7Please provide numerical value!");
                    break;
                }
                break;
            case "apply":
                Item item = player.getInventory().getItemInHand();
                if (item.getId() == Item.AIR){
                    player.sendMessage("§c»§r§7You must hold Bank Note item!");
                    break;
                }

                this.loader.applyNote(player, item);
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }
}

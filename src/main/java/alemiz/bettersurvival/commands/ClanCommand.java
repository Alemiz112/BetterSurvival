package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.utils.Command;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

import java.util.Arrays;
import java.util.List;

public class ClanCommand extends Command {

    public PlayerClans loader;

    public ClanCommand(String name, PlayerClans loader) {
        super(name, "All clan commands in one place", "");

        this.usage = "§7/clan create <name> : Creates new clan\n" +
                "§7/clan invite <player> : Invites player to your clan\n" +
                "§7clan kick <player> : Kicks player from your clan\n" +
                "§7/clan invitations : Lists all pending invitations\n" +
                "§7clan accept <clan - rawName> : Accept invitation from clan\n" +
                "§7/clan deny <clan - rawName> : Deny invitation from clan\n" +
                "§aYou can use clan chat by starting message with §6%§a!";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, false),
                new CommandParameter("sub-action", CommandParamType.STRING, true)
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
        String clanName;
        String playerName;
        Clan clan;

        if (args.length < 1){
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "create":
                 clanName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                this.loader.createClan(player, clanName);
                break;
            case "invite":
                playerName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                this.loader.invite(playerName, player);
                break;
            case "kick":
                playerName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                clan = this.loader.getClan(player);

                if (clan == null){
                    player.sendMessage("§c»§7You are not in any clan!");
                    break;
                }
                clan.kickPlayer(playerName, player);
                break;
            case "invitations":
                this.loader.sendInvitationsMessage(player);
                break;
            case "accept":
                clan = this.loader.getClan(player);
                if (clan != null){
                    player.sendMessage("§c»§7You are already in clan! If you want to switch clan, leave your clan first!");
                    break;
                }

                List<String> pendingInvites = this.loader.getInvitations(player);
                if (!pendingInvites.contains(args[1])){
                    player.sendMessage("§c»§7No invitation from §6"+args[1]+"§7 was found! Please ensure that you have entered right raw name.");
                    break;
                }

                clan = this.loader.getClans().get(args[1]);
                if (clan == null){
                    player.sendMessage("§c»§7This clan does no longer exists!");
                    break;
                }

                this.loader.clearInvitations(player);
                clan.addPlayer(player);
                break;
            case "deny":
                Config config = ConfigManager.getInstance().loadPlayer(player);
                List<String> invites = config.getStringList("clanInvites");

                if (!invites.remove(args[1])){
                    player.sendMessage("§c»§7No invitation from §6"+args[1]+"§7 was found! Please ensure that you have entered right raw name.");
                    break;
                }

                config.set("clanInvites", invites);
                config.save();

                player.sendMessage("§6»§7You have denied invitation from clan §6"+args[1]+"§7!");
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }

}

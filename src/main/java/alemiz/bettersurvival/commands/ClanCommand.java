package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Command;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cubemc.nukkit.connector.modules.Money;

import java.util.Arrays;
import java.util.List;

public class ClanCommand extends Command {

    public PlayerClans loader;

    public ClanCommand(String name, PlayerClans loader) {
        super(name, "All clan commands in one place", "");

        this.usage = "§7/clan create <name> : Creates new clan\n" +
                "§7/clan invite <player> : Invites player to your clan\n" +
                "§7/clan kick <player> : Kicks player from your clan\n" +
                "§7/clan invitations : Lists all pending invitations\n" +
                "§7/clan accept <clan - rawName> : Accept invitation from clan\n" +
                "§7/clan deny <clan - rawName> : Deny invitation from clan\n" +
                "§7/clan leave : Leave current clan\n" +
                "§7/clan destroy : Destroy your clan. All clan values will be lost!\n" +
                "§7/clan info : Shows info about your clan\n" +
                "§7/clan admin add <player> : Adds admin to your clan\n" +
                "§7/clan admin remove <player> : Removes admin from your clan\n" +
                "§7/clan bank note <value> : Creates bank note signed by your clan\n" +
                "§7/clan bank apply : Applies note from your hand to clan bank\n" +
                "§7/clan bank donate <value> : Gives money to clan bank\n" +
                "§7/clan bank status: Shows your clan bank status\n" +
                "§7/clan land create: Creates clan land\n" +
                "§7/clan land remove: Removes clan land\n" +
                "§7/clan home <create|remove|home>: Classic clan homes\n" +
                "§7/clan listhome : Lists all homes\n" +
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
        Config config;

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

                config = ConfigManager.getInstance().loadPlayer(player);
                List<String> pendingInvites = config.getStringList("clanInvites");

                if (!pendingInvites.remove(args[1])){
                    player.sendMessage("§c»§7No invitation from §6"+args[1]+"§7 was found! Please ensure that you have entered right raw name.");
                    break;
                }

                clan = this.loader.getClans().get(args[1]);
                if (clan == null){
                    player.sendMessage("§c»§7This clan does no longer exists!");
                    config.set("clanInvites", pendingInvites);
                    config.save();
                    break;
                }

                this.loader.clearInvitations(player);
                clan.addPlayer(player);
                break;
            case "deny":
                config = ConfigManager.getInstance().loadPlayer(player);
                List<String> invites = config.getStringList("clanInvites");

                if (!invites.remove(args[1])){
                    player.sendMessage("§c»§7No invitation from §6"+args[1]+"§7 was found! Please ensure that you have entered right raw name.");
                    break;
                }

                config.set("clanInvites", invites);
                config.save();

                player.sendMessage("§6»§7You have denied invitation from clan §6"+args[1]+"§7!");
                break;
            case "info":
                clan = this.checkForClan(player);
                if (clan == null) break;

                player.sendMessage(clan.buildTextInfo());
                break;
            case "leave":
                clan = this.checkForClan(player);
                if (clan == null) break;

                clan.removePlayer(player);
                break;
            case "destroy":
                clan = this.checkForClan(player);
                if (clan == null) break;

                this.loader.destroyClan(player, clan);
                break;
            case "admin":
                if (args.length < 3){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "add":
                        clan.addAdmin(args[2], player);
                        break;
                    case "remove":
                        clan.removeAdmin(args[2], player);
                        break;
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;
                }
                break;
            case "bank":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "status":
                        player.sendMessage("§a"+clan.getName()+"§a Clan:\n§3»§7 Money: §e"+clan.getMoney()+"§7/§6"+clan.getMaxMoney()+"$");
                    break;
                    case "note":
                        try {
                            clan.createBankNote(player, Integer.parseInt(args[2]));
                        }catch (Exception e){
                            player.sendMessage("§c»§7Please provide numerical value!");
                        }
                        break;
                    case "apply":
                        clan.applyBankNote(player);
                        break;
                    case "donate":
                        try {
                            int value = Integer.parseInt(args[2]);
                            boolean success = (Money.getInstance().getMoney(player) - value) >= 0;

                            if (!success){
                                player.sendMessage("§c»§7You do not have enough coins to donate!");
                                break;
                            }

                            if (!clan.addMoney(value)){
                                player.sendMessage("§c»§7Clan bank limit has been reached!");
                                break;
                            }

                            Money.getInstance().reduceMoney(player, value);
                            clan.onDonate(player, value);
                        }catch (Exception e){
                            player.sendMessage("§c»§7Please provide numerical value!");
                        }
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;
                }
                break;
            case "land":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "create":
                        clan.createLand(player);
                        break;
                    case "remove":
                        clan.removeLand(player);
                        break;
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;

                }
                break;
            case "home":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "create":
                    case "add":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            return true;
                        }

                        clan.createHome(player, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                        break;
                    case "remove":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            return true;
                        }

                        clan.removeHome(player, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                        break;
                    default:
                        clan.teleportToHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        break;
                }
                break;
            case "listhome":
                clan = this.checkForClan(player);
                if (clan == null) break;

                int homeLimit = clan.getConfig().getInt("homeLimit");
                player.sendMessage("§a"+clan.getName()+"§a Clan:\n" +
                        "§3»§7 Homes: §a"+clan.getHomes().size()+"§7/§2"+homeLimit+"\n" +
                        "§3»§7Home List: §e"+String.join(", ", clan.getHomes().keySet()));
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }

    private Clan checkForClan(Player player){
        Clan clan = this.loader.getClan(player);
        if (clan == null){
            player.sendMessage("§c»§7You are not in any clan!");
            return null;
        }
        return clan;
    }

}

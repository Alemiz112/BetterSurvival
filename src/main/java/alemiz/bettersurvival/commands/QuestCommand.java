package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.quests.SurvivalQuests;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class QuestCommand extends Command {

    public SurvivalQuests loader;

    public QuestCommand(String name, SurvivalQuests loader) {
        super(name, "Visit QuestMaster", "");
        this.usage = "§7/quest : Visit QuestMaster";
        this.setUsage(this.getUsageMessage());

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
            this.loader.teleportToSpawn(player);
            return true;
        }

        if (!player.hasPermission(this.loader.configFile.getString("questManagePermission"))){
            player.sendMessage("§c»§7You dont have permission to configure quests!");
            return true;
        }

        switch (args[0]){
            case "setup":
                this.loader.setQuestMasterSpawn(player);
                player.sendMessage("§6»§7Shop spawn was saved!");
                break;
            case "spawn":
                this.loader.spawnQuestMaster(player);
                break;
            default:
                player.sendMessage("§6Quests Commands:\n" +
                        "§7/quest : Visit QuestMaster\n"+
                        "§7/quest spawn : Spawn QuestMaster\n" +
                        "§7/quest setup : Save quest spawn position");
                break;
        }
        return true;
    }
}

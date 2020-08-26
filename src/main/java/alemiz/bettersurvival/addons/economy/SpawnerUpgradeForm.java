package alemiz.bettersurvival.addons.economy;

import alemiz.bettersurvival.utils.form.CustomForm;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.item.Item;
import cubemc.nukkit.connector.modules.Money;

import java.util.ArrayList;
import java.util.List;

public class SpawnerUpgradeForm extends CustomForm {

    private final transient EconomySpawners loader;
    private final transient List<SpawnerLevel> levels = new ArrayList<>();

    public SpawnerUpgradeForm(Player player, EconomySpawners loader){
        super("§l§8Spawner Upgrades");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        StringBuilder builder = new StringBuilder("Upgrade your spawner and decrease mob spawn time!\n" +
                "Default interval: §l§a"+EconomySpawners.DEFAULT_LEVEL.getAverageDelay()+" §rseconds\n");
        List<String> levels = new ArrayList<>();

        for (SpawnerLevel level : this.loader.getSpawnerLevels().values()){
            if (level == EconomySpawners.DEFAULT_LEVEL){
                continue;
            }

            builder.append("Level ").append(level.getLevel()).append(": §l§e").append(level.getAverageDelay()).append(" §rseconds\n");
            levels.add("§5Level "+level.getLevel()+" §7Price: §8"+level.getPrice()+"$");
            this.levels.add(level);
        }
        this.addElement(new ElementLabel(builder.toString()));
        this.addElement(new ElementDropdown("Select spawner level:", levels));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null || this.getResponse().getDropdownResponse(1) == null) return;
        int levelId = this.getResponse().getDropdownResponse(1).getElementID();

        if (this.levels.size() <= levelId) return;
        SpawnerLevel level = this.levels.get(levelId);

        boolean success = Money.getInstance().reduceMoney(player, level.getPrice());
        if (!success){
            player.sendMessage("§c»§7You dont have §e"+level.getPrice()+"§7 coins to buy §6spawner upgrade§7!");
            return;
        }

        Item item = this.loader.buildSpawnerUpgrade(player.getName(), level.getLevel());
        if (player.getInventory().canAddItem(item)){
            player.getInventory().addItem(item);
        }else {
            player.getLevel().dropItem(player, item);
        }

        String message = this.loader.loader.configFile.getString("spawnerUpgradeBuyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{level}", String.valueOf(level.getLevel()));
        player.sendMessage(message);
    }
}

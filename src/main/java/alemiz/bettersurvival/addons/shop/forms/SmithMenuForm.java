package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

public class SmithMenuForm extends SimpleForm {

    private final transient SmithShop loader;

    public SmithMenuForm(Player player, SmithShop loader){
        super("§l§8Smith the Man", "§7Select one of mine tools!");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addButton(new ElementButton("§aRename Item\n§7»Click to open!"));
        this.addButton(new ElementButton("§eBuy Enchants\n§7»Click to open!"));
        this.addButton(new ElementButton("§6Repair Item\n§7»Click to open!"));

        Addon addon = Addon.getAddon(BetterEconomy.class);
        if ((addon instanceof BetterEconomy) && addon.configFile.getBoolean("economySpawners")){
            this.addButton(new ElementButton("§3Spawner Upgrades\n§7»Click to open!"));
        }
        this.addButton(new ElementButton("§cHelp\n§7»Click to open!"));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        int response = this.getResponse().getClickedButtonId();

        switch (response){
            case 0:
                this.loader.sendRenameForm(player);
                break;
            case 1:
                this.loader.sendEnchantsForm(player);
                break;
            case 2:
                this.loader.sendRepairForm(player);
                break;
            case 3:
                Addon addon = Addon.getAddon(BetterEconomy.class);
                if ((addon instanceof BetterEconomy) && addon.configFile.getBoolean("economySpawners")){
                    BetterEconomy betterEconomy = (BetterEconomy) addon;
                    betterEconomy.economySpawners.sendSpawnerShopForm(player);
                    break;
                }
            case 4:
                this.loader.sendHelpForm(player);
                break;
        }
    }
}

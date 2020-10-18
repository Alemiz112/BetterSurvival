package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.Enchant;
import alemiz.bettersurvival.addons.shop.SmithShop;
import cubemc.commons.nukkit.utils.forms.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.item.Item;
import cubemc.nukkit.connector.modules.Money;

public class SmithEnchantLevelForm extends SimpleForm {

    private final transient SmithShop loader;
    private final transient Enchant enchant;

    public SmithEnchantLevelForm(Player player, Enchant enchant, SmithShop loader){
        super();
        this.player = player;
        this.enchant = enchant;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Levels of "+this.enchant.getFormattedName());
        this.setContent("§7Please select enchantment level.");

        for (int i = 0; i < this.enchant.prices.length; i++){
            int level = i+1;
            this.addButton(new ElementButton("§fLevel: "+level+"§7 Price: §8"+this.enchant.getPrice(level)+"$\n§7»Click to Buy!"));
        }

        this.addButton(new ElementButton("Exit"));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null || this.getResponse().getClickedButton().getText().equals("Exit")) return;
        int level = this.getResponse().getClickedButtonId()+1;

        if (level > this.enchant.prices.length){
            player.sendMessage("§c»§7You chose highest level than maximum allowed!");
            return;
        }

        int price = enchant.getPrice(level);
        boolean success = Money.getInstance().reduceMoney(player, price);

        if (success){
            Item item = enchant.getEnchantItem(level);
            player.getInventory().addItem(item);
        }

        String message = this.loader.getLoader().messageFormat(player, (success? "messageSuccess" : "messageFail"), price);
        message = message.replace("{item}", this.enchant.getFormattedName()+" Enchant");
        player.sendMessage(message);
    }
}

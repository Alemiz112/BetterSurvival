package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.form.CustomForm;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;

import java.util.Arrays;

public class ShopItemForm extends CustomForm {

    private final transient SurvivalShop loader;
    private final transient ShopItem item;

    public ShopItemForm(Player player, ShopItem item, SurvivalShop loader){
        super("§l§8Buy "+item.getFormattedName());
        this.player = player;
        this.item = item;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addElement(new ElementLabel("§7One package contains §8"+this.item.getCount()+"x "+this.item.getFormattedName()+"§r§7!"));
        this.addElement(new ElementStepSlider("§7Select package count", Arrays.asList("1", "2", "3","4", "6", "8", "10", "12", "16"), 0));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        int stackCount = Integer.parseInt(this.getResponse().getStepSliderResponse(1).getElementContent());

        boolean success = item.buyItem(player, stackCount);
        String message = this.loader.messageFormat(player, (success? "messageSuccess" : "messageFail"), item.getStackPrice(stackCount));
        message = message.replace("{item}", item.getName());
        player.sendMessage(message);
    }
}

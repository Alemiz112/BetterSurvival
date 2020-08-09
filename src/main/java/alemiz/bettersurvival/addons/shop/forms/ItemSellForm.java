package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.ShopCategory;
import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.utils.form.CustomForm;
import alemiz.bettersurvival.utils.form.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.item.Item;
import cubemc.nukkit.connector.modules.Money;

import java.util.Arrays;

public class ItemSellForm extends CustomForm {

    private final transient ShopCategory category;

    public ItemSellForm(Player player, ShopCategory category){
        super();
        this.player = player;
        this.category = category;
    }

    @Override
    public Form buildForm() {
        this.setTitle("§l§8Sell "+category.getCategoryName().toUpperCase());
        this.addElement(new ElementLabel("§7Please choose item you want to sell."));

        ElementDropdown dropdown = new ElementDropdown("");
        for (ShopItem item : category.getItems()){
            dropdown.addOption("§5"+item.getFormattedName()+" §8Offer: §l"+item.getSellPrice()+"$");
        }
        this.addElement(dropdown);
        this.addElement(new ElementStepSlider("§7Item count", Arrays.asList("1", "2", "5", "10", "20", "40", "64")));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;

        int itemIndex = this.getResponse().getDropdownResponse(1).getElementID();
        int count = Integer.parseInt(this.getResponse().getStepSliderResponse(2).getElementContent());

        ShopItem shopItem = this.category.getItem(itemIndex);
        if (shopItem == null){
            player.sendMessage("§c»§7Unable to sell item. Item not found!");
            return;
        }

        Item item = shopItem.getItemSample();
        item.setCount(count);

        if (!player.getInventory().contains(item)){
            player.sendMessage("§c»§7Unable to sell item. You do not own this item!");
            return;
        }

        player.getInventory().removeItem(item);

        Money.getInstance().addMoney(player, count * shopItem.getSellPrice());
        player.sendMessage("§a»§7"+item.getName()+" was sold successfully!");
    }
}

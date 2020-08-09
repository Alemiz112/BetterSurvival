package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.ShopCategory;
import alemiz.bettersurvival.addons.shop.ShopItem;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cubemc.nukkit.connector.modules.Money;

public class ShopItemListForm extends SimpleForm {

    private final transient ShopCategory category;

    public ShopItemListForm(Player player, ShopCategory category) {
        super();
        this.player = player;
        this.category = category;
    }

    @Override
    public Form buildForm() {
        String categoryName = this.category.getCategoryName();
        this.setTitle("§l§8Shop " + categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1));
        this.setContent("§7Pickup item by your own choice. Your coins: §8" + Money.getInstance().getMoney(player, false) + "$");

        for (ShopItem item : this.category.getItems()) {
            ElementButton button = new ElementButton("§5" + item.getFormattedName() + "\n§7Package: §8" + item.getCount() + " §7Price: §8" + item.getPrice() + "$");

            if (item.canUseImage()) {
                String image = item.getCustomImage();
                if (image.equals("")) {
                    image = "textures/items/" + item.buildItem().getName().replace(" ", "_").toLowerCase();
                }
                button.addImage(new ElementButtonImageData(item.getImageType(), image));
            }
            this.addButton(button);
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        ShopItem item = this.category.getItem(this.getResponse().getClickedButtonId());

        if (item != null) {
            new ShopItemForm(player, item, this.category.getLoader()).buildForm().sendForm();
        }
    }
}

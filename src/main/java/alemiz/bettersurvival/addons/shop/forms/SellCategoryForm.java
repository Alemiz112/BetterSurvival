package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.SellManager;
import alemiz.bettersurvival.addons.shop.ShopCategory;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class SellCategoryForm extends SimpleForm {

    private final transient SellManager sellManager;
    private transient List<ShopCategory> categories;

    public SellCategoryForm(Player player, SellManager sellManager){
        super("§l§8Sell Items", "§7Please select category.");
        this.player = player;
        this.sellManager = sellManager;
    }

    @Override
    public Form buildForm() {
        this.addButton(new ElementButton("§fSell All"));

        this.categories = new ArrayList<>(this.sellManager.getLoader().getCategories().values());
        for (ShopCategory category : this.categories){
            this.addButton(new ElementButton("§5"+category.getCategoryName().toUpperCase()+"\n§7»Click to Open"));
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;

        if (this.getResponse().getClickedButton().getText().equals("§fSell All")){
            this.sellManager.sellAll(player);
            return;
        }

        int response = this.getResponse().getClickedButtonId()-1;
        if (response > this.categories.size()){
            player.sendMessage("§c»§7Please choose right category!");
            return;
        }

        ShopCategory category = this.categories.get(response);
        if (category != null){
            this.sellManager.sendSellForm(category, player);
        }
    }
}

package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

import java.util.Arrays;
import java.util.Collection;

public class SellManager {

    private SurvivalShop loader;

    public SellManager(SurvivalShop loader){
        this.loader = loader;
    }

    public void sendForm(Player player){
        FormWindowSimple form = new FormWindowSimple("§l§8Sell Items", "§7Please select category.");

        Collection<ShopCategory> categories = this.loader.getCategories().values();
        for (ShopCategory category : categories){
            form.addButton(new ElementButton("§5"+category.getCategoryName().toUpperCase()+"\n§7»Click to Open"));
        }

        player.showFormWindow(form);
    }

    public void sendSellForm(ShopCategory category, Player player){
        FormWindowCustom form = new FormWindowCustom("§l§8Sell "+category.getCategoryName().toUpperCase());
        form.addElement(new ElementLabel("§7Please choose item you want to sell."));

        ElementDropdown dropdown = new ElementDropdown("");
        for (ShopItem item : category.getItems()){
            dropdown.addOption("§5"+item.getItemSample().getName()+" §8Offer: §l"+item.getSellPrice()+"$");
        }

        form.addElement(dropdown);
        form.addElement(new ElementStepSlider("§7Item count", Arrays.asList("1", "2", "5", "10", "20", "40", "64")));

        player.showFormWindow(form);
    }

    public void handleForm(FormWindowSimple form, Player player){
        if (form == null || player == null || form.getResponse() == null) return;

        String response = form.getResponse().getClickedButton().getText();
        String categoryName = response.split("\n")[0].substring(2).toLowerCase();
        ShopCategory category = this.loader.getCategory(categoryName);

        if (category == null) return;

        this.sendSellForm(category, player);
    }

    public void handleSellForm(FormWindowCustom form, Player player){
        if (form == null || player == null || form.getResponse() == null) return;

        String categoryName = form.getTitle().substring(9).toLowerCase();
        ShopCategory category = this.loader.getCategory(categoryName);

        int itemIndex = form.getResponse().getDropdownResponse(1).getElementID();
        int count = Integer.parseInt(form.getResponse().getStepSliderResponse(2).getElementContent());

        ShopItem shopItem = category.getItem(itemIndex);
        if (shopItem == null){
            player.sendMessage("§c»§7Unable to shell item. Item not found!");
            return;
        }

        Item item = shopItem.getItemSample();
        item.setCount(count);

        if (!player.getInventory().contains(item)){
            player.sendMessage("§c»§7Unable to shell item. You do not own this item!");
            return;
        }

        player.getInventory().removeItem(item);

        EconomyAPI.getInstance().addMoney(player, count * shopItem.getSellPrice());
        player.sendMessage("§a»§7"+item.getName()+" was sold successfully!");
    }
}

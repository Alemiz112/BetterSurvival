package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class SellManager {

    private SurvivalShop loader;

    public SellManager(SurvivalShop loader){
        this.loader = loader;
    }

    public void sendForm(Player player){
        FormWindowSimple form = new FormWindowSimple("§l§8Sell Items", "§7Please select category.");
        form.addButton(new ElementButton("§fSell All"));

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
            dropdown.addOption("§5"+item.getFormattedName()+" §8Offer: §l"+item.getSellPrice()+"$");
        }

        form.addElement(dropdown);
        form.addElement(new ElementStepSlider("§7Item count", Arrays.asList("1", "2", "5", "10", "20", "40", "64")));

        player.showFormWindow(form);
    }

    public void handleForm(FormWindowSimple form, Player player){
        if (form == null || player == null || form.getResponse() == null) return;

        String response = form.getResponse().getClickedButton().getText();
        if (response.equals("§fSell All")){
            this.sellAll(player);
            return;
        }

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

        EconomyAPI.getInstance().addMoney(player, count * shopItem.getSellPrice());
        player.sendMessage("§a»§7"+item.getName()+" was sold successfully!");
    }

    private Integer sellItem(Player player, Item item){
        if (player == null || item == null) return null;
        ShopItem shopItem = null;

        for (ShopCategory category : this.loader.getCategories().values()){
            for (ShopItem sshopItem : category.getItems()){
                if (!item.equals(sshopItem.getItemSample(), true, false)) continue;
                shopItem = sshopItem;
                break;
            }
        }

        if (shopItem == null) return null;
        int price = item.getCount() * shopItem.getSellPrice();

        player.getInventory().removeItem(item);
        return price;
    }

    public void sellAll(Player player){
        if (player == null) return;
        int totalPrice = 0;

        for (Item item : player.getInventory().getContents().values()){
            if (item.isArmor()) continue;

            Integer price = this.sellItem(player, item);
            if (price != null) totalPrice += price;
        }

        EconomyAPI.getInstance().addMoney(player, totalPrice);

        String message = this.loader.configFile.getString("sellAllMessage");
        message = message.replace("{money}", String.valueOf(totalPrice));
        player.sendMessage(message);
    }

    public void sellHand(Player player){
        if (player == null) return;
        PlayerInventory inv = player.getInventory();
        Item handItem = inv.getItemInHand();

        if (handItem.getId() == Item.AIR){
            player.sendMessage("§c»§7Please hold an item in your hand!");
            return;
        }

        ShopItem shopItem = null;
        for (ShopCategory category : this.loader.getCategories().values()){
            for (ShopItem sshopItem : category.getItems()){
                if (!handItem.equals(sshopItem.getItemSample(), true, false)) continue;
                shopItem = sshopItem;
                break;
            }
        }

        if (shopItem == null){
            player.sendMessage("§c»§7Unknown item! Can not sold item in your hand.");
            return;
        }

        int basePrice = shopItem.getSellPrice();
        int count =0;

        for (Item item : new ArrayList<>(inv.getContents().values())){
            if (item.equals(item, true, false)){
                count += item.getCount();
                inv.removeItem(item);
            }
        }

        inv.removeItem(handItem);

        int totalPrice = count * basePrice;
        EconomyAPI.getInstance().addMoney(player, totalPrice);

        String message = this.loader.configFile.getString("sellHandMessage");
        message = message.replace("{money}", String.valueOf(totalPrice));
        player.sendMessage(message);
    }
}

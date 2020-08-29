package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.shop.forms.ItemSellForm;
import alemiz.bettersurvival.addons.shop.forms.SellCategoryForm;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

public class SellManager {

    private SurvivalShop loader;

    public SellManager(SurvivalShop loader){
        this.loader = loader;
    }

    public void sendForm(Player player){
        new SellCategoryForm(player, this).buildForm().sendForm();
    }

    public void sendSellForm(ShopCategory category, Player player){
        new ItemSellForm(player, category).buildForm().sendForm();
    }

    private Integer sellItem(Player player, Item item){
        if (player == null || item == null) return null;

        if ((Addon.getAddon(BetterEconomy.class) instanceof BetterEconomy) && BetterEconomy.isBankNote(item)){
            return null;
        }

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

        for (Item item : inv.getContents().values()){
            if (item.equals(handItem, true, false)){
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

    public SurvivalShop getLoader() {
        return this.loader;
    }
}

package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cubemc.nukkit.connector.modules.Money;

public class ShopItem {

    public String name;
    public String customImage = "";
    public int itemId;
    public int count;
    public int price;
    public int meta = 0;

    public ShopItem(String name, int itemID, int count, int price){
        this.name = name;
        this.itemId = itemID;
        this.count = count;
        this.price = price;
    }

    public boolean buyItem(Player player){
        if (player == null) return false;

        int balance = Money.getInstance().getMoney(player, false);
        if ((balance - this.price) < 1) return false;

        Money.getInstance().reduceMoney(player, this.price);
        player.getInventory().addItem(this.buildItem());
        return true;
    }

    public Item buildItem(){
        return Item.get(this.itemId, this.meta, this.count);
    }

    public String getName() {
        return name;
    }

    public String getCustomImage() {
        return customImage;
    }

    public int getId() {
        return itemId;
    }

    public int getMeta() {
        return meta;
    }

    public int getCount() {
        return count;
    }

    public int getPrice() {
        return price;
    }
}

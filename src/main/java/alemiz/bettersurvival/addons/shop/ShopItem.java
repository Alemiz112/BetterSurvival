package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.item.Item;

public class ShopItem {

    public String name;

    public String customImage = "";
    public boolean useImage = true;
    public String imageType = ElementButtonImageData.IMAGE_DATA_TYPE_PATH;

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
        //TODO: implement with EconomyAPI

        player.getInventory().addItem(this.buildItem());
        return true;
    }

    public void setCustomImage(String customImage) {
        if (customImage.equals("false")){
            this.useImage = false;
            return;
        }

        if (customImage.startsWith("blocks/")){
            this.customImage = "textures/"+customImage;
            return;
        }

        this.imageType = ElementButtonImageData.IMAGE_DATA_TYPE_URL;
        this.customImage = customImage;
    }

    public Item buildItem(){
        return Item.get(this.itemId, this.meta, this.count);
    }

    public String getName() {
        return this.name;
    }

    public boolean canUseImage() {
        return this.useImage;
    }

    public String getCustomImage() {
        return this.customImage;
    }

    public String getImageType() {
        return this.imageType;
    }

    public int getId() {
        return this.itemId;
    }

    public int getMeta() {
        return this.meta;
    }

    public int getCount() {
        return this.count;
    }

    public int getPrice() {
        return this.price;
    }
}

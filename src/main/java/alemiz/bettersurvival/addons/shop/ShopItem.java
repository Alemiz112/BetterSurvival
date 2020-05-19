package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

public class ShopItem {

    public final String name;
    public final String formattedName;

    public String customImage = "";
    public boolean useImage = true;
    public String imageType = ElementButtonImageData.IMAGE_DATA_TYPE_PATH;

    public final int itemId;
    public final int count;
    public final int price;
    public int sellPrice = 0;
    public int meta = 0;

    public ShopItem(String name, int itemId, int count, int price){
        this.name = name;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.formattedName = this.buildFormattedName();
    }

    public boolean buyItem(Player player){
        if (player == null || EconomyAPI.getInstance().reduceMoney(player, this.count) < 1) return false;

        player.getInventory().addItem(this.buildItem());
        return true;
    }

    public void setCustomImage(String customImage) {
        if (customImage.equals("false")){
            this.useImage = false;
            return;
        }

        if (customImage.startsWith("blocks/") || customImage.startsWith("items/")){
            this.customImage = "textures/"+customImage;
            return;
        }

        this.imageType = ElementButtonImageData.IMAGE_DATA_TYPE_URL;
        this.customImage = customImage;
    }

    public Item getItemSample(){
        return Item.get(this.itemId, this.meta, this.count);
    }

    public String buildFormattedName(){
        String[] words = this.name.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words){
            builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        return builder.substring(0, builder.length()-1);
    }

    public Item buildItem(){
        return Item.get(this.itemId, this.meta, this.count);
    }

    public String getName() {
        return this.name;
    }

    public String getFormattedName(){
        return this.formattedName;
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

    public int getSellPrice() {
        return this.sellPrice == 0? this.price : this.sellPrice;
    }
}

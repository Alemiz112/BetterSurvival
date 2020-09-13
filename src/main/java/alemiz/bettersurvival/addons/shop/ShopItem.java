package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.item.Item;
import cubemc.nukkit.connector.modules.Money;

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
    public boolean canBeSold = true;
    public int meta = 0;

    public ShopItem(String name, int itemId, int count, int price){
        this.name = name;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.formattedName = this.buildFormattedName();
    }

    public boolean buyItem(Player player){
        return this.buyItem(player, 1);
    }

    public boolean buyItem(Player player, int stackCount){
        if (player == null || !Money.getInstance().reduceMoney(player, this.getStackPrice(stackCount))) return false;

        Item item = this.buildItem(stackCount);
        if (!player.getInventory().canAddItem(item)){
            player.sendMessage("§6»§7You do not have space in your inventory! Item was dropped!");
            player.getLevel().dropItem(player.add(0.5), item);
        }else {
            player.getInventory().addItem(item);
        }
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

    public int getStackPrice(int stackCount){
        return this.price * stackCount;
    }

    public int getStackCount(int stackCount){
        return this.count * stackCount;
    }

    public Item buildItem(){
        return this.buildItem(1);
    }

    public Item buildItem(int stackCount){
        return Item.get(this.itemId, this.meta, this.getStackCount(stackCount));
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

    public boolean canBeSold() {
        return this.canBeSold;
    }
}

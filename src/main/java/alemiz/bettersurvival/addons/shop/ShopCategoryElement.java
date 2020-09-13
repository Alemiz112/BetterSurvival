package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.addons.shop.forms.ShopItemListForm;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ShopCategoryElement {

    protected SurvivalShop loader;

    protected String category;
    protected List<ShopItem> items = new ArrayList<>();

    public ShopCategoryElement(String categoryName, JsonObject data, SurvivalShop loader){
        this.loader = loader;
        this.category = categoryName;

        JsonObject items = data.getAsJsonObject("items");
        for (Map.Entry<String, JsonElement> entry : items.entrySet()){
            if (!entry.getValue().isJsonObject()){
                continue;
            }
            String itemName = entry.getKey();
            JsonObject itemJson = entry.getValue().getAsJsonObject();

            ShopItem item = new ShopItem(itemName,
                    itemJson.get("id").getAsInt(),
                    itemJson.get("count").getAsInt(),
                    itemJson.get("price").getAsInt());

            if (itemJson.has("meta")) item.meta = itemJson.get("meta").getAsInt();
            if (itemJson.has("image")) item.setCustomImage(itemJson.get("image").getAsString());
            if (itemJson.has("sell")) item.sellPrice = itemJson.get("sell").getAsInt();
            this.items.add(item);
        }
    }

    public void sendForm(Player player){
        new ShopItemListForm(player, this).buildForm().sendForm();
    }

    public String getFormattedName(){
        return TextUtils.headerFormat(this.category);
    }

    public String getCategoryName() {
        return this.category;
    }

    public List<ShopItem> getItems() {
        return this.items;
    }

    public ShopItem getItem(int index){
        return this.items.get(index);
    }

    public SurvivalShop getLoader() {
        return this.loader;
    }
}

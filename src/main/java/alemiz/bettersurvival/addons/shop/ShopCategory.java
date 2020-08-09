package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.addons.shop.forms.ShopItemListForm;
import cn.nukkit.Player;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ShopCategory {

    private SurvivalShop loader;

    private String category;
    private List<ShopItem> items = new ArrayList<>();

    public ShopCategory(String categoryName, JSONObject data, SurvivalShop loader){
        this.loader = loader;
        this.category = categoryName;

        for (String itemKey : data.keySet()){
            if (!(data.get(itemKey) instanceof JSONObject)) continue;
            JSONObject itemJson = (JSONObject) data.get(itemKey);

            ShopItem item = new ShopItem(itemKey,
                    itemJson.getAsNumber("id").intValue(),
                    itemJson.getAsNumber("count").intValue(),
                    itemJson.getAsNumber("price").intValue());

            if (itemJson.containsKey("meta")) item.meta = itemJson.getAsNumber("meta").intValue();
            if (itemJson.containsKey("image")) item.setCustomImage(itemJson.getAsString("image"));
            if (itemJson.containsKey("sell")) item.sellPrice = itemJson.getAsNumber("sell").intValue();
            this.items.add(item);
        }
    }

    public void sendForm(Player player){
        new ShopItemListForm(player, this).buildForm().sendForm();
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

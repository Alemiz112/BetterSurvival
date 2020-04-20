package alemiz.bettersurvival.addons.shop;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import cubemc.nukkit.connector.modules.Money;
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
            this.items.add(item);
        }
    }

    public void sendForm(Player player){
        FormWindowSimple form = new FormWindowSimple("§l§8Shop "+this.category.substring(0, 1).toUpperCase()+this.category.substring(1), "");
        form.setContent("§7Pickup item by your own choice. Your coins: §8"+ Money.getInstance().getMoney(player, false)+"$");


        for (ShopItem item : this.items){
            String nameFormat = item.getName().substring(0, 1).toUpperCase() + item.getName().substring(1);
            ElementButton button = new ElementButton("§5"+nameFormat+"\n§7Count: §8"+item.getCount()+" §7Price: §8"+item.getPrice()+"$");

            if (item.canUseImage()){
                String image = item.getCustomImage();
                if (image.equals("")){
                    image = "textures/items/"+item.buildItem().getName().replace(" ", "_").toLowerCase();
                }

                button.addImage(new ElementButtonImageData(item.getImageType(), image));
            }

            form.addButton(button);
        }

        player.showFormWindow(form);
    }

    public String getCategoryName() {
        return category;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public ShopItem getItem(int index){
        return this.items.get(index);
    }
}

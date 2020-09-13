package alemiz.bettersurvival.addons.shop;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import com.google.gson.JsonObject;

public class Enchant {

    public final String name;
    public final String formattedName;

    public final int enchantId;
    public final int[] prices;

    public Enchant(String name, int enchantId, int price, JsonObject json){
        this.name = name;
        this.enchantId = enchantId;
        this.formattedName = this.buildFormattedName();

        Enchantment enchantment = this.getEnchantment();
        int[] levels = new int[enchantment.getMaxLevel()];

        for (int i = 0; i < enchantment.getMaxLevel(); i++){
            int index = i+1;
            if (!json.has("price"+index)) continue;

            levels[i] = json.get("price"+index).getAsInt();
        }
        this.prices = levels;
    }

    public String buildFormattedName(){
        String[] words = this.name.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words){
            builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        return builder.substring(0, builder.length()-1);
    }

    public Enchantment getEnchantment(){
        return Enchantment.get(this.enchantId);
    }

    public Enchantment getEnchantment(int level){
        return Enchantment.get(this.enchantId).setLevel(level);
    }

    public String getName() {
        return this.name;
    }

    public String getFormattedName(){
        return this.formattedName;
    }

    public int getEnchantId() {
        return this.enchantId;
    }

    public int getBasePrice() {
        return this.prices[0];
    }

    public int getPrice(int level){
        return level > this.prices.length? 0 :this.prices[level-1];
    }

    public Item getEnchantItem(){
        return this.getEnchantItem(1);
    }

    public Item getEnchantItem(int level){
        Item item = SmithShop.getEnchantItem();
        if (item == null) return null;

        item.getNamedTag().putInt("enchant_id", this.enchantId);
        item.getNamedTag().putInt("enchant_level", level);
        item.addEnchantment(this.getEnchantment(level));
        item.setCustomName("§r§l§a"+this.formattedName+"§r§b Orb");
        //item.setLore(ArrayUtils.addAll(new String[]{"§r§5Level: "+level}, item.getLore()));
        return item;
    }
}

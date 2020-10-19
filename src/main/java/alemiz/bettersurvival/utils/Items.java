package alemiz.bettersurvival.utils;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;

public class Items {

    public static Item deepCopy(Item copyOf) {
        try {
            Item item;
            if (copyOf instanceof ItemBlock){
                item = copyOf.getClass().getConstructor(Block.class, Integer.class, int.class).newInstance(copyOf.getBlock(), copyOf.getDamage(), copyOf.getCount());
            }else {
                item = copyOf.getClass().newInstance();
                item.setDamage(copyOf.getDamage());
                item.setCount(copyOf.getCount());
            }
            if (copyOf.hasCompoundTag()){
                item.setNamedTag(copyOf.getNamedTag().clone());
            }
            return item;
        }catch (Exception e){
            //ignore
        }
        return null;
    }
}

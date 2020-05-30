package alemiz.bettersurvival.utils;

import cn.nukkit.item.Item;

public class Items {

    public static Item deepCopy(Item copyOf) {
        try {
            Item item = copyOf.getClass().newInstance();
            if (copyOf.hasCompoundTag()) item.setNamedTag(copyOf.getNamedTag().clone());
            return item;
        }catch (InstantiationException | IllegalAccessException e){
            //ignore
        }
        return null;
    }
}

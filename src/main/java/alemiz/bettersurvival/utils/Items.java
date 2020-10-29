/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

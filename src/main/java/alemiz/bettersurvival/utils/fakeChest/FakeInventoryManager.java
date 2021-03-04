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

package alemiz.bettersurvival.utils.fakeChest;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import java.util.HashMap;
import java.util.Map;

public class FakeInventoryManager {

    public static final int INV_NORMAL = 0;
    public static final int INV_DOUBLE = 1;


    private static Map<String, FakeInventory> inventories = new HashMap<>();

    public static FakeInventory createInventory(Player player, String title, Map<Integer, Item> items){
        return createInventory(player, title, items, INV_NORMAL);
    }

    public static FakeInventory createInventory(Player player, String title, Map<Integer, Item> items, int type){
        Vector3 pos = player.add(0, 2, 0);

        FakeInventory inventory;
        switch (type){
            case INV_DOUBLE:
                inventory = new DoubleFakeInventory(title, pos, items);
                break;
            default:
                inventory = new FakeInventory(title, pos, items);
                break;
        }

        storeInventory(player, inventory);
        return inventory;
    }

    public static void storeInventory(Player player, FakeInventory inventory) {
        inventories.put(player.getName(), inventory);
    }

    public static void removeInventory(Player player){
        FakeInventory inventory = inventories.remove(player.getName());
        if (inventory != null) {
            inventory.close(player);
        }
    }

    public static FakeInventory getWindow(Player player) {
        return inventories.get(player.getName());
    }

    public static boolean hasWindow(Player player){
        return inventories.containsKey(player.getName());
    }
}

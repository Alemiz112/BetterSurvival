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

        inventories.put(player.getName(), inventory);
        return inventory;
    }

    public static void removeInventory(Player player){
        FakeInventory inventory = inventories.remove(player.getName());
        if (inventory != null) inventory.removeInventory(player);
    }

    public static boolean hasWindow(Player player){
        return inventories.containsKey(player.getName());
    }
}

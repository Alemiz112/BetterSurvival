package alemiz.bettersurvival.utils.fakeChest;

import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;

import java.util.Map;

public class FakeInventory extends ContainerInventory {


    public FakeInventory(Vector3 pos, Map<Integer, Item> items) {
        super(new Holder(pos.x, pos.y, pos.z), InventoryType.CHEST, items);
    }

    static final class Holder extends Vector3 implements InventoryHolder {

        private Holder(double x, double y, double z) {
            super(x, y, z);
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}

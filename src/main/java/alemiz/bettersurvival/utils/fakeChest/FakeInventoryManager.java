package alemiz.bettersurvival.utils.fakeChest;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class FakeInventoryManager {

    public static final String IS_INVENTORY_ITEM = "is-inv-item";

    private static Map<String, Inventory> inventories = new HashMap<>();

    public static void createInventory(Player player, String title, Map<Integer, Item> items){
        Vector3 pos = player.add(0, -1, 0);
        sendFakeInventory(player, title);

        items.forEach((Integer slot, Item item) -> {
            CompoundTag nbt = new CompoundTag()
                    .putBoolean(IS_INVENTORY_ITEM, true);

            item.setNamedTag(nbt);
        });

        FakeInventory inventory = new FakeInventory(pos, items);

        if (player.addWindow(inventory) == -1){
            removeRealChest(player, pos);
        }else inventories.put(player.getName(), inventory);
    }

    public static void removeInventory(Player player){
        Inventory inventory = inventories.remove(player.getName());

        if (inventory != null){
            removeRealChest(player, (Vector3) inventory.getHolder());
        }
    }

    private static void removeRealChest(Player player, Vector3 pos){
        player.getLevel().sendBlocks(new Player[]{player}, new Vector3[]{pos});
    }

    private static void sendFakeInventory(Player player, String title){
        UpdateBlockPacket pk1 = new UpdateBlockPacket();
        pk1.x = (int) player.x;
        pk1.y = (int) player.y - 1;
        pk1.z = (int) player.z;
        pk1.dataLayer = 0;
        pk1.flags = UpdateBlockPacket.FLAG_NONE;
        pk1.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(player.protocol, BlockID.CHEST, 0);
        player.dataPacket(pk1);

        BlockEntityDataPacket pk2 = new BlockEntityDataPacket();
        pk2.x = (int) player.x;
        pk2.y = (int) player.y - 1;
        pk2.z = (int) player.z;
        CompoundTag nbt = new CompoundTag()
                .putString("CustomName", title);

        try {
            pk2.namedTag = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, true);
        } catch (Exception e) {
        }
        player.dataPacket(pk2);
    }

    public static boolean hasWindow(Player player){
        return inventories.containsKey(player.getName());
    }
}

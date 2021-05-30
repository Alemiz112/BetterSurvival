/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival.utils.fakeChest;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;

import java.nio.ByteOrder;
import java.util.Map;

public class DoubleFakeInventory extends FakeInventory {

    public DoubleFakeInventory(String title, Vector3 pos, Map<Integer, Item> items) {
        super(title, pos, items, InventoryType.DOUBLE_CHEST);
    }

    @Override
    protected void openInventory(Player player) {
        Server.getInstance().getScheduler().scheduleDelayedTask(null, () -> {
            if (player.addWindow(this) == -1){
                this.removeInventory(player);
            }
        }, 3);
    }

    @Override
    protected void placeBlocks(Player player) {
        Position blockA = player.add(0, 2, 0);
        Position blockB = player.add(0, 2, 1);

        this.placeChest(player, blockA);
        this.placeChest(player, blockB);

        this.sendChest(player, blockA, blockB);
        this.sendChest(player, blockB, blockA);
    }

    protected void sendChest(Player player, Position pos1, Position pos2) {
        BlockEntityDataPacket packet = new BlockEntityDataPacket();
        packet.x = (int) pos1.x;
        packet.y = (int) pos1.y;
        packet.z = (int) pos1.z;

        CompoundTag nbt = new CompoundTag()
                .putString("CustomName", this.title)
                .putString("id", BlockEntity.CHEST)
                .putInt("x", (int) pos1.x)
                .putInt("y", (int) pos1.y)
                .putInt("z", (int) pos1.z)
                .putInt("pairx", (int) pos2.x)
                .putInt("pairz", (int) pos2.z);

        try {
            packet.namedTag = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, true);
        } catch (Exception e) {
            //ignore
        }
        player.dataPacket(packet);
    }

    @Override
    public void removeInventory(Player player) {
        if (player == null) return;
        player.getLevel().sendBlocks(new Player[]{player}, new Vector3[]{player.add(0, 2, 0), player.add(0, 2, 1)});
    }
}

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
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class FakeInventory extends ContainerInventory {

    public enum Flags{
        IS_FAKE_ITEM("is-fake"),
        IS_LOCKED("can-move"),
        IS_VOTE_INV("vote-inv"),
        IS_INV_SEE("invsee-inv");

        private final String label;

        Flags(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Map<Flags, Boolean> itemFlags = new HashMap<>();
    private Map<Flags, Boolean> inventoryFlags = new HashMap<>();

    protected String title = "Chest";

    public FakeInventory(String title, Vector3 pos, Map<Integer, Item> items){
        this(title, pos, items, InventoryType.CHEST);
    }

    public FakeInventory(String title, Vector3 pos, Map<Integer, Item> items, InventoryType type) {
        super(new Holder(pos.x, pos.y, pos.z), type , items);

        if (title != null) this.title = title;
    }

    public void showInventory(Player player){
        if (player == null) return;

        this.placeBlocks(player);
        this.applyItemFlagsOnContents();

        this.openInventory(player);
    }

    protected void openInventory(Player player){
        Server.getInstance().getScheduler().scheduleDelayedTask(null, () -> {
            if (player.addWindow(this) == -1){
                this.removeInventory(player);
            }
        }, 0);
    }

    protected void placeBlocks(Player player){
        Position block = player.add(0, 2, 0);

        this.placeChest(player, block);
        this.sendChest(player, block);
    }

    protected void placeChest(Player player, Position pos){
        UpdateBlockPacket packet = new UpdateBlockPacket();
        packet.x = (int) pos.x;
        packet.y = (int) pos.y;
        packet.z = (int) pos.z;
        packet.dataLayer = 0;
        packet.flags = UpdateBlockPacket.FLAG_NONE;
        packet.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(player.protocol, BlockID.CHEST, 0);
        player.dataPacket(packet);
    }

    protected void sendChest(Player player, Position pos){
        BlockEntityDataPacket packet = new BlockEntityDataPacket();
        packet.x = (int) pos.x;
        packet.y = (int) pos.y;
        packet.z = (int) pos.z;

        CompoundTag nbt = new CompoundTag()
                .putString("CustomName", this.title)
                .putString("id", BlockEntity.CHEST)
                .putInt("x", (int) pos.x)
                .putInt("y", (int) pos.y)
                .putInt("z", (int) pos.z);

        try {
            packet.namedTag = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, true);
        } catch (Exception e) {
            //ignore
        }
        player.dataPacket(packet);
    }

    public void removeInventory(Player player){
        if (player != null) {
            player.getLevel().sendBlocks(new Player[]{player}, new Vector3[]{player.add(0, 2, 0)});
        }
    }

    public boolean slotChange(Player player, SlotChangeAction action){
        FakeSlotChangeEvent event = new FakeSlotChangeEvent(player, this, action);
        event.call();
        return !event.isCancelled();
    }

    @Override
    public void onClose(Player who) {
        this.removeInventory(who);
        super.onClose(who);
    }

    public Boolean getItemFlag(Flags flag){
        return this.itemFlags.getOrDefault(flag, null);
    }

    public Boolean hasItemFlag(Flags flag){
        return this.getItemFlag(flag) != null;
    }

    public void setItemFlag(Flags flag, Boolean value){
        this.itemFlags.put(flag, value);
    }

    public void applyItemFlagsOnContents(){
        for (Flags flag : this.itemFlags.keySet()){
            this.applyItemFlagOnContents(flag);
        }
    }

    public void applyItemFlagOnContents(Flags flagKey){
        Boolean flag = this.getItemFlag(flagKey);
        if (flag == null) return;

        this.slots.forEach((Integer slot, Item item) -> {
            CompoundTag nbt = new CompoundTag().putByte(flagKey.getLabel(), flag? 1 : 0);
            item.setNamedTag(nbt);
        });
    }

    public boolean getInventoryFlag(Flags flags){
        return this.inventoryFlags.getOrDefault(flags, false);
    }

    public boolean hasInventoryFlag(Flags flags){
        return this.inventoryFlags.containsKey(flags);
    }

    public void setInventoryFlag(Flags flag, Boolean value){
        this.inventoryFlags.put(flag, value);
    }

    public Map<Flags, Boolean> getItemFlags() {
        return itemFlags;
    }

    public Map<Flags, Boolean> getInventoryFlags() {
        return inventoryFlags;
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

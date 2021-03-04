/*
 * Copyright 2021 Alemiz
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

import alemiz.bettersurvival.addons.Troller;
import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.PlayerEnderChestInventory;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.math.Vector3;

public class FakeEnderChest extends FakeInventory {

    private final Troller loader;
    private final PlayerEnderChestInventory hostInventory;

    public FakeEnderChest(String title, Vector3 pos, PlayerEnderChestInventory hostInventory, Troller loader) {
        super(title, pos, hostInventory.getContents(), InventoryType.CHEST);
        this.hostInventory = hostInventory;
        this.loader = loader;
    }

    @Override
    public boolean slotChange(Player player, SlotChangeAction action) {
        if (this.hostInventory == null) {
            return false;
        }
        return this.hostInventory.setItem(action.getSlot(), action.getTargetItem(), true);
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        this.loader.onEndInvClose(this);
    }

    public PlayerEnderChestInventory getHostInventory() {
        return this.hostInventory;
    }
}

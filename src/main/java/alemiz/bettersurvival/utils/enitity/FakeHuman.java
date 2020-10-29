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

package alemiz.bettersurvival.utils.enitity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddPlayerPacket;

import java.nio.charset.StandardCharsets;

public class FakeHuman extends EntityHuman {

    public FakeHuman(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public static CompoundTag createNbt(Location pos, String nameTag, Skin skin, Player player){
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("", pos.x))
                        .add(new DoubleTag("", pos.y))
                        .add(new DoubleTag("", pos.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", (float) pos.getYaw()))
                        .add(new FloatTag("", (float) pos.getPitch())))
                .putString("NameTag", nameTag)
                .putCompound("Skin", new CompoundTag()
                        .putByteArray("Data", skin.getSkinData().data)
                        .putString("ModelId", skin.getSkinId())
                        .putString("GeometryName", "geometry.humanoid.custom")
                        .putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8)));

        if (player != null){
            nbt.putString("npc_item", player.getInventory().getItemInHand().getName());
            nbt.putString("npc_helmet", player.getInventory().getHelmet().getName());
            nbt.putString("npc_chestplate", player.getInventory().getChestplate().getName());
            nbt.putString("npc_leggings", player.getInventory().getLeggings().getName());
            nbt.putString("npc_boots", player.getInventory().getBoots().getName());
        }
        return nbt;
    }

    public static FakeHuman createEntity(Location pos, String nameTag, Skin skin){
        return createEntity(pos, nameTag, skin, null);
    }

    public static FakeHuman createEntity(Location pos, String nameTag, Skin skin, Player player){
        CompoundTag nbt = createNbt(pos, nameTag, skin, player);
        FakeHuman entity = (FakeHuman) Entity.createEntity("FakeHuman", pos.getLevel().getChunk(pos.getChunkX(), pos.getChunkZ()), nbt);

        entity.setNameTag(nameTag);
        return entity;
    }

    @Override
    public void spawnTo(Player player) {
        if (this.hasSpawned.containsKey(player.getLoaderId())) return;

        this.hasSpawned.put(player.getLoaderId(), player);
        this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, new Player[]{player});

        this.inventory.setItemInHand(Item.fromString(this.namedTag.getString("npc_item")));
        this.inventory.setHelmet(Item.fromString(this.namedTag.getString("npc_helmet")));
        this.inventory.setChestplate(Item.fromString(this.namedTag.getString("npc_chestplate")));
        this.inventory.setLeggings(Item.fromString(this.namedTag.getString("npc_leggings")));
        this.inventory.setBoots(Item.fromString(this.namedTag.getString("npc_boots")));
        this.inventory.sendArmorContents(player);

        AddPlayerPacket pk = new AddPlayerPacket();
        pk.uuid = this.getUniqueId();
        pk.username = this.getName();
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.yaw = (float) this.yaw;
        pk.pitch = (float) this.pitch;
        pk.item = this.getInventory().getItemInHand();
        pk.metadata = this.dataProperties;
        player.dataPacket(pk);

        this.inventory.sendArmorContents(player);
        this.server.removePlayerListData(this.getUniqueId(), new Player[]{player});

        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled(true);
        super.attack(source);
        return false;
    }
}

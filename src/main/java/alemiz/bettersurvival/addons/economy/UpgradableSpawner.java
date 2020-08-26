package alemiz.bettersurvival.addons.economy;

import cn.nukkit.blockentity.BlockEntitySpawner;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ShortTag;

public class UpgradableSpawner extends BlockEntitySpawner {

    public UpgradableSpawner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if (!this.namedTag.contains("spawner_level") || !(this.namedTag.get("spawner_level") instanceof ShortTag)){
            this.setSpawnerLevel(EconomySpawners.DEFAULT_LEVEL);
        }

        super.initBlockEntity();
    }

    public void setSpawnerLevel(SpawnerLevel level){
        this.namedTag.putShort("spawner_level", level.getLevel());
        this.namedTag.putShort(TAG_MIN_SPAWN_DELAY, level.getMinSpawnDelay());
        this.namedTag.putShort(TAG_MAX_SPAWN_DELAY, level.getMaxSpawnDelayTicked());
        this.setMinSpawnDelay(level.getMinSpawnDelay());
        this.setMinSpawnDelay(level.getMaxSpawnDelayTicked());
    }

    public int getSpawnerLevel(){
        return this.namedTag.getInt("spawner_level");
    }
}

package alemiz.bettersurvival.addons.economy;

public class SpawnerLevel {

    private final int level;
    private final int minSpawnDelay;
    private final int maxSpawnDelay;
    private final int price;

    public SpawnerLevel(int level, int minSpawnDelay, int maxSpawnDelay, int price){
        this.level = level;
        this.minSpawnDelay = minSpawnDelay;
        this.maxSpawnDelay = maxSpawnDelay;
        this.price = price;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMinSpawnDelay() {
        return this.minSpawnDelay;
    }

    public int getMaxSpawnDelay() {
        return this.maxSpawnDelay;
    }

    public int getMinSpawnDelayTicked(){
        return this.minSpawnDelay*20;
    }

    public int getMaxSpawnDelayTicked(){
        return this.maxSpawnDelay*20;
    }

    public int getAverageDelay(){
        return (this.minSpawnDelay + this.maxSpawnDelay) / 2;
    }

    public int getPrice() {
        return this.price;
    }
}

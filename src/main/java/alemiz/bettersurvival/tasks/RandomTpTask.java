package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.scheduler.Task;

import java.util.concurrent.ThreadLocalRandom;

public class RandomTpTask extends Task {

    private final Player player;
    private final String message;

    private int x;
    private int z;

    private int countdown = 5;

    public RandomTpTask(Player player, String message){
        this.player = player;
        this.message = message;

        this.getRandomPos();
    }

    @Override
    public void onRun(int i) {
        if (player == null) return;

        int y;
        boolean found = false;

        Level level = player.getLevel();
        BaseFullChunk chunk = level.getChunk(x >> 4, z >> 4);
        boolean nether = level.getDimension() == Level.DIMENSION_NETHER;

        for (y = 0; y <= (nether? 128 : 256); y++){
            if (nether && y >= 128){
                this.clearSpawn(new Position(this.x, y, this.z, player.getLevel()));
                found = true;
                break;
            }

            if (MoreVanilla.UNSAFE_BLOCKS.get(chunk.getBlockId(x & 0xF, y, z & 0xF)) ||
                    chunk.getBlockId(x & 0xF, y+1, z & 0xF) != BlockID.AIR ||
                    chunk.getBlockId(x & 0xF, y+2, z & 0xF) != BlockID.AIR) continue;
            found = true;
            break;
        }

        if (!found){
            this.getRandomPos();
            BetterSurvival.getInstance().getServer().getScheduler().scheduleDelayedTask(this, 40);
            return;
        }

        if (this.countdown > 0){
            player.sendMessage("§6»§7Teleporting in §6"+this.countdown+"§7!");

            this.countdown--;
            BetterSurvival.getInstance().getServer().getScheduler().scheduleDelayedTask(this, 20);
            return;
        }


        player.teleport(new Location(this.x, y+1, this.z, player.getLevel()));
        player.sendMessage(this.message);
    }

    private void getRandomPos(){
        Level level = player.getLevel();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        boolean invertX = rand.nextBoolean();
        boolean invertZ = rand.nextBoolean();

        this.x = rand.nextInt(invertX? -50000 : +20000, invertX? -20000 : +50000);
        this.z = rand.nextInt(invertZ? -50000 : +20000, invertZ? -20000 : +50000);
        level.generateChunk(x >> 4, z >> 4, true);
    }

    private void clearSpawn(Position pos){
        Level level = pos.getLevel();
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        int z = (int) pos.getZ();

        for (int i = 0; i < 2; i++){
            level.setBlockIdAt(x, y+i, z, Block.AIR);

            level.setBlockIdAt(x+1, y+i, z, Block.AIR);
            level.setBlockIdAt(x-1, y+i, z, Block.AIR);

            level.setBlockIdAt(x, y+i, z+1, Block.AIR);
            level.setBlockIdAt(x, y+i, z-1, Block.AIR);

            level.setBlockIdAt(x+1, y+i, z+1, Block.AIR);
            level.setBlockIdAt(x+1, y+i, z-1, Block.AIR);
            level.setBlockIdAt(x-1, y, z+1, Block.AIR);
            level.setBlockIdAt(x-1, y+i, z-1, Block.AIR);
        }
    }
}

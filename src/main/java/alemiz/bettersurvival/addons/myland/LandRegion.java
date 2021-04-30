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

package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.exception.CancelException;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.List;

public class LandRegion {

    public static final String WHITELIST_ADD = "add";
    public static final String WHITELIST_REMOVE = "remove";
    public static final String WHITELIST_LIST = "list";


    public final String owner;
    public final String land;

    protected Level level;
    protected Vector3f pos1;
    protected Vector3f pos2;

    protected List<String> whitelist = new ArrayList<>();

    protected boolean liquidFlow = true;
    protected boolean pistonMovement = true;

    public LandRegion(String owner, String name){
        this.owner = owner;
        this.land = name;
    }

    public boolean onInteract(Player player, Block block) throws CancelException {
        return this.whitelist.contains(player.getName().toLowerCase());
    }

    public boolean canManage(String player){
        return this.owner.equals(player.toLowerCase());
    }

    public void addWhitelist(String player){
        this.whitelist.add(player.toLowerCase());
        this.save();
    }

    public void whitelistRemove(String player){
        this.whitelist.remove(player.toLowerCase());
        this.save();
    }

    public void load(ConfigSection config){
        this.level = Server.getInstance().getLevelByName(config.getString("level"));
        if (this.level == null){
            this.level = Server.getInstance().getDefaultLevel();
        }

        List<Integer> data = config.getIntegerList("pos0");
        this.pos1 = new Vector3f(data.get(0), data.get(1), data.get(2));
        data = config.getIntegerList("pos1");
        this.pos2 = new Vector3f(data.get(0), data.get(1), data.get(2));

        this.whitelist = config.getStringList("whitelist");
        this.liquidFlow = config.getBoolean("liquidFlow", true);
        this.pistonMovement = config.getBoolean("pistonMovement");
    }

    public void save(){
        Config config = ConfigManager.getInstance().loadPlayer(owner);
        if (config == null) {
            return;
        }

        config.set("land."+land.toLowerCase()+".level", this.level.getFolderName().toLowerCase());
        config.set("land."+land.toLowerCase()+".pos0", new float[]{pos1.getX(), pos1.getY(), pos1.getZ()});
        config.set("land."+land.toLowerCase()+".pos1", new float[]{pos2.getX(), pos2.getY(), pos2.getZ()});

        config.set("land."+land.toLowerCase()+".whitelist", this.whitelist);
        config.set("land."+land.toLowerCase()+".liquidFlow", this.liquidFlow);
        config.set("land."+land.toLowerCase()+".pistonMovement", this.pistonMovement);
        config.save();
    }

    public boolean validate(){
        return this.level != null && this.pos1 != null && this.pos2 != null;
    }

    public String getName(){
        return this.land;
    }

    public Level getLevel() {
        return this.level;
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
        this.save();
    }

    public Vector3f getPos1() {
        return this.pos1;
    }

    public Vector3f getPos2() {
        return this.pos2;
    }

    public void setLiquidFlow(boolean liquidFlow) {
        this.liquidFlow = liquidFlow;
        this.save();
    }

    public boolean canLiquidFlow() {
        return this.liquidFlow;
    }

    public void setPistonMovement(boolean pistonMovement) {
        this.pistonMovement = pistonMovement;
        this.save();
    }

    public boolean isPistonMovementEnabled() {
        return this.pistonMovement;
    }
}

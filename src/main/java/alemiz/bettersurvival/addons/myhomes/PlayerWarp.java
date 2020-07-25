package alemiz.bettersurvival.addons.myhomes;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.ConfigSection;

public class PlayerWarp {

    private final String name;
    private final String owner;
    private final String category;
    private final Position pos;

    public PlayerWarp(String name, String category, String owner, Position pos){
        this.name = name;
        this.category = category;
        this.owner = owner;
        this.pos = pos;
    }

    public void teleport(Player player){
        if (player == null || this.pos == null) return;
        player.teleport(this.pos);
    }

    public ConfigSection save(){
        ConfigSection data = new ConfigSection();
        data.set("name", this.name);
        data.set("category", this.category);
        data.set("pos", (int) this.pos.getX()+","+ (int) this.pos.getY()+","+ (int) this.pos.getZ());
        data.set("level", this.pos.getLevel().getFolderName());
        return data;
    }

    public String getName() {
        return this.name;
    }

    public String getRawName(){
        return this.name.toLowerCase().replace(" ", "_");
    }

    public String getCategory() {
        return this.category;
    }

    public String getOwner() {
        return this.owner;
    }

    public Position getPos() {
        return this.pos;
    }
}

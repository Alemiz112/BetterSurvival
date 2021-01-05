package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.EggCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.entity.EntitySpawnEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cubemc.nukkit.connector.modules.npc.NpcModule;
import cubemc.nukkit.connector.utils.exception.InvalidSkinException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;


public class EasterAddon extends Addon {

    private List<Skin> eggSkins = new ArrayList<>();
    private List<Long> eggs = new ArrayList<>();

    private List<String> setters = new ArrayList<>();

    public EasterAddon(String path){
        super("easteraddon", path);
    }

    @Override
    public void postLoad() {
        List<String> skins = configFile.getStringList("eggs");
        if (skins.isEmpty()){
            this.plugin.getLogger().info("§eUnable to load easter eggs! Not all data provided!");
            return;
        }

        for (String skinFile : skins){
            Skin skin = this.generateSkin(skinFile);
            if (skin == null || !skin.isValid()){
                this.plugin.getLogger().info("§eUnable to load egg "+skinFile+"!");
                continue;
            }

            this.eggSkins.add(skin);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("eggs", Arrays.asList("egg1", "egg2"));
            configFile.set("eggGeometryFile", "easter-egg.json");
            configFile.set("eggGeometryName", "geometry.easteregg");

            configFile.set("eggTitle", "§bEaster Egg%n§7»Punch me!");
            configFile.set("eggHead", 1.2);

            configFile.set("eggFoundMessage", "§6»§7You have found new egg!");
            configFile.set("eggRemoveMessage", "§6»§7All easter eggs were removed!");
            configFile.set("eggFoundReward", Arrays.asList("264:0:1", "357:0:1"));

            configFile.set("eggCommandPermission", "bettersurvival.eastereggs");
            configFile.set("setterAddMessage", "§6»§7Place block to add new Easter Egg!");
            configFile.set("setterRemoveMessage", "§6»§7You have exited setter mode for Easter Eggs! Totally spawned §6{eggs}§7 eggs!");
            configFile.save();
        }

        File folder = new File(ConfigManager.getInstance().ADDONS_PATH+"/skins");
        if (!folder.exists()){
            folder.mkdirs();
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("egg", new EggCommand("egg", this), false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event){
        Entity entity = event.getEntity();

        if (entity == null || !this.isEasterEgg(entity) || this.eggs.contains(entity.getId())) return;
        this.eggs.add(entity.getId());
        entity.setNameTagAlwaysVisible(false);
    }

    @EventHandler
    public void onEntityDespawn(EntityDespawnEvent event){
        Entity entity = event.getEntity();

        if (entity == null || !this.isEasterEgg(entity)) return;
        this.eggs.remove(entity.getId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();

        if (!(event.getDamager() instanceof Player) || !this.isEasterEgg(entity)) return;
        this.addToList((Player) event.getDamager(), entity);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if (!this.setters.contains(event.getPlayer().getName())) return;

        this.spawnRandomEgg(event.getPlayer(), event.getBlock());
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        this.setters.remove(event.getPlayer().getName());
    }

    private Skin generateSkin(String skinFile){
        String path = ConfigManager.getInstance().ADDONS_PATH+"/skins";
        String geometryFile = configFile.getString("eggGeometryFile");
        String geometryName = configFile.getString("eggGeometryName");

        if (geometryName == null || geometryFile == null){
            return null;
        }

        try {
            Skin skin = new Skin();
            InputStream geometryStream = new FileInputStream(path+"/"+geometryFile);
            Scanner geometryScanner = new Scanner(geometryStream).useDelimiter("\\A");

            InputStream skinStream = new FileInputStream(path+"/"+skinFile+".png");
            BufferedImage skinImage = ImageIO.read(skinStream);

            skin.setGeometryName(geometryName);
            skin.setGeometryData(geometryScanner.hasNext() ? geometryScanner.next() : "");
            skin.setSkinId(geometryName);
            skin.setSkinData(skinImage);
            return skin;
        }catch (Exception e){
            this.plugin.getLogger().error("§c"+e.getMessage(), e);
        }

        return null;
    }

    public void spawnRandomEgg(Player player){
        this.spawnRandomEgg(player, null);
    }

    public void spawnRandomEgg(Player player, Vector3 pos){
        if (player == null) return;

        String name = configFile.getString("eggTitle");
        String geometryName = configFile.getString("eggGeometryName");

        Entity entity;
        try {
            Skin skin = this.eggSkins.get(new Random().nextInt(this.eggSkins.size()));
            entity = NpcModule.getInstance().createCustomEntity(player, skin, geometryName, name);
        }catch (InvalidSkinException e){
            player.sendMessage("§c»§7"+e.getMessage());
            this.plugin.getLogger().error(e.getMessage(), e.getTrackedException());
            return;
        }

        if (pos != null){
            entity.teleport(pos.add(0.5, 0, 0.5));
        }

        entity.setNameTagAlwaysVisible(false);
        entity.setDataProperty(new FloatEntityData(
                Entity.DATA_BOUNDING_BOX_HEIGHT,
                (float) configFile.getInt("eggHead")), true);

        entity.namedTag.putBoolean("easterEgg", true);
        entity.namedTag.putFloat("head", (float) configFile.getInt("eggHead"));
        entity.namedTag.putString("eggHash", UUID.randomUUID().toString());
        entity.saveNBT();

        this.eggs.add(entity.getId());
    }

    public void removeAllEggs(Player player){
        if (player == null) return;
        if (!player.hasPermission(configFile.getString("eggCommandPermission"))){
            player.sendMessage("§cYou dont have permission to remove easter eggs!");
            return;
        }

        Level level = player.getLevel();

        List<Long> entities = new ArrayList<Long>(this.eggs);
        for (long entityId : entities){
            Entity entity = level.getEntity(entityId);

            if (entity == null || !this.isEasterEgg(entity)) continue;
            this.eggs.remove(entityId);

            entity.getLevel().setBlock(entity, Block.get(Block.AIR));
            entity.close();
        }

        String message = configFile.getString("eggRemoveMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void addToList(Player player, Entity entity){
        if (player == null || entity == null || !this.isEasterEgg(entity)) return;

        String eggHash = this.getEggHash(entity);
        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        List<String> eggs = config.getStringList("easterEggs");
        if (!eggs.isEmpty() && eggs.contains(eggHash)){
            player.sendMessage("§e»§7You have already found this easter egg!");
            player.sendTip("§bFound §3"+eggs.size()+"§b/§3"+this.eggs.size()+"§b eggs");
            return;
        }

        eggs.add(eggHash);
        config.set("easterEggs", eggs);
        config.save();

        player.sendTip("§bFound §3"+eggs.size()+"§b/§3"+this.eggs.size()+"§b eggs");
        this.giveReward(player);
    }

    private void giveReward(Player player){
        List<String> reward = configFile.getStringList("eggFoundReward");

        for (String itemString : reward){
            Item item = Item.fromString(itemString);
            player.getInventory().addItem(item);
        }

        player.getLevel().addSound(player, Sound.FIREWORK_TWINKLE,1.0F, 1.0F, player);
        String message = configFile.getString("eggFoundMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void clearFoundEggs(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
                    config.remove("easterEggs");
                    config.save();
                }
                plugin.getLogger().info("§eAll found Easter Eggs were removed!");
            }
        };
        this.plugin.getServer().getScheduler().scheduleTask(task, true);
    }


    public void addSetter(Player player){
        if (player == null) return;
        if (!player.hasPermission(configFile.getString("eggCommandPermission"))){
            player.sendMessage("§cYou dont have permission to set easter eggs!");
            return;
        }

        if (this.setters.contains(player.getName())){
            this.removeSetter(player);
            return;
        }

        this.setters.add(player.getName());

        String message = configFile.getString("setterAddMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void removeSetter(Player player){
        if (player == null) return;
        this.setters.remove(player.getName());

        String message = configFile.getString("setterRemoveMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{eggs}", String.valueOf(this.eggs.size()));
        player.sendMessage(message);
    }

    public String getEggHash(Entity entity){
        if (!this.isEasterEgg(entity)) return null;
        return entity.namedTag.getString("eggHash");
    }


    public boolean isEasterEgg(Entity entity){
        return entity.namedTag.contains("easterEgg");
    }

}

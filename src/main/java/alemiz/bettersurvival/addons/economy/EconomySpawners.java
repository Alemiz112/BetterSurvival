package alemiz.bettersurvival.addons.economy;

import alemiz.bettersurvival.utils.Items;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.ConfigSection;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EconomySpawners implements Listener {

    public static SpawnerLevel DEFAULT_LEVEL = null;
    private static final Item spawnerTool;
    static {
        Item item = Item.get(Item.BLAZE_ROD, 0, 1);
        item.setCustomName("§r§6Spawner Upgrade");
        item.getNamedTag().putByte("spawner_upgrade", 1);
        item.setLore("§r§5Touch the spawner to upgrade it");
        spawnerTool = Items.deepCopy(item);
    }

    protected final BetterEconomy loader;
    private final Map<Integer, SpawnerLevel> spawnerLevels = new HashMap<>();
    private final List<String> spawnerInfo = new ArrayList<>();


    private boolean canBreakSpawner;
    private int spawnerPrice;

    public EconomySpawners(BetterEconomy loader){
        this.loader = loader;

        ConfigSection section = loader.configFile.getSection("spawners");
        for (String levelString : section.getKeys(false)){
            int level;
            try {
                level = Integer.parseInt(levelString);
            }catch (NumberFormatException e){
                this.loader.plugin.getLogger().error("Can not load spawner level '"+levelString+"'!", e);
                continue;
            }
            this.spawnerLevels.put(level, new SpawnerLevel(level,
                    section.getInt(levelString+".minDelay"),
                    section.getInt(levelString+".maxDelay"),
                    section.getInt(levelString+".price")));
        }

        this.canBreakSpawner = loader.configFile.getBoolean("spawnerBreaks");
        this.spawnerPrice = loader.configFile.getInt("spawnerBreakPrice");
        DEFAULT_LEVEL = new SpawnerLevel(
                0,
                loader.configFile.getInt("spawnerDefaultMin"),
                loader.configFile.getInt("spawnerDefaultMax"),
                0
        );
        this.spawnerLevels.put(0, DEFAULT_LEVEL);
        BlockEntity.registerBlockEntity(BlockEntity.MOB_SPAWNER, UpgradableSpawner.class);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        this.spawnerInfo.remove(player.getName());
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event){
        if (event.isCancelled() || event.getBlock().getId() != Block.MONSTER_SPAWNER){
            return;
        }

        Player player = event.getPlayer();
        player.sendMessage("§6»§7You have placed spawner with level §6"+DEFAULT_LEVEL.getLevel()+"§7! To increase mob spawn time, buy spawner upgrade!");
    }

    @EventHandler
    public void onUpgrade(PlayerInteractEvent event){
        if (event.isCancelled() || event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.getBlock().getId() != Block.MONSTER_SPAWNER) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        Block block = event.getBlock();

        BlockEntity blockEntity = player.getLevel().getBlockEntity(block);
        if (this.spawnerInfo.contains(player.getName())){
            event.setCancelled(this.sendSpawnerInfo(player, blockEntity));
            this.spawnerInfo.remove(player.getName());
            return;
        }

        if (!(blockEntity instanceof UpgradableSpawner)){
            return;
        }
        if (!item.hasCompoundTag() || item.getNamedTag().getByte("spawner_upgrade") != 1 || !item.getNamedTag().contains("spawner_level")){
            return;
        }

        int levelInt = item.getNamedTag().getInt("spawner_level");
        SpawnerLevel level = this.spawnerLevels.get(levelInt);
        UpgradableSpawner spawner = (UpgradableSpawner) blockEntity;
        if (level == null){
            return;
        }

        if (spawner.getSpawnerLevel() >= level.getLevel()){
            player.sendMessage("§c»§7Level of this spawner is higher than your upgrade! TIP: Use §6/spawnerinfo §7to get spawner info.");
            return;
        }

        spawner.setSpawnerLevel(level);
        player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());

        String message = this.loader.configFile.getString("spawnerUpgradeMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{level}", String.valueOf(level.getLevel()));
        player.sendMessage(message);
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event){
        if (!this.canBreakSpawner || event.isCancelled() || event.getBlock().getId() != Block.MONSTER_SPAWNER) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.hasPermission(this.loader.configFile.getString("spawnerBreakBypass")) || player.isOp()){
            event.setDrops(new Item[]{Item.get(Item.MONSTER_SPAWNER, 0, 1)});
            return;
        }

        new SpawnerBreakForm(player, block, this).buildForm().sendForm();
        event.setCancelled(true);
    }

    public Item buildSpawnerUpgrade(String owner, int level){
        Item item = getSpawnerTool();
        item.setCustomName(item.getCustomName()+" §eLevel §l"+level);
        item.setLore(ArrayUtils.addAll(new String[]{"§r§5Created For: §d"+owner}, item.getLore()));

        CompoundTag tag = item.getNamedTag();
        tag.putInt("spawner_level", level);
        item.setNamedTag(tag);
        return item;
    }

    public void sendSpawnerShopForm(Player player){
        new SpawnerUpgradeForm(player, this).buildForm().sendForm();
    }

    public boolean sendSpawnerInfo(Player player, BlockEntity blockEntity){
        if (player == null || !(blockEntity instanceof UpgradableSpawner)) return false;

        UpgradableSpawner spawner = (UpgradableSpawner) blockEntity;
        SpawnerLevel level = this.spawnerLevels.get(spawner.getSpawnerLevel());

        if (level == null){
            player.sendMessage("§c»§7Unknown spawner level!");
            return false;
        }

        String message = this.loader.configFile.getString("spawnerInfoMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{level}", String.valueOf(level.getLevel()));
        message = message.replace("{delay}", String.valueOf(level.getAverageDelay()));
        player.sendMessage(message);
        return true;
    }

    public void addSpawnInfoPlayer(Player player){
        if (!this.spawnerInfo.contains(player.getName())){
            this.spawnerInfo.add(player.getName());
        }
    }

    public Map<Integer, SpawnerLevel> getSpawnerLevels() {
        return this.spawnerLevels;
    }

    public SpawnerLevel getLowestLevel(){
        return this.spawnerLevels.get(0);
    }

    public SpawnerLevel getHighestLevel(){
        return this.spawnerLevels.get(this.spawnerLevels.size()-1);
    }

    public int getSpawnerPrice() {
        return this.spawnerPrice;
    }

    public static Item getSpawnerTool() {
        return Items.deepCopy(spawnerTool);
    }
}

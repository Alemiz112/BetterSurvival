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

package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.ClanLand;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.commands.LandCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import alemiz.bettersurvival.utils.exception.CancelException;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntityHopper;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityPainting;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.block.LiquidFlowEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.inventory.InventoryMoveItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;


public class MyLandProtect extends Addon {

    public static final BitSet INTERACT_BLOCKS = new BitSet();

    private final Map<String, LinkedList<Block>> selectors = new HashMap<>();
    private final Map<String, LandRegion> lands = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public static String WAND = "§6LandWand";
    public static String PERM_VIP = "bettersurvival.land.vip";
    public static String PERM_ACCESS = "bettersurvival.land.access";
    public static String PERM_ACCESS_CHEST = "bettersurvival.chest.access";

    static {
        INTERACT_BLOCKS.set(BlockID.CHEST);
        INTERACT_BLOCKS.set(BlockID.TRAPPED_CHEST);
        INTERACT_BLOCKS.set(BlockID.ENDER_CHEST);
        INTERACT_BLOCKS.set(BlockID.SHULKER_BOX);
        INTERACT_BLOCKS.set(BlockID.UNDYED_SHULKER_BOX);
        INTERACT_BLOCKS.set(BlockID.ANVIL);
        INTERACT_BLOCKS.set(BlockID.BEDROCK);
    }

    public MyLandProtect(String path){
        super("mylandprotect", path);
    }

    @Override
    public void postLoad() {
        WAND = configFile.getString("wandName");
        PERM_VIP = configFile.getString("landsVipPermission");
        PERM_ACCESS = configFile.getString("landsAccessPermission");
        PERM_ACCESS_CHEST = configFile.getString("chestsAccessPermission");

        List<SuperConfig> playerEntries = ConfigManager.getInstance().loadAllPlayers();
        int successLands = 0;
        int failedAttempts = 0;

        for (SuperConfig config : playerEntries){
            int lands = this.loadLands(config);
            if (lands < 0){
                failedAttempts++;
            }else {
                successLands += lands;
            }
        }

        this.plugin.getLogger().info("§eAll lands loaded! Entries: §3"+playerEntries.size());
        this.plugin.getLogger().info("§eTotal Lands: §3"+successLands+" §eFailed: §c"+failedAttempts);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("enablePrivateChests", true);

            configFile.set("wandName", "§6Land§eWand");

            configFile.set("landsLimit", 2);
            configFile.set("landsLimitSize", 50);
            configFile.set("landsLimitSizeVip", 100);

            configFile.set("landsVipPermission", "bettersurvival.land.vip");
            configFile.set("landsAccessPermission", "bettersurvival.land.access");
            configFile.set("chestsAccessPermission", "bettersurvival.chest.access");

            configFile.set("landNotExists", "§6»§7Land not found!");
            configFile.set("landWithNameExists", "§6»§7Land §6{land}§7 already exists§7!");
            configFile.set("landWarn", "§6»§7Hey §6@{player}§7, this is not your region! Ask §6@{owner} §7to access §6{land}§7!");
            configFile.set("landTooBig", "§6»§7Selected land is bigger than maximum allowed limit §6{limit} blocks§7!");
            configFile.set("landPosSelected", "§6»§7Successfully selected {select} position at §6{pos}§7!");
            configFile.set("landLimitWarn", "§6»§7Lands limit reached!");
            configFile.set("landHereNotFound", "§6»§7This land is free§7!");

            configFile.set("landCreate", "§6»§7You have created new land §6{land}§7! You have §6{limit}§7 free lands!");
            configFile.set("landRemove", "§6»§7You have removed your land §6{land}§7!");
            configFile.set("landSetPos", "§6»§7Touch 2 blocks with wand to select border positions§7!");
            configFile.set("landWhitelist", "§6»§7Whitelist for §6{land}§7 saved§7!");
            configFile.set("landWhitelistList", "§6»{land}§7 access: {players}");
            configFile.set("landFlowSetting", "§6»§7Water and lava flow in land §6{land}§7 was §6{state}§7!");
            configFile.set("landPistonSetting", "§6»§7Pistons in land §6{land}§7 were §6{state}§7!");
            configFile.set("landHere", "§6»§7The land §6{land}§7 is owned by §6{owner}§7!");
            configFile.set("landList", "§6»§7Your lands: {lands}");

            configFile.set("landWhitelistAdd", "§6»§7You gain access §6@{player}§7 to your land §6{land}§7!");
            configFile.set("landWhitelistRemove", "§6»§7You restrict §6@{player}§7's access to your land §6{land}§7!");

            configFile.set("landClanExists", "§6»§7Your §6@{clan}§7 clan has already land!");

            configFile.set("privateChestCreate", "§6»§r§7You have successfully created private chest!");
            configFile.set("privateChestAccessDenied", "§c»§r§7This chest is owned by §6@{owner}§7! You can not access it.");
            configFile.set("privateChestDestroy", "§c»§r§7You have successfully destroyed private chest!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        if (configFile.getBoolean("enable", true)){
            this.registerCommand("land", new LandCommand("land", this));
        }
    }

    @EventHandler
    public void onBlockTouch(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        String item = player.getInventory().getItemInHand().getName();

        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && item.equals(WAND)){
            LinkedList<Block> blocks = new LinkedList<>();

            if (this.selectors.containsKey(player.getName().toLowerCase())){
                blocks = this.selectors.get(player.getName().toLowerCase());
            }

            if (blocks.size() >= 2) blocks.clear();

            blocks.add(event.getBlock());
            this.selectors.put(player.getName().toLowerCase(), blocks);

            String message = configFile.getString("landPosSelected");
            message = message.replace("{pos}", event.getBlock().x +", "+ event.getBlock().y +", "+ event.getBlock().z);
            message = message.replace("{player}", player.getDisplayName());
            message = message.replace("{select}", (blocks.size() == 1)? "first" : "second");
            player.sendMessage(message);
            event.setCancelled();
            return;
        }

        Collection<LandRegion> regions = this.getLandsByPos(block);
        for (LandRegion region : regions) {
            if (!this.interact(player, region, block)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!configFile.getBoolean("enablePrivateChests", false)) {
            return;
        }

        if (block.getId() == Block.CHEST && (block.getLevel().getBlockEntity(block) instanceof BlockEntityChest)){
            BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);

            if (!this.interactChest(player, chest)){
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Collection<LandRegion> regions = this.getLandsByPos(block);
        for (LandRegion region : regions) {
            if (!this.interact(player, region)){
                event.setCancelled(true);
                return;
            }
        }

        if (!configFile.getBoolean("enablePrivateChests", false)) {
            return;
        }

        if ((block.getId() == Block.WALL_SIGN || block.getId() == Block.SIGN_POST) && (player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)){
            BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);
            if (this.signInteract(event, sign)) return;
        }

        if (block.getId() == Block.CHEST && (block.getLevel().getBlockEntity(block) instanceof BlockEntityChest)){
            BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);

            if (!this.interactChest(player, chest)){
                event.setCancelled();
            }
        }
    }

    private boolean signInteract(BlockBreakEvent event, BlockEntitySign sign){
        Player player = event.getPlayer();
        String[] lines = sign.getText();

        if (lines == null || lines.length < 1 || lines[0] == null || !(lines[0].equals("[private]") || lines[0].equals("§r§f[§clocked§f]"))) {
            return false;
        }

        BlockEntityChest chest = this.getChestBySign(sign);
        if (chest == null) {
            return false;
        }

        switch (lines[0]){
            case "§r§f[§clocked§f]":
                String owner = this.getPrivateChestOwner(chest);
                if (owner == null || this.removePrivateChest(player, chest)) break;
                event.setCancelled();
                break;
            case "[private]":
                if (this.createPrivateChest(player, chest)){
                    event.setCancelled();
                    sign.setText("§r§f[§clocked§f]", "§a"+player.getName());
                }
                break;
        }

        return true;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Collection<LandRegion> regions = this.getLandsByPos(block);
        for (LandRegion region : regions) {
            if (!this.interact(player, region)){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        for (Position block : event.getBlockList()){
            if (this.isPrivateChest(block) || this.getLandByPos(block) != null) {
                event.setBlockList(new ArrayList<>());
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event){
        if (!(event.getSource() instanceof BlockEntityHopper)) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (inventory == null || !(inventory.getHolder() instanceof BlockEntityChest)) {
            return;
        }

        BlockEntityChest chest = (BlockEntityChest) inventory.getHolder();
        if (this.isPrivateChest(chest)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrame(ItemFrameDropItemEvent event){
        Position pos = event.getItemFrame();
        Player player = event.getPlayer();

        Collection<LandRegion> regions = this.getLandsByPos(pos);
        for (LandRegion region : regions) {
            if (!this.interact(player, region)){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();

        if (!(entity instanceof EntityPainting)){
            return;
        }

        Collection<LandRegion> regions = this.getLandsByPos(entity);
        for (LandRegion region : regions) {
            if (!this.interact(player, region)){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onLiquid(LiquidFlowEvent event){
        Block source = event.getTo();

        LandRegion region = this.getLandByPos(source, true, false);
        if (region != null && !region.canLiquidFlow()){
            event.setCancelled(true);
        }
    }

    public boolean interact(Player player, LandRegion region){
        return this.interact(player, region, null);
    }

    public boolean interact(Player player, LandRegion region, Block block){
        if (region == null) return true;
        if (region.owner.equals(player.getName().toLowerCase())) return true;
        if (player.isOp() || player.hasPermission(PERM_ACCESS)) return true;

        boolean clanLand = region instanceof ClanLand;
        boolean canInteract;

        try {
            canInteract = region.onInteract(player, block);
        }catch (CancelException e){
            //Using CancelException we can prevent notify message
            return e.getValue() instanceof Boolean && (Boolean) e.getValue();
        }

        if (!canInteract){
            String message = configFile.getString("landWarn");
            message = message.replace("{land}", clanLand? "" : region.land);
            message = message.replace("{player}", player.getDisplayName());
            message = message.replace("{owner}", region.owner + (clanLand? " Clan" : ""));
            player.sendMessage(message);
        }
        return canInteract;
    }

    public LandRegion getLandByPos(Position position){
        return this.getLandByPos(position, false, true);
    }

    public LandRegion getLandByPos(Position position, boolean prioritize){
        return this.getLandByPos(position, prioritize, true);
    }

    /**
     * @param prioritize returns regions by priority
     * Highest priority have player regions and than clan regions
     * Priority helps in clan land block restriction
     * @param vertical check if Y coordination matches too
     */
    public LandRegion getLandByPos(Position position, boolean prioritize, boolean vertical) {
        Collection<LandRegion> foundRegions = this.getLandsByPos(position, vertical);
        LandRegion foundregion = null;

        for (LandRegion region : foundRegions) {
            if (prioritize && !(region instanceof ClanLand)){
                return region;
            }

            foundregion = region;
        }
        return foundregion;
    }

    public Collection<LandRegion> getLandsByPos(Position position) {
        return this.getLandsByPos(position, true);
    }

    public Collection<LandRegion> getLandsByPos(Position position, boolean vertical) {
        List<LandRegion> regions = new ArrayList<>();
        for (LandRegion region : this.lands.values()) {
            if (position.getLevel() != null && !region.level.getFolderName().equals(position.getLevel().getFolderName())){
                continue;
            }

            if (this.isInside(position.asVector3f(), region.getPos1(), region.getPos2(), vertical)){
                regions.add(region);
            }
        }
        return regions;
    }

    public boolean isInside(Vector3 position, Vector3 in1, Vector3 in2){
        return this.isInside(position.asVector3f(), in1.asVector3f(), in2.asVector3f());
    }

    public boolean isInside(Vector3 position, Vector3 in1, Vector3 in2, boolean vertical){
        return this.isInside(position.asVector3f(), in1.asVector3f(), in2.asVector3f(), vertical);
    }

    public boolean isInside(Vector3f position, Vector3f in1, Vector3f in2){
        return this.isInside(position, in1, in2, true);
    }

    public boolean isInside(Vector3f position, Vector3f in1, Vector3f in2, boolean vertical){
        boolean horizontal = position.x >= Math.min(in1.x, in2.x) && position.x <= Math.max(in1.x, in2.x) &&
                position.z >= Math.min(in1.z, in2.z) && position.z <= Math.max(in1.z, in2.z);

        if (vertical){
            return horizontal && position.y >= Math.min(in1.y, in2.y) && position.y <= Math.max(in1.y, in2.y);
        }
        return horizontal;
    }

    public boolean interactChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return true;
        if (player.isOp() || player.hasPermission(PERM_ACCESS_CHEST)) return true;

        String owner = this.getPrivateChestOwner(chest);
        if (owner == null || owner.equalsIgnoreCase(player.getName())) return true;

        String message = configFile.getString("privateChestAccessDenied");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{owner}", owner);
        player.sendMessage(message);

        return false;
    }

    public BlockEntityChest getChestBySign(BlockEntity block){
        if (block.getLevel().getBlockEntity(block.north()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.north());
        }else if (block.getLevel().getBlockEntity(block.south()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.south());
        }else if (block.getLevel().getBlockEntity(block.east()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.east());
        }else if (block.getLevel().getBlockEntity(block.west()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.west());
        }
        return null;
    }

    public Set<String> getLands(Player player){
        return this.getLands(player.getName());
    }

    public Set<String> getLands(String player){
        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("land").getKeys(false);
    }

    public boolean validateLand(Player player, boolean clanMode, Block block1, Block block2, Level level){
        if (block1 == null || block2 == null){
            if (player != null){
                player.sendMessage(configFile.getString("landSetPos"));
            }
            return false;
        }

        // 1. Check if land is in spawn
        if (level.isInSpawnRadius(block1) || level.isInSpawnRadius(block2)){
            player.sendMessage("§c»§7You can not create land inside spawn area!");
            return false;
        }

        // 2. Check if land is in other land or over other land
        LandRegion region = null;
        Vector3f pos1 = block1.asVector3f();
        Vector3f pos2 = block2.asVector3f();

        for (LandRegion landRegion : this.lands.values()){
            if (this.isInside(pos1, landRegion.getPos1(), landRegion.getPos2()) ||
                    this.isInside(pos2, landRegion.getPos1(), landRegion.getPos2()) ||
                    this.isInside(landRegion.getPos1(), pos1, pos2) ||
                    this.isInside(landRegion.getPos2(), pos1, pos2)){
                region = landRegion;
                break;
            }
        }

        Clan playerClan = null;
        if (region != null && !region.canManage(player.getName()) && !player.hasPermission(PERM_ACCESS)){
            boolean cancel = true;
            if ((region instanceof ClanLand)){
                PlayerClans playerClans = Addon.getAddon(PlayerClans.class);
                if (playerClans != null) {
                    playerClan = playerClans.getClan(player);
                    if (playerClan != null && playerClan.getName().equals(((ClanLand) region).getClan().getName())){
                        cancel = false;
                    }
                }
            }

            if (cancel){
                String message = configFile.getString("landWarn");
                message = message.replace("{land}", (region instanceof ClanLand)? "" : region.land);
                message = message.replace("{player}", player.getDisplayName());
                message = message.replace("{owner}", region.owner + ((region instanceof ClanLand)? " Clan" : ""));
                player.sendMessage(message);
                return false;
            }
        }

        // 3. Check land sizes
        int landSize;
        if (clanMode){
            landSize = playerClan == null ? 375 : playerClan.getMaxLandSize();
        }else {
            landSize = configFile.getInt("landsLimitSize");
            if (player != null && player.hasPermission(PERM_VIP)){
                landSize = configFile.getInt("landsLimitSizeVip");
            }
        }

        if (player != null && player.isOp()) return true;

        if ((Math.max(pos1.x, pos2.x) - Math.min(pos1.x, pos2.x)) > landSize ||
                (Math.max(pos1.y, pos2.y) - Math.min(pos1.y, pos2.y)) > landSize ||
                (Math.max(pos1.z, pos2.z) - Math.min(pos1.z, pos2.z)) > landSize){

            if (player != null){
                String message = configFile.getString("landTooBig");
                message = message.replace("{player}", player.getDisplayName());
                message = message.replace("{limit}", String.valueOf(landSize));
                player.sendMessage(message);
            }
            return false;
        }
        return true;
    }

    public int loadLands(SuperConfig config){
       String owner = config.getName().substring(0, config.getName().lastIndexOf("."));
       int count = 0;

       for (String land : config.getSection("land").getKeys(false)){
           try {
               ConfigSection section = config.getSection("land."+land);
               LandRegion region = new LandRegion(owner, land);
               region.load(section);

               if (!region.validate()){
                   return -1;
               }

               region.save(); //Save updated data
               this.lands.put(owner.toLowerCase()+"-"+land, region);
               count++;
           }catch (Exception e){
               this.plugin.getLogger().warning("§cCan not land §4"+land+"§c for player §4"+owner+"§c!");
               return -1;
           }
       }
       return count;
    }

    public boolean loadClanLand(Clan clan){
        Config config = clan.getConfig();
        this.plugin.getLogger().info("§eLoading land for clan §3"+clan.getName()+"§e!");

        ClanLand region = new ClanLand(clan);
        region.load(config.getSection("land"));

        if (!region.validate()){
            this.plugin.getLogger().warning("§cCan not load land for clan §4"+clan.getName()+"§c!");
            return false;
        }

        region.save(); //Save updated data
        this.lands.put(clan.getRawName(), region);
        return true;
    }

    public void createLand(Player player, String land){
        this.createLand(player, land, false);
    }

    public void createLand(Player player, String land, boolean clanMode){
        Runnable task = () -> {
            if (player == null || !player.isConnected()) return;
            Config config;
            Clan clan = null;
            int freeLands = 0;

            if (clanMode){
                PlayerClans playerClans = Addon.getAddon(PlayerClans.class);
                if (playerClans == null || (clan = playerClans.getClan(player)) == null) {
                    player.sendMessage("§c»§7You are not in any clan!");
                    return;
                }

                config = clan.getConfig();
                if (config.exists("land")){
                    String message = configFile.getString("landClanExists");
                    message = message.replace("{clan}", clan.getName());
                    player.sendMessage(message);
                    return;
                }
            }else {
                config = ConfigManager.getInstance().loadPlayer(player);
                if (config == null) return;
                int limit = configFile.getInt("landsLimit");
                Set<String> lands = config.getSection("land").getKeys(false);
                freeLands = limit - lands.size();

                if (freeLands < 1 && !player.isOp()){
                    String message = configFile.getString("landLimitWarn");
                    message = message.replace("{land}", land);
                    message = message.replace("{player}", player.getDisplayName());
                    player.sendMessage(message);
                    return;
                }

                if (lands.contains(land)){
                    String message = configFile.getString("landWithNameExists");
                    message = message.replace("{land}", land);
                    message = message.replace("{player}", player.getDisplayName());
                    player.sendMessage(message);
                    return;
                }
            }

            if (!this.selectors.containsKey(player.getName().toLowerCase())){
                player.sendMessage(configFile.getString("landSetPos"));
                return;
            }

            player.sendMessage("§6»§r§7Validating land. Please wait...");

            LinkedList<Block> blocks = this.selectors.get(player.getName().toLowerCase());
            Block block1 = blocks.poll();
            Block block2 = blocks.poll();

            if (block1 == null || block2 == null || !this.validateLand(player, clanMode, block1, block2, player.getLevel())){
                this.selectors.remove(player.getName().toLowerCase());
                return;
            }

            this.selectors.remove(player.getName().toLowerCase());

            LandRegion region = clanMode? new ClanLand(clan) : new LandRegion(player.getName().toLowerCase(), land);
            region.level = player.getLevel();
            region.pos1 = block1.asVector3f();
            region.pos2 = block2.asVector3f();

            if (!region.validate()){
                player.sendMessage("§c»§7Land is invalid! Please try again!");
                return;
            }

            String index = clanMode? "land.pos0" : "land."+land+".pos0";
            config.set(index, new double[]{block1.getX(), block1.getY(), block1.getZ()});
            String index2 = clanMode? "land.pos1" : "land."+land+".pos1";
            config.set(index2, new double[]{block2.getX(), block2.getY(), block2.getZ()});
            if (!clanMode)  config.set("land."+land+".whitelist", new String[0]);
            config.save();

            String message = configFile.getString("landCreate");
            message = message.replace("{land}", (clanMode? clan.getName()+" land" : land));
            message = message.replace("{player}", player.getName());
            message = message.replace("{limit}", player.isOp()? "unlimited" : String.valueOf(freeLands));
            player.sendMessage(message);

            this.lands.put(clanMode? clan.getRawName() : player.getName().toLowerCase()+"-"+land, region);
        };
        this.plugin.getServer().getScheduler().scheduleTask(this.plugin, task, true);
    }

    public void removeLand(Player player, String land){
        if (player == null || !player.isConnected()) {
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) {
            return;
        }

        if (!config.exists("land."+land)){
            this.regionNotFound(player);
            return;
        }

        ((Map<?, ?>) config.get("land")).remove(land);
        config.save();

        this.lands.remove(player.getName().toLowerCase()+"-"+land);

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", land);
        player.sendMessage(message);
    }

    public void removeClanLand(Player player){
        if (player == null) return;

        Clan clan = ((PlayerClans) Addon.getAddon(PlayerClans.class)).getClan(player);
        if (clan == null) {
            player.sendMessage("§c»§7You are not in any clan!");
            return;
        }

        Config config = clan.getConfig();
        config.remove("land");
        config.save();

        this.lands.remove(clan.getRawName());

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", clan.getName());
        player.sendMessage(message);
    }

    public void findLand(Player player){
        Block block = player.getLevel().getBlock(player.clone());
        LandRegion land;

        if (block == null || (land = getLandByPos(block)) == null){
            String message = configFile.getString("landHereNotFound");
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        if (land instanceof ClanLand){
            player.sendMessage("§6»§7This land is owned by §6"+land.owner+" Clan§7!");
            return;
        }

        String message = configFile.getString("landHere");
        message = message.replace("{owner}", land.owner);
        message = message.replace("{land}", land.land);
        player.sendMessage(message);
    }

    public void whitelist(Player owner, String player, LandRegion region, String action){
        if (region == null){
            this.regionNotFound(owner);
            return;
        }

        if (!region.canManage(owner.getName())){
            String message = configFile.getString("landWarn");
            message = message.replace("{land}", region.land);
            message = message.replace("{player}", owner.getName());
            message = message.replace("{owner}", region.owner);
            owner.sendMessage(message);
            return;
        }

        switch (action){
            case LandRegion.WHITELIST_ADD:
                region.addWhitelist(player);
                break;
            case LandRegion.WHITELIST_REMOVE:
                region.whitelistRemove(player);
                break;
            case LandRegion.WHITELIST_LIST:
                String players = String.join(", ", region.getWhitelist());

                String message = configFile.getString("landWhitelistList");
                message = message.replace("{land}", region instanceof ClanLand? "§7Clan land" : region.land);
                message = message.replace("{player}", owner.getName());
                message = message.replace("{players}", players);
                owner.sendMessage(message);
                return; //exit
        }

        String message = configFile.getString("landWhitelist");
        message = message.replace("{land}", region instanceof ClanLand? "clan land" : region.land);
        message = message.replace("{player}", owner.getName());
        owner.sendMessage(message);
    }

    public boolean createPrivateChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return false;

        String owner = this.getPrivateChestOwner(chest);
        if (owner != null){
            player.sendMessage("§c»§r§7This chest is already owned by §6@"+owner+"§7!");
            return false;
        }

        chest.namedTag.putString("private_owner", player.getName());

        String message = configFile.getString("privateChestCreate");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
        return true;
    }

    public boolean removePrivateChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return false;

        BlockEntityChest privateChest = this.getPrivateChest(chest);
        if (privateChest == null) return false;

        String owner = privateChest.namedTag.getString("private_owner");

        if (!owner.equalsIgnoreCase(player.getName()) && !player.isOp() && !player.hasPermission(PERM_ACCESS_CHEST)){
            String message = configFile.getString("privateChestAccessDenied");
            message = message.replace("{player}", player.getName());
            message = message.replace("{owner}", owner);
            player.sendMessage(message);
            return false;
        }

        privateChest.namedTag.remove("private_owner");
        String message = configFile.getString("privateChestDestroy");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
        return true;
    }

    public boolean isPrivateChest(Position pos){
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);
        if (!(entity instanceof BlockEntityChest)) return false;

        return this.isPrivateChest((BlockEntityChest) entity);
    }

    public boolean isPrivateChest(BlockEntityChest chest){
        return this.getPrivateChestOwner(chest) != null;
    }

    public String getPrivateChestOwner(Position pos){
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);
        if (!(entity instanceof BlockEntityChest)) return null;

        return this.getPrivateChestOwner((BlockEntityChest) entity);
    }

    public String getPrivateChestOwner(BlockEntityChest chest){
        if (chest == null) return null;

        String owner = null;
        if (chest.namedTag.contains("private_owner")){
            owner = chest.namedTag.getString("private_owner");
        }else if (chest.getPair() != null && chest.getPair().namedTag.contains("private_owner")){
            owner = chest.getPair().namedTag.getString("private_owner");
        }

        return owner;
    }

    public BlockEntityChest getPrivateChest(Position pos){
        if (pos == null) return null;
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);

        if (!(entity instanceof BlockEntityChest)) return null;
        BlockEntityChest chest = (BlockEntityChest) entity;

        if (!chest.namedTag.getString("private_owner").equals("")){
            return chest;
        }else if (chest.isPaired() && !chest.getPair().namedTag.getString("private_owner").equals("")){
            return chest.getPair();
        }
        return null;
    }

    public void listLands(Player player){
        Set<String> lands = this.getLands(player);

        String message = configFile.getString("landList");
        message = message.replace("{lands}", String.join(", ", lands));
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void regionNotFound(Player player){
        String message = configFile.getString("landNotExists");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void waterFlowMessage(Player player, String landName, boolean state){
        String message = configFile.getString("landFlowSetting");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", landName);
        message = message.replace("{state}", state? "enabled" : "disabled");
        player.sendMessage(message);
    }

    public void pistonMovementMessage(Player player, String landName, boolean state){
        String message = configFile.getString("landPistonSetting");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", landName);
        message = message.replace("{state}", state? "enabled" : "disabled");
        player.sendMessage(message);
    }

    public Map<String, LandRegion> getLands() {
        return this.lands;
    }

    public LandRegion getLand(Player player, String land){
        return this.lands.get(player.getName().toLowerCase()+"-"+land);
    }
}

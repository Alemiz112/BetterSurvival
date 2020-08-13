package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.*;
import alemiz.bettersurvival.tasks.MuteCheckTask;
import alemiz.bettersurvival.tasks.RandomTpTask;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.Player;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.*;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;

import java.util.*;
import java.util.stream.Collectors;

public class MoreVanilla extends Addon{

    public static BitSet UNSAFE_BLOCKS = new BitSet();

    static {
        UNSAFE_BLOCKS.set(BlockID.AIR);
        UNSAFE_BLOCKS.set(BlockID.LAVA);
        UNSAFE_BLOCKS.set(BlockID.STILL_LAVA);
        UNSAFE_BLOCKS.set(BlockID.WATER);
        UNSAFE_BLOCKS.set(BlockID.STILL_WATER);
        UNSAFE_BLOCKS.set(BlockID.FIRE);
        UNSAFE_BLOCKS.set(BlockID.CACTUS);
        UNSAFE_BLOCKS.set(BlockID.NETHER_PORTAL);
        UNSAFE_BLOCKS.set(BlockID.END_PORTAL);
    }

    protected Map<String, String> tpa = new HashMap<>();
    protected Map<String, Location> back = new HashMap<>();

    protected Map<String, Date> mutedPlayers = new HashMap<>();
    protected Map<String, Integer> randTpDelay = new HashMap<>();
    protected Map<String, Position> sleepPos = new HashMap<>();

    protected Map<String, Boolean> keepInvCache = new HashMap<>();

    public MoreVanilla(String path){
        super("morevanilla", path);
    }

    @Override
    public void postLoad() {
        plugin.getServer().getScheduler().scheduleRepeatingTask(new MuteCheckTask(this), 20*60, true);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("keepInvCommand", true);
            configFile.set("keepInventoryMessage", "§6»§7You have turned KeepInventory §6{state}§7!");
            configFile.set("permission-keepInvAll", "bettersurvival.vanilla.keepinvall");
            configFile.set("permission-keepinvTools", "bettersurvival.vanilla.keepinvtools");

            configFile.set("chatFormat", "§6{player} §7> {message}");
            configFile.set("playerNotFound", "§6»§7Player was not found!");
            configFile.set("permission-manage", "bettersurvival.vanilla.manage");

            configFile.set("permission-fly", "bettersurvival.fly");
            configFile.set("flyMessage", "§6»§7Flying mode has been turned §6{state}§7!");

            configFile.set("permission-tpa", "bettersurvival.tpa");
            configFile.set("tpaMessage", "§6»§7Teleport request was sent to @{player}§7!");
            configFile.set("tpaRequestMessage", "§6»§7Player @{player}§7 wants teleport to you. Write §8/tpa a§7!");
            configFile.set("tpaAcceptMessage", "§6»§7Player @{player}§7 accepted your request!");
            configFile.set("tpaDennyMessage", "§6»§7Player @{player}§7 denied your request!");
            configFile.set("tpaDennyConfirmMessage", "§6»§7You denied teleport request!");
            configFile.set("tpaNoRequests", "§6»§7You dont have any requests!");

            configFile.set("permission-heal", "bettersurvival.heal");
            configFile.set("healMessage", "§6»§7You was healed!");

            configFile.set("permission-back", "bettersurvival.back");
            configFile.set("backMessage", "§6»§7You was teleported back to your death position!");
            configFile.set("backPosNotFound", "§6»§7You dont have saved any death position!");

            configFile.set("permission-feed", "bettersurvival.feed");
            configFile.set("feedMessage", "§6»§7Your feed level has been increased to {state}!");

            configFile.set("permission-near", "bettersurvival.near");
            configFile.set("nearMessage", "§6»§7Players near you: §6{players}§7!");

            configFile.set("permission-jump", "bettersurvival.jump");
            configFile.set("jumpMessage", "§6Woosh!");
            configFile.set("jumpPower", 1.5);

            configFile.set("permission-randtp", "bettersurvival.randtp");
            configFile.set("randtpMessage", "§6»§7You was teleported to random location!");

            configFile.set("permission-mute", "bettersurvival.mute");
            configFile.set("muteMessage", "§c»§7You was muted for {time} minutes!");
            configFile.set("muteChatMessage", "§c»§7You was muted! You cant chat now!");
            configFile.set("unmuteMessage", "§a»§7You was unmuted! Please respect rules to be not muted again :D");

            configFile.set("showCoordinates", true);
            configFile.set("doImmediateRespawn", true);
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("tpa", new TpaCommand("tpa", this));
        registerCommand("fly", new FlyCommand("fly", this));
        registerCommand("heal", new HealCommand("heal", this));
        registerCommand("feed", new FeedCommand("feed", this));
        registerCommand("back", new BackCommand("back", this));
        registerCommand("near", new NearCommand("near", this));
        registerCommand("jump", new JumpCommand("jump", this));
        registerCommand("rand", new RandCommand("rand", this));
        registerCommand("mute", new MuteCommand("mute", this));
        registerCommand("unmute", new UnmuteCommand("unmute", this));
        if (configFile.getBoolean("keepInvCommand")) registerCommand("keepinv", new KeepInvCommand("keepinv", this));
    }

    @EventHandler
    public void onLevelLoad(LevelLoadEvent event){
        GameRules gameRules = event.getLevel().getGameRules();
        gameRules.setGameRule(GameRule.SHOW_COORDINATES,
                configFile.getBoolean("showCoordinates", true));
        gameRules.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN,
                configFile.getBoolean("doImmediateRespawn", true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        /* Set Default Permissions*/
        player.addAttachment(plugin, configFile.getString("permission-tpa"), true);
        player.addAttachment(plugin, configFile.getString("permission-back"), true);
        player.addAttachment(plugin, configFile.getString("permission-randtp"), true);

        if (configFile.getBoolean("keepInvCommand")){
            Config config = ConfigManager.getInstance().loadPlayer(player);
            this.keepInvCache.put(player.getName(), config.getBoolean("keepInventory"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        this.back.remove(player.getName());
        this.randTpDelay.remove(player.getName());
        this.sleepPos.remove(player.getName());
        this.keepInvCache.remove(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event){
        Player player = event.getPlayer();

        if (this.mutedPlayers.containsKey(player.getName())){
            String message = configFile.getString("muteChatMessage");
            message = message.replace("{player}", player.getName());

            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }

        String format = configFile.getString("chatFormat");
        format = format.replace("{player}", player.getName());
        format = format.replace("{message}", event.getMessage());
        event.setFormat(format);

        for (Player pplayer : this.plugin.getServer().getOnlinePlayers().values()){
            if (!event.getMessage().contains("@"+pplayer.getName())) continue;

            PlaySoundPacket packet = new PlaySoundPacket();
            packet.name = "note.hat"; //cubemc use as info sound
            packet.volume = 1;
            packet.pitch = 1;
            packet.x = (int) player.getX();
            packet.y = (int) player.getY();
            packet.z = (int) player.getZ();
            pplayer.dataPacket(packet);
        }
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event){
        Player player = event.getPlayer();
        this.sleepPos.put(player.getName(), player.clone());
    }

    @EventHandler
    public void onWakeUp(PlayerBedLeaveEvent event){
        Player player = event.getPlayer();
        Position safeSpawn = this.sleepPos.getOrDefault(player.getName(), player.getLevel().getSafeSpawn(player.getSpawn()));
        player.teleport(safeSpawn);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        boolean canFly = player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
        if (!player.isCreative() && canFly){
            player.getAdventureSettings().set(AdventureSettings.Type.FLYING, false);
            player.getAdventureSettings().update();
            player.sendMessage("§c»§7You can not attack player while flying!");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        this.back.put(player.getName(), player.clone());

        if (configFile.getBoolean("keepInvCommand") && this.keepInventory(player) &&
                (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)){
            event.setKeepInventory(true);
            this.dropDeathItems(player, event.getDrops());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;

        if (changeArmor(player, item)){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        if (!(event.getWindow() instanceof FormWindowSimple) || event.getResponse() == null) return;

        FormWindowSimple form = (FormWindowSimple) event.getWindow();
        Player player = event.getPlayer();

        if (form.getTitle().equals("Player Teleport")){
            String response = form.getResponse().getClickedButton().getText();
            response = response.substring(2, response.indexOf("\n"));
            tpa(player, response);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Item item = event.getItem();

        switch (item.getId()){
            case Item.DIAMOND_PICKAXE:
            case Item.GOLD_PICKAXE:
            case Item.IRON_PICKAXE:
            case Item.STONE_PICKAXE:
            case Item.WOODEN_PICKAXE:
            case Item.DIAMOND_AXE:
            case Item.GOLD_AXE:
            case Item.IRON_AXE:
            case Item.STONE_AXE:
            case Item.WOODEN_AXE:
            case Item.DIAMOND_SHOVEL:
            case Item.GOLD_SHOVEL:
            case Item.IRON_SHOVEL:
            case Item.STONE_SHOVEL:
            case Item.WOODEN_SHOVEL:
                break;
            default:
                return;
        }

        CompoundTag namedTag = item.getNamedTag() == null? new CompoundTag() : item.getNamedTag();
        int broken = namedTag.getInt("brokenBlocks")+1;

        namedTag.putInt("brokenBlocks", broken);
        item.setNamedTag(namedTag);
        item.setLore("§r§5Block Destroyed: "+broken);
        player.getInventory().setItemInHand(item);
    }

    public boolean changeArmor(Player player, Item item){
        if (!item.isArmor()) return false;
        if (item.getId() == Item.SKULL) return false;

        PlayerInventory inv = player.getInventory();
        Item changed = null;

        inv.remove(item);

        if (item.isHelmet()){
            changed = inv.getHelmet();
            inv.setHelmet(item);

        }else if (item.isChestplate()){
            changed = inv.getChestplate();
            inv.setChestplate(item);

        }else if (item.isLeggings()){
            inv.setLeggings(item);

        }else if (item.isBoots()){
            changed = inv.getBoots();
            inv.setBoots(item);
        }

        if (changed != null && changed.getId() != Item.AIR){
            inv.addItem(changed);
        }
        return true;
    }

    private void dropDeathItems(Player player, Item[] oldDrops){
        if (player.hasPermission(configFile.getString("permission-keepInvAll")) || player.isOp()){
            return;
        }

        List<Item> keep = new ArrayList<>();
        boolean keepTools = player.hasPermission(configFile.getString("permission-keepinvTools"));

        PlayerInventory inv = player.getInventory();
        inv.clearAll();

        for (Item item : oldDrops){
            if (item.isArmor() || (item.isTool() && keepTools)){
                keep.add(item);
            }else {
                player.getLevel().dropItem(player, item, null, true, 40);
            }
        }
        inv.addItem(keep.toArray(new Item[0]));
    }

    public void tpa(Player executor, String player){
        Player pplayer = Server.getInstance().getPlayer(player);

        if (pplayer == null || !pplayer.isConnected()){
            executor.sendMessage("§6»§7Player §6@"+player+"§7 is not online!");
            return;
        }
        this.tpa.put(pplayer.getName(), executor.getName());

        String message = configFile.getString("tpaMessage");
        message = message.replace("{player}", pplayer.getName());
        executor.sendMessage(message);

        String rmessage = configFile.getString("tpaRequestMessage");
        rmessage = rmessage.replace("{player}", executor.getName());
        pplayer.sendMessage(rmessage);
    }

    public void tpaAccept(Player player){
        if (!tpa.containsKey(player.getName()) || tpa.get(player.getName()) == null){
            String message = configFile.getString("tpaNoRequests").replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        Player requester = Server.getInstance().getPlayer(tpa.get(player.getName()));
        if (requester == null || !requester.isConnected()){
            player.sendMessage("§cPlayer is not online!");
        }else {
            requester.teleport(player);

            String message = configFile.getString("tpaAcceptMessage").replace("{player}", player.getName());
            requester.sendMessage(message);
        }

        tpa.remove(player.getName());
    }

    public void tpaDenny(Player player){
        if (!tpa.containsKey(player.getName())){
            return;
        }

        if (tpa.get(player.getName()) != null){
            Player requester = Server.getInstance().getPlayer(tpa.get(player.getName()));
            if (requester != null && requester.isConnected()){
                String message = configFile.getString("tpaDennyMessage").replace("{player}", player.getName());
                requester.sendMessage(message);
            }
        }

        tpa.remove(player.getName());

        String message = configFile.getString("tpaDennyConfirmMessage").replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void fly(Player player, String executor){
        Player pexecutor = Server.getInstance().getPlayer(executor);
        if (!checkForPlayer(player, pexecutor)) return;

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-fly"))){
            player.sendMessage("§cYou dont have permission to fly!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null && (!pexecutor.hasPermission(configFile.getString("permission-manage")) || !pexecutor.hasPermission(configFile.getString("permission-manage")))) {
            pexecutor.sendMessage("§cYou dont have permission to give fly!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())){
            pexecutor.sendMessage("§6»§7You changed flying mode of §6@"+player.getName()+"§7!");
        }

        boolean canFly = player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
        player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, !canFly);
        player.getAdventureSettings().update();

        String message = configFile.getString("flyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", (!canFly ? "on" : "off"));
        player.sendMessage(message);
    }


    public void feed(Player player, String executor){
        Player pexecutor = Server.getInstance().getPlayer(executor);
        if (!checkForPlayer(player, pexecutor)) return;

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-feed"))){
            player.sendMessage("§cYou dont have permission to feed!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null && (!pexecutor.hasPermission(configFile.getString("permission-manage")) || !pexecutor.hasPermission(configFile.getString("permission-manage")))){
            pexecutor.sendMessage("§cYou dont have permission to give feed to player!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())){
            pexecutor.sendMessage("§6»§7You feeded §6@"+player.getName()+"§7!");
        }

        player.getFoodData().reset();

        String message = configFile.getString("feedMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", (String.valueOf(player.getFoodData().getLevel())));
        player.sendMessage(message);
    }

    public void heal(Player player, String executor){
        Player pexecutor = Server.getInstance().getPlayer(executor);
        if (!checkForPlayer(player, pexecutor)) return;

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-heal"))){
            player.sendMessage("§cYou dont have permission to heal yourself!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null && (!pexecutor.hasPermission(configFile.getString("permission-manage")) || !pexecutor.hasPermission(configFile.getString("permission-manage")))){
            pexecutor.sendMessage("§cYou dont have permission to heal player!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())){
            pexecutor.sendMessage("§6»§7You healed §6@"+player.getName()+"§7!");
        }

        player.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(1).setDuration(5 * 20));

        String message = configFile.getString("healMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", (String.valueOf(player.getHealth())));
        player.sendMessage(message);
    }

    public void back(Player player){
        Location location = this.back.get(player.getName());
        if (location == null){
            String message = configFile.getString("backPosNotFound");
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        this.back.remove(player.getName());
        player.teleport(location);

        String message = configFile.getString("backMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public List<Player> getNearPlayers(Location pos, int radius){
        Level level = pos.getLevel();
        List<Player> players = new ArrayList<>();

        for (int x = -radius; x < radius; x++){
            for (int z = -radius; z < radius; z++){
                players.addAll(level.getChunkPlayers(pos.getChunkX()+x, pos.getChunkZ()+z).values());
            }
        }
        return players.stream().distinct().collect(Collectors.toList());
    }

    public void jump(Player player){
        if (!player.hasPermission(configFile.getString("permission-jump"))){
            player.sendMessage("§cYou dont have permission to jump!");
            return;
        }

        int power = configFile.getInt("jumpPower");
        int x = 0;
        int z = 0;

        switch (player.getDirection().getIndex()){
            case 2:
                z = -power;
                break;
            case 3:
                z = +power;
                break;
            case 4:
                x = -power;
                break;
            case 5:
                x = +power;
                break;
        }
        Vector3 motion = new Vector3(x, power, z);


        SpawnParticleEffectPacket packet = new SpawnParticleEffectPacket();
        packet.position = player.asVector3f();
        packet.dimensionId = player.getLevel().getDimension();
        packet.identifier = "minecraft:water_evaporation_bucket_emitter";
        for (Player pplayer : player.getViewers().values()){ pplayer.dataPacket(packet);
}
        player.addEffect(Effect.getEffect(Effect.DAMAGE_RESISTANCE).setAmplifier(100).setDuration(20*5).setVisible(false));
        player.setMotion(motion);

        String message = configFile.getString("jumpMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void randomTp(Player player){
        if (!player.hasPermission(configFile.getString("permission-randtp"))){
            player.sendMessage("§cYou dont have permission to teleport randomly!");
            return;
        }

        Integer lastTp = this.randTpDelay.get(player.getName());
        int now = Server.getInstance().getTick();

        if (lastTp != null && (now - lastTp < (20 * 10))){
            player.sendMessage("§c»§7Please wait! you have benn teleported just few seconds ago!");
            return;
        }

        this.randTpDelay.put(player.getName(), Server.getInstance().getTick());
        player.sendMessage("§6»§7Finding nice location... This usually takes some time!");

        String message = configFile.getString("randtpMessage");
        message = message.replace("{player}", player.getName());
        this.plugin.getServer().getScheduler().scheduleDelayedTask(new RandomTpTask(player, message), 30);
    }

    public void mute(Player player, String executor, String time){
        Player pexecutor = Server.getInstance().getPlayer(executor);
        if (!checkForPlayer(player, pexecutor)) return;

        if (executor.equals(player.getName())){
            player.sendMessage("§cYou cant mute yourself!");
            return;
        }

        if (!executor.equals("console") && pexecutor != null && !pexecutor.hasPermission(configFile.getString("permission-mute"))){
            pexecutor.sendMessage("§cYou dont have permission to mute player!");
            return;
        }

        int minutes = 0;
        int seconds = 0;

        try {
            String[] data = time.split(":");
            minutes = Integer.parseInt(data[0]);

            if (data.length > 1){
                seconds = Integer.parseInt(data[1]);
            }
        }catch (Exception e){
            if (pexecutor == null) return;
            pexecutor.sendMessage("§c»§7Bad time parameter provided. Please use following format: §8mm:ss");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())){
            pexecutor.sendMessage("§6»§7You muted §6@"+player.getName()+"§7 for §8"+time+"§7!");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, seconds);

        this.mutedPlayers.put(player.getName(), calendar.getTime());

        String message = configFile.getString("muteMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{time}", time);
        player.sendMessage(message);
    }

    public void unmute(String playerName, String executor){
        Player pexecutor = Server.getInstance().getPlayer(executor);
        Player player = Server.getInstance().getPlayer(playerName);


        if (!executor.equals("console") && pexecutor != null && !pexecutor.hasPermission(configFile.getString("permission-mute"))){
            pexecutor.sendMessage("§cYou dont have permission to unmute player!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())){
            pexecutor.sendMessage("§6»§7You unmuted §6@"+player.getName()+"§7!");
        }

        this.mutedPlayers.remove(playerName);
        if (player == null) return;

        String message = configFile.getString("unmuteMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void setKeepInventory(Player player, boolean enable){
        if (player == null) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        config.set("keepInventory", enable);
        config.save();

        this.keepInvCache.put(player.getName(), enable);

        String message = configFile.getString("keepInventoryMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", enable? "on" : "off");
        player.sendMessage(message);
    }

    public boolean keepInventory(Player player){
        return player != null && this.keepInvCache.getOrDefault(player.getName(), false);
    }

    public boolean checkForPlayer(Player player, Player pexecutor){
        if (player == null){
            if (pexecutor != null){
                String message = configFile.getString("playerNotFound");
                pexecutor.sendMessage(message);
            }
            return false;
        }
        return true;
    }

    public Map<String, Date> getMutedPlayers() {
        return mutedPlayers;
    }
}

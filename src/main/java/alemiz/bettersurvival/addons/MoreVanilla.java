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

package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.*;
import alemiz.bettersurvival.tasks.RandomTpTask;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.AdventureSettings;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.Player;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.*;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import net.jodah.expiringmap.ExpiringMap;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MoreVanilla extends Addon {

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

    private final Map<String, String> tpaCache = ExpiringMap.builder().expiration(2, TimeUnit.MINUTES).build();
    private final Map<String, Location> backCache = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build();
    private final ExpiringMap<String, String> mutedPlayers = ExpiringMap.builder().variableExpiration().expirationListener(this::onMuteExpired).build();
    private final Map<String, Position> randomTpCache = ExpiringMap.builder().expiration(15, TimeUnit.SECONDS).build();

    private final Map<String, Boolean> keepInvCache = new HashMap<>();
    private final Map<String, String> keepInvDamagerCache = ExpiringMap.builder().expiration(20, TimeUnit.SECONDS).build();

    private final Map<String, Position> sleepPos = new HashMap<>();

    public MoreVanilla(String path) {
        super("morevanilla", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
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
            configFile.set("tpaMessage", "§6»§7Teleport request was sent to @{player}§7! It will expire after §62§7 minutes!");
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
        this.registerCommand("tpa", new TpaCommand("tpa", this));
        this.registerCommand("fly", new FlyCommand("fly", this));
        this.registerCommand("heal", new HealCommand("heal", this));
        this.registerCommand("feed", new FeedCommand("feed", this));
        this.registerCommand("back", new BackCommand("back", this));
        this.registerCommand("near", new NearCommand("near", this));
        this.registerCommand("jump", new JumpCommand("jump", this));
        this.registerCommand("rand", new RandCommand("rand", this));
        this.registerCommand("mute", new MuteCommand("mute", this));
        this.registerCommand("unmute", new UnmuteCommand("unmute", this));
        if (configFile.getBoolean("keepInvCommand")) {
            this.registerCommand("keepinv", new KeepInvCommand("keepinv", this));
        }
    }

    @EventHandler
    public void onLevelLoad(LevelLoadEvent event) {
        GameRules gameRules = event.getLevel().getGameRules();
        gameRules.setGameRule(GameRule.SHOW_COORDINATES,
                configFile.getBoolean("showCoordinates", true));
        gameRules.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN,
                configFile.getBoolean("doImmediateRespawn", true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Set Default Permissions
        player.addAttachment(this.plugin, configFile.getString("permission-tpa"), true);
        player.addAttachment(this.plugin, configFile.getString("permission-back"), true);
        player.addAttachment(this.plugin, configFile.getString("permission-randtp"), true);

        if (configFile.getBoolean("keepInvCommand")){
            Config config = ConfigManager.getInstance().loadPlayer(player);
            this.keepInvCache.put(player.getName(), config.getBoolean("keepInventory"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.backCache.remove(player.getName());
        this.randomTpCache.remove(player.getName());
        this.sleepPos.remove(player.getName());
        this.keepInvCache.remove(player.getName());
    }

    @EventHandler
    public void onTell(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/tell")){
            return;
        }

        Player player = event.getPlayer();
        if (this.onMuteChat(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.onMuteChat(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        this.sleepPos.put(player.getName(), player.clone());
    }

    @EventHandler
    public void onWakeUp(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        Position spawn = this.sleepPos.remove(player.getName());
        if (spawn == null) {
            spawn = player.getLevel().getSafeSpawn(player.getSpawn());
        }
        player.teleport(spawn);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        // Store last damager of player
        this.keepInvDamagerCache.put(player.getName(), damager.getName());

        boolean canFly = damager.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
        if (canFly && !damager.isCreative() && !damager.isOp()){
            damager.getAdventureSettings().set(AdventureSettings.Type.FLYING, false);
            damager.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, false);
            damager.getAdventureSettings().update();
            damager.sendMessage("§c»§7You can not attack player while flying!");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        this.backCache.put(player.getName(), player.getLocation());
        player.sendMessage("§6»§7Your death position was saved! You have §65§7 minutes to return!");

        // Keep inventory check
        if (!configFile.getBoolean("keepInvCommand") || !this.keepInventory(player)) {
            return;
        }

        String lastDamager = this.keepInvDamagerCache.remove(player.getName());
        DamageCause cause = player.getLastDamageCause().getCause();

        if (lastDamager != null || (cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.ENTITY_EXPLOSION)) {
            event.setKeepInventory(true);
            this.dropDeathItems(player, event.getDrops());
        }
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event) {
        if (!(event.getWindow() instanceof FormWindowSimple) || event.getResponse() == null) {
            return;
        }

        FormWindowSimple form = (FormWindowSimple) event.getWindow();
        Player player = event.getPlayer();

        if (form.getTitle().equals("Player Teleport")) {
            String response = form.getResponse().getClickedButton().getText();
            response = response.substring(2, response.indexOf("\n"));
            this.tpa(player, response);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        if (!item.isAxe() && !item.isPickaxe() &&
                !item.isSword() && !item.isShovel()) {
            return;
        }

        CompoundTag namedTag = item.getNamedTag() == null ? new CompoundTag() : item.getNamedTag();
        int broken = namedTag.getInt("brokenBlocks") + 1;

        namedTag.putInt("brokenBlocks", broken);
        item.setNamedTag(namedTag);
        item.setLore("§r§5Block Destroyed: "+broken);
        player.getInventory().setItemInHand(item);
    }

    private void dropDeathItems(Player player, Item[] oldDrops) {
        if (player.hasPermission(configFile.getString("permission-keepInvAll"))) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        inv.clearAll();

        List<Item> keep = new ArrayList<>();
        boolean keepTools = player.hasPermission(configFile.getString("permission-keepinvTools"));
        for (Item item : oldDrops) {
            if (item.isArmor() || (item.isTool() && keepTools)) {
                keep.add(item);
            } else {
                player.getLevel().dropItem(player, item, null, true, 40);
            }
        }
        inv.addItem(keep.toArray(new Item[0]));
    }

    public void tpa(Player executor, String player) {
        Player pplayer = this.plugin.getServer().getPlayer(player);
        if (pplayer == null || !pplayer.isConnected()) {
            executor.sendMessage("§6»§7Player §6@" + player + "§7 is not online!");
            return;
        }

        this.tpaCache.put(pplayer.getName(), executor.getName());

        String message = configFile.getString("tpaMessage");
        message = message.replace("{player}", pplayer.getDisplayName());
        executor.sendMessage(message);

        String rmessage = configFile.getString("tpaRequestMessage");
        rmessage = rmessage.replace("{player}", executor.getDisplayName());
        pplayer.sendMessage(rmessage);
    }

    public void tpaAccept(Player player) {
        String targetName = this.tpaCache.remove(player.getName());
        if (targetName == null) {
            String message = configFile.getString("tpaNoRequests").replace("{player}", player.getDisplayName());
            player.sendMessage(message);
            return;
        }

        Player target = this.plugin.getServer().getPlayer(targetName);
        if (target == null || !target.isConnected()) {
            player.sendMessage("§cPlayer is not online!");
        } else {
            target.teleport(player);

            String message = configFile.getString("tpaAcceptMessage").replace("{player}", player.getDisplayName());
            target.sendMessage(message);
        }
    }

    public void tpaDenny(Player player) {
        String targetName = this.tpaCache.remove(player.getName());
        if (targetName == null) {
            return;
        }

        Player target = this.plugin.getServer().getPlayer(targetName);
        if (target != null && target.isConnected()){
            String message = configFile.getString("tpaDennyMessage").replace("{player}", player.getDisplayName());
            target.sendMessage(message);
        }

        String message = configFile.getString("tpaDennyConfirmMessage").replace("{player}", player.getDisplayName());
        player.sendMessage(message);
    }

    public void fly(Player player, String executor) {
        Player pexecutor = this.plugin.getServer().getPlayer(executor);
        if (!this.checkForPlayer(player, pexecutor)) {
            return;
        }

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-fly"))) {
            player.sendMessage("§cYou dont have permission to fly!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null &&
                !pexecutor.hasPermission(configFile.getString("permission-manage"))) {
            pexecutor.sendMessage("§cYou dont have permission to give fly!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())) {
            pexecutor.sendMessage("§6»§7You changed flying mode of §6@"+player.getDisplayName()+"§7!");
        }

        boolean canFly = player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
        player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, !canFly);
        player.getAdventureSettings().update();

        String message = configFile.getString("flyMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{state}", (!canFly ? "on" : "off"));
        player.sendMessage(message);
    }


    public void feed(Player player, String executor) {
        Player pexecutor = this.plugin.getServer().getPlayer(executor);
        if (!this.checkForPlayer(player, pexecutor)) {
            return;
        }

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-feed"))) {
            player.sendMessage("§cYou dont have permission to feed!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null &&
                !pexecutor.hasPermission(configFile.getString("permission-manage"))) {
            pexecutor.sendMessage("§cYou dont have permission to give feed to player!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())) {
            pexecutor.sendMessage("§6»§7You feeded §6@"+player.getDisplayName()+"§7!");
        }

        player.getFoodData().reset();

        String message = configFile.getString("feedMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{state}", (String.valueOf(player.getFoodData().getLevel())));
        player.sendMessage(message);
    }

    public void heal(Player player, String executor) {
        Player pexecutor = this.plugin.getServer().getPlayer(executor);
        if (!this.checkForPlayer(player, pexecutor)) {
            return;
        }

        if (executor.equals(player.getName()) && !player.hasPermission(configFile.getString("permission-heal"))) {
            player.sendMessage("§cYou dont have permission to heal yourself!");
            return;
        }

        if (!executor.equals("console") && !executor.equals(player.getName()) && pexecutor != null &&
                !pexecutor.hasPermission(configFile.getString("permission-manage"))) {
            pexecutor.sendMessage("§cYou dont have permission to heal player!");
            return;
        }

        if (pexecutor != null && !executor.equals(player.getName())) {
            pexecutor.sendMessage("§6»§7You healed §6@"+player.getDisplayName()+"§7!");
        }

        player.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(1).setDuration(5 * 20));

        String message = configFile.getString("healMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{state}", (String.valueOf(player.getHealth())));
        player.sendMessage(message);
    }

    public void back(Player player) {
        Location location = this.backCache.remove(player.getName());
        if (location == null) {
            String message = configFile.getString("backPosNotFound");
            message = message.replace("{player}", player.getDisplayName());
            player.sendMessage(message);
            return;
        }
        player.teleport(location);

        String message = configFile.getString("backMessage");
        message = message.replace("{player}", player.getDisplayName());
        player.sendMessage(message);
    }

    public List<Player> getNearPlayers(Location pos, int radius) {
        List<Player> players = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                players.addAll(pos.getLevel().getChunkPlayers(pos.getChunkX() + x, pos.getChunkZ() + z).values());
            }
        }
        return players.stream().distinct().collect(Collectors.toList());
    }

    public void jump(Player player) {
        if (!player.hasPermission(configFile.getString("permission-jump"))) {
            player.sendMessage("§cYou dont have permission to jump!");
            return;
        }

        int power = configFile.getInt("jumpPower");
        Vector3 motion = player.getDirectionVector().multiply(power);
        player.setMotion(motion);

        player.getLevel().addParticleEffect(player, ParticleEffect.WATER_EVAPORATION_BUCKET);
        player.addEffect(Effect.getEffect(Effect.DAMAGE_RESISTANCE).setAmplifier(100).setDuration(100).setVisible(false));

        String message = configFile.getString("jumpMessage");
        message = message.replace("{player}", player.getDisplayName());
        player.sendMessage(message);
    }

    public void randomTp(Player player) {
        if (!player.hasPermission(configFile.getString("permission-randtp"))) {
            player.sendMessage("§cYou dont have permission to teleport randomly!");
            return;
        }

        Position lastPos = this.randomTpCache.get(player.getName());
        if (lastPos != null) {
            player.sendMessage("§c»§7You are teleporting too fast! Please wait few seconds.");
            return;
        }

        this.randomTpCache.put(player.getName(), player.getPosition());
        player.sendMessage("§6»§7Finding nice location... This usually takes few seconds!");

        String message = configFile.getString("randtpMessage");
        message = message.replace("{player}", player.getDisplayName());
        this.plugin.getServer().getScheduler().scheduleDelayedTask(new RandomTpTask(player, message), 30);
    }

    public void mute(Player player, String executor, String expiryString) {
        this.mute(player, executor, expiryString, null);
    }

    public void mute(Player player, String executorName, String expiryString, String reason) {
        Player executor = this.plugin.getServer().getPlayer(executorName);
        if (!this.checkForPlayer(player, executor)) {
            return;
        }

        if (executorName.equals(player.getName())) {
            player.sendMessage("§cYou cant mute yourself!");
            return;
        }

        if (!executorName.equals("console") && executor != null && !executor.hasPermission(configFile.getString("permission-mute"))) {
            executor.sendMessage("§cYou dont have permission to mute player!");
            return;
        }

        int muteTime;
        try {
            String[] data = expiryString.split(":");
            muteTime = Integer.parseInt(data[0]) * 60;
            if (data.length > 1) {
                muteTime += Integer.parseInt(data[1]);
            }
        } catch (Exception e) {
            if (executor != null) {
                executor.sendMessage("§c»§7Bad time parameter provided. Please use following format: §8mm:ss");
            }
            return;
        }

        if (executor != null && !executorName.equals(player.getName())) {
            executor.sendMessage("§6»§7You muted §6@" + player.getDisplayName() + "§7 for §8" + muteTime + "§7 seconds!");
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "unknown";
        }
        this.mutedPlayers.put(player.getName(), reason, muteTime, TimeUnit.SECONDS);

        String message = configFile.getString("muteMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{time}", String.valueOf(muteTime));
        message = message.replace("{reason}", reason);
        player.sendMessage(message);
    }

    public void unmute(String playerName, String executorName) {
        Player executor = this.plugin.getServer().getPlayer(executorName);
        if (!executorName.equals("console") && executor != null && !executor.hasPermission(configFile.getString("permission-mute"))) {
            executor.sendMessage("§cYou dont have permission to unmute player!");
            return;
        }

        String muteReason = this.mutedPlayers.remove(playerName);
        if (muteReason == null) {
            if (executor != null) {
                executor.sendMessage("§c»§cPlayer §6@" + playerName +"§7 is not muted!");
            }
            return;
        }

        if (executor != null) {
            executor.sendMessage("§6»§7You unmuted §6@"+playerName+"§7!");
        }
        this.onMuteExpired(playerName, muteReason);
    }

    private boolean onMuteChat(Player player) {
        if (this.mutedPlayers.containsKey(player.getName())) {
            String message = configFile.getString("muteChatMessage");
            message = message.replace("{player}", player.getDisplayName());
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    private void onMuteExpired(String playerName, String reason) {
        Player player = this.plugin.getServer().getPlayer(playerName);
        if (player != null) {
            String message = configFile.getString("unmuteMessage");
            message = message.replace("{player}", player.getDisplayName());
            player.sendMessage(message);
        }
    }

    public void setKeepInventory(Player player, boolean enable) {
        if (player == null) {
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        config.set("keepInventory", enable);
        config.save();

        this.keepInvCache.put(player.getName(), enable);

        String message = configFile.getString("keepInventoryMessage");
        message = message.replace("{player}", player.getDisplayName());
        message = message.replace("{state}", enable? "on" : "off");
        player.sendMessage(message);
    }

    public boolean keepInventory(Player player) {
        return player != null && this.keepInvCache.getOrDefault(player.getName(), false);
    }

    public boolean checkForPlayer(Player player, Player pexecutor) {
        if (player == null) {
            if (pexecutor != null) {
                String message = configFile.getString("playerNotFound");
                pexecutor.sendMessage(message);
            }
            return false;
        }
        return true;
    }
}

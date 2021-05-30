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

package alemiz.bettersurvival.addons.quests;

import alemiz.bettersurvival.addons.quests.forms.QuestMenuForm;
import alemiz.bettersurvival.commands.QuestCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.TimeUtils;
import alemiz.bettersurvival.utils.enitity.FakeHuman;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SurvivalQuests extends Addon {

    private final Map<String, Quest> questMap = new HashMap<>();
    private final Map<String, QuestIngredient> questIngredientMap = new HashMap<>();
    private final Map<String, PlayerQuestData> playerQuestDataMap = new HashMap<>();

    private final IntSet loggingItems = new IntOpenHashSet();
    private final ObjectList<String> loggingEntities = new ObjectArrayList<>();

    private int questHourInterval;
    private Quest actualQuest;
    private boolean particles;
    private int particleDelay;

    private Position questMasterSpawn;

    public SurvivalQuests(String path) {
        super("survivalquests", path);
    }

    @Override
    public void postLoad() {
        JsonElement json = this.getJsonResource("quests.json");
        if (!json.isJsonObject()){
            this.setEnabled(false);
            return;
        }
        JsonObject jsonObject = json.getAsJsonObject();

        JsonObject ingredientJson = jsonObject.getAsJsonObject("ingredients");
        for (Map.Entry<String, JsonElement> entry : ingredientJson.entrySet()){
            String ingredientId = entry.getKey();
            try {
                QuestIngredient ingredient = QuestIngredient.fromJson(ingredientId, entry.getValue().getAsJsonObject());
                if (ingredient == null){
                 this.plugin.getLogger().warning("§cUnable to load QuestIngredient "+ingredientId+"!");
                }else {
                    this.registerIngredient(ingredient);
                }
            }catch (Exception e){
                this.plugin.getLogger().warning("§cUnable to load ingredient "+ingredientId+"!");
            }
        }

        JsonObject questsJson = jsonObject.getAsJsonObject("quests");
        for (Map.Entry<String, JsonElement> entry : questsJson.entrySet()){
            String questId = entry.getKey();
            try {
                Quest quest = Quest.fromJson(questId, entry.getValue().getAsJsonObject(), this);
                this.registerQuest(quest);
            }catch (Exception e){
                this.plugin.getLogger().warning("§cUnable to load quest "+questId+"!");
            }
        }

        this.questHourInterval = configFile.getInt("questInterval");
        this.particles = configFile.getBoolean("particles");
        this.particleDelay = configFile.getInt("particlesDelay", 1200);

        this.questMasterSpawn = this.plugin.getServer().getDefaultLevel().getSafeSpawn();
        if (configFile.exists("questMasterSpawn")){
            String[] data = configFile.getString("questMasterSpawn").split(",");
            Level level = this.plugin.getServer().getLevelByName(data[3]);
            this.questMasterSpawn = new Position(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), level);
        }

        this.actualQuest = this.getQuest(configFile.getString("lastQuestId"));
        if (!this.refreshQuest()){
            String lastRefreshString = configFile.getString("lastQuestRefresh");
            ZonedDateTime lastRefresh = ZonedDateTime.parse(lastRefreshString, TimeUtils.DATE_TIME_FORMATTER);
            this.actualQuest.setValidTime(lastRefresh.plusHours(this.questHourInterval));
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);
            configFile.set("lastQuests", new ArrayList<>());
            configFile.set("questInterval", 24);
            configFile.set("particles", true);
            configFile.set("particlesDelay", 1200);

            configFile.set("questManagePermission", "bettersurvival.quest.manage");

            configFile.set("newQuestMessage", "§a»§7New daily quest has been chosen! Visit §eQuestMaster§7 to complete the quest!");
            configFile.set("questMenuMessage", "§7Welcome at QuestMaster! How's your quest going, {player}? Haven't you started yet?\n" +
                    "§rStart by opening quest offer! Come back once you have completed it to claim your reward! You have only one day!");
            configFile.set("completedQuestMessage", "§a»§7Congratulations! You have completed my §6'{quest}'§7 quest! You will get your reward!");
            configFile.save();
        }
        this.saveFromResources("quests.json");
    }

    @Override
    public void registerCommands() {
        this.registerCommand("quest", new QuestCommand("quest", this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        Config config = ConfigManager.getInstance().loadPlayer(player);
        PlayerQuestData questData = PlayerQuestData.createFrom(player, config);
        this.playerQuestDataMap.put(player.getName(), questData);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        PlayerQuestData questData = this.playerQuestDataMap.remove(player.getName());
        if (questData != null){
            questData.save();
        }
    }

    @EventHandler
    public void onQuestMaster(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)){
            return;
        }
        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();

        CompoundTag nbt = entity.namedTag;
        if (nbt.getByte("quest_npc") != 1 || nbt.getByte("quest_master") != 1){
            return;
        }

        new QuestMenuForm(player, this).buildForm().sendForm();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event){
        if (event.isCancelled()) return;

        Item result = event.getRecipe().getResult();
        int resultId = result.getId();
        if (!this.loggingItems.contains(resultId)){
            return;
        }

        PlayerQuestData questData = this.getQuestData(event.getPlayer());
        if (questData != null){
            questData.onItemCraft(resultId, result.getCount(), this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDamageByEntityEvent event){
        if (event.isCancelled() || !(event.getDamager() instanceof Player)){
            return;
        }
        Entity entity = event.getEntity();
        String entityId = entity.getSaveId().toLowerCase();
        if ((entity.getHealth() - event.getFinalDamage()) >= 1 || !this.loggingEntities.contains(entityId)){
            return;
        }

        PlayerQuestData questData = this.getQuestData((Player) event.getDamager());
        if (questData != null){
            questData.onEntityKill(entityId, this);
        }
    }

    public void spawnQuestMaster(Player player){
        if (player == null) return;
        if (!player.hasPermission(configFile.getString("questManagePermission"))){
            player.sendMessage("§cYou do not have permission to spawn QuestMaster!");
        }

        FakeHuman entity = FakeHuman.createEntity(player, "§l§eQuest Master\n§r§aCheck daily quest!\n§r§7»Punch me!", player.getSkin(), player);
        entity.setNameTagAlwaysVisible(true);

        entity.namedTag.putByte("quest_npc", 1);
        entity.namedTag.putByte("quest_master", 1);
        entity.spawnToAll();
        entity.saveNBT();
    }

    public boolean registerIngredient(QuestIngredient ingredient){
        if (ingredient == null || this.questIngredientMap.containsKey(ingredient.getIngredientId())){
            return false;
        }

        if (ingredient.getType() == QuestIngredient.TYPE.CRAFT && !this.loggingItems.contains(ingredient.getItem().getId())){
            this.loggingItems.add(ingredient.getItem().getId());
        }else if (ingredient.getType() == QuestIngredient.TYPE.KILL){
            String entity = ingredient.getEntityName().toLowerCase();
            if (!this.loggingEntities.contains(entity)){
                this.loggingEntities.add(entity);
            }
        }
        this.questIngredientMap.put(ingredient.getIngredientId(), ingredient);
        return true;
    }

    public QuestIngredient getIngredient(String ingredientId){
        return this.questIngredientMap.get(ingredientId.toLowerCase());
    }

    public boolean registerQuest(Quest quest){
        if (quest == null || this.questMap.containsKey(quest.getQuestId())){
            return false;
        }
        this.questMap.put(quest.getQuestId(), quest);
        return true;
    }

    public Quest getQuest(String questId){
        return questId == null || questId.isEmpty()? null : this.questMap.get(questId);
    }

    public boolean refreshQuest(){
        String lastRefreshString = configFile.getString("lastQuestRefresh");
        ZonedDateTime today = TimeUtils.now();

        if (this.actualQuest != null && !lastRefreshString.isEmpty()){
            ZonedDateTime lastRefresh = ZonedDateTime.parse(lastRefreshString, TimeUtils.DATE_TIME_FORMATTER);
            if (lastRefresh.plusHours(this.questHourInterval).isAfter(today)){
                return false;
            }
        }

        Quest quest = this.getRandomQuestByLast();
        if (quest == null) return false;
        this.setActualQuest(quest, today.plusHours(this.questHourInterval));

        configFile.set("lastQuestRefresh", today.format(TimeUtils.DATE_TIME_FORMATTER));
        configFile.set("lastQuestId", quest.getQuestId());
        this.addToLastQuests(quest.getQuestId(), false);
        configFile.save();

        String message = configFile.getString("newQuestMessage");
        message = message.replace("{quest}", quest.getQuestName());
        this.plugin.getServer().broadcastMessage(message);
        return true;
    }

    private Quest getRandomQuestByLast(){
        List<String> allQuests = new ArrayList<>(this.questMap.keySet());
        List<String> lastQuests = configFile.getStringList("lastQuests");
        if (!lastQuests.isEmpty()){
            allQuests.removeAll(lastQuests);
            if (allQuests.isEmpty()){
                this.clearLastQuests();
                allQuests = new ArrayList<>(this.questMap.keySet());
            }
        }

        int index = ThreadLocalRandom.current().nextInt(allQuests.size());
        return this.getQuest(allQuests.get(index));
    }

    public void clearLastQuests(){
        configFile.set("lastQuests", new ArrayList<>());
        configFile.save();
    }

    public void addToLastQuests(String questId, boolean save){
        List<String> lastQuests = new ArrayList<>(configFile.getStringList("lastQuests"));
        lastQuests.add(questId);
        configFile.set("lastQuests", lastQuests);
        if (save) configFile.save();
    }

    public void setActualQuest(Quest actualQuest, ZonedDateTime validTime) {
        if (this.actualQuest != null){
            this.actualQuest.setValidTime(null);
        }
        actualQuest.setValidTime(validTime);
        this.actualQuest = actualQuest;
    }

    public Quest getActualQuest() {
        return this.actualQuest;
    }

    public boolean isCompletedQuest(Player player, Quest quest){
        if (player == null || quest == null) return false;
        Config config = ConfigManager.getInstance().loadPlayer(player);
        String completedTime = config.getString("completedQuests."+quest.getQuestId());
        if (completedTime.isEmpty()){
            return false;
        }
        ZonedDateTime completedAt = ZonedDateTime.parse(completedTime, TimeUtils.DATE_TIME_FORMATTER);
        return completedAt.isBefore(quest.getValidTime()) && completedAt.isAfter(quest.getValidTime().minusHours(this.questHourInterval));
    }

    public void setQuestCompleted(Player player, Quest quest){
        if (player == null || quest == null) return;
        Config config = ConfigManager.getInstance().loadPlayer(player);
        config.set("completedQuests."+quest.getQuestId(), TimeUtils.now().format(TimeUtils.DATE_TIME_FORMATTER));
        config.save();

        PlayerQuestData questData = this.getQuestData(player);
        if (questData != null){
            questData.clear();
        }
    }

    public void setQuestMasterSpawn(Position pos){
        String[] data = new String[]{String.valueOf((int) pos.getX()), String.valueOf((int) pos.getY()), String.valueOf((int) pos.getZ()), pos.getLevel().getFolderName()};
        this.questMasterSpawn = pos;

        this.configFile.set("questMasterSpawn", String.join(",", data));
        this.configFile.save();
    }

    public void teleportToSpawn(Player player){
        if (player != null && this.questMasterSpawn != null){
            player.teleport(this.questMasterSpawn);
        }
    }

    public PlayerQuestData getQuestData(Player player){
        if (player == null){
            return null;
        }
        return this.playerQuestDataMap.get(player.getName());
    }

    public boolean useParticles() {
        return this.particles;
    }

    public int getParticleDelay() {
        return this.particleDelay;
    }
}

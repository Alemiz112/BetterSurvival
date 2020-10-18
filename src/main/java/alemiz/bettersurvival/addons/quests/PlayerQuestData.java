/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival.addons.quests;

import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.TimeUtils;
import cn.nukkit.Player;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerQuestData {

    public static PlayerQuestData createFrom(Player player, Config config){
        ConfigSection section = config.getSection("questData");
        PlayerQuestData questData = new PlayerQuestData(player);

        String createdAt = section.getString("dataCreatedAt");
        if (createdAt.isEmpty()){ //Data was not created
            questData.createdAt = TimeUtils.now().format(TimeUtils.DATE_TIME_FORMATTER);
            return questData;
        }
        questData.createdAt = createdAt;

        if (section.exists("craftMap")){
            ConfigSection craftSection = section.getSection("craftMap");
            for (String key : craftSection.getKeys(false)){
                try {
                    int itemId = Integer.parseInt(key);
                    int count = craftSection.getInt(key);
                    questData.setCraftCount(itemId, count);
                }catch (NumberFormatException e){}
            }
        }

        if (section.exists("killMap")){
            ConfigSection killSection = section.getSection("killMap");
            for (String key : killSection.getKeys(false)){
                int count = killSection.getInt(key);
                questData.setEntityKillCount(key, count);
            }
        }
        return questData;
    }

    private final Player player;
    private String createdAt;

    private final Int2ObjectMap<AtomicInteger> craftCountMap = new Int2ObjectOpenHashMap<>();
    private final Map<String, AtomicInteger> killCountMap = new HashMap<>();

    public PlayerQuestData(Player player){
        this.player = player;
    }

    public void serializeInto(Config config){
        config.set("questData.dataCreatedAt", this.createdAt);

        if (!this.craftCountMap.isEmpty()){
            for (Int2ObjectMap.Entry<AtomicInteger> entry : this.craftCountMap.int2ObjectEntrySet()){
                config.set("questData.craftMap."+entry.getIntKey(), entry.getValue().get());
            }
        }else {
            ((Map<?, ?>) config.get("questData")).remove("craftMap");
        }


        if (!this.killCountMap.isEmpty()){
            for (Map.Entry<String, AtomicInteger> entry : this.killCountMap.entrySet()){
                config.set("questData.killMap."+entry.getKey(), entry.getValue());
            }
        }else {
            ((Map<?, ?>) config.get("questData")).remove("killMap");
        }
    }

    public void save(){
        Config config = ConfigManager.getInstance().loadPlayer(this.player);
        this.serializeInto(config);
        config.save();
    }

    public void clear(){
        this.createdAt = TimeUtils.now().format(TimeUtils.DATE_TIME_FORMATTER);
        this.craftCountMap.clear();
        this.killCountMap.clear();
        this.save();
    }

    public int onItemCraft(int itemId, int count, SurvivalQuests loader){
        AtomicInteger craftCount = this.craftCountMap.computeIfAbsent(itemId, i -> new AtomicInteger(0));
        return craftCount.addAndGet(count);
    }

    public int onEntityKill(String entityName, SurvivalQuests loader){
        AtomicInteger killCount = this.killCountMap.computeIfAbsent(entityName, i -> new AtomicInteger(0));
        return killCount.incrementAndGet();
    }


    public void setCraftCount(int itemId, int count){
        this.craftCountMap.put(itemId, new AtomicInteger(count));
    }

    public void setEntityKillCount(String entityName, int count){
        this.killCountMap.put(entityName, new AtomicInteger(count));
    }

    public int getCraftCount(int itemId){
        AtomicInteger craftCount = this.craftCountMap.get(itemId);
        return craftCount == null? 0 : craftCount.get();
    }

    public int getEntityKillCount(String entityName){
        AtomicInteger killCount = this.killCountMap.get(entityName);
        return killCount == null? 0 : killCount.get();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public Int2ObjectMap<AtomicInteger> getCraftCountMap() {
        return this.craftCountMap;
    }

    public Map<String, AtomicInteger> getKillCountMap() {
        return this.killCountMap;
    }
}

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

package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class Addon implements Listener {

    private static final Map<Class<? extends Addon>, Addon> addons = new HashMap<>();

    public static Map<Class<? extends Addon>, Addon> getAddons() {
        return addons;
    }

    public static void loadAddon(Class<? extends Addon> clazz, String configName){
        try {
            Constructor<? extends Addon> constructor = clazz.getConstructor(String.class);
            Addon addon = constructor.newInstance(configName);
            boolean enable = addon.configFile.getBoolean("enable", false);
            addon.setEnabled(enable);
            if (enable){
                Addon.addons.put(clazz, addon);
            }
        }catch (Exception e){
            BetterSurvival.getInstance().getLogger().error("Unable to enable addon: §3"+clazz.getSimpleName(), e);
        }
    }

    public static <T extends Addon> T getAddon(Class<T> clazz){
        Addon addon = Addon.addons.get(clazz);
        if (addon == null) {
            return null;
        }

        if (clazz.isAssignableFrom(addon.getClass())) {
            return (T) addon;
        }
        return null;
    }

    public static boolean isAddonEnabled(Class<? extends Addon> clazz) {
        Addon addon = Addon.addons.get(clazz);
        return addon != null && addon.isEnabled();
    }

    public static void disableAddons(){
        for (Addon addon : Addon.addons.values()){
            addon.setEnabled(false);
        }
    }


    public final BetterSurvival plugin;
    public final String name;
    public final String PATH;
    public final Config configFile;
    protected boolean enabled = false;
    protected final Map<String, Command> commands = new HashMap<>();

    public Addon(String name, String path){
        this.PATH = path;
        this.name = name;
        this.plugin = BetterSurvival.getInstance();
        this.configFile = ConfigManager.getInstance().loadAddon(this);
        this.loadConfig();
    }

    public void setEnabled(boolean enabled) {
        if (enabled && this.preLoad()){
            this.plugin.getLogger().info("§eLoading BetterSurvival addon: §3"+this.name);
            Server.getInstance().getPluginManager().registerEvents(this, this.plugin);

            this.postLoad();
            this.loadListeners();
            this.registerCommands();
        }else {
            if (this.enabled) this.plugin.getLogger().info("§eUnloading BetterSurvival addon: §3"+this.name);
            this.onUnload();
        }
        this.enabled = enabled;
    }

    public abstract void loadConfig();

    /**
     * Once addon is enabled this method is called and should be used to register commands.
     * It is recommended to use addon.registerCommand() method to assign registered command to current addon.
     */
    public void registerCommands() {
    }

    public void registerCommand(String fallbackPrefix, Command command){
        this.registerCommand(fallbackPrefix, command, true);
    }

    public boolean registerCommand(String fallbackPrefix, Command command, boolean map){
        boolean registered = this.plugin.getServer().getCommandMap().register(fallbackPrefix, command);;
        if (registered && map) {
            this.commands.put(fallbackPrefix, command);
        }
        return registered;
    }

    public boolean saveFromResources(String fileName){
        try {
            File shopFile = new File(ConfigManager.ADDONS_PATH +"/"+fileName);
            if (!shopFile.createNewFile()){
                return false;
            }
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            OutputStream outputStream = new FileOutputStream(shopFile);
            outputStream.write(buffer);
            inputStream.close();
            return true;
        }catch (Exception e){
            this.plugin.getLogger().info("§eUnable to save "+fileName+" from resources!");
        }
        return false;
    }

    public JsonElement getJsonResource(String fileName){
        return ConfigManager.getInstance().loadJson(ConfigManager.ADDONS_PATH+"/"+fileName);
    }

    /**
     * Implement in parent.
     */
    public void loadListeners(){
    }


    /**
     * Implement in parent.
     * @return if enable addon.
     */
    public boolean preLoad(){
        return true;
    }

    /**
     * Implement in parent.
     */
    public void postLoad(){
    }

    /**
     * Implement in parent.
     */
    public void onUnload(){
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Map<String, Command> getCommands() {
        return this.commands;
    }
}

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

public abstract class Addon implements Listener{

    public String PATH = null;
    public Config configFile = null;
    protected boolean enabled = false;
    protected Map<String, Command> commands = new HashMap<>();

    protected static Map<Class<?>, Addon> addons = new HashMap<>();

    public static Map<Class<?>, Addon> getAddons() {
        return addons;
    }

    public static void loadAddon(Class<?> clazz, String configName){
        try {
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Addon addon = (Addon) constructor.newInstance(configName);
            boolean enable = addon.configFile.getBoolean("enable", false);
            addon.setEnabled(enable);
            if (enable){
                Addon.addons.put(clazz, addon);
            }
        }catch (Exception e){
            BetterSurvival.getInstance().getLogger().error("Unable to enable addon: §3"+clazz.getSimpleName(), e);
        }
    }

    public static Addon getAddon(Class<?> clazz){
        return Addon.addons.get(clazz);
    }

    public static void disableAddons(){
        for (Addon addon : Addon.addons.values()){
            addon.setEnabled(false);
        }
    }


    public String name;
    public BetterSurvival plugin;

    public Addon(String name, String path){
        this.PATH = path;
        this.name = name;
        this.plugin = BetterSurvival.getInstance();
        this.configFile = ConfigManager.getInstance().loadAddon(this);
        this.loadConfig();
    }

    public void setEnabled(boolean enabled) {
        if (enabled && this.preLoad()){
            this.plugin.getLogger().info("§eLoading BetterSurvival addon: §3"+name);
            Server.getInstance().getPluginManager().registerEvents(this, this.plugin);

            this.postLoad();
            this.loadListeners();
            this.registerCommands();
        }else {
            if (this.enabled) this.plugin.getLogger().info("§eUnloading BetterSurvival addon: §3"+name);
            this.onUnload();
        }
        this.enabled = enabled;
    }

    public abstract void loadConfig();

    public void registerCommands(){
        //Should be implemented
    }

    public void registerCommand(String fallbackPrefix, Command command){
        this.registerCommand(fallbackPrefix, command, true);
    }

    public boolean registerCommand(String fallbackPrefix, Command command, boolean map){
        boolean registered = this.plugin.getServer().getCommandMap().register(fallbackPrefix, command);;

        if (registered && map) this.commands.put(fallbackPrefix, command);
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

    public void loadListeners(){
        //Implemented by parent
    }


    public boolean preLoad(){
        //Implemented by parent
        //true = load
        return true;
    }

    public void postLoad(){
        //Implemented by parent
    }

    public void onUnload(){
        //Implemented by parent
    }


    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}

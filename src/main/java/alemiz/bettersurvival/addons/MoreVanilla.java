package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.FeedCommand;
import alemiz.bettersurvival.commands.FlyCommand;
import alemiz.bettersurvival.commands.HealCommand;
import alemiz.bettersurvival.commands.TpaCommand;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.Player;
import cn.nukkit.potion.Effect;

import java.util.HashMap;
import java.util.Map;

public class MoreVanilla extends Addon{

    protected Map<String, String> tpa = new HashMap<>();

    public MoreVanilla(String path){
        super("morevanilla", path);
        registerCommands();
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);

            configFile.set("chatFormat", "§6{player} §7> {message}");
            configFile.set("playerNotFound", "§6»§7Player {player} was not found!");

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

            configFile.set("permission-feed", "bettersurvival.feed");
            configFile.set("feedMessage", "§6»§7Your feed level has been increased to {state}!");
            configFile.save();
        }
    }

    public void registerCommands(){
        if (configFile.getBoolean("enable", true)){
            plugin.getServer().getCommandMap().register("tpa", new TpaCommand("tpa", this));
            plugin.getServer().getCommandMap().register("fly", new FlyCommand("fly", this));
            plugin.getServer().getCommandMap().register("heal", new HealCommand("heal", this));
            plugin.getServer().getCommandMap().register("feed", new FeedCommand("feed", this));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        /* Set Default Permissions*/
        event.getPlayer().addAttachment(plugin, configFile.getString("permission-tpa"), true);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event){
        Player player = event.getPlayer();
        String format = configFile.getString("chatFormat");

        format = format.replace("{player}", player.getName());
        format = format.replace("{message}", event.getMessage());
        event.setFormat(format);
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

        if (!executor.equals("console") && pexecutor != null && !pexecutor.hasPermission(configFile.getString("permission-fly"))){
            pexecutor.sendMessage("§cYou dont have permission to fly!");
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

        if (!executor.equals("console") && pexecutor != null && !pexecutor.hasPermission(configFile.getString("permission-feed"))){
            pexecutor.sendMessage("§cYou dont have permission to feed!");
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

        if (!executor.equals("console") && pexecutor != null && !pexecutor.hasPermission(configFile.getString("permission-heal"))){
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


    public boolean checkForPlayer(Player player, Player pexecutor){
        if (player == null){
            if (pexecutor != null){
                String message = configFile.getString("playerNotFound");
                message = message.replace("{player}", "");
                pexecutor.sendMessage(message);
            }
            return false;
        }
        return true;
    }
}

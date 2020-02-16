package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.VanishCommand;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class Troller extends Addon {

    protected List<String> vanishPlayers = new ArrayList<>();

    public Troller(String path){
        super("troller", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("permission-vanish", "bettersurvival.troller.vanish");
            configFile.set("vanishMessage", "§6»§7Woosh! Your vanish mode was turned §6{state}!");
        }
    }

    @Override
    public void registerCommands() {
        plugin.getServer().getCommandMap().register("vanish", new VanishCommand("vanish", this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        for (String name : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(name);
            if (player == null || !player.isConnected()) continue;

            player.hidePlayer(pplayer);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        this.vanishPlayers.remove(player.getName());
        for (Player pplayer : plugin.getServer().getOnlinePlayers().values()){
            pplayer.showPlayer(player);
        }
    }

    public void vanish(Player player){
        if (!player.hasPermission(configFile.getString("permission-vanish"))){
            player.sendMessage("§cYou dont have permission to vanish!");
            return;
        }

        boolean hidden = this.vanishPlayers.contains(player.getName());
        if (!hidden) this.vanishPlayers.add(player.getName());

        for (Player pplayer : plugin.getServer().getOnlinePlayers().values()){
            if (hidden){
                pplayer.showPlayer(player);
            }else pplayer.hidePlayer(player);
        }

        String message = configFile.getString("vanishMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", ((hidden)? "off" : "on"));
        player.sendMessage(message);
    }
}

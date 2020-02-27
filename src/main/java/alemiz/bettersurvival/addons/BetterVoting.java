package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.item.Item;
import io.pocketvote.event.VoteDispatchEvent;
import io.pocketvote.event.VoteEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BetterVoting extends Addon {

    /*TODO: Implement multi-server voting system using MySql database and one server with PocketVote
    *  Must implement & create permissions system to support permission rewards - done */

    public BetterVoting(String path){
        super("bettervoting", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            //configFile.set("usePocketVote", true);

            configFile.set("rewardPermissions", new String[]{"bettersurvival.feed", "bettersurvival.jump", "bettersurvival.land.vip"});
            configFile.set("permissionsExpiry", 3); //days

            configFile.set("rewardItems", Arrays.asList("265:0:5", "322:0:1"));
            configFile.set("voteMessage", "§b@{player} §3has voted for this awesome server!");
            configFile.set("playerVoteMessage", "§6»@{player} thanks for voting! You received reward!");

            configFile.save();
        }
    }

    @EventHandler
    public void onVote(VoteEvent event){
        voteReceive(event.getPlayer());

        /* Cancel event as we handle it here*/
        event.setCancelled();
    }

    @EventHandler
    public void onVote(VoteDispatchEvent event){
        voteReceive(event.getPlayer());

        /* Cancel event as we handle it here*/
        event.setCancelled();
    }

    public void voteReceive(String username){
        Player player = this.plugin.getServer().getPlayer(username);

        if (player == null) return;

        List<String> rewards = this.configFile.getStringList("rewardItems");
        for (String reward : rewards){
            Item item = Item.fromString(reward.substring(0, reward.lastIndexOf(":")));
            item.setCount(Integer.parseInt(reward.substring(reward.lastIndexOf(":")+1)));

            player.getInventory().addItem(item);
        }

        if (Addon.getAddon("playerpermissions") != null && (Addon.getAddon("playerpermissions") instanceof PlayerPermissions)){
            List<String> permissions = this.configFile.getStringList("rewardPermissions");
            int expiry = this.configFile.getInt("permissionsExpiry");

            LocalDateTime date = LocalDateTime.from(new Date().toInstant().atZone(ZoneId.of("UTC"))).plusDays(expiry);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (String permission : permissions){
                ((PlayerPermissions) Addon.getAddon("playerpermissions")).addPermission(player, permission, date.format(formatter));
            }
        }


        String message = this.configFile.getString("playerVoteMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);

        message = this.configFile.getString("voteMessage");
        message = message.replace("{player}", player.getName());
        plugin.getServer().broadcastMessage(message);
    }

}

package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.item.Item;
import io.pocketvote.event.VoteDispatchEvent;
import io.pocketvote.event.VoteEvent;

import java.util.Arrays;
import java.util.List;

public class BetterVoting extends Addon {

    /*TODO: Implement multi-server voting system using MySql database and one server with PocketVote
    *  Must implement & create permissions system to support permission rewards */

    public BetterVoting(String path){
        super("bettervoting", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            //configFile.set("usePocketVote", true);

            //configFile.set("rewardPermissions", new String[]{"bettersurvival.feed", "bettersurvival.jump", "bettersurvival.land.vip"});
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
            item.setCount(Integer.parseInt(reward.substring(reward.lastIndexOf(":"))));

            player.getInventory().addItem(item);
        }

        //TODO: permissions

        String message = this.configFile.getString("playerVoteMessage");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);

        message = this.configFile.getString("voteMessage");
        message = message.replace("{player}", player.getName());
        plugin.getServer().broadcastMessage(message);
    }

}

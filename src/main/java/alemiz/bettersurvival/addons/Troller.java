package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.BlockCommand;
import alemiz.bettersurvival.commands.UnblockCommand;
import alemiz.bettersurvival.commands.VanishCommand;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.DummyBossBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Troller extends Addon {

    protected List<String> vanishPlayers = new ArrayList<>();
    protected Map<String, List<Block>> blocksBefore = new HashMap<>();

    public Troller(String path){
        super("troller", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("playerNotFound", "§6»§7Player {player} was not found!");
            configFile.set("blockNotFound", "§6»§7Block with id {id} was not found!");

            configFile.set("permission-vanish", "bettersurvival.troller.vanish");
            configFile.set("vanishMessage", "§6»§7Woosh! Your vanish mode was turned §6{state}!");

            configFile.set("permission-block", "bettersurvival.troller.block");
            configFile.set("blockMessage", "§6»§7Some blocks spawned around §6@{victim}§7!");
            configFile.set("unblockMessage", "§6»@{victim}§7 was freed!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        plugin.getServer().getCommandMap().register("vanish", new VanishCommand("vanish", this));
        plugin.getServer().getCommandMap().register("block", new BlockCommand("block", this));
        plugin.getServer().getCommandMap().register("unblock", new UnblockCommand("unblock", this));
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


    public void showVanishPlayers(Player player){
        for (String pname : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(pname);

            if (pplayer == null || !pplayer.isConnected()){
                this.vanishPlayers.remove(pname);
                continue;
            }

            player.showPlayer(pplayer);
        }
    }

    public void hideVanishPlayers(Player player){
        for (String pname : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(pname);

            if (pplayer == null || !pplayer.isConnected()){
                this.vanishPlayers.remove(pname);
                continue;
            }

            player.hidePlayer(pplayer);
        }
    }

    public void vanish(Player player){
        if (!player.hasPermission(configFile.getString("permission-vanish"))){
            player.sendMessage("§cYou dont have permission to vanish!");
            return;
        }

        DummyBossBar bossBar = null;
        long bossBarId = 0;
        if (Addon.getAddon("betterlobby") != null && Addon.getAddon("betterlobby").enabled){
            bossBarId = ((BetterLobby) Addon.getAddon("betterlobby")).getBossBars().get(player.getName());
            bossBar = ((BetterLobby) Addon.getAddon("betterlobby")).buildBossBar(player);
        }

        boolean hidden = this.vanishPlayers.contains(player.getName());
        if (hidden){
            this.vanishPlayers.remove(player.getName());
            hideVanishPlayers(player);
        }else {
            this.vanishPlayers.add(player.getName());
            showVanishPlayers(player);
            if (bossBar != null) bossBar.setText(bossBar.getText()+" §7- §3Vanished");
        }

        for (Player pplayer : plugin.getServer().getOnlinePlayers().values()){
            if (hidden){
                if (this.vanishPlayers.contains(pplayer.getName())){
                    player.hidePlayer(pplayer);
                    continue;
                }
                pplayer.showPlayer(player);
            }else{
                if (this.vanishPlayers.contains(pplayer.getName())){
                    player.showPlayer(pplayer);
                    continue;
                }
                pplayer.hidePlayer(player);
            }
        }

        if (bossBar != null){
            player.removeBossBar(bossBarId);
            player.createBossBar(bossBar);
        }

        String message = configFile.getString("vanishMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", ((hidden)? "off" : "on"));
        player.sendMessage(message);
    }

    public List<Block> setBlocksArround(Player player, Block block){
        Vector3 base = player.clone();
        List<Vector3> positions = new ArrayList<Vector3>(){{
            add(base.add(0, -1, 0));
            add(base.add(0, 2, 0));

            add(base.add(1, 0, 0));
            add(base.add(1, 1, 0));
            add(base.add(-1, 0, 0));
            add(base.add(-1, 1, 0));

            add(base.add(0, 0, 1));
            add(base.add(0, 1, 1));
            add(base.add(0, 0, -1));
            add(base.add(0, 1, -1));
        }};

        List<Block> blocksBefore = new ArrayList<>();
        for (Vector3 position : positions){
            blocksBefore.add(player.level.getBlock(position));
            player.getLevel().setBlock(position, block, true, true);
        }

        return blocksBefore;
    }

    public void replaceBlocks(List<Block> replaceWith){
        for (Block block : replaceWith){
            Level level = block.getLevel();
            if (level == null) continue;

            level.setBlock(new Vector3(block.x, block.y, block.z), block, true, true);
        }
    }

    public void block(Player player, String victim, String blockString){
        if (!player.hasPermission(configFile.getString("permission-block"))){
            player.sendMessage("§cYou dont have permission to block player!");
            return;
        }

        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player)) return;

        Block block = null;
        Item item = Item.fromString(blockString);
        if (item != null) block = item.getBlock();

        if (block == null){
            String message = configFile.getString("blockNotFound");
            message = message.replace("{id}", blockString);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        this.blocksBefore.put(pvictim.getName(), setBlocksArround(pvictim, block));

        String message = configFile.getString("blockMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void unblock(Player player, String victim){
        if (!player.hasPermission(configFile.getString("permission-block"))){
            player.sendMessage("§cYou dont have permission to block player!");
            return;
        }

        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player)) return;

        List<Block> blocksBefore = this.blocksBefore.get(pvictim.getName());
        if (blocksBefore != null){
            replaceBlocks(blocksBefore);
        }else {
            Block block = Block.get(Block.AIR);
            setBlocksArround(pvictim, block);
        }

        String message = configFile.getString("unblockMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public boolean checkForPlayer(Player player, Player executor){
        if (player == null){
            if (executor != null){
                String message = configFile.getString("playerNotFound");
                message = message.replace("{player}", player.getName());
                executor.sendMessage(message);
            }
            return false;
        }
        return true;
    }
}

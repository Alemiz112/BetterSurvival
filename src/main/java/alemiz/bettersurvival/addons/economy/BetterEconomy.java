package alemiz.bettersurvival.addons.economy;

import alemiz.bettersurvival.commands.BankCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Items;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.economyapi.EconomyAPI;
import org.apache.commons.lang3.ArrayUtils;

public class BetterEconomy extends Addon {

    private static final Item bankNote;

    static {
        Item item = Item.get(Item.PAPER, 0, 1);
        item.setCustomName("§r§eBank Note");
        item.getNamedTag().putByte("economy_note", 1);
        item.setLore("§r§5Use §d/bank apply§5 to apply note");
        bankNote = item;
    }

    public BetterEconomy(String path) {
        super("bettereconomy", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("enableWithdraw", true);
            configFile.set("maxWithdrawAmount", 50000);

            configFile.set("noteCreateMessage", "§6»§7You have successfully created Bank Note with price §e{money}$§7!");
            configFile.set("noteApplyMessage", "§6»§7Bank Note was applied to your global balance. Your new balance is §e{money}$§7!");
            configFile.set("failMessage", "§c»§7You do not have enough coins to create bank note§7!");
            configFile.set("failMessageLimit", "§c»§7Maximum limit to bank note is §e{limit}$§7!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("bank", new BankCommand("bank", this));
    }

    public void createNote(Player player, int price){
        this.createNote(player, price, false);
    }

    public void createNote(Player player, int price, boolean clanMode){
        if (player == null || price == 0) return;
        int limit = configFile.getInt("maxWithdrawAmount", 50000);

        if (price > limit){
            String message = configFile.getString("failMessageLimit");
            message = message.replace("{player}", player.getName());
            message = message.replace("{limit}", TextUtils.formatBigNumber(limit));
            player.sendMessage(message);
            return;
        }

        //TODO: include clan mode economy
        boolean success = EconomyAPI.getInstance().reduceMoney(player, price) >= 1;
        String owner = player.getName(); //may be clan name too

        if (success){
            Item item = getBankNote();
            item.setCustomName(item.getCustomName()+" §6"+TextUtils.formatBigNumber(price)+"$");
            item.setLore(ArrayUtils.addAll(new String[]{"§r§5Value: "+price+"$", "§r§5Created For: §d"+owner}, item.getLore()));

            CompoundTag tag = item.getNamedTag();
            tag.putInt("economy_value", price);
            item.setNamedTag(tag);

            if (!player.getInventory().isFull()){
                player.getInventory().addItem(item);
            }else {
                player.getLevel().dropItem(player, item);
            }
        }

        String message = configFile.getString(success? "noteCreateMessage" : "failMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", TextUtils.formatBigNumber(price));
        player.sendMessage(message);
    }

    public void applyNote(Player player, Item item){
        this.applyNote(player, item, false);
    }

    public void applyNote(Player player, Item item, boolean clanMode){
        if (player == null || item == null) return;

        if (!item.hasCompoundTag() || item.getNamedTag().getByte("economy_note") != 1 || !item.getNamedTag().contains("economy_value")){
            player.sendMessage("§c»§7This is not right, applicable Bank Note! Please hold your Bank Note in hand!");
            return;
        }

        int value = item.getNamedTag().getInt("economy_value");

        //TODO: include clan mode economy
        EconomyAPI.getInstance().addMoney(player, value);

        PlayerInventory inv = player.getInventory();
        inv.clear(inv.getHeldItemIndex());

        String message = configFile.getString("noteApplyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", TextUtils.formatBigNumber(EconomyAPI.getInstance().myMoney(player)));
        player.sendMessage(message);
    }

    public static Item getBankNote(){
        return Items.deepCopy(bankNote);
    }
}

package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.ModalForm;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

public class SmithRepairForm extends ModalForm {

    private final transient SmithShop loader;

    public SmithRepairForm(Player player, SmithShop loader){
        super("§l§8Smith Repair", "", "", "Exit");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        Item item = this.player.getInventory().getItemInHand();
        if (item.getId() == Item.AIR){
            this.setContent("§cYou do not hold any item!");
            return this;
        }

        float damage = ((float) item.getDamage() / item.getMaxDurability()) * 100;
        int price = this.loader.getPriceByDamage(damage);

        if (price == 0){
            this.setContent("§cThis item can not be repaired!");
            return this;
        }

        this.setContent("§7Item in your hand will be repaired! Price is based on item damage.\n" +
                "§7Price: §l"+price+" emeralds§r\n" +
                "§7Would you like to repair it?");
        this.setButton1("Yes");
        this.setButton2("Exit");
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null || this.getResponse().getClickedButtonText().equals("Exit")) return;
        PlayerInventory inv = player.getInventory();

        Item item = inv.getItemInHand();
        float damage = ((float) item.getDamage() / item.getMaxDurability()) * 100;
        int price = this.loader.getPriceByDamage(damage);

        if (price == 0){
            player.sendMessage("§c»§r§7This item can not be repaired!");
            return;
        }

        Item emerald = Item.get(Item.EMERALD, 0, 1);
        int balance = 0;
        for(Item slot : inv.getContents().values()){
            if (slot.getId() != emerald.getId() || !slot.getName().equals(emerald.getName())) continue;
            balance += slot.getCount();
        }

        if (balance < price){
            player.sendMessage("§c»§r§7You do not have enough emeralds to repair item!");
            return;
        }

        emerald.setCount(price);
        inv.removeItem(emerald);

        item.setDamage(0);
        inv.setItemInHand(item);

        player.sendMessage("§a»§r§7Your item was successfully repaired!");
        player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
    }
}

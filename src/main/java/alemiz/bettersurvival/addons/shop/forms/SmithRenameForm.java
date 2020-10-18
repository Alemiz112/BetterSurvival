package alemiz.bettersurvival.addons.shop.forms;

import alemiz.bettersurvival.utils.form.CustomForm;
import cubemc.commons.nukkit.utils.forms.Form;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

public class SmithRenameForm extends CustomForm {

    public SmithRenameForm(Player player){
        super();
        this.player = player;
    }

    @Override
    public Form buildForm() {
        Item item = this.player.getInventory().getItemInHand();
        this.addElement(new ElementLabel("§7Item in your hand will be renamed!"));
        this.addElement(new ElementInput("§7Enter new name:", item.getCustomName().equals("") ? item.getName() : item.getCustomName()));
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;
        String customName = this.getResponse().getInputResponse(1);

        if (customName.length() > 16){
            player.sendMessage("§c»§r§7The name length limit has been reached! Please use maximum 16 characters!");
            return;
        }

        Item item = player.getInventory().getItemInHand();
        item.setCustomName("§r§f"+customName);
        player.getInventory().setItemInHand(item);
        player.sendMessage("§a»§r§7Your item was renamed!");
        player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
    }
}

package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.utils.enitity.FakeHuman;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

public class SmithShop {

    private final SurvivalShop loader;

    public SmithShop(SurvivalShop loader){
        this.loader = loader;
    }

    public void createSmith(Player player){
        if (player == null) return;

        if (!player.hasPermission(loader.configFile.getString("shopManagePermission"))){
            player.sendMessage("§cYou do not have permission to create smith!");
            return;
        }

        FakeHuman entity = FakeHuman.createEntity(player, "§l§aSmith\n§r§7»Punch me!", player.getSkin(), player);
        entity.setNameTagAlwaysVisible(true);

        entity.namedTag.putByte("shop_npc", 1);
        entity.namedTag.putByte("shop_smith", 1);

        entity.spawnToAll();
        entity.saveNBT();
    }

    public void onDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        if (entity.namedTag.getByte("shop_smith") == 0) return;

        Player player = (Player) event.getDamager();
        this.sendMenuForm(player);
        //TODO: open form
    }

    public void onSpawn(Entity entity){
        //if (entity.namedTag.getByte("shop_smith") == 0) return;
        //Ignore now
    }

    public void sendMenuForm(Player player){
        if (player == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Smith the Man", "§7Select one of mine tools!");
        form.addButton(new ElementButton("§aRename Item\n§7»Click to open!"));
        form.addButton(new ElementButton("§eBuy Enchants\n§7»Click to open!"));
        form.addButton(new ElementButton("§cHelp\n§7»Click to open!"));

        player.showFormWindow(form);
    }

    public void handleMenu(FormWindowSimple form, Player player){
        if (player == null) return;
        String response = form.getResponse().getClickedButton().getText().split("\n")[0];

        switch (response){
            case "§aRename Item":
                this.sendRenameForm(player);
                break;
            case "§eBuy Enchants":
                break;
            case "§cHelp":
                break;
        }
    }

    public void sendRenameForm(Player player){
        if (player == null) return;

        Item item = player.getInventory().getItemInHand();
        if (item.getId() == Item.AIR){
            player.sendMessage("§c»§r§7You do not hold any item!");
            return;
        }

        FormWindowCustom form = new FormWindowCustom("§l§8Rename Item");
        form.addElement(new ElementLabel("§7Item in your hand will be renamed!"));
        form.addElement(new ElementInput("§7Enter new name:", item.getCustomName().equals("") ? item.getName() : item.getCustomName()));

        player.showFormWindow(form);
    }

    public void handleRenameForm(FormWindowCustom form, Player player){
        if (player == null) return;
        String customName = form.getResponse().getInputResponse(1);

        Item item = player.getInventory().getItemInHand();
        if (item.getId() == Item.AIR){
            player.sendMessage("§c»§r§7Can not change name of air!");
            return;
        }

        item.setCustomName("§r§f"+customName);
        player.getInventory().setItemInHand(item);
        player.sendMessage("§a»§r§7Your item was renamed!");
    }
}

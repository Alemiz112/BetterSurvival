package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.enitity.FakeHuman;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SmithShop {

    private final SurvivalShop loader;

    private List<Enchant> enchants = new ArrayList<>();
    private static Item enchantItem;

    public SmithShop(SurvivalShop loader){
        this.loader = loader;
        loader.saveFromResources("enchants.json");

        JSONObject enchantsData = ConfigManager.getInstance().loadJson(ConfigManager.getInstance().ADDONS_PATH+"/enchants.json");
        Set<String> enchants = enchantsData.keySet();

        for (String enchantName : enchants){
            if (!(enchantsData.get(enchantName) instanceof JSONObject)) continue;

            JSONObject enchantJson = (JSONObject) enchantsData.get(enchantName);
            Enchant enchant = new Enchant(enchantName, enchantJson.getAsNumber("id").intValue(), enchantJson.getAsNumber("price1").intValue(), enchantJson);
            this.enchants.add(enchant);
        }

        Item item = Item.get(Item.ENDER_EYE, 0, 1);
        item.setLore("§r§5Use /ench to apply enchant", "§r§5You must hold item in hand");
        enchantItem = item;
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
    }

    public void onSpawn(Entity entity){
        //Ignore now
    }

    public void sendMenuForm(Player player){
        if (player == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Smith the Man", "§7Select one of mine tools!");
        form.addButton(new ElementButton("§aRename Item\n§7»Click to open!"));
        form.addButton(new ElementButton("§eBuy Enchants\n§7»Click to open!"));
        form.addButton(new ElementButton("§cHelp\n§7»Click to open!"));
        form.addButton(new ElementButton("§6Enchant Split\n§7»Click to open!"));

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
                this.sendEnchantsForm(player);
                break;
            case "§6Enchant Split":
                break;
            case "§cHelp":
                this.sendHelpForm(player);
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
        player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
    }

    public void sendEnchantsForm(Player player){
        if (player == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Enchants Shop", "§7You can add special powers to items by enchanting them. TIP: Click on enchant to buy it.");
        for (Enchant enchant : this.enchants){
            form.addButton(new ElementButton("§5"+enchant.getFormattedName()+"\n§7Starting at: §8"+enchant.getBasePrice()+"$"));
        }

        form.addButton(new ElementButton("Exit"));
        player.showFormWindow(form);
    }

    public void handleEnchantsForm(FormWindowSimple form, Player player){
        if (player == null) return;
        int response = form.getResponse().getClickedButtonId();

        if (response >= this.enchants.size()) return;

        Enchant enchant = this.enchants.get(response);
        if (enchant == null){
            player.sendMessage("§c»§r§Enchantment was not found!");
            return;
        }

        this.sendEnchantLevelForm(player, enchant);
    }

    public void sendEnchantLevelForm(Player player, Enchant enchant){
        if (player == null || enchant == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Levels of "+enchant.getFormattedName()+"", "§7Please select enchantment level.");
        for (int i = 0; i < enchant.prices.length; i++){
            int level = i+1;
            form.addButton(new ElementButton("§fLevel: "+level+"§7 Price: §8"+enchant.getPrice(level)+"$\n§7»Click to Buy!"));
        }

        form.addButton(new ElementButton("Exit"));
        player.showFormWindow(form);
    }

    public void sendHelpForm(Player player){
        if (player == null) return;

        //TODO: text
        FormWindowSimple form = new FormWindowSimple("§l§8Smith Help", "§7!");
        player.showFormWindow(form);
    }

    public Enchant getEnchant(int id){
        for (Enchant enchant : this.enchants){
            if (enchant.getEnchantId() == id) return enchant;
        }

        return null;
    }

    public List<Enchant> getEnchants() {
        return this.enchants;
    }

    public static Item getEnchantItem() {
        return enchantItem.clone();
    }

}

package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.commands.EnchCommand;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.Items;
import alemiz.bettersurvival.utils.enitity.FakeHuman;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import me.onebone.economyapi.EconomyAPI;
import net.minidev.json.JSONObject;

import java.util.*;

public class SmithShop {

    private final SurvivalShop loader;

    private List<Enchant> enchants = new ArrayList<>();
    private final static Item enchantItem;

    static {
        Item item = Item.get(Item.ENDER_EYE, 0, 1);
        item.setLore("§r§5Use §d/ench§5 to apply enchant", "§r§5You must hold target item");
        item.getNamedTag().putByte("enchant_orb", 1);
        enchantItem = item;
    }

    public SmithShop(SurvivalShop loader){
        this.loader = loader;
        loader.saveFromResources("enchants.json");
        loader.registerCommand("ench", new EnchCommand("ench", this));

        JSONObject enchantsData = ConfigManager.getInstance().loadJson(ConfigManager.getInstance().ADDONS_PATH+"/enchants.json");
        Set<String> enchants = enchantsData.keySet();

        for (String enchantName : enchants){
            if (!(enchantsData.get(enchantName) instanceof JSONObject)) continue;

            JSONObject enchantJson = (JSONObject) enchantsData.get(enchantName);
            Enchant enchant = new Enchant(enchantName, enchantJson.getAsNumber("id").intValue(), enchantJson.getAsNumber("price1").intValue(), enchantJson);
            this.enchants.add(enchant);
        }
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
        form.addButton(new ElementButton("§6Repair Item\n§7»Click to open!"));
        form.addButton(new ElementButton("§cHelp\n§7»Click to open!"));

        player.showFormWindow(form);
    }

    public void handleMenu(FormWindowSimple form, Player player){
        if (player == null || form.getResponse() == null) return;
        String response = form.getResponse().getClickedButton().getText().split("\n")[0];

        switch (response){
            case "§aRename Item":
                this.sendRenameForm(player);
                break;
            case "§eBuy Enchants":
                this.sendEnchantsForm(player);
                break;
            case "§6Repair Item":
                this.sendRepairForm(player);
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
        if (player == null || form.getResponse() == null) return;
        String customName = form.getResponse().getInputResponse(1);

        if (customName.length() > 16){
            player.sendMessage("§c»§r§7The name length limit has been reached! Please use maximum 16 characters!");
            return;
        }

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
        if (player == null || form.getResponse() == null) return;
        int response = form.getResponse().getClickedButtonId();

        if (response >= this.enchants.size()) return;

        Enchant enchant = this.enchants.get(response);
        if (enchant == null){
            player.sendMessage("§c»§7Enchantment was not found!");
            return;
        }

        this.sendEnchantLevelForm(player, enchant);
    }

    public void sendEnchantLevelForm(Player player, Enchant enchant){
        if (player == null || enchant == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Levels of "+enchant.getFormattedName(), "§7Please select enchantment level.");
        for (int i = 0; i < enchant.prices.length; i++){
            int level = i+1;
            form.addButton(new ElementButton("§fLevel: "+level+"§7 Price: §8"+enchant.getPrice(level)+"$\n§7»Click to Buy!"));
        }

        form.addButton(new ElementButton("Exit"));
        player.showFormWindow(form);
    }

    public void handleEnchantLevelForm(FormWindowSimple form, Player player){
        if (player == null || form.getResponse() == null || form.getResponse().getClickedButton().getText().equals("Exit")) return;

        int level = form.getResponse().getClickedButtonId()+1;
        String enchantName = form.getTitle().substring(14);

        Enchant enchant = null;
        for (Enchant enchant1 : this.enchants){
            if (!enchant1.getFormattedName().equals(enchantName)) continue;
            enchant = enchant1;
            break;
        }

        if (enchant == null){
            player.sendMessage("§c»§7Enchantment was not found!");
            return;
        }

        if (level > enchant.prices.length){
            player.sendMessage("§c»§7You chose highest level than maximum allowed!");
            return;
        }

        int price = enchant.getPrice(level);
        boolean success = EconomyAPI.getInstance().reduceMoney(player, price) >= 1;

        if (success){
            Item item = enchant.getEnchantItem(level);
            player.getInventory().addItem(item);
        }

        String message = this.loader.messageFormat(player, (success? "messageSuccess" : "messageFail"), price);
        message = message.replace("{item}", enchantName+" Enchant");
        player.sendMessage(message);
    }

    public void sendHelpForm(Player player){
        if (player == null) return;

        FormWindowSimple form = new FormWindowSimple("§l§8Smith Help", "§7Rename Item:\n" +
                "§7To rename item you must hold it in hand. Renaming is free.\n" +
                "§7Enchant Item:\n" +
                "§7Enchantments are applied by orbs. You can trade this orbs with players. To apply enchantments on item use /ench." +
                "Than all orbs from your inventory will be applied on item. WARNING: Even enchants that are not compatible with your tool may apply." +
                "To prevent this please store orbs, that you do not want to apply in chest.\n" +
                "§7Repair Item:\n" +
                "Item in your hand will be repaired. Repair price is in Emeralds based on item damage. Repair will not reset block destroyed counter.");
        form.addButton(new ElementButton("Exit"));
        player.showFormWindow(form);
    }

    public Item enchantItem(Player player, Item target){
        if (player == null || target == null || target.getId() == Item.AIR) return null;
        PlayerInventory inv = player.getInventory();

        boolean update = false;
        List<Map.Entry<Integer, Item>> enchants = new ArrayList<>();

        for (Map.Entry<Integer, Item> entry : inv.getContents().entrySet()){
            Item item = entry.getValue();
            if (item.getNamedTag() == null ||item.getNamedTag().getByte("enchant_orb") != 1) continue;

            int enchantId = item.getNamedTag().getInt("enchant_id");
            int level = item.getNamedTag().getInt("enchant_level");

            //In case of wrong ID UnknownEnchantment will be returned
            if (level <= 0 || Enchantment.get(enchantId).getName().equals("unknown")) continue;

            for (Map.Entry<Integer, Item> enchant : new ArrayList<>(enchants)){
                int enchantId1 = enchant.getValue().getNamedTag().getInt("enchant_id");
                int level1 = enchant.getValue().getNamedTag().getInt("enchant_level");

                if (enchantId1 != enchantId || level1 >= level) continue;
                enchants.remove(enchant);
            }
            enchants.add(entry);
        }

        if (enchants.isEmpty()){
            player.sendMessage("§c»§r§7You do not have any applicable orb in your inventory!");
            return null;
        }

        for (Map.Entry<Integer, Item> entry : enchants){
            Item item = entry.getValue();
            int level = item.getNamedTag().getInt("enchant_level");
            int enchantId = item.getNamedTag().getInt("enchant_id");

            Enchant enchant = this.getEnchant(enchantId);
            if (enchant == null) continue;

            Enchantment localEnchantment = enchant.getEnchantment(level);
            Enchantment[] enchantments = target.getEnchantments();

            boolean proccess = enchantments.length == 0;

            for (Enchantment enchantment : enchantments){
                if (enchantment.getId() == localEnchantment.getId() && enchantment.getLevel() >= localEnchantment.getLevel()) continue;
                proccess = true;
            }

            if (!proccess) continue;

            update = true;
            item.setCount(item.getCount() -1);
            inv.setItem(entry.getKey(), item);
            target.addEnchantment(localEnchantment);
        }

        if (update){
            player.sendMessage("§a»§r§7Your item was successfully enchanted!");
            player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
            return target;
        }

        player.sendMessage("§6»§r§7No changes were applied on your item. Maybe incompatible enchants?");
        return null;
    }

    public void sendRepairForm(Player player){
        if (player == null) return;

        Item item = player.getInventory().getItemInHand();
        if (item.getId() == Item.AIR){
            player.sendMessage("§c»§r§7You do not hold any item!");
            return;
        }

        float damage = ((float) item.getDamage() / item.getMaxDurability()) * 100;
        int price = this.getPriceByDamage(damage);

        if (price == 0){
            player.sendMessage("§c»§r§7This item can not be repaired!");
            return;
        }

        FormWindowModal form = new FormWindowModal("§l§8Smith Repair", "§7Item in your hand will be repaired! Price is based on item damage.\n" +
                "§7Price: §l"+price+" emeralds§r\n" +
                "§7Would you like to repair it?",
                "Yes",
                "Exit");

        player.showFormWindow(form);
    }

    public void handleRepairForm(FormWindowModal form, Player player){
        if (player == null || form == null || form.getResponse() == null || form.getResponse().getClickedButtonText().equals("Exit")) return;
        PlayerInventory inv = player.getInventory();


        Item item = inv.getItemInHand();
        float damage = ((float) item.getDamage() / item.getMaxDurability()) * 100;
        int price = this.getPriceByDamage(damage);

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

    public Enchant getEnchant(int id){
        for (Enchant enchant : this.enchants){
            if (enchant.getEnchantId() == id) return enchant;
        }
        return null;
    }

    public int getPriceByDamage(float damage){
        if (damage > 0 && damage <= 15){
            return 10;
        }else if (damage > 15 && damage <= 30){
            return 20;
        }else if (damage > 30 && damage <= 50){
            return 35;
        }else if (damage > 50 && damage <= 70){
            return 60;
        }else if (damage > 70 && damage <= 90){
            return 85;
        }else if (damage > 90){
            return 125;
        }

        return 0;
    }

    public List<Enchant> getEnchants() {
        return this.enchants;
    }

    public static Item getEnchantItem() {
        return Items.deepCopy(enchantItem);
    }

    public SurvivalShop getLoader() {
        return loader;
    }
}

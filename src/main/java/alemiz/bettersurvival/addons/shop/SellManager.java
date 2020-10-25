/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alemiz.bettersurvival.addons.shop;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.shop.forms.ItemSellForm;
import alemiz.bettersurvival.addons.shop.forms.SellCategoryForm;
import alemiz.bettersurvival.utils.Addon;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;

public class SellManager {

    private SurvivalShop loader;

    public SellManager(SurvivalShop loader){
        this.loader = loader;
    }

    public void sendForm(Player player){
        new SellCategoryForm(player, this).buildForm().sendForm();
    }

    public void sendSellForm(ShopCategoryElement category, Player player){
        new ItemSellForm(player, category).buildForm().sendForm();
    }

    private Integer sellItem(Player player, Item item){
        if (player == null || item == null) return null;

        if ((Addon.getAddon(BetterEconomy.class) instanceof BetterEconomy) && BetterEconomy.isBankNote(item)){
            return null;
        }

        ShopItem shopItem = null;

        for (ShopCategory category : this.loader.getCategories().values()){
            for (ShopItem sshopItem : category.getAllItems()){
                if (!item.equals(sshopItem.getItemSample(), true, false)) continue;
                shopItem = sshopItem;
                break;
            }
        }

        if (shopItem == null || !shopItem.canBeSold()) return null;
        int price = item.getCount() * shopItem.getSellPrice();

        player.getInventory().removeItem(item);
        return price;
    }

    public void sellAll(Player player){
        if (player == null) return;
        int totalPrice = 0;

        for (Item item : player.getInventory().getContents().values()){
            if (item.isArmor()) continue;

            Integer price = this.sellItem(player, item);
            if (price != null) totalPrice += price;
        }

        EconomyAPI.getInstance().addMoney(player, totalPrice);

        String message = this.loader.configFile.getString("sellAllMessage");
        message = message.replace("{money}", String.valueOf(totalPrice));
        player.sendMessage(message);
    }

    public void sellHand(Player player){
        if (player == null) return;
        PlayerInventory inv = player.getInventory();
        Item handItem = inv.getItemInHand();

        if (handItem.getId() == Item.AIR){
            player.sendMessage("§c»§7Please hold an item in your hand!");
            return;
        }

        ShopItem shopItem = null;
        for (ShopCategory category : this.loader.getCategories().values()){
            for (ShopItem sshopItem : category.getItems()){
                if (!handItem.equals(sshopItem.getItemSample(), true, false)) continue;
                shopItem = sshopItem;
                break;
            }
        }

        if (shopItem == null){
            player.sendMessage("§c»§7Unknown item! Can not sold item in your hand.");
            return;
        }

        if (!shopItem.canBeSold()){
            player.sendMessage("§c»§7This item can not be sold!");
            return;
        }

        int basePrice = shopItem.getSellPrice();
        int count = 0;

        for (Item item : inv.getContents().values()){
            if (item.equals(handItem, true, false)){
                count += item.getCount();
                inv.removeItem(item);
            }
        }

        inv.removeItem(handItem);

        int totalPrice = count * basePrice;
        EconomyAPI.getInstance().addMoney(player, totalPrice);

        String message = this.loader.configFile.getString("sellHandMessage");
        message = message.replace("{money}", String.valueOf(totalPrice));
        player.sendMessage(message);
    }

    public SurvivalShop getLoader() {
        return this.loader;
    }
}

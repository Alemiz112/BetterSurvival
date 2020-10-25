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

package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class WarpMenu extends SimpleForm {

    private final transient MyHomes loader;
    private transient List<WarpCategory> categories;

    public WarpMenu(Player player, MyHomes loader){
        super("Warp Menu", "§7Please select category to show list of warps. You can also create your own warp!");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addButton(new ElementButton("§dAdd Warp"));

        this.categories = new ArrayList<>(this.loader.getWarpCategories().values());
        for (WarpCategory category : this.categories){
            int count = category.getWarps().size();
            this.addButton(new ElementButton("§5"+category.getFormattedName()+"\n§7Available warps: §8§l"+count));
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.categories.isEmpty() || this.getResponse() == null) return;

        if (this.getResponse().getClickedButton().getText().equals("§dAdd Warp")){
            new AddWarpForm(player, this.loader).buildForm().sendForm();
            return;
        }

        int index = this.getResponse().getClickedButtonId() - 1;
        WarpCategory category = this.categories.get(index);
        new WarpCategoryForm(player, category, this.loader).buildForm().sendForm();
    }
}

/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.form.CustomForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseData;
import cubemc.commons.nukkit.utils.forms.Form;

import java.util.ArrayList;
import java.util.List;

public class AddWarpForm extends CustomForm {

    private final transient MyHomes loader;
    private transient List<WarpCategory> categories;

    public AddWarpForm(Player player, MyHomes loader){
        super("Add Warp");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addElement(new ElementLabel("§7Player warps are places registered by players. Anyone can visit this places. You are able to create own warp too."));
        this.addElement(new ElementInput("§7Enter warp name:", "The Gardens..."));

        ElementDropdown categoryDropdown = new ElementDropdown("§7Choose category:");
        this.categories = new ArrayList<>(this.loader.getWarpCategories().values());

        for (WarpCategory category : this.categories){
            categoryDropdown.addOption(category.getFormattedName());
        }
        this.addElement(categoryDropdown);
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;

        String warpName = this.getResponse().getInputResponse(1);
        FormResponseData categoryResponse = this.getResponse().getDropdownResponse(2);

        if (warpName == null || categoryResponse == null){
            return;
        }

        int index = categoryResponse.getElementID();
        WarpCategory category = this.categories.get(index);
        this.loader.createWarp(player, warpName, category);
    }
}

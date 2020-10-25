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

package alemiz.bettersurvival.utils.form;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowCustom;

import java.util.List;

public class CustomForm extends FormWindowCustom implements Form{

    protected transient BetterSurvival plugin;
    protected transient Player player;

    public CustomForm(){
        this("");
    }

    public CustomForm(String title) {
        super(title);
        this.plugin = BetterSurvival.getInstance();
    }

    public CustomForm(String title, List<Element> contents) {
        super(title, contents);
        this.plugin = BetterSurvival.getInstance();
    }

    public CustomForm(String title, List<Element> contents, String icon) {
        super(title, contents, icon);
        this.plugin = BetterSurvival.getInstance();
    }

    public CustomForm(String title, List<Element> contents, ElementButtonImageData icon) {
        super(title, contents, icon);
        this.plugin = BetterSurvival.getInstance();
    }

    @Override
    public void sendForm() {
        if (player != null) this.player.showFormWindow(this);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}

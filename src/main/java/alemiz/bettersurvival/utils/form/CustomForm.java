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

package alemiz.bettersurvival.utils.form;

import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButtonImageData;

import java.util.ArrayList;
import java.util.List;

public class CustomForm extends cubemc.commons.nukkit.utils.forms.CustomForm {

    public CustomForm() {
        super("");
    }

    public CustomForm(String title) {
        super(title, new ArrayList());
    }

    public CustomForm(String title, List<Element> contents) {
        super(title, contents, (ElementButtonImageData)null);
    }

}

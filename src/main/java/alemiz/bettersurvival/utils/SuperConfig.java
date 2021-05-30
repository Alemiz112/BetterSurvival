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

package alemiz.bettersurvival.utils;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.io.File;

public class SuperConfig extends Config {

    private String name = "";

    public SuperConfig(String file) {
        this((String)file, -1);
        name = file;
    }

    public SuperConfig(File file) {
        this((String)file.toString(), -1);
        name = file.getName();
    }

    public SuperConfig(String file, int type) {
        super(file, type, new ConfigSection());
        name = file;
    }

    public SuperConfig(File file, int type) {
        super(file.toString(), type, new ConfigSection());
        name = file.getName();
    }

    public String getName() {
        return name;
    }
}

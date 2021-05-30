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

import cn.nukkit.utils.TextFormat;

public abstract class Command extends cn.nukkit.command.Command {

    public String usageTitle= "";
    public String usage = "";

    public boolean ignoreInHelpTexts = false;

    public Command(String name) {
        super(name);
    }

    public Command(String name, String description) {
        super(name, description);
        this.setDescription(TextFormat.clean(description));
        this.setUsageTitle(name);
    }

    public Command(String name, String description, String usageMessage) {
        super(name, description, usageMessage);
        this.setDescription(TextFormat.clean(description));
        this.setUsageTitle(name);
    }

    public Command(String name, String description, String usageMessage, String[] aliases) {
        super(name, description, usageMessage, aliases);
        this.setDescription(TextFormat.clean(description));
        this.setUsageTitle(name);
    }

    public void setUsageTitle(String name){
        this.usageTitle = ("§6"+name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase() + " Command");
    }

    public String getUsageMessage(){
        return this.usageTitle + ":\n"+ this.usage;
    }
}

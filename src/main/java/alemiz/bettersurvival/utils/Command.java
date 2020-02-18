package alemiz.bettersurvival.utils;

import cn.nukkit.utils.TextFormat;

public abstract class Command extends cn.nukkit.command.Command {

    public String usageTitle= "";
    public String usage = "";

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

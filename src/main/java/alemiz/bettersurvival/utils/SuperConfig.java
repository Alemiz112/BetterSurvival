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

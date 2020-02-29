package alemiz.bettersurvival.utils;

import cn.nukkit.event.Listener;

/**
 * This class is used to create additional listener for addon
 */

public abstract class CustomListener implements Listener {

    protected Addon parent = null;

    public CustomListener(Addon parent){
        this.parent = parent;
    }

    public Addon getParent() {
        return parent;
    }
}

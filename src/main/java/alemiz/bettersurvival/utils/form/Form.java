package alemiz.bettersurvival.utils.form;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;

public interface Form {

    default void handle(Player player){
        //Implement in parent
    }

    default Form buildForm(){
        //Implement in parent
        return this;
    }

    default void buildAndSendAsync(){
        Runnable task = () -> {
            Form form = this.buildForm();
            if (form != null) form.sendForm();
        };
        BetterSurvival plugin = BetterSurvival.getInstance();
        plugin.getServer().getScheduler().scheduleTask(plugin, task, true);
    }

    default void buildAndSend(){
        //This may be used in case when we have to wait for some result
    }

    default void buildAndSend(boolean async){
        if (!async) this.buildAndSend();
        BetterSurvival plugin = BetterSurvival.getInstance();
        plugin.getServer().getScheduler().scheduleTask(plugin, () -> this.buildAndSend(), true);
    }

    void sendForm();
}

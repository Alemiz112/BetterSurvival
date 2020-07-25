package alemiz.bettersurvival.utils.form;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.List;

public class SimpleForm extends FormWindowSimple implements Form {

    protected transient BetterSurvival plugin;
    protected transient Player player;

    public SimpleForm() {
        super("", "");
        this.plugin = BetterSurvival.getInstance();
    }

    public SimpleForm(String title, String content) {
        super(title, content);
        this.plugin = BetterSurvival.getInstance();
    }

    public SimpleForm(String title, String content, List<ElementButton> buttons) {
        super(title, content, buttons);
        this.plugin = BetterSurvival.getInstance();
    }

    @Override
    public void sendForm() {
        if (player != null) this.player.showFormWindow(this);
    }
}

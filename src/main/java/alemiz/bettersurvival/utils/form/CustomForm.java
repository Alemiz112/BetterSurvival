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
}

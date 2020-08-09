package alemiz.bettersurvival.utils.form;

import alemiz.bettersurvival.BetterSurvival;
import cn.nukkit.Player;
import cn.nukkit.form.window.FormWindowModal;

public class ModalForm extends FormWindowModal implements Form {

    protected transient BetterSurvival plugin;
    protected transient Player player;

    public ModalForm(String title, String content, String trueButtonText, String falseButtonText) {
        super(title, content, trueButtonText, falseButtonText);
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

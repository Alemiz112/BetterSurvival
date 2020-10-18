package alemiz.bettersurvival.utils.form;

public class ModalForm extends cubemc.commons.nukkit.utils.forms.ModalForm {

    public ModalForm(String title, String content) {
        super(title, content, "", "");
    }

    public ModalForm(String title, String content, String trueButtonText, String falseButtonText) {
        super(title, content, trueButtonText, falseButtonText);
    }

}

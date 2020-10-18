package alemiz.bettersurvival.utils.form;

import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButtonImageData;

import java.util.ArrayList;
import java.util.List;

public class CustomForm extends cubemc.commons.nukkit.utils.forms.CustomForm {

    public CustomForm() {
        super("");
    }

    public CustomForm(String title) {
        super(title, new ArrayList());
    }

    public CustomForm(String title, List<Element> contents) {
        super(title, contents, (ElementButtonImageData)null);
    }

}

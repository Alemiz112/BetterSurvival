package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class WarpCategoryForm extends SimpleForm {

    private final transient MyHomes loader;
    private final transient WarpCategory category;
    private transient List<PlayerWarp> warps;

    public WarpCategoryForm(Player player, WarpCategory category, MyHomes loader){
        super("", "");
        this.player = player;
        this.category = category;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.setTitle(this.category.getFormattedName() + " Warps");
        this.setContent("§7Select warp you want to visit.");

        this.warps = new ArrayList<>(this.category.getWarps().values());
        if (this.warps.isEmpty()){
            this.setContent("§7Woops! Looks like this category is empty! Add your warp here!");
            this.addButton(new ElementButton("§dAdd Warp"));
            return this;
        }

        for (PlayerWarp warp : this.warps){
            this.addButton(new ElementButton("§d"+warp.getName()+"\n§7Owner: §8"+warp.getOwner()));
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.getResponse() == null) return;

        if (this.getResponse().getClickedButton().getText().equals("§dAdd Warp")){
            new AddWarpForm(player, this.loader).buildForm().sendForm();
            return;
        }

        int index = this.getResponse().getClickedButtonId();
        PlayerWarp warp = this.warps.get(index);
        warp.teleport(player);

        String message = this.loader.configFile.getString("warpTeleport");
        message = message.replace("{player}", player.getName());
        message = message.replace("{warp}", warp.getName());
        player.sendMessage(message);
    }
}

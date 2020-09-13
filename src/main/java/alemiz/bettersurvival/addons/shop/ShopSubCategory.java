package alemiz.bettersurvival.addons.shop;

import cn.nukkit.form.element.ElementButtonImageData;
import com.google.gson.JsonObject;

public class ShopSubCategory extends ShopCategoryElement{

    private boolean useImage = false;
    private String customImage = "";
    private String imageType = ElementButtonImageData.IMAGE_DATA_TYPE_PATH;

    public ShopSubCategory(String categoryName, JsonObject data, SurvivalShop loader) {
        super(categoryName, data, loader);

        if (data.has("image")){
            this.setCustomImage(data.get("image").getAsString());
        }
    }

    public void setCustomImage(String customImage) {
        if (customImage.equals("false")){
            this.useImage = false;
            return;
        }

        this.useImage = true;
        if (customImage.startsWith("blocks/") || customImage.startsWith("items/")){
            this.customImage = "textures/"+customImage;
            return;
        }

        this.imageType = ElementButtonImageData.IMAGE_DATA_TYPE_URL;
        this.customImage = customImage;
    }

    public String getCustomImage() {
        return this.customImage;
    }

    public String getImageType() {
        return this.imageType;
    }

    public boolean canUseImage() {
        return this.useImage;
    }
}

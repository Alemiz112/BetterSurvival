/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

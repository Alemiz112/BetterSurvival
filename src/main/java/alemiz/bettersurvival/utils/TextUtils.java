package alemiz.bettersurvival.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class TextUtils {

    static public String formatBigNumber(double value) {
        NumberFormat format = NumberFormat.getInstance(new Locale("sk", "SK"));
        return format.format(value);
    }
}

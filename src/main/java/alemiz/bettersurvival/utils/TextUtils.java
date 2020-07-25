package alemiz.bettersurvival.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class TextUtils {

    static public String formatBigNumber(double value) {
        NumberFormat format = NumberFormat.getInstance(new Locale("sk", "SK"));
        return format.format(value);
    }

    public static String headerFormat(String string){
        String[] words = string.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words){
            builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        return builder.substring(0, builder.length()-1);
    }
}

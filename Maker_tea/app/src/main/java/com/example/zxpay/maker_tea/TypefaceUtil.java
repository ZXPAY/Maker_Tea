package com.example.zxpay.maker_tea;

import android.content.Context;
import android.graphics.Typeface;
import java.lang.reflect.Field;

public class TypefaceUtil {


    public static void overrideFont(Context context,
                                    String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(
                    context.getAssets(), customFontFileNameInAssets);

            final Field defaultFontTypefaceField = Typeface.class
                    .getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            System.out.println("Can not set custom font "
                    + customFontFileNameInAssets + " instead of "
                    + defaultFontNameToOverride);
        }
    }
}
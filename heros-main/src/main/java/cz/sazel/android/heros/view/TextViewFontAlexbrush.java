package cz.sazel.android.heros.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by wojta on 27.4.14.
 */
public class TextViewFontAlexbrush extends TextView {

    private static Typeface tf;

    public TextViewFontAlexbrush(Context context) {
        super(context);
        init();
    }

    public TextViewFontAlexbrush(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewFontAlexbrush(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            if (tf==null)
                tf = Typeface.createFromAsset(getContext().getAssets(), "AlexBrush-Regular.ttf");
            setTypeface(tf);
        }
    }


}

package cz.sazel.android.heros.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by wojta on 27.4.14.
 */
class TextViewFontAlexbrush : TextView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        if (!isInEditMode) {
            if (tf == null)
                tf = Typeface.createFromAsset(context.assets, "AlexBrush-Regular.ttf")
            typeface = tf
        }
    }

    companion object {

        private var tf: Typeface? = null
    }


}

package com.tomtomkenya.ludouikit.components

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.color.MaterialColors
import com.tomtomkenya.ludouikit.R

/**
 * LudoTextField wraps TextInputLayout with branded corner radius and hint styling.
 */
class LudoTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    init {
        setBoxBackgroundMode(BOX_BACKGROUND_FILLED)
        setBoxCornerRadii(
            resources.getDimension(R.dimen.ludo_corner_medium),
            resources.getDimension(R.dimen.ludo_corner_medium),
            resources.getDimension(R.dimen.ludo_corner_medium),
            resources.getDimension(R.dimen.ludo_corner_medium)
        )
        boxBackgroundColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant)
        boxStrokeColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline)
        boxStrokeWidth = resources.getDimensionPixelSize(R.dimen.ludo_textfield_stroke)
        hintTextColor = context.getColorStateList(R.color.ludo_hint_state)
    }
}

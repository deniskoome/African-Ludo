package com.tomtomkenya.ludouikit.components

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.tomtomkenya.ludouikit.R

/**
 * LudoButton centralises primary/secondary button styling for the African Ludo apps.
 * The view automatically applies the brand corner radius, typography, and ripple feedback.
 */
class LudoButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        stateListAnimator = null
        insetTop = resources.getDimensionPixelSize(R.dimen.ludo_button_inset)
        insetBottom = insetTop
        iconGravity = ICON_GRAVITY_TEXT_START
        isAllCaps = false
        cornerRadius = resources.getDimensionPixelSize(R.dimen.ludo_corner_medium)
        updateRippleColor()
    }

    private fun updateRippleColor() {
        val primary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)
        val onPrimary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary)
        val outline = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline)
        rippleColor = android.content.res.ColorStateList.valueOf(primary)
        foregroundTintList = android.content.res.ColorStateList.valueOf(onPrimary)
        strokeColor = android.content.res.ColorStateList.valueOf(outline)
        strokeWidth = resources.getDimensionPixelSize(R.dimen.ludo_button_stroke)
    }
}

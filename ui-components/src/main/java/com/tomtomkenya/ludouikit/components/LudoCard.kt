package com.tomtomkenya.ludouikit.components

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.tomtomkenya.ludouikit.R

/**
 * LudoCard standardises elevation, radius, and stroke for card surfaces.
 */
class LudoCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        radius = resources.getDimension(R.dimen.ludo_corner_large)
        cardElevation = resources.getDimension(R.dimen.ludo_card_elevation)
        useCompatPadding = true
        strokeWidth = resources.getDimensionPixelSize(R.dimen.ludo_card_stroke)
        strokeColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline)
        preventCornerOverlap = false
    }
}

package com.tomtomkenya.ludouikit.components

import android.view.View
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.tomtomkenya.ludouikit.R

/**
 * LudoSnackbar shows feedback using Material motion & colour tokens.
 */
object LudoSnackbar {

    fun show(view: View, message: CharSequence, length: Int = Snackbar.LENGTH_SHORT) {
        val snackbar = Snackbar.make(view, message, length)
        val background = MaterialColors.getColor(view, com.google.android.material.R.attr.colorInverseSurface)
        val foreground = MaterialColors.getColor(view, com.google.android.material.R.attr.colorInverseOnSurface)
        snackbar.setBackgroundTint(background)
        snackbar.setTextColor(foreground)
        snackbar.animationMode = Snackbar.ANIMATION_MODE_FADE
        snackbar.show()
    }
}

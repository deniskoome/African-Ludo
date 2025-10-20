package com.tomtomkenya.ludouikit.components

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tomtomkenya.ludouikit.R

/**
 * Helper for creating branded dialogs with consistent styling across Android surfaces.
 */
object LudoDialog {

    fun build(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ) = MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_Ludo_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.ludo_dialog_positive) { dialog, _ ->
            dialog.dismiss()
            onPositive?.invoke()
        }
        .setNegativeButton(R.string.ludo_dialog_negative) { dialog, _ ->
            dialog.dismiss()
            onNegative?.invoke()
        }
}

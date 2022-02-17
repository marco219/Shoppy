package com.marcoassenza.shoppy.views.helpers

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.marcoassenza.shoppy.R


fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
    }
    return false
}

fun View.showUndoActionSnackbar(text: String, action: () -> Unit) {
    Snackbar.make(
        this,
        text,
        Snackbar.LENGTH_LONG
    ).apply {
        setAction(R.string.undo) { action.invoke() }
    }.show()
}

fun View.showLongSnackbar(text: String) {
    Snackbar.make(
        this,
        text,
        Snackbar.LENGTH_LONG
    ).apply {
        this.setBackgroundTint(resources.getColor(R.color.seed, context.theme))
    }.show()
}

fun View.showIndefiniteSnackbar(text: String): Snackbar {
    return Snackbar.make(
        this,
        text,
        Snackbar.LENGTH_INDEFINITE
    ).apply {
        this.setBackgroundTint(resources.getColor(R.color.error, context.theme))
        show()
    }
}
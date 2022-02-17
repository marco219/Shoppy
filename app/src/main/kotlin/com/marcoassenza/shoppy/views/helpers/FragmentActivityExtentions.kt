package com.marcoassenza.shoppy.views.helpers

import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.marcoassenza.shoppy.R

fun FragmentActivity.mainFabCustomizer(
    textResourceId: Int,
    iconResourceId: Int,
    onClickAction: () -> Unit
): ExtendedFloatingActionButton? {
    val context = applicationContext
    val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
    fab?.apply {
        text = context.getString(textResourceId)
        icon = ResourcesCompat.getDrawable(context.resources, iconResourceId, null)
        setOnClickListener {
            onClickAction()
        }
    }?.show()

    return fab
}
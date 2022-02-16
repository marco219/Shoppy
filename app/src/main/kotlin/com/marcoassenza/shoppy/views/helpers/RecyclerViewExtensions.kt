package com.marcoassenza.shoppy.views.helpers

import android.app.Activity
import android.content.res.Configuration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

fun RecyclerView.setDynamicStaggeredGridLayout(
    recyclerViewAdapter: RecyclerView.Adapter<*>,
    activity: Activity?
) {
    adapter = recyclerViewAdapter
    layoutManager = activity?.resources?.configuration?.orientation.let {
        when (it) {
            Configuration.ORIENTATION_LANDSCAPE -> StaggeredGridLayoutManager(
                3,
                StaggeredGridLayoutManager.VERTICAL
            )
            else -> StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
        }
    }
}

fun RecyclerView.setLinearLayout(
    recyclerViewAdapter: RecyclerView.Adapter<*>,
    activity: Activity?
) {
    adapter = recyclerViewAdapter
    layoutManager = LinearLayoutManager(
        activity?.applicationContext,
        LinearLayoutManager.HORIZONTAL,
        false
    )
}

fun RecyclerView.enableShowHideExtendedFab(fab: ExtendedFloatingActionButton?) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0)
                fab?.hide()
            else if (dy < 0)
                fab?.show()
        }
    })
}
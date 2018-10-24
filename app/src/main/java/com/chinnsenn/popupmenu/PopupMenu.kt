package com.chinnsenn.popupmenu

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class PopupMenu(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : ViewGroup(context, attributes, defStyleAttr) {



    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context) : this(context, null)

    init {
        initViews()
    }

    private fun initViews() {

    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
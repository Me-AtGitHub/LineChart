package com.au.linechart.chartView

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class ChartView : RelativeLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attributeSet,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attributeSet, defStyleAttr, defStyleRes) {

        init()
    }


    private fun init() {

    }

    // linear data
    private var lineData: MutableList<Float> = mutableListOf()
        set(value) {
            field = value
            requestLayout()
        }


}
package com.au.lineChart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.au.mylibrary.R

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

        context.obtainStyledAttributes(attributeSet, R.styleable.ChartView).let {

            chartHorizontalPadding =
                it.getDimension(R.styleable.ChartView_chart_HorizontalPadding, 20F)
            chartBottomPadding = it.getDimension(R.styleable.ChartView_chart_BottomPadding, 20F)
            chartHeightFraction = it.getFloat(R.styleable.ChartView_chart_HeightFraction, 0.6F)
            chartLineColor = it.getColor(R.styleable.ChartView_chart_LineColor, Color.BLUE)
            chartLineWidth = it.getDimension(R.styleable.ChartView_chart_LineWidth, 20F)
            chartSelectorIndicator = it.getDrawable(R.styleable.ChartView_chart_SelectorIndicator)
            chartSelectorIndicatorHeight =
                it.getDimension(R.styleable.ChartView_chart_SelectorIndicatorHeight, 20F)

            chartSelectorLineColor =
                it.getColor(R.styleable.ChartView_chart_SelectorLineColor, Color.BLUE)

            textSize = it.getDimension(R.styleable.ChartView_android_textSize, 32F)
            textColor = it.getColor(R.styleable.ChartView_android_textColor, Color.BLUE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textTypeface = it.getFont(R.styleable.ChartView_android_fontFamily)
            }

            it.recycle()

        }

        init()

    }


    private fun init() {

        setWillNotDraw(true)

        points = mutableListOf()

        drawableView = View(context)
        dottedLineView = View(context)
        textView = TextView(context)

        addView(drawableView)
        addView(dottedLineView)
        addView(textView)

        Log.d(TAG, "init: ")
    }

    // linear data
    private var lineData: MutableList<Float> = mutableListOf()
        set(value) {
            field = value
            selectedPosition = value.count() - 1
            postInvalidate()
        }

    var chartHorizontalPadding: Float = 0F
    var chartBottomPadding: Float = 0F
    var chartHeightFraction: Float = 1.0f
    var chartLineColor: Int = Color.BLUE
    var chartLineWidth: Float = 20F

    var chartSelectorIndicator: Drawable? = null
    var chartSelectorIndicatorHeight: Float = 32F
    var chartSelectorLineColor: Int = Color.BLUE

    var textSize: Float = 0F
    var textTypeface: Typeface? = Typeface.DEFAULT
    var textColor: Int = Color.BLUE

    private lateinit var drawableView: View
    private lateinit var textView: TextView
    private lateinit var dottedLineView: View

    data class Point(val x: Float, val y: Float)

    private lateinit var points: MutableList<Point>

    private var mCanvas: Canvas? = null
    private var xUnit: Float = 0.0f
    private var selectedPosition: Int = -1
    private var selectedPoint: Point? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val eventX: Float = event?.x ?: 0F
        when (event?.action) {

            MotionEvent.ACTION_MOVE -> {

                val position = ((eventX / xUnit).toInt())

                if (position in 0 until points.count() && position != selectedPosition) {
                    selectedPosition = position
                    selectedPoint = points[position]
                    selectedPoint?.let {
                        Log.d("pointSelected", "$position")
                        pointSelected(it.x, it.y, position)
                    }
                }
                return true

            }
            MotionEvent.ACTION_DOWN -> {

                val position = ((eventX / xUnit).toInt())

                if (position in 0 until points.count()) {

                    if (position != selectedPosition) {
                        selectedPosition = position
                        selectedPoint = points[position]
                        selectedPoint?.let {
                            Log.d("pointSelected", "$position")
                            pointSelected(it.x, it.y, position)

                        }
                    }
                }
                return true
            }
        }

        return false

    }

    private fun calculatePoints() {

        points.clear()
        val max = lineData.max()
        val min = lineData.min()

        xUnit = (width - (chartHorizontalPadding)) / (lineData.count() - 1)

        for (i in 0 until lineData.count()) {

            val startX: Float =
                ((width - (chartHorizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + chartHorizontalPadding
            val startY =
                (height - ((height * chartHeightFraction) * ((lineData[i] - min) / (max - min)))) - chartBottomPadding

            points.add(Point(startX, startY))

        }

        mCanvas?.let {
            drawData(it)
        }

    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            mCanvas = it
            if (lineData.isNotEmpty())
                calculatePoints()
        }
    }

    private fun drawData(mCanvas: Canvas) {

        selectedPoint = points.last()

        for (i in 0 until points.count())
            if (i < points.count() - 1) {
                drawLines(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, mCanvas)
            }

        if (selectedPosition in 0 until points.count()) {
            selectedPoint = points[selectedPosition]
            selectedPoint?.let {
                pointSelected(it.x, it.y, selectedPosition)
            }
        }

    }

    private fun drawLines(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, mCanvas: Canvas) {
        mCanvas.drawLine(
            xStart,
            yStart,
            xEnd,
            yEnd,
            Paint().apply {
                strokeCap = Paint.Cap.ROUND
                strokeWidth = chartLineWidth
                color = chartLineColor
            })
    }

    private fun pointSelected(x: Float, y: Float, position: Int) {

        drawableView.background = chartSelectorIndicator
        dottedLineView.background = AppCompatResources.getDrawable(
            context,
            R.drawable.vertical_dotted_line
        )?.apply {
            setTint(chartSelectorLineColor)
        }

        drawableView.layoutParams =
            LayoutParams(chartSelectorIndicatorHeight.toInt(), chartSelectorIndicatorHeight.toInt())
        dottedLineView.layoutParams = LayoutParams(
            chartLineWidth.toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        textView.translationZ = 8F
        textView.text = lineData[position].toInt().toString() + "€"

        textView.textSize = textSize
        textView.typeface = textTypeface
        textView.setTextColor(textColor)

        if (position == lineData.count() - 1)
            textView.x = (x - textView.width)
        else textView.x = x - (textView.width / 2)

        textView.y = 0F

        drawableView.translationZ = 8F
        drawableView.x = x - (drawableView.width / 2)
        drawableView.y = y - (drawableView.width / 2)

        /* line position */
        dottedLineView.y = y - (drawableView.width / 2)
        dottedLineView.x = x - (dottedLineView.width / 2)

//        postInvalidate() // redraw the layout
    }


    companion object {
        private const val TAG = "ChartView"
    }

}
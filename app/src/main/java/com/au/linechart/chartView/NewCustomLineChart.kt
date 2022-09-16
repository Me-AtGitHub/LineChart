package com.au.linechart.chartView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.au.linechart.R

class NewCustomLineChart : RelativeLayout {


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : this(
        context,
        attributeSet,
        defStyle,
        0
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyle: Int,
        defStyleRes: Int
    ) : super(context, attributeSet, defStyle, defStyleRes) {

        setWillNotDraw(true)

        drawableView = View(context)
        dottedLineView = View(context)
        textView = TextView(context)

        addView(drawableView)
        addView(dottedLineView)
        addView(textView)
        setBackgroundColor(Color.TRANSPARENT)

    }

    private var _lineData = mutableListOf<Float>()
    var lineData
        get() = _lineData
        set(value) {
            firstTime = true
            _lineData = value
            selectedPosition = value.count() - 1
            postInvalidate()
        }

    private var drawableView: View
    private var textView: TextView
    private var dottedLineView: View

    private var firstTime: Boolean = true

    var xUnit: Int = 0
    var textColor: Int = Color.BLUE
    var textTypeface: Typeface = Typeface.DEFAULT
    var textSize: Float = 16f
    var selectorLineColor: Int = Color.BLUE

    var selectorDrawable: Drawable? = AppCompatResources.getDrawable(context, R.drawable.radio_select)
    var selectorPointSize: Int = 40

    var horizontalPadding: Int = 32
    var heightFraction: Float = 1.0f
    var bottomPadding: Int = 40

    var lineWidth = 4f
    var lineColor = Color.BLUE

    var mCanvas: Canvas? = null
    var selectedPosition: Int = 0
    var selectedPoint: Point? = null

    data class Point(val x: Float, val y: Float)

    private val points: MutableList<Point> = mutableListOf()

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

        xUnit = (width - (horizontalPadding)) / (lineData.count() - 1)

        for (i in 0 until lineData.count()) {

            val startX: Float =
                ((width - (horizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + horizontalPadding
            val startY =
                (height - ((height * heightFraction) * ((lineData[i] - min) / (max - min)))) - bottomPadding

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

        for (i in 0 until points.count()) {
            if (i < points.count() - 1) {
                drawLines(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, mCanvas)
            }
        }

        if (selectedPosition in 0 until points.count()) {
            selectedPoint = points[selectedPosition]
            selectedPoint?.let {
                pointSelected(it.x, it.y, selectedPosition)
            }
        }

        firstTime = false

    }

    private fun drawLines(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, mCanvas: Canvas) {
        mCanvas.drawLine(
            xStart,
            yStart,
            xEnd,
            yEnd,
            Paint().apply {
                strokeCap = Paint.Cap.ROUND
                strokeWidth = lineWidth
                color = lineColor
            })
    }

    private fun pointSelected(x: Float, y: Float, position: Int) {

        drawableView.background = selectorDrawable
        dottedLineView.background = AppCompatResources.getDrawable(
            context,
            R.drawable.vertical_dotted_line
        )?.apply {
            setTint(selectorLineColor)
        }

        drawableView.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
        dottedLineView.layoutParams = LayoutParams(
            lineWidth.toInt(),
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

}
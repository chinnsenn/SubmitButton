package com.chinnsenn.schedulebar

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class ScheduleBar(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : View(context, attributeSet, defStyleAttr) {
    var barColor: Int? = null
        set(value) {
            field = value
            mPaintBarFg.color = value!!
        }

    var bubbleColor: Int? = null
        set(value) {
            field = value
            mPaintBubble.color = value!!
        }

    var textColor: Int? = null
        set(value) {
            field = value
            mPaintText.color = value!!
        }

    var totalValue: Int = 100
        set(value) {
            field = value
            mAnimatorBar.setIntValues(startValue, value)
        }

    var startValue = 0
        set(value) {
            field = value
            mAnimatorBar.setIntValues(value, totalValue)
        }

    var currentProcess: Int? = null
        set(value) {
            if (value!! < startValue || value > totalValue) throw RuntimeException("currentProcess must be between startValue and totalValue")
            field = if (value >= totalValue) totalValue else value
            mAnimatorBar.start()
        }

    var duration = 3000
        set(value) {
            field = value
            mAnimatorBar.duration = value.toLong()
        }

    var stageCount = 0

    private var mWidthBubble = 35f

    private var mHeightBubble = 17f

    private var mWidthTriangle = 4f

    private var mHeightTriangle = 2f

    private var mPaintBarBg = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mPaintBarFg = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mPaintBubble = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mPaintText = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mRectFBar = RectF()

    private var mRectFClip = RectF()

    private var mPathBarBg = Path()

    private var mPathBarFg = Path()

    private var mPathBubble = Path()

    private var mRectFBubble = RectF()

    private var mMaxBarHeight: Float = 3f

    private var mDefaultStageCount = 5

    private var mPaddingVertical: Float = 25f

    private var mPaddingHorizontal: Float = 20f

    private var centerY: Float? = null

    private var centerX: Float? = null

    private var progressText: String? = null

    private var mAnimatorBar: ValueAnimator = ValueAnimator.ofInt(0, totalValue)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null) {
        throw RuntimeException("can not initialize")
    }

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScheduleBar)
        barColor = typedArray.getColor(R.styleable.ScheduleBar_barColor, Color.parseColor("#F4C949"))
        bubbleColor = typedArray.getColor(R.styleable.ScheduleBar_bubbleColor, Color.parseColor("#46B7F2"))
        textColor = typedArray.getColor(R.styleable.ScheduleBar_textColor, Color.WHITE)
        duration = typedArray.getInteger(R.styleable.ScheduleBar_duration, 3000)
        stageCount = typedArray.getInteger(R.styleable.ScheduleBar_stageCount, mDefaultStageCount)

        typedArray.recycle()
        mMaxBarHeight = dp2px(3f).toFloat()

        mWidthBubble = dp2px(35f).toFloat()
        mHeightBubble = dp2px(17f).toFloat()
        mWidthTriangle = dp2px(4f).toFloat()
        mHeightTriangle = dp2px(2f).toFloat()
        mPaddingVertical = mHeightBubble + mHeightTriangle + dp2px(10f)
        mPaddingHorizontal = mWidthBubble / 2f

        mPaintBarBg.color = Color.WHITE
        mPaintBarBg.style = Paint.Style.FILL

        mPaintBarFg.color = barColor!!
        mPaintBarFg.style = Paint.Style.FILL

        mPaintBubble.color = bubbleColor!!
        mPaintBubble.style = Paint.Style.FILL

        mPaintText.color = textColor!!
        mPaintText.style = Paint.Style.FILL
        mPaintText.textAlign = Paint.Align.CENTER
        mPaintText.textSize = dp2px(9f).toFloat()
        mAnimatorBar.duration = duration.toLong()

        mAnimatorBar.addUpdateListener {
            val progress = it.animatedValue as Int
            if (progress <= currentProcess!!) {
                progressText = progress.toString()
                mRectFClip.right = measuredWidth * ((progress - startValue) / (totalValue - startValue).toFloat()) + mPaddingHorizontal
                invalidate()
            } else {
                progressText = currentProcess.toString()
                invalidate()
                mAnimatorBar.cancel()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (mMaxBarHeight + mPaddingVertical * 4).toInt())
        centerX = measuredWidth / 2f; centerY = measuredHeight / 2f

        mRectFBar.set(mPaddingHorizontal, mPaddingVertical, measuredWidth.toFloat() - mPaddingHorizontal, mPaddingVertical + mMaxBarHeight)

        mRectFClip.set(0f, 0f, 0f, measuredHeight.toFloat())

        mPathBarBg.addRoundRect(mRectFBar, mMaxBarHeight / 2, mMaxBarHeight / 2, Path.Direction.CW)

        if (stageCount > 0) {
            val lengthPerStage = (measuredWidth - mPaddingHorizontal * 2) / (stageCount - 1)
            for (i in 0 until stageCount) {
                mPathBarBg.addCircle((i * lengthPerStage) + mPaddingHorizontal, mMaxBarHeight / 2 + mPaddingVertical, mMaxBarHeight * 3 / 2f, Path.Direction.CW)
            }
        }

        mPathBarFg.addPath(mPathBarBg)

        mRectFBubble.set(0f, 0f, mWidthBubble, mHeightBubble)
        val radius = mHeightBubble / 2f
        mPathBubble.addRoundRect(mRectFBubble, radius, radius, Path.Direction.CCW)
        mPathBubble.moveTo((mWidthBubble - mWidthTriangle) / 2f, mHeightBubble)
        mPathBubble.lineTo(mWidthBubble / 2f, mHeightBubble + mHeightTriangle)
        mPathBubble.lineTo((mWidthBubble + mWidthTriangle) / 2f, mHeightBubble)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawPath(mPathBarBg, mPaintBarBg)

        canvas?.save()
        canvas?.translate(mRectFClip.right - mPaddingHorizontal, 0f)
        canvas?.drawPath(mPathBubble, mPaintBubble)
        canvas?.drawText(progressText!!, mWidthBubble / 2f, getBaseLine(mHeightBubble / 2f), mPaintText)
        canvas?.restore()

        canvas?.save()
        canvas?.clipRect(mRectFClip)
        canvas?.drawPath(mPathBarFg, mPaintBarFg)
        canvas?.restore()
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    private fun px2dp(px: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px.toFloat(), context.resources.displayMetrics)
    }

    private fun getBaseLine(centerY: Float): Float {
        val fontMetrics = mPaintText.fontMetrics
        return (centerY + (fontMetrics?.bottom!! - fontMetrics.top) / 2 - fontMetrics.bottom)
    }

}
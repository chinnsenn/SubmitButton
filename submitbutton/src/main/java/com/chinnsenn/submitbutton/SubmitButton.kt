package com.chinnsenn.submitbutton

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import java.lang.ref.WeakReference
import kotlin.math.min


class SubmitButton(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : View(context, attributeSet, defStyleAttr) {

    companion object {
        const val STATE_INIT = 1
        const val STATE_CLICK = 2
        const val STATE_CANCEL = 3
        const val STATE_START = 4
        const val STATE_LOADING = 5
        const val STATE_COMPLETE = 6
        const val STATE_FAILURE = 7

        const val LOAD_SPEED = 3
    }

    var submitText: String = "Submit"
        set(value) {
            field = value
            this.mButtonText = value
        }

    var completeText: String = "Complete"

    var failureText: String = "Failure"

    var buttonColor: Int? = null
        set(value) {
            field = value
            mPaintButton?.color = value!!
            mPaintBackground?.color = value
            mPaintText?.color = value
            mStartValueAnimator?.setObjectValues(Color.WHITE, value)
            mEndValueAnimator?.setObjectValues(value, Color.WHITE)
        }

    var progressColor: Int? = null
        set(value) {
            field = value!!
            mPaintProcess?.color = value
        }

    var buttonStrokeWidth: Float? = null
        set(value) {
            field = value
            mPaintButton?.strokeWidth = value!!
            mPaintBackground?.strokeWidth = value
            mPaintProcess?.strokeWidth = value * 2
            requestLayout()
        }

    var buttonTextSize: Float? = null
        set(value) {
            field = value
            mPaintText?.textSize = dp2px(value!!).toFloat()
        }

    var unKnownProgress: Boolean = true

    private var mListener: OnStatusListener? = null

    private var mDuration: Long = 400L

    private var mButtonText: String? = null

    private var mPaintButton: Paint? = null

    private var mPaintText: Paint? = null

    private var mPaintProcess: Paint? = null

    private var mButtonRectF: RectF? = null

    private var mRectFArc: RectF? = null

    private var mPathArc: Path? = null

    private var mPaintBackground: Paint? = null

    private var mButtonRatio: Float? = null

    private var centerY: Float? = null

    private var centerX: Float? = null

    private var mStartValueAnimator: ValueAnimator? = null //背景由白变绿动画

    private var mEndValueAnimator: ValueAnimator? = null //背景由绿变白动画

    private var mButtonValueAnimator: ValueAnimator? = null //按钮伸缩动画

    private var mTextValueAnimator: ValueAnimator? = null //按钮文字动画

    private var isDrawBackground = false

    private var isDrawText = true

    private var mDefaultWidth = 200f

    private var mDefaultHeight = 50f

    private var mWidthDelta = 0f

    private var mCurrentState = STATE_INIT

    private var mProgress: Float = 0f

    private var mDegrees = 0f

    private var isIncreasing = true


    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null) {
        throw RuntimeException("can not initialize")
    }

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SubmitButton)
        buttonColor = typedArray.getColor(R.styleable.SubmitButton_buttonColor, Color.parseColor("#2bcb96"))
        progressColor = typedArray.getColor(R.styleable.SubmitButton_progressColor, Color.parseColor("#2bcb96"))
        buttonStrokeWidth = typedArray.getFloat(R.styleable.SubmitButton_buttonStrokeWidth, 5f)
        submitText = if (typedArray.getString(R.styleable.SubmitButton_submitText) == null) "Submit" else typedArray.getString(R.styleable.SubmitButton_submitText)!!
        completeText = if (typedArray.getString(R.styleable.SubmitButton_completeText) == null) "Complete" else typedArray.getString(R.styleable.SubmitButton_completeText)!!
        failureText = if (typedArray.getString(R.styleable.SubmitButton_failureText) == null) "Complete" else typedArray.getString(R.styleable.SubmitButton_failureText)!!
        buttonTextSize = typedArray.getDimensionPixelSize(R.styleable.SubmitButton_buttonTextSize, 20).toFloat()
        unKnownProgress = typedArray.getBoolean(R.styleable.SubmitButton_unKnownProgress, true)

        mButtonText = submitText
        typedArray.recycle()

        initViews()
        initAnimator()
    }

    private fun initViews() {
        mPaintButton = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintButton?.color = this.buttonColor!!
        mPaintButton?.style = Paint.Style.STROKE
        mPaintButton?.strokeWidth = buttonStrokeWidth!!

        mPaintProcess = Paint(mPaintButton)
        mPaintProcess?.strokeWidth = buttonStrokeWidth!! * 2

        mPaintText = Paint(mPaintButton)
        mPaintText?.style = Paint.Style.FILL
        mPaintText?.textSize = buttonTextSize!!.toFloat()
        mPaintText?.textAlign = Paint.Align.CENTER

        mPaintBackground = Paint(mPaintButton)
        mPaintBackground?.style = Paint.Style.FILL_AND_STROKE

        mButtonRectF = RectF()
        mRectFArc = RectF()
        mPathArc = Path()
    }

    private fun initAnimator() {
        mStartValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.WHITE, buttonColor!!)
        mEndValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), buttonColor!!, Color.WHITE)

        val textSizePX = buttonTextSize!!.toFloat()

        mTextValueAnimator = ValueAnimator.ofFloat(textSizePX, textSizePX * 0.8f, textSizePX)
        mButtonValueAnimator = ValueAnimator()

        mTextValueAnimator?.duration = mDuration * 3 / 5
        mTextValueAnimator?.startDelay = mDuration

        mStartValueAnimator?.duration = mDuration
        mEndValueAnimator?.duration = mDuration
        mButtonValueAnimator?.duration = mDuration
        mButtonValueAnimator?.interpolator = AccelerateInterpolator()

        val updateListener = AnimatorUpdateListener(this)
        mStartValueAnimator?.addUpdateListener(updateListener)
        mEndValueAnimator?.addUpdateListener(updateListener)
        mButtonValueAnimator?.addUpdateListener(updateListener)
        mTextValueAnimator?.addUpdateListener(updateListener)

        val listener = AnimatorListener(this)
        mStartValueAnimator?.addListener(listener)
        mEndValueAnimator?.addListener(listener)
        mButtonValueAnimator?.addListener(listener)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        when (action) {
            MotionEvent.ACTION_DOWN -> if (mCurrentState == STATE_INIT || mCurrentState == STATE_CANCEL) {
                mStartValueAnimator?.start()
                mTextValueAnimator?.start()
                mCurrentState = STATE_CLICK
            }

            MotionEvent.ACTION_MOVE -> {

            }

            MotionEvent.ACTION_UP -> {
                if (mCurrentState == STATE_CLICK) {
                    if (event.x >= 0 && event.x <= measuredWidth && event.y >= 0 && event.y <= measuredHeight) {
                        val delay = mStartValueAnimator?.duration!! - mStartValueAnimator?.currentPlayTime!!
                        mButtonValueAnimator?.startDelay = if (delay == mDuration) 0L + mTextValueAnimator?.duration!! else delay + mTextValueAnimator?.duration!!
                        mButtonValueAnimator?.start()
                        mEndValueAnimator?.startDelay = mButtonValueAnimator?.startDelay!! - 100L
                        mEndValueAnimator?.start()
                        mCurrentState = STATE_START
                        mListener?.onStart()
                    } else {
                        mEndValueAnimator?.startDelay = 0L
                        mEndValueAnimator?.start()
                        mCurrentState = STATE_CANCEL
                        mListener?.onCancel()
                    }
                }
            }
        }
        return true
    }

    fun setOnStatusListener(listener: OnStatusListener) {
        this.mListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var thisWidth = dp2px(mDefaultWidth)
        var thisHeight = dp2px(mDefaultHeight)

        when (widthMode) {
            MeasureSpec.AT_MOST -> {
                thisWidth = when (thisWidth) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> {
                        widthSize
                    }
                    else -> {
                        min(thisWidth, widthSize)
                    }
                }
            }
            MeasureSpec.EXACTLY -> {
                thisWidth = widthSize
            }
            MeasureSpec.UNSPECIFIED -> {
                thisWidth = dp2px(mDefaultWidth)
            }
        }

        when (heightMode) {
            MeasureSpec.AT_MOST -> {
                thisHeight = when (thisHeight) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> {
                        heightSize
                    }
                    else -> {
                        min(thisHeight, heightSize)
                    }
                }
            }
            MeasureSpec.EXACTLY -> {
                thisHeight = heightSize
            }

            MeasureSpec.UNSPECIFIED -> {
                thisHeight = dp2px(mDefaultHeight)
            }
        }

        if (thisHeight * 3 > thisWidth) {
            thisHeight = thisWidth / 3
        }

        setMeasuredDimension(thisWidth, thisHeight)

        mButtonRatio = measuredHeight / 2f

        val delta = measuredWidth.toFloat() - measuredHeight.toFloat()
        mButtonValueAnimator?.setFloatValues(0f, delta)

        mRectFArc?.set(buttonStrokeWidth!! + delta / 2, buttonStrokeWidth!!, measuredWidth.toFloat() - buttonStrokeWidth!! - delta / 2, measuredHeight.toFloat() - buttonStrokeWidth!!)

        centerX = measuredWidth / 2f; centerY = measuredHeight / 2f
    }

    override fun onDraw(canvas: Canvas?) {

        mButtonRectF?.set(buttonStrokeWidth!! + mWidthDelta, buttonStrokeWidth!!, measuredWidth.toFloat() - buttonStrokeWidth!! - mWidthDelta, measuredHeight.toFloat() - buttonStrokeWidth!!)

        //绘制按钮背景
        if (isDrawBackground) {
            canvas?.drawRoundRect(mButtonRectF!!, mButtonRatio!!, mButtonRatio!!, mPaintBackground!!)
        }

        //绘制按钮线框
        canvas?.drawRoundRect(mButtonRectF!!, mButtonRatio!!, mButtonRatio!!, mPaintButton!!)

        //绘制按钮文字
        if (isDrawText) {
            canvas?.drawText(mButtonText!!, centerX!!, getBaseLine(centerY!!), mPaintText!!)
        }

        //绘制加载框
        if (mCurrentState == STATE_LOADING) {
            //无法计算进度
            if (unKnownProgress) {
                canvas?.save()
                mDegrees += LOAD_SPEED
                canvas?.rotate(mDegrees, centerX!!, centerY!!)
                if (isIncreasing) {
                    mProgress += LOAD_SPEED
                    canvas?.drawArc(mRectFArc!!, -90F, mProgress, false, mPaintProcess!!)
                    if (mProgress >= 360f) isIncreasing = false
                } else {
                    mProgress -= LOAD_SPEED
                    canvas?.drawArc(mRectFArc!!, -90F + (360f - mProgress), mProgress, false, mPaintProcess!!)
                    if (mProgress <= 0f) isIncreasing = true
                }
                invalidate()
                canvas?.restore()
            } else {
                //给具体进度
                canvas?.drawArc(mRectFArc!!, -90F, mProgress, false, mPaintProcess!!)
                val percent = String.format("%d%%", (mProgress * 100 / 360).toInt())
                println("percent = $percent")
                mPaintText?.color = buttonColor!!
                canvas?.drawText(percent, centerX!!, getBaseLine(centerY!!), mPaintText!!)
            }
        }
    }

    fun setProgress(percent: Float) {
        if (!unKnownProgress) {
            this.mProgress = percent * 360f
            invalidate()
            if (percent >= 1f) {
                stop()
            }
        }
    }

    fun setProgressAndTotal(progress: Float, total: Float) {
        setProgress(progress / total)
    }

    fun stop() {
        if (mCurrentState == STATE_LOADING) {
            endLoading()
        }
    }

    fun failure() {
        if (mCurrentState == STATE_LOADING) {
            mCurrentState = STATE_FAILURE
            mButtonText = failureText
            mButtonValueAnimator?.startDelay = 0L
            mButtonValueAnimator?.start()
            mStartValueAnimator?.setObjectValues(Color.WHITE, Color.RED)
            mPaintButton?.color = Color.RED
            mPaintButton?.strokeWidth = buttonStrokeWidth!!
            mStartValueAnimator?.start()
        }
    }

    private fun endLoading() {
        mCurrentState = STATE_COMPLETE
        mListener?.onComplete()
        mButtonValueAnimator?.startDelay = 0L
        mButtonValueAnimator?.start()
        mPaintButton?.color = buttonColor!!
        mPaintButton?.strokeWidth = buttonStrokeWidth!!
        mStartValueAnimator?.start()
        mTextValueAnimator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mStartValueAnimator?.cancel()
        mEndValueAnimator?.cancel()
        mButtonValueAnimator?.cancel()
        mTextValueAnimator?.cancel()
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    private fun px2dp(px: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px.toFloat(), context.resources.displayMetrics)
    }

    private fun getBaseLine(centerY: Float): Float {
        val fontMetrics = mPaintText?.fontMetrics
        return (centerY + (fontMetrics?.bottom!! - fontMetrics.top) / 2 - fontMetrics.bottom)
    }

    interface OnStatusListener {
        fun onStart()
        fun onLoad()
        fun onComplete()
        fun onCancel()
    }

    class AnimatorListener(submitButton: SubmitButton) : Animator.AnimatorListener {
        private val mSubmitButtonWeakReference: WeakReference<SubmitButton> = WeakReference(submitButton)

        override fun onAnimationStart(animation: Animator) {
            val submitButton = mSubmitButtonWeakReference.get()
            when (animation) {
                submitButton?.mStartValueAnimator!! -> {
                    submitButton.isDrawBackground = true
                }
                submitButton.mButtonValueAnimator!! -> {
                    submitButton.isDrawText = false
                }
                submitButton.mEndValueAnimator!! -> {
                    if (STATE_FAILURE == submitButton.mCurrentState) {
                        submitButton.mButtonText = submitButton.submitText
                        submitButton.mPaintButton?.color = submitButton.buttonColor!!
                        submitButton.mCurrentState = STATE_INIT
                    }
                }
            }
        }

        override fun onAnimationEnd(animation: Animator) {
            val submitButton = mSubmitButtonWeakReference.get()
            when (animation) {
                submitButton?.mStartValueAnimator!! -> {
                    if (STATE_COMPLETE == submitButton.mCurrentState) {
                        submitButton.mCurrentState = STATE_INIT
                        submitButton.isDrawText = true
                        submitButton.mButtonText = submitButton.completeText
                        submitButton.mProgress = 0f
                        submitButton.mEndValueAnimator?.start()
                    } else if (STATE_FAILURE == submitButton.mCurrentState) {
                        submitButton.mProgress = 0f
                        submitButton.mEndValueAnimator?.startDelay = submitButton.mDuration * 2
                        submitButton.mEndValueAnimator?.start()
                        submitButton.mStartValueAnimator?.setObjectValues(Color.WHITE, submitButton.buttonColor)
                    }
                }
                submitButton.mEndValueAnimator!! -> {
                    if (submitButton.mCurrentState == STATE_INIT) {
                        submitButton.mStartValueAnimator?.cancel()
                        submitButton.mEndValueAnimator?.cancel()
                        submitButton.mButtonValueAnimator?.cancel()
                        submitButton.mTextValueAnimator?.cancel()
                    }
                    submitButton.isDrawBackground = true
                }
                submitButton.mButtonValueAnimator!! -> {
                    if (animation is ValueAnimator) {
                        if (submitButton.mCurrentState != STATE_COMPLETE && submitButton.mCurrentState != STATE_FAILURE) {
                            submitButton.mPaintButton?.color = Color.LTGRAY
                            submitButton.mPaintButton?.strokeWidth = submitButton.buttonStrokeWidth!! * 2
                            submitButton.mButtonValueAnimator?.setFloatValues(submitButton.measuredWidth.toFloat() - submitButton.measuredHeight.toFloat(), 0f)
                            submitButton.mCurrentState = STATE_LOADING
                            submitButton.mListener?.onLoad()
                        } else {
                            submitButton.isDrawText = true
                            submitButton.mButtonValueAnimator?.setFloatValues(0f, submitButton.measuredWidth.toFloat() - submitButton.measuredHeight.toFloat())
                        }
                    }
                }
            }
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }

    class AnimatorUpdateListener(submitButton: SubmitButton) : ValueAnimator.AnimatorUpdateListener {
        private val weakReference: WeakReference<SubmitButton> = WeakReference(submitButton)

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            val submitButton = weakReference.get()
            when (animation!!) {
                submitButton?.mStartValueAnimator!! -> {
                    val color: Int = animation.animatedValue as Int
                    submitButton.mPaintBackground?.color = color
                    if (animation.currentPlayTime >= animation.duration / 2L) {
                        if (submitButton.mPaintText?.color == submitButton.buttonColor!!) {
                            submitButton.mPaintText?.color = Color.WHITE
                        }
                    }
                    submitButton.invalidate()
                }
                submitButton.mEndValueAnimator!! -> {
                    val color: Int = animation.animatedValue as Int
                    submitButton.mPaintBackground?.color = color
                    if (submitButton.mPaintText?.color != submitButton.buttonColor!! && animation.currentPlayTime >= animation.duration / 2L) {
                        submitButton.mPaintText?.color = submitButton.buttonColor!!
                    }
                    if (submitButton.mCurrentState == STATE_INIT && animation.currentPlayTime >= animation.duration / 2L) {
                        submitButton.mButtonText = submitButton.submitText
                    }
                    submitButton.invalidate()
                }
                submitButton.mButtonValueAnimator!! -> {
                    submitButton.mWidthDelta = (animation.animatedValue as Float) / 2
                    submitButton.invalidate()
                }
                submitButton.mTextValueAnimator!! -> {
                    submitButton.mPaintText?.textSize = animation.animatedValue as Float
                    submitButton.invalidate()
                }

            }
        }
    }

}


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
    }

    private var mButtonColor: Int? = null

    private var mButtonStrokeWidth: Float? = null

    private var mPaintButton: Paint? = null

    private var mPaintText: Paint? = null

    private var mPaintProcess: Paint? = null

    private var mButtonRectF: RectF? = null

    private var mRectFArc: RectF? = null

    private var mPathArc: Path? = null

    private var mPaintBackground: Paint? = null

    private var mButtonRatio: Float? = null

    private var mButtonText: String? = "Submit"

    private var mButtonTextSize: Float? = null

    private var centerY: Float? = null

    private var centerX: Float? = null

    private var mStartValueAnimator: ValueAnimator? = null //背景由白变绿动画

    private var mEndValueAnimator: ValueAnimator? = null //背景由绿变白动画

    private var mButtonValueAnimator: ValueAnimator? = null //按钮伸缩动画

    private var mProgressValueAnimator: ValueAnimator? = null //进度环动画

    private var mTextValueAnimator: ValueAnimator? = null //按钮文字动画

    private var isDrawBackground = false

    private var isDrawText = true

    private var mDefaultWidth = 200f

    private var mDefaultHeight = 50f

    private var mWidthDelta = 0f

    private var mCurrentState = STATE_INIT

    private var mProgress: Float = 0f

    private var mDuration: Long = 400L

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null) {
        throw RuntimeException("can not initialize")
    }

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SubmitButton)
        mButtonColor = typedArray.getColor(R.styleable.SubmitButton_buttonColor, Color.parseColor("#2bcb96"))
        mButtonStrokeWidth = typedArray.getFloat(R.styleable.SubmitButton_buttonStrokeWidth, 5f)
        mButtonText = if (typedArray.getString(R.styleable.SubmitButton_buttonText) == null) "Submit" else typedArray.getString(R.styleable.SubmitButton_buttonText)
        mButtonTextSize = typedArray.getDimension(R.styleable.SubmitButton_buttonTextSize, 20f)
        typedArray.recycle()

        initViews()
        initAnimator()
    }

    private fun initViews() {
        mPaintButton = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintButton?.color = this.mButtonColor!!
        mPaintButton?.style = Paint.Style.STROKE
        mPaintButton?.strokeWidth = mButtonStrokeWidth!!

        mPaintProcess = Paint(mPaintButton)
        mPaintProcess?.strokeWidth = mButtonStrokeWidth!! * 2

        mPaintText = Paint(mPaintButton)
        mPaintText?.style = Paint.Style.FILL
        mPaintText?.textSize = dp2px(mButtonTextSize!!).toFloat()
        mPaintText?.textAlign = Paint.Align.CENTER

        mPaintBackground = Paint(mPaintButton)
        mPaintBackground?.style = Paint.Style.FILL_AND_STROKE

        mButtonRectF = RectF()
        mRectFArc = RectF()
        mPathArc = Path()
    }

    private fun initAnimator() {
        mStartValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.WHITE, mButtonColor!!)
        mEndValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), mButtonColor!!, Color.WHITE)
        mProgressValueAnimator = ValueAnimator.ofFloat(0f, 1f)

        val textSizePX = dp2px(mButtonTextSize!!).toFloat()

        mTextValueAnimator = ValueAnimator.ofFloat(textSizePX, textSizePX * 0.8f, textSizePX)
        mButtonValueAnimator = ValueAnimator()

        mTextValueAnimator?.duration = mDuration * 3 / 5
        mTextValueAnimator?.startDelay = mDuration

        mStartValueAnimator?.duration = mDuration
        mEndValueAnimator?.duration = mDuration
        mButtonValueAnimator?.duration = mDuration
        mProgressValueAnimator?.duration = mDuration.times(10)
        mButtonValueAnimator?.interpolator = AccelerateInterpolator()
        mProgressValueAnimator?.interpolator = AccelerateInterpolator()

        val updateListener = AnimatorUpdateListener(this)
        mStartValueAnimator?.addUpdateListener(updateListener)
        mEndValueAnimator?.addUpdateListener(updateListener)
        mButtonValueAnimator?.addUpdateListener(updateListener)
        mProgressValueAnimator?.addUpdateListener(updateListener)
        mTextValueAnimator?.addUpdateListener(updateListener)

        val listener = AnimatorListener(this)
        mStartValueAnimator?.addListener(listener)
        mEndValueAnimator?.addListener(listener)
        mButtonValueAnimator?.addListener(listener)
        mProgressValueAnimator?.addListener(listener)
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
                    } else {
                        mEndValueAnimator?.startDelay = 0L
                        mEndValueAnimator?.start()
                        mCurrentState = STATE_CANCEL
                    }
                }
            }
        }
        return true
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

        mRectFArc?.set(mButtonStrokeWidth!! + delta / 2, mButtonStrokeWidth!!, measuredWidth.toFloat() - mButtonStrokeWidth!! - delta / 2, measuredHeight.toFloat() - mButtonStrokeWidth!!)

        centerX = measuredWidth / 2f; centerY = measuredHeight / 2f
    }

    override fun onDraw(canvas: Canvas?) {

        mButtonRectF?.set(mButtonStrokeWidth!! + mWidthDelta, mButtonStrokeWidth!!, measuredWidth.toFloat() - mButtonStrokeWidth!! - mWidthDelta, measuredHeight.toFloat() - mButtonStrokeWidth!!)

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
            mPathArc?.addArc(mRectFArc!!, -90f, mProgress)
            canvas?.drawPath(mPathArc!!, mPaintProcess!!)
            mPathArc?.reset()
        }
//        canvas?.drawPath(mPathCheckMark!!, mPaintCheckMark!!)
    }

    public fun setProgress(percent: Float) {
        this.mProgress = percent * 360f
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mStartValueAnimator?.cancel()
        mEndValueAnimator?.cancel()
        mButtonValueAnimator?.cancel()
        mProgressValueAnimator?.cancel()
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
                submitButton.mProgressValueAnimator!! -> {
                    submitButton.mCurrentState = STATE_LOADING
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
                        submitButton.mButtonText = "完成"
                        submitButton.mProgress = 0f
                        submitButton.mEndValueAnimator?.start()
                    }
                }
                submitButton.mEndValueAnimator!! -> {
                    if (submitButton.mCurrentState == STATE_INIT) {
                        submitButton.mStartValueAnimator?.cancel()
                        submitButton.mEndValueAnimator?.cancel()
                        submitButton.mButtonValueAnimator?.cancel()
                        submitButton.mProgressValueAnimator?.cancel()
                        submitButton.mTextValueAnimator?.cancel()
                    }
                    submitButton.isDrawBackground = true
                }
                submitButton.mButtonValueAnimator!! -> {
                    if (animation is ValueAnimator) {
                        if (submitButton.mCurrentState != STATE_COMPLETE) {
                            submitButton.mPaintButton?.color = Color.LTGRAY
                            submitButton.mPaintButton?.strokeWidth = submitButton.mButtonStrokeWidth!! * 2
                            submitButton.mButtonValueAnimator?.setFloatValues(submitButton.measuredWidth.toFloat() - submitButton.measuredHeight.toFloat(), 0f)
                            submitButton.mProgressValueAnimator?.start()
                        } else if (submitButton.mCurrentState == STATE_COMPLETE) {
                            submitButton.isDrawText = true
                            submitButton.mButtonValueAnimator?.setFloatValues(0f, submitButton.measuredWidth.toFloat() - submitButton.measuredHeight.toFloat())
                        }
                    }
                }
                submitButton.mProgressValueAnimator!! -> {
                    submitButton.mCurrentState = STATE_COMPLETE
                    submitButton.mButtonValueAnimator?.startDelay = 0L
                    submitButton.mButtonValueAnimator?.start()
                    submitButton.mPaintButton?.color = submitButton.mButtonColor!!
                    submitButton.mPaintButton?.strokeWidth = submitButton.mButtonStrokeWidth!!
                    submitButton.mStartValueAnimator?.start()
                    submitButton.mTextValueAnimator?.start()
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
                        if (submitButton.mPaintText?.color == submitButton.mButtonColor!!) {
                            submitButton.mPaintText?.color = Color.WHITE
                        }
                    }
                    submitButton.invalidate()
                }
                submitButton.mEndValueAnimator!! -> {
                    val color: Int = animation.animatedValue as Int
                    submitButton.mPaintBackground?.color = color
                    if (submitButton.mPaintText?.color != submitButton.mButtonColor!! && animation.currentPlayTime >= animation.duration / 2L) {
                        submitButton.mPaintText?.color = submitButton.mButtonColor!!
                    }
                    if (submitButton.mCurrentState == STATE_INIT && animation.currentPlayTime >= animation.duration / 2L) {
                        submitButton.mButtonText = "确认"
                    }
                    submitButton.invalidate()
                }
                submitButton.mButtonValueAnimator!! -> {
                    submitButton.mWidthDelta = (animation.animatedValue as Float) / 2
                    submitButton.invalidate()
                }
                submitButton.mProgressValueAnimator!! -> {
                    submitButton.setProgress(animation.animatedValue as Float)
                }
                submitButton.mTextValueAnimator!! -> {
                    submitButton.mPaintText?.textSize = animation.animatedValue as Float
                    submitButton.invalidate()
                }

            }
        }
    }

}


package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates
import androidx.core.content.withStyledAttributes as withStyledAttributes

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    var text=" "
    set(value) {
        field=value
        invalidate()
    }

    var textcolor=0
    private var buttonBackgroundColor=0
    private var buttonBackgroundAnimColor=0
    
    private var textBounds:Rect=Rect()
    private val progressRectCircle= RectF()
    private var progressCircleSize=0f
    
    private var currentProgressCircularAnimationValue=0f
    private val progressCircleAnimator=ValueAnimator.ofFloat(0F,360f).apply { 
        repeatMode=ValueAnimator.RESTART
        repeatCount=ValueAnimator.INFINITE
        interpolator=LinearInterpolator()
        addUpdateListener { 
            currentProgressCircularAnimationValue=it.animatedValue as Float
            invalidate()
        }
    }
    
    var animateButton:ButtonState=ButtonState.Completed
    set(value) {
        if(value!=animateButton){
            field=value
        }
        invalidate()
    }
    
    private var textPaint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply { 
        style=Paint.Style.FILL
        textAlign=Paint.Align.CENTER
        typeface= Typeface.DEFAULT
        textSize=40f
    }
    
    private val buttonPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        style=Paint.Style.FILL
    }
    
    
    private val buttonAnimationRect=RectF()
    private var currentButtonBackgroundAnimationValue=0f
    private lateinit var buttonBackgroundAnimator:ValueAnimator
    private val animatorSet:AnimatorSet=AnimatorSet().apply { 
        duration=TimeUnit.SECONDS.toMillis(5)
        disableViewDuringAnimation(this@LoadingButton)
    }

    fun AnimatorSet.disableViewDuringAnimation(view:View)=apply {
        doOnStart { view.isEnabled=false }
        doOnEnd { view.isEnabled=true }
    }
    
    private fun setAnimator(){
        ValueAnimator.ofFloat(0f,widthSize.toFloat()).apply { 
            repeatMode=ValueAnimator.RESTART
            repeatCount=ValueAnimator.INFINITE
            interpolator=LinearInterpolator()
            addUpdateListener { 
                currentButtonBackgroundAnimationValue=it.animatedValue as Float
                invalidate()
            }
        }.also { 
            buttonBackgroundAnimator=it
            animatorSet.playTogether(progressCircleAnimator,buttonBackgroundAnimator)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize=(min(w,h)/2f)*0.6f
        setAnimator()
    }

    private fun computeButtonAnimationrect(){
        buttonAnimationRect.set(
            0f+paddingLeft,
            0f+paddingTop,
            currentButtonBackgroundAnimationValue,
            heightSize.toFloat()
        )
    }

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }


    init {
        context.withStyledAttributes(attrs,R.styleable.LoadingButton){
            text=getText(R.styleable.LoadingButton_text).toString()
            textcolor=getColor(R.styleable.LoadingButton_textColor,Color.BLACK)
            buttonBackgroundColor=getColor(R.styleable.LoadingButton_buttonBackgroundColor,Color.DKGRAY)
            buttonBackgroundAnimColor=getColor(R.styleable.LoadingButton_buttonBackgroundAnimColor,Color.GRAY)
            
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas.let {
            it?.apply {
                drawRectangleButton()
                drawTextOnButton()
                drawAnimationOnButton()
            }
        }

    }
    
    private fun Canvas.drawRectangleButton(){
        if(animateButton==ButtonState.Loading)
        {
            buttonPaint.apply { 
                buttonPaint.color=buttonBackgroundColor
            }.run { 
                drawRoundRect(
                    0f+paddingStart,
                    0f+paddingTop,
                    widthSize.toFloat()+paddingLeft,
                    heightSize.toFloat()+paddingBottom,
                    30f,
                    30f,
                    buttonPaint
                )
            }
            buttonPaint.apply { 
                color=buttonBackgroundAnimColor
            }.run { 
                drawRoundRect(buttonAnimationRect,30f,30f,buttonPaint)
            }
            buttonPaint.color=Color.YELLOW
            drawArc(
                progressRectCircle,
                0f,
                currentProgressCircularAnimationValue,
                true,
                buttonPaint
            )
        }
        else
        {
            if(!isEnabled){
                isEnabled=true
            }
            animatorSet.cancel()
            buttonPaint.apply { 
                buttonPaint.color=buttonBackgroundColor
            }.run { 
                drawRoundRect(
                    0f+paddingStart,
                    0f+paddingTop,
                    widthSize.toFloat()+paddingRight,
                    heightSize.toFloat()+paddingBottom,
                    30f,
                    30f,
                    buttonPaint
                )
            }
        }
    }
    
    private fun Canvas.drawTextOnButton(){
        textPaint.color=textcolor
        drawText(
            text.toString(),
            (widthSize/2f),
            (heightSize/2f)+textPaint.computeTextOffset(),
            textPaint
        )
    }
    
    private fun TextPaint.computeTextOffset()=((descent()-ascent())/2)-descent()
    
    private fun drawAnimationOnButton()
    {
        if(animateButton==ButtonState.Loading)
        {
            computeButtonAnimationrect()
            retrieveButtonTextBounds()
            computeProgressCircleRect()
            animatorSet.start()
        }
    }
    
    private fun retrieveButtonTextBounds(){
        textPaint.getTextBounds(text,0,text.length,textBounds)
    }
    
    private fun computeProgressCircleRect(){
        val horizontalCenter=(textBounds.right+textBounds.width()+16f)
        val verticleCenter=(heightSize/2f)
        
        progressRectCircle.set(
            horizontalCenter-progressCircleSize,
            verticleCenter-progressCircleSize,
            horizontalCenter+progressCircleSize,
            verticleCenter+progressCircleSize
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}
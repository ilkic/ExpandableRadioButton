/**
 * Created by onur on 25.02.2022
 */

package com.markakod.expandableRadioButton.tooltip

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt


class ArrowDrawable internal constructor(@ColorInt foregroundColor: Int, direction: Int) :
    ColorDrawable() {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBackgroundColor: Int = Color.TRANSPARENT
    private var mPath: Path? = null
    private val mDirection: Int
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath(bounds)
    }

    @Synchronized
    private fun updatePath(bounds: Rect) {
        mPath = Path()
        when (mDirection) {
            TOP -> {
                mPath!!.moveTo(0f, bounds.height().toFloat())
                mPath!!.lineTo(bounds.width().toFloat() / 2f, 0f)
                mPath!!.lineTo(bounds.width().toFloat(), bounds.height().toFloat())
                mPath!!.lineTo(0f, bounds.height().toFloat())
            }
            BOTTOM -> {
                mPath!!.moveTo(0f, 0f)
                mPath!!.lineTo(bounds.width().toFloat() / 2f, bounds.height().toFloat())
                mPath!!.lineTo(bounds.width().toFloat(), 0f)
                mPath!!.lineTo(0f, 0f)
            }
        }
        mPath!!.close()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(mBackgroundColor)
        if (mPath == null) updatePath(bounds)
        canvas.drawPath(mPath!!, mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColor(@ColorInt color: Int) {
        mPaint.color = color
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        if (mPaint.colorFilter != null) {
            return PixelFormat.TRANSLUCENT
        }
        when (mPaint.color ushr 24) {
            255 -> return PixelFormat.OPAQUE
            0 -> return PixelFormat.TRANSPARENT
        }
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        const val AUTO = 0
        const val TOP = 1
        const val BOTTOM = 2
    }

    init {
        mPaint.color = foregroundColor
        mDirection = direction
    }
}
/**
 * Created by onur on 25.02.2022
 */

package com.markakod.expandableRadioButton.tooltip

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat


object TooltipUtils {
    fun calculateRectOnScreen(view: View): RectF {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            location[0] + view.measuredWidth.toFloat(),
            location[1] + view.measuredHeight.toFloat()
        )
    }

    fun calculateRectInWindow(view: View): RectF {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            location[0] + view.measuredWidth.toFloat(),
            location[1] + view.measuredHeight.toFloat()
        )
    }

    fun dpFromPx(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    fun pxFromDp(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    fun setWidth(view: View, width: Float) {
        var params: ViewGroup.LayoutParams? = view.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(width.toInt(), view.height)
        } else {
            params.width = width.toInt()
        }
        view.layoutParams = params
    }

    fun tooltipGravityToArrowDirection(tooltipGravity: Int): Int {
        return when (tooltipGravity) {
            Gravity.TOP -> ArrowDrawable.BOTTOM
            Gravity.BOTTOM -> ArrowDrawable.TOP
            Gravity.CENTER -> ArrowDrawable.TOP
            else -> throw IllegalArgumentException("Gravity must have be CENTER, TOP or BOTTOM.")
        }
    }

    fun setX(view: View, x: Int) {
        view.x = x.toFloat()
    }

    fun setY(view: View, y: Int) {
        view.y = y.toFloat()
    }

    private fun getOrCreateMarginLayoutParams(view: View): MarginLayoutParams {
        val lp: ViewGroup.LayoutParams? = view.layoutParams
        return if (lp != null) {
            if (lp is MarginLayoutParams) {
                lp
            } else {
                MarginLayoutParams(lp)
            }
        } else {
            MarginLayoutParams(view.width, view.height)
        }
    }

    fun removeOnGlobalLayoutListener(view: View, listener: OnGlobalLayoutListener?) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    fun setTextAppearance(tv: TextView, @StyleRes textAppearanceRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.setTextAppearance(textAppearanceRes)
        } else {
            tv.setTextAppearance(tv.context, textAppearanceRes)
        }
    }

    fun getColor(context: Context, @ColorRes colorRes: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(colorRes)
        } else {
            context.resources.getColor(colorRes)
        }
    }

    fun getDrawable(context: Context, @DrawableRes drawableRes: Int): Drawable {
        return ContextCompat.getDrawable(context, drawableRes)!!
    }

    /**
     * Verify if the first child of the rootView is a FrameLayout.
     * Used for cases where the Tooltip is created inside a Dialog or DialogFragment.
     *
     * @param anchorView
     * @return FrameLayout or anchorView.getRootView()
     */
    fun findFrameLayout(anchorView: View): ViewGroup {
        var rootView = anchorView.rootView as ViewGroup
        if (rootView.childCount == 1 && rootView.getChildAt(0) is FrameLayout) {
            rootView = rootView.getChildAt(0) as ViewGroup
        }
        return rootView
    }
}
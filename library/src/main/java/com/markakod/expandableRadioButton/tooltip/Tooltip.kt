/**
 * Created by onur on 25.02.2022
 */

package com.markakod.expandableRadioButton.tooltip

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.*
import com.markakod.expandableRadioButton.R


@SuppressLint("ClickableViewAccessibility")
class Tooltip private constructor(builder: Builder) : PopupWindow.OnDismissListener {
    private var mContext: Context? = null
    private var mOnDismissListener: OnDismissListener? = null
    private var mOnShowListener: OnShowListener? = null
    private var mPopupWindow: PopupWindow? = null
    private var mGravity: Int = Gravity.NO_GRAVITY
    private var mArrowDirection: Int = ArrowDrawable.BOTTOM
    private var mModal: Boolean = false
    private var mContentView: View? = null
    private var mContentLayout: View? = null

    @IdRes
    private var mTextViewId: Int = 0
    private var mOverlayWindowBackgroundColor: Int = Color.TRANSPARENT
    private var mText: CharSequence = ""
    private var mAnchorView: View? = null
    private var mTransparentOverlay: Boolean = true
    private var mOverlayOffset: Float = 0f
    private var mOverlayMatchParent: Boolean = false
    private var mMaxWidth: Float = 0f
    private var mOverlay: View? = null
    private var mRootView: ViewGroup? = null
    private var mShowArrow: Boolean = true
    private var mArrowView: ImageView? = null
    private var mArrowDrawable: Drawable? = null
    private var mMargin: Float = 0f
    private var mPadding: Float = 0f
    private var mArrowWidth: Float = 0f
    private var mArrowHeight: Float = 0f
    private var mFocusable: Boolean = true
    private var dismissed = false
    private var width: Int = 0
    private var height: Int = 0
    private var mIgnoreOverlay: Boolean = false
    private var mArrowColor: Int = 0
    private fun init() {
        configPopupWindow()
        configContentView()
    }

    private fun configPopupWindow() {
        mPopupWindow = PopupWindow(mContext, null, mDefaultPopupWindowStyleRes)
        mPopupWindow!!.setOnDismissListener(this)
        mPopupWindow!!.width = width
        mPopupWindow!!.height = height
        mPopupWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mPopupWindow!!.isOutsideTouchable = true
        mPopupWindow!!.isTouchable = true

        mPopupWindow!!.setTouchInterceptor(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                dismiss()
                return true
            }
        })
        mPopupWindow!!.isClippingEnabled = false
        mPopupWindow!!.isFocusable = true
    }

    fun show() {
        if (isShowing) return
        mContentLayout!!.viewTreeObserver.addOnGlobalLayoutListener(mLocationLayoutListener)
        mContentLayout!!.viewTreeObserver.addOnGlobalLayoutListener(mAutoDismissLayoutListener)
        mRootView!!.post {
            if (mRootView!!.isShown) mPopupWindow!!.showAtLocation(
                mRootView,
                Gravity.NO_GRAVITY,
                mRootView!!.width,
                mRootView!!.height
            ) else Log.e(
                TAG,
                "Tooltip cannot be shown, root view is invalid or has been closed."
            )
        }
    }

    private fun createOverlay() {
        if (mIgnoreOverlay) {
            return
        }
        mOverlay = View(mContext)

        if (mOverlayMatchParent) mOverlay!!.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ) else mOverlay!!.layoutParams = ViewGroup.LayoutParams(
            mRootView!!.width, mRootView!!.height
        )
        mOverlay!!.setOnTouchListener(mOverlayTouchListener)
        mRootView!!.addView(mOverlay)
    }

    private fun calculatePopupLocation(): PointF {
        val location = PointF()
        val anchorRect: RectF = TooltipUtils.calculateRectInWindow(mAnchorView!!)
        val anchorCenter = PointF(anchorRect.centerX(), anchorRect.centerY())

        if (mGravity == Gravity.TOP && mPopupWindow!!.contentView.height > (anchorRect.top - mMargin)) {
            mGravity = Gravity.BOTTOM
            mArrowDirection = ArrowDrawable.TOP
            val linearLayout = mContentView?.parent as LinearLayout
            linearLayout.removeAllViews()
            configArrowView(linearLayout)
        }
        when (mGravity) {
            Gravity.START -> {
                location.x = anchorRect.left - mPopupWindow!!.contentView.width - mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.END -> {
                location.x = anchorRect.right + mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.TOP -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorRect.top - mPopupWindow!!.contentView.height - mMargin
            }
            Gravity.BOTTOM -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorRect.bottom + mMargin
            }
            Gravity.CENTER -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            else -> throw IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.")
        }
        return location
    }

    private fun configContentView() {
        val inflater = LayoutInflater.from(mContext!!)
        mContentView = inflater.inflate(R.layout.layout_tooltip, null, false)

        val tv = mContentView?.findViewById(R.id.textView) as? TextView
        tv?.text = mText
        mContentView?.setPadding(
            mPadding.toInt(), mPadding.toInt(), mPadding.toInt(),
            mPadding.toInt()
        )
        val linearLayout = LinearLayout(mContext)
        linearLayout.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        linearLayout.orientation = LinearLayout.VERTICAL
        val layoutPadding = 0
        linearLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding)
        if (mShowArrow) {
            configArrowView(linearLayout)
        } else {
            linearLayout.addView(mContentView)
        }
        val contentViewParams = LinearLayout.LayoutParams(width, height, 0f)
        contentViewParams.gravity = Gravity.CENTER
        contentViewParams.setMargins(mMargin.toInt(), 0, mMargin.toInt(), 0)
        mContentView?.layoutParams = contentViewParams
        mContentLayout = linearLayout
        mContentLayout?.visibility = View.INVISIBLE
        mPopupWindow!!.contentView = mContentLayout
    }


    private fun configArrowView(linearLayout: LinearLayout) {
        mArrowView = ImageView(mContext)
        mArrowDrawable = ArrowDrawable(mArrowColor, mArrowDirection)
        mArrowView?.setImageDrawable(mArrowDrawable)
        val arrowLayoutParams: LinearLayout.LayoutParams =
            if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                LinearLayout.LayoutParams(
                    mArrowWidth.toInt(),
                    mArrowHeight.toInt(), 0f
                )
            } else {
                LinearLayout.LayoutParams(
                    mArrowHeight.toInt(),
                    mArrowWidth.toInt(), 0f
                )
            }
        arrowLayoutParams.gravity = Gravity.CENTER
        mArrowView?.layoutParams = arrowLayoutParams
        if (mArrowDirection == ArrowDrawable.BOTTOM) {
            linearLayout.addView(mContentView)
            linearLayout.addView(mArrowView)
        } else {
            linearLayout.addView(mArrowView)
            linearLayout.addView(mContentView)
        }
    }

    fun dismiss() {
        if (dismissed) return
        dismissed = true
        if (mPopupWindow != null) {
            mPopupWindow!!.dismiss()
        }
    }


    val isShowing: Boolean
        get() = mPopupWindow != null && mPopupWindow!!.isShowing

    fun <T : View?> findViewById(id: Int): T {
        return mContentLayout!!.findViewById(id)
    }

    override fun onDismiss() {
        dismissed = true
        if (mRootView != null && mOverlay != null) {
            mRootView!!.removeView(mOverlay)
        }
        mRootView = null
        mOverlay = null
        if (mOnDismissListener != null) mOnDismissListener!!.onDismiss(this)
        mOnDismissListener = null
        TooltipUtils.removeOnGlobalLayoutListener(
            mPopupWindow!!.contentView,
            mLocationLayoutListener
        )
        TooltipUtils.removeOnGlobalLayoutListener(
            mPopupWindow!!.contentView,
            mArrowLayoutListener
        )
        TooltipUtils.removeOnGlobalLayoutListener(
            mPopupWindow!!.contentView,
            mShowLayoutListener
        )
        TooltipUtils.removeOnGlobalLayoutListener(
            mPopupWindow!!.contentView,
            mAnimationLayoutListener
        )
        TooltipUtils.removeOnGlobalLayoutListener(
            mPopupWindow!!.contentView,
            mAutoDismissLayoutListener
        )
        mPopupWindow = null
    }

    private val mOverlayTouchListener: View.OnTouchListener =
        View.OnTouchListener { _, _ -> mModal }
    private val mLocationLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            if (mMaxWidth > 0 && mContentView!!.width > mMaxWidth) {
                TooltipUtils.setWidth(mContentView!!, mMaxWidth)
                popup.update(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                return
            }
            TooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mArrowLayoutListener)
            val location = calculatePopupLocation()
            popup.isClippingEnabled = true
            popup.update(location.x.toInt(), location.y.toInt(), popup.width, popup.height)
            popup.contentView.requestLayout()
            createOverlay()
        }
    }
    private val mArrowLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            TooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mShowLayoutListener)
            if (mShowArrow) {
                val achorRect: RectF = TooltipUtils.calculateRectOnScreen(mAnchorView!!)
                val contentViewRect: RectF =
                    TooltipUtils.calculateRectOnScreen(mContentLayout!!)
                var x: Float = mContentLayout!!.paddingLeft + TooltipUtils.pxFromDp(2f)
                val centerX: Float = contentViewRect.width() / 2f - mArrowView!!.width / 2f
                val newX = centerX - (contentViewRect.centerX() - achorRect.centerX())
                if (newX > x) {
                    x = if (newX + mArrowView!!.width + x > contentViewRect.width()) {
                        contentViewRect.width() - mArrowView!!.width - x
                    } else {
                        newX
                    }
                }
                var y: Float = mArrowView!!.top.toFloat()
                y += if (mArrowDirection == ArrowDrawable.BOTTOM) -1 else +1

                TooltipUtils.setX(mArrowView!!, x.toInt())
                TooltipUtils.setY(mArrowView!!, y.toInt())
            }
            popup.contentView.requestLayout()
        }
    }
    private val mShowLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            TooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)
            if (mOnShowListener != null) mOnShowListener!!.onShow(this@Tooltip)
            mOnShowListener = null
            mContentLayout!!.visibility = View.VISIBLE
        }
    }
    private val mAnimationLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            TooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.requestLayout()
        }
    }




    private val mAutoDismissLayoutListener = OnGlobalLayoutListener {
        val popup = mPopupWindow
        if (popup == null || dismissed) return@OnGlobalLayoutListener
        if (!mRootView!!.isShown) dismiss()
    }

    interface OnDismissListener {
        fun onDismiss(tooltip: Tooltip?)
    }

    interface OnShowListener {
        fun onShow(tooltip: Tooltip?)
    }


    class Builder(context: Context?) {
        var context: Context? = null
        var modal = true
        var text: CharSequence = ""
        var anchorView: View? = null
        var arrowDirection: Int = ArrowDrawable.TOP
        var gravity = Gravity.BOTTOM
        var transparentOverlay = true
        var overlayOffset = -1f
        var overlayMatchParent = true
        var maxWidth = 0f
        var showArrow = true
        var arrowDrawable: Drawable? = null
        var margin = -1f
        var padding = -1f
        var onDismissListener: OnDismissListener? = null
        var onShowListener: OnShowListener? = null
        private var backgroundColor = 0
        private var textColor = 0
        var arrowColor = 0
        var arrowHeight = 0f
        var arrowWidth = 0f
        var focusable = true
        var width = ViewGroup.LayoutParams.WRAP_CONTENT
        var height = ViewGroup.LayoutParams.WRAP_CONTENT
        var ignoreOverlay = false
        var overlayWindowBackgroundColor = 0

        @Throws(IllegalArgumentException::class)
        fun build(): Tooltip {
            validateArguments()

            if (backgroundColor == 0) {
                backgroundColor = TooltipUtils.getColor(context!!, mDefaultBackgroundColorRes)
            }
            if (overlayWindowBackgroundColor == 0) {
                overlayWindowBackgroundColor = Color.BLACK
            }
            if (textColor == 0) {
                textColor = TooltipUtils.getColor(context!!, mDefaultTextColorRes)
            }

            if (arrowColor == 0) {
                arrowColor = TooltipUtils.getColor(context!!, mDefaultArrowColorRes)
            }
            if (margin < 0) {
                margin = context!!.resources.getDimension(mDefaultMarginRes)
            }
            if (padding < 0) {
                padding = context!!.resources.getDimension(mDefaultPaddingRes)
            }
            if (showArrow) {
                if (arrowDirection == ArrowDrawable.AUTO)
                    arrowDirection = TooltipUtils.tooltipGravityToArrowDirection(gravity)
                if (arrowDrawable == null) arrowDrawable = ArrowDrawable(arrowColor, arrowDirection)
                if (arrowWidth == 0f) arrowWidth = context!!.resources.getDimension(
                    mDefaultArrowWidthRes
                )
                if (arrowHeight == 0f) arrowHeight = context!!.resources.getDimension(
                    mDefaultArrowHeightRes
                )
            }

            if (overlayOffset < 0) {
                overlayOffset = context!!.resources.getDimension(mDefaultOverlayOffsetRes)
            }
            return Tooltip(this)
        }

        @Throws(IllegalArgumentException::class)
        private fun validateArguments() {
            requireNotNull(context) { "Context not specified." }
            requireNotNull(anchorView) { "Anchor view not specified." }
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }


        fun modal(modal: Boolean): Builder {
            this.modal = modal
            return this
        }


        fun text(text: CharSequence): Builder {
            this.text = text
            return this
        }


        fun text(@StringRes textRes: Int): Builder {
            text = context!!.getString(textRes)
            return this
        }


        fun anchorView(anchorView: View?): Builder {
            this.anchorView = anchorView
            return this
        }


        fun gravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }


        fun arrowDirection(arrowDirection: Int): Builder {
            this.arrowDirection = arrowDirection
            return this
        }


        fun transparentOverlay(transparentOverlay: Boolean): Builder {
            this.transparentOverlay = transparentOverlay
            return this
        }


        fun maxWidth(@DimenRes maxWidthRes: Int): Builder {
            maxWidth = context!!.resources.getDimension(maxWidthRes)
            return this
        }


        fun maxWidth(maxWidth: Float): Builder {
            this.maxWidth = maxWidth
            return this
        }


        fun padding(padding: Float): Builder {
            this.padding = padding
            return this
        }


        fun padding(@DimenRes paddingRes: Int): Builder {
            padding = context!!.resources.getDimension(paddingRes)
            return this
        }


        fun margin(margin: Float): Builder {
            this.margin = margin
            return this
        }


        fun margin(@DimenRes marginRes: Int): Builder {
            margin = context!!.resources.getDimension(marginRes)
            return this
        }

        fun textColor(textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        fun backgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun overlayWindowBackgroundColor(@ColorInt overlayWindowBackgroundColor: Int): Builder {
            this.overlayWindowBackgroundColor = overlayWindowBackgroundColor
            return this
        }


        fun showArrow(showArrow: Boolean): Builder {
            this.showArrow = showArrow
            return this
        }

        fun arrowDrawable(arrowDrawable: Drawable?): Builder {
            this.arrowDrawable = arrowDrawable
            return this
        }

        fun arrowDrawable(@DrawableRes drawableRes: Int): Builder {
            arrowDrawable = TooltipUtils.getDrawable(context!!, drawableRes)
            return this
        }

        fun arrowColor(@ColorInt arrowColor: Int): Builder {
            this.arrowColor = arrowColor
            return this
        }


        fun arrowHeight(arrowHeight: Float): Builder {
            this.arrowHeight = arrowHeight
            return this
        }


        fun arrowWidth(arrowWidth: Float): Builder {
            this.arrowWidth = arrowWidth
            return this
        }

        fun onDismissListener(onDismissListener: OnDismissListener?): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun onShowListener(onShowListener: OnShowListener?): Builder {
            this.onShowListener = onShowListener
            return this
        }


        fun focusable(focusable: Boolean): Builder {
            this.focusable = focusable
            return this
        }


        fun overlayOffset(@Dimension overlayOffset: Float): Builder {
            this.overlayOffset = overlayOffset
            return this
        }


        fun overlayMatchParent(overlayMatchParent: Boolean): Builder {
            this.overlayMatchParent = overlayMatchParent
            return this
        }

        fun ignoreOverlay(ignoreOverlay: Boolean): Builder {
            this.ignoreOverlay = ignoreOverlay
            return this
        }

        init {
            this.context = context
        }
    }

    companion object {
        private val TAG = Tooltip::class.java.simpleName

        // Default Resources
        private const val mDefaultPopupWindowStyleRes = android.R.attr.popupWindowStyle
        private val mDefaultBackgroundColorRes: Int = R.color.tooltipColor
        private val mDefaultTextColorRes: Int = R.color.white
        private val mDefaultArrowColorRes: Int = R.color.tooltipColor
        private val mDefaultMarginRes: Int = R.dimen.tooltip_margin_
        private val mDefaultPaddingRes: Int = R.dimen.tooltip_padding
        private val mDefaultArrowWidthRes: Int = R.dimen.tooltip_arrow_width
        private val mDefaultArrowHeightRes: Int = R.dimen.tooltip_arrow_height
        private val mDefaultOverlayOffsetRes: Int = R.dimen.tooltip_overlay_offset
    }

    init {
        mContext = builder.context
        mGravity = builder.gravity
        mOverlayWindowBackgroundColor = builder.overlayWindowBackgroundColor
        mArrowDirection = builder.arrowDirection
        mModal = builder.modal
        mText = builder.text
        mAnchorView = builder.anchorView
        mTransparentOverlay = builder.transparentOverlay
        mOverlayOffset = builder.overlayOffset
        mOverlayMatchParent = builder.overlayMatchParent
        mMaxWidth = builder.maxWidth
        mShowArrow = builder.showArrow
        mArrowWidth = builder.arrowWidth
        mArrowHeight = builder.arrowHeight
        mArrowDrawable = builder.arrowDrawable
        mMargin = builder.margin
        mPadding = builder.padding
        mOnDismissListener = builder.onDismissListener
        mOnShowListener = builder.onShowListener
        mFocusable = builder.focusable
        mRootView = TooltipUtils.findFrameLayout(mAnchorView!!)
        mIgnoreOverlay = builder.ignoreOverlay
        width = builder.width
        height = builder.height
        mArrowColor = builder.arrowColor
        init()
    }
}
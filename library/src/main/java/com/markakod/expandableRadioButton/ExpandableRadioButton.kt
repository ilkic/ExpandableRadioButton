/**
 * Created by onur on 23.02.2022
 */

package com.markakod.expandableRadioButton

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import com.markakod.expandableRadioButton.tooltip.ArrowDrawable
import com.markakod.expandableRadioButton.tooltip.Tooltip


class ExpandableRadioButton : ConstraintLayout {
    private var item: ExpandableRadioItem? = null

    var onItemSelectListener: OnItemSelectListener? = null

    var titleText: CharSequence? = null
        set(value) {
            field = value
            titleTextView?.text = value
        }

    var isInfoButtonEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                infoImageButton?.visibility = View.VISIBLE
            } else {
                infoImageButton?.visibility = View.GONE
            }

        }

    var infoText: CharSequence? = null

    var isAnimationEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                var layoutTransition: LayoutTransition? = layoutTransition
                if (layoutTransition == null) {
                    layoutTransition = LayoutTransition()
                }
                layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0)
                layoutTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING)
                layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
                this.layoutTransition = layoutTransition
            } else {
                this.layoutTransition = null
            }
            checkParentAnimation()
        }

    var isRadioButtonSelected: Boolean = false
        set(value) {
            field = value
            statusImageView?.isSelected = value
            if (value) {
                if (canExpand) {
                    expandableViewContainer?.visibility = View.VISIBLE
                }
                titleStyleActive?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        titleTextView?.setTextAppearance(it)
                    } else {
                        titleTextView?.setTextAppearance(context, it)
                    }
                }
            } else {
                if (canExpand) {
                    expandableViewContainer?.visibility = View.GONE
                }
                titleStyleDefault?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        titleTextView?.setTextAppearance(it)
                    } else {
                        titleTextView?.setTextAppearance(context, it)
                    }
                }
            }
        }

    var expandableTitleText: CharSequence? = null
        set(value) {
            field = value
            expandableViewTitleTextView?.text = value
        }

    var expandableSubtitleText: CharSequence? = null
        set(value) {
            field = value
            expandableViewSubtitleTextView?.text = value
        }

    var expandableImageDrawable: Drawable? = null
        set(value) {
            field = value
            expandableViewImageView?.setImageDrawable(value)
        }

    var canExpand: Boolean = true
        set(value)  {
            field = value
            if (value) {
                expandableViewContainer?.visibility = if(isRadioButtonSelected) View.VISIBLE else View.GONE
            } else {
                expandableViewContainer?.visibility = View.GONE
            }
        }

    var expandableView: ViewGroup? = null
        set (value) {
            field = value
            if (value == null) {
                expandableViewContainer?.removeAllViews()
            } else {
                expandableViewContainer?.addView(expandableView)
            }
        }


    private var titleStyleActive: Int? = null
    private var titleStyleDefault: Int? = null

    private var statusImageView: ImageView? = null
    private var titleTextView: TextView? = null
    private var infoImageButton: ImageButton? = null
    internal var expandableViewContainer: ViewGroup? = null
    private var expandableViewTitleTextView: TextView? = null
    private var expandableViewSubtitleTextView: TextView? = null
    private var expandableViewImageView: ImageView? = null

    private var tooltip: Tooltip? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        obtainAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
        obtainAttributes(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
        obtainAttributes(context, attrs)
    }

    private fun obtainAttributes(context: Context, attrs: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableRadioButton)
        try {
            val theme = typedArray.getResourceId(
                R.styleable.ExpandableRadioButton_erb_theme,
                R.style.ExpandableRadioButton
            )
            val contextThemeWrapper = ContextThemeWrapper(context, context.theme)
            contextThemeWrapper.setTheme(theme)
            titleText = typedArray.getString(R.styleable.ExpandableRadioButton_erb_titleText)
            infoText = typedArray.getString(R.styleable.ExpandableRadioButton_erb_infoText)

            titleStyleActive = typedArray.getResourceId(
                R.styleable.ExpandableRadioButton_erb_titleStyle_active,
                R.style.ExpandableRadioButton_TitleStyle_Active
            )
            titleStyleDefault = typedArray.getResourceId(
                R.styleable.ExpandableRadioButton_erb_titleStyle_default,
                R.style.ExpandableRadioButton_TitleStyle
            )
            isInfoButtonEnabled = typedArray.getBoolean(
                R.styleable.ExpandableRadioButton_erb_isInfoButtonEnabled,
                false
            )
            infoText = typedArray.getString(R.styleable.ExpandableRadioButton_erb_infoText)
            isAnimationEnabled = typedArray.getBoolean(
                R.styleable.ExpandableRadioButton_erb_isAnimationEnabled,
                true
            )

            canExpand = typedArray.getBoolean(R.styleable.ExpandableRadioButton_erb_canExpand, true)
            isRadioButtonSelected = typedArray.getBoolean(
                R.styleable.ExpandableRadioButton_erb_isRadioButtonSelected,
                false
            )

            val layoutId = typedArray.getResourceId(
                R.styleable.ExpandableRadioButton_erb_expandLayoutId,
                -1
            )
            if (layoutId != -1) {
                val _expandableView = LayoutInflater.from(context).inflate(layoutId, expandableViewContainer, false)
                if (_expandableView is ViewGroup) {
                    this.expandableView = _expandableView
                } else {
                    throw IllegalArgumentException("Expandable view must be extends from ViewGroup")
                }

            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.layout_expandable_radio_button, this, true)
        this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                checkParentAnimation()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        statusImageView = findViewById(R.id.statusImageView)
        titleTextView = findViewById(R.id.titleTextView)
        infoImageButton = findViewById(R.id.infoImageButton)
        expandableViewContainer = findViewById(R.id.expandableLayout)


        infoImageButton?.setOnClickListener {
            showTooltip()
        }


        isClickable = true
        isFocusable = true

        setOnClickListener {
            if (item == null) {// check, is it in a recyclerview, disable unselect listener if it is
                if (isRadioButtonSelected) {
                    onItemSelectListener?.onExpandableRadioButtonUnselected(null)
                } else {
                    onItemSelectListener?.onExpandableRadioButtonSelected(null)
                }
            } else {
                onItemSelectListener?.onExpandableRadioButtonSelected(item)
            }
        }
    }

    private fun checkParentAnimation() {
        if (parent != null) {
            val parent = parent as ViewGroup
            var lt = parent.layoutTransition
            if (isAnimationEnabled) {
                if (lt == null) {
                    lt = LayoutTransition()
                }
                lt.enableTransitionType(LayoutTransition.CHANGING)
                parent.layoutTransition = lt
            }
        }
    }

    private fun showTooltip() {
        infoImageButton?.let {
            tooltip?.dismiss()
            tooltip = Tooltip.Builder(context)
                .anchorView(infoImageButton!!)
                .gravity(Gravity.TOP)
                .text(infoText!!)
                .modal(true)
                .arrowDirection(ArrowDrawable.BOTTOM)
                .showArrow(true)
                .build()
            tooltip?.show()
        }
    }


    fun setItem(i: ExpandableRadioItem) {
        item = i
        titleText = i.titleText
        infoText = i.infoText
        isRadioButtonSelected = i.isSelected
        isInfoButtonEnabled = i.isInfoButtonEnabled
        canExpand = i.canExpand
    }


    interface OnItemSelectListener {
        fun onExpandableRadioButtonSelected(item: ExpandableRadioItem?)
        fun onExpandableRadioButtonUnselected(item: ExpandableRadioItem?)
    }
}
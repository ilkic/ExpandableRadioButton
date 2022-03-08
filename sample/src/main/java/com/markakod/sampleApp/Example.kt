/**
 * Created by onur on 27.02.2022
 */

package com.markakod.sampleApp

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import androidx.core.text.toSpanned
import com.markakod.expandableRadioButton.ExpandableRadioItem

data class Example(
    val id: Int,
    val title: String,
    val price: Float,
    val campaignText: String?,
    val description: String?
) {
    fun getPriceAsSpannable(): SpannableStringBuilder {
        val spannableString = SpannableStringBuilder("8,90 TL")
        spannableString.setSpan(
            RelativeSizeSpan(1.57f),
            0,
            1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(1f),
            2,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }
}

data class ExampleDescriptor(val model: Example) :
    ExpandableRadioItem {
    override var isSelected: Boolean = false
    override var titleText: CharSequence = model.title
    override var isInfoButtonEnabled: Boolean = model.description != null
    override var infoText: CharSequence? = model.description
    override var canExpand: Boolean = model.campaignText != null
    var imageDrawable: Drawable? = null
}

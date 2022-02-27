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
        val spannableString = SpannableStringBuilder("89,90 TL")
        spannableString.setSpan(
            RelativeSizeSpan(2f),
            0,
            spannableString.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(0.50f),
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
    override var expandableTitleText: CharSequence? = model.getPriceAsSpannable().toSpanned()
    override var expandableSubtitleText: CharSequence? = model.campaignText
    override var expandableImage: Drawable? = null
    override var canExpand: Boolean = model.campaignText != null
}

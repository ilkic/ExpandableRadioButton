/**
 * Created by onur on 26.02.2022
 */
package com.markakod.expandableRadioButton

import android.graphics.drawable.Drawable

interface ExpandableRadioItem {
    var isSelected: Boolean
    var titleText: CharSequence
    var isInfoButtonEnabled: Boolean
    var infoText: CharSequence?
    var expandableTitleText: CharSequence?
    var expandableSubtitleText: CharSequence?
    var expandableImage: Drawable?
    var canExpand: Boolean
}
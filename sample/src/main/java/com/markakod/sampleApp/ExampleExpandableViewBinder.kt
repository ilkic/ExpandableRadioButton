package com.markakod.sampleApp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.markakod.expandableRadioButton.ExpandableRadioItemRecyclerAdapter

class ExampleExpandableViewBinder: ExpandableRadioItemRecyclerAdapter.ExpandableViewBinder<ExampleDescriptor> {

	override fun onExpandableViewBind(view: ViewGroup?, item: ExampleDescriptor) {
		val expandableViewImageView = view?.findViewById<ImageView>(R.id.expandableViewImageView)
		val expandableViewTitleTextView = view?.findViewById<TextView>(R.id.expandableViewTitleTextView)
		val expandableViewSubtitleTextView = view?.findViewById<TextView>(R.id.expandableViewSubtitleTextView)

		expandableViewImageView?.setImageDrawable(item.imageDrawable)
		expandableViewTitleTextView?.text = item.model.getPriceAsSpannable()
		expandableViewSubtitleTextView?.text = item.model.campaignText
	}

	override fun onExpandableViewCreate(parent: ViewGroup): ViewGroup {
		return LayoutInflater.from(parent.context).inflate(R.layout.layout_example_expandable_view, parent, false) as ConstraintLayout
	}
}
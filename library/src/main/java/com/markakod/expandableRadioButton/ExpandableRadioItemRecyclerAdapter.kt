/**
 * Created by onur on 26.02.2022
 */

package com.markakod.expandableRadioButton

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ExpandableRadioItemRecyclerAdapter<T: ExpandableRadioItem>(
    val expandableViewBinder: ExpandableViewBinder<T>,
    di: DiffUtil.ItemCallback<T>
): ListAdapter<T, ExpandableRadioItemRecyclerAdapter.ViewHolder>(di),
    ExpandableRadioButton.OnItemSelectListener {

    var onItemSelectListener: OnItemSelectListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_item, parent, false))
        vh.erb.expandableView = expandableViewBinder.onExpandableViewCreate(vh.erb.expandableViewContainer!!)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), this)
        expandableViewBinder.onExpandableViewBind(holder.erb.expandableView, getItem(position))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onExpandableRadioButtonSelected(item: ExpandableRadioItem?) {
        val index = currentList.indexOf(item)
        if (index >= 0) {
            if (currentList[index].isSelected) return
            val currentIndex = currentList.indexOfFirst { it.isSelected }
            if (currentIndex >= 0) {
                currentList[currentIndex].isSelected = false
                onExpandableRadioButtonUnselected(currentList[currentIndex])
                notifyItemChanged(currentIndex)
            }

            currentList[index].isSelected = true
            onItemSelectListener?.onExpandableRadioItemSelected(currentList[index])
            notifyItemChanged(index)
        }
    }

    override fun onExpandableRadioButtonUnselected(item: ExpandableRadioItem?) {
        val index = currentList.indexOf(item)
        if (index >= 0) {
            currentList[index].isSelected = false
            onItemSelectListener?.onExpandableRadioItemUnselected(currentList[index])
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var erb: ExpandableRadioButton = view.findViewById(R.id.expandableRadioButton)
        fun <T: ExpandableRadioItem> bind(item: T, onItemSelectListener: ExpandableRadioButton.OnItemSelectListener) {
            erb.setItem(item)
            erb.onItemSelectListener = onItemSelectListener
        }
    }

    interface OnItemSelectListener<T> {
        fun onExpandableRadioItemSelected(item: T)
        fun onExpandableRadioItemUnselected(item: T)
    }

    interface ExpandableViewBinder<T> {
        fun onExpandableViewBind(view: ViewGroup?, item: T)
        fun onExpandableViewCreate(parent: ViewGroup): ViewGroup?
    }
}
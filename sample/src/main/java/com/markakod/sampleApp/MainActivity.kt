package com.markakod.sampleApp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.markakod.expandableRadioButton.ExpandableRadioButton
import com.markakod.expandableRadioButton.ExpandableRadioItem
import com.markakod.expandableRadioButton.ExpandableRadioItemRecyclerAdapter


private const val TAG = "ExampleRadioButton"


class ExampleDiffCallback : DiffUtil.ItemCallback<ExampleDescriptor>() {
    override fun areItemsTheSame(oldItem: ExampleDescriptor, newItem: ExampleDescriptor): Boolean {
        return oldItem.model.id == newItem.model.id
    }

    override fun areContentsTheSame(
        oldItem: ExampleDescriptor,
        newItem: ExampleDescriptor
    ): Boolean {
        return oldItem == newItem
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main)

        val radioButton = findViewById<ExpandableRadioButton>(R.id.radioButton)
        radioButton.onItemSelectListener = object : ExpandableRadioButton.OnItemSelectListener {
            override fun onExpandableRadioButtonSelected(item: ExpandableRadioItem?) {
                radioButton.isRadioButtonSelected = true
            }

            override fun onExpandableRadioButtonUnselected(item: ExpandableRadioItem?) {
                radioButton.isRadioButtonSelected = false
            }
        }

        val exampleList = mutableListOf<Example>()
        for (i in 0..10) {
            exampleList.add(
                Example(
                    i,
                    if (i % 3 == 0) "Title $i" else "Mevcut Evde İnternetim üzerine Tivibu Ev (IPTV) almak istiyorum",
                    0f,
                    if (i % 4 == 0) "Tarifeye Ek\nx 2$i Ay" else null,
                    if (i % 2 == 0) "$i " + getString(R.string.info_text) else null
                )
            )
        }

        val itemList = mutableListOf<ExampleDescriptor>()
        for (i in 0 until exampleList.size) {
            val item = ExampleDescriptor(exampleList[i])
            item.expandableImage = ContextCompat.getDrawable(this, R.mipmap.modem)
            itemList.add(item)
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = ExpandableRadioItemRecyclerAdapter(
            ExampleDiffCallback()
        )
        adapter.onItemSelectListener =
            object : ExpandableRadioItemRecyclerAdapter.OnItemSelectListener<ExampleDescriptor> {
                override fun onExpandableRadioItemSelected(item: ExampleDescriptor) {
                    Log.d(TAG, "Item selected $item")
                }

                override fun onExpandableRadioItemUnselected(item: ExampleDescriptor) {
                    Log.d(TAG, "Item unselected $item")
                }
            }
        recyclerView.adapter = adapter
        adapter.submitList(itemList)


    }
}
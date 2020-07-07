package com.example.placebook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.placebook.R
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel.BookmarkView
import kotlinx.android.synthetic.main.bookmark_item.view.*

//1
class BookmarkListAdapter(
    private var bookmarData: List<BookmarkView>?,
    private val mapsActivity: MapsActivity
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {
    // 2
    class ViewHolder(v: View, private val mapsActivity: MapsActivity) : RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.bookmarkNameTextView
        val categoryImageView: ImageView = v.bookmarkIcon

        init {
            v.setOnClickListener {
                val bookmarkView = itemView.tag as BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
    }

    // 3
    fun setBookmarkData(bookmarks: List<BookmarkView>) {
        this.bookmarData = bookmarks
        notifyDataSetChanged()
    }

    // 4
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkListAdapter.ViewHolder {
        val vh = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bookmark_item, parent, false
            ), mapsActivity
        )
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 5
        val bookmarkData = bookmarData ?: return
        // 6
        val bookmarkViewData = bookmarkData[position]
        // 7
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        holder.categoryImageView.setImageResource(R.mipmap.ic_other)
    }

    // 8
    override fun getItemCount(): Int {
        return bookmarData?.size ?: 0
    }


}
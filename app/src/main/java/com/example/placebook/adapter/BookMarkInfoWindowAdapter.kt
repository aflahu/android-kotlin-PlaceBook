package com.example.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.placebook.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookMarkInfoWindowAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {
    private val contents: View

    init {
        contents = context.layoutInflater.inflate(
            R.layout.content_bookmark_info, null
        )
    }

    override fun getInfoContents(marker: Marker): View {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""

        val imageView = contents.findViewById<ImageView>(R.id.photo)
        imageView.setImageBitmap(marker.tag as Bitmap?)
        return contents
    }

    override fun getInfoWindow(marker: Marker): View? {
        // this function is required
        // not replacing
        return null
    }


}
package com.example.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {
    // 1
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }
    // 2
    private lateinit var listener: PhotoOptionDialogListener
    // 3
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 4
        listener = activity as PhotoOptionDialogListener
        // 5
        var captureSelectIdx = -1
        var pickSelectIdx = -1
        // 6
        val options = ArrayList<String>()
        // 7 unmutabe to prevent compiler error
        val context = activity as Context
        // 8
        if (canCapture(context)) {
            options.add("Camera")
            captureSelectIdx = 0
        }
        // 1
        if (canPick(context)) {
            options.add("Gallery")
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }
        // 2
        return AlertDialog.Builder(context).setTitle("Photo Option")
            .setItems(options.toTypedArray<CharSequence>()) { _, which ->
                if (which == captureSelectIdx) {
                    // 3
                    listener.onCaptureClick()
                } else if (which == pickSelectIdx) {
                    //4
                    listener.onPickClick()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        // 5
        fun canPick(context: Context): Boolean {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            return (pickIntent.resolveActivity(context.packageManager) != null)
        }
        // 6
        fun canCapture(context: Context): Boolean {
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )
            return (captureIntent.resolveActivity(context.packageManager) != null)
        }
        // 7
        fun newInstance(context: Context): PhotoOptionDialogFragment? {
            // 8
            if (canPick(context) || canCapture(context)) {
                val frag = PhotoOptionDialogFragment()
                return frag
            } else {
                return null
            }
        }
    }
}
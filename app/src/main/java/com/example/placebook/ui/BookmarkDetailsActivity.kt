package com.example.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.example.placebook.R
import com.example.placebook.util.ImageUtils
import com.example.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID, 0)

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
            this, Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                it?.let {
                    bookmarkDetailsView = it

                    populateFields()
                    populateImageView()
                }
            }
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun populateFields() {
        bookmarkDetailsView?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }

            imageViewPlace.setOnClickListener {
                replaceImage()
            }
        }
    }

    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }

        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = editTextName.text.toString()
            bookmarkView.notes = editTextNotes.text.toString()
            bookmarkView.address = editTextAddress.text.toString()
            bookmarkView.phone = editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCaptureClick() {
        // 1
        photoFile = null
        try {
            // 2
            photoFile = ImageUtils.createUniqueImageFle(this)
        } catch (ex: java.io.IOException) {
            // 3
            return
        }

        // 4
        photoFile?.let { photoFile ->
            val photoUrl =
                FileProvider.getUriForFile(this, "com.example.placebook.fileprovider", photoFile)
            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUrl)
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY
            )
            intentActivities.map { it.activityInfo.packageName }.forEach {
                grantUriPermission(
                    it,
                    photoUrl,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }

    }

    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)

        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val photoFile = photoFile ?: return
                    val url = FileProvider.getUriForFile(
                        this,
                        "com.example.placebook.fileprovider",
                        photoFile
                    )
                    revokeUriPermission(url, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let { updateImage(it) }
                }
                REQUEST_GALLERY_IMAGE -> {
                    if (data != null && data.data != null) {

                        val imageUri = data.data as Uri
                        val image = getImageWithAuthority(imageUri)
                        image?.let { updateImage(it) }
                    }
                }
            }
        }
    }

    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(
            uri, resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height), this
        )
    }

    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(
            filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height)
        )
    }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}
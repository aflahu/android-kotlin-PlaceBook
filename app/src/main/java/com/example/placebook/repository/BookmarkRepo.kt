package com.example.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placebook.R
import com.example.placebook.db.BookmarkDao
import com.example.placebook.db.PlaceBookDatabase
import com.example.placebook.model.Bookmark
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkRepo(private val context: Context) {

    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()
    private var categoryMap: HashMap<Place.Type, String> = buildCategoryMap()
    val categories: List<String>
        get() = ArrayList(allCategories.keys)

    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insretBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    fun getBookmark(bookmarkid: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkid)
    }

    fun getLiveBookmark(bookmarkid: Long): LiveData<Bookmark> {
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkid)
        return bookmark
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

    private fun buildCategoryMap(): HashMap<Place.Type, String> {
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging"
        )
    }

    fun placeTypeToCategory(placeType: Place.Type): String {
        var category = "Other"
        if (categoryMap.containsKey(placeType)) {
            category = categoryMap[placeType].toString()
        }
        return category
    }

    private fun buildCategories(): HashMap<String, Int> {
        return hashMapOf(
            "Gas" to R.mipmap.ic_gas,
            "Lodging" to R.mipmap.ic_lodging,
            "Other" to R.mipmap.ic_other,
            "Restaurant" to R.mipmap.ic_restaurant,
            "Shopping" to R.mipmap.ic_shop
        )
    }

    private var allCategories: HashMap<String, Int> = buildCategories()

    fun getCategoryResourceId(placeCategory: String): Int? {
        return allCategories[placeCategory]
    }
}
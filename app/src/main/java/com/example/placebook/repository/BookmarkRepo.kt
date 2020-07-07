package com.example.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placebook.db.BookmarkDao
import com.example.placebook.db.PlaceBookDatabase
import com.example.placebook.model.Bookmark

class BookmarkRepo(context: Context) {

    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

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
}
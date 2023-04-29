/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.catalogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.readium.r2.testapp.db.BookDatabase
import org.readium.r2.testapp.domain.model.Book
import org.readium.r2.testapp.domain.model.Catalog

class CatalogFeedListViewModel(application: Application) : AndroidViewModel(application) {

    private val catalogDao = BookDatabase.getDatabase(application).catalogDao()
    private val bookDao = BookDatabase.getDatabase(application).booksDao()
    private val repository = CatalogRepository(catalogDao)
    val catalogs = repository.getCatalogsFromDatabase()

    fun insertCatalog(catalog: JSONObject) = viewModelScope.launch {
        val id = repository.insertCatalog(
            Catalog(
                title = catalog.getString("name"),
                href = "",
                type = 1
            )
        )
        val books = catalog.getJSONArray("jingInfoList")
        for (j in 0 until books.length()) {
            val book = books.getJSONObject(j)
            bookDao.insertBook(
                Book(
                    title = book.getString("jing"),
                    href = "",
                    author = book.getString("author"),
                    identifier = "",
                    type = "2",
                    progression = "{}",
                    catalog = id
                )
            )
        }
    }
}

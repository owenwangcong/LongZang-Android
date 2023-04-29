/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.catalogs

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.readium.r2.testapp.db.BookDatabase
import org.readium.r2.testapp.domain.model.Book
import org.readium.r2.testapp.domain.model.Catalog

class CatalogViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val bookDao = BookDatabase.getDatabase(application).booksDao()

    fun parseCatalog(catalog: Catalog): LiveData<List<Book>>? {
        catalog.id?.let {
            return bookDao.getAllBooksByCatalog(it)
        }
        return null
    }
}

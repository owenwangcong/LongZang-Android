/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.bookshelf

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.readium.r2.shared.UserException
import org.readium.r2.testapp.domain.model.Book
import org.readium.r2.testapp.reader.ReaderActivityContract
import org.readium.r2.testapp.reader.ReaderRepository
import org.readium.r2.testapp.utils.EventChannel

class BookshelfViewModel(application: Application) : AndroidViewModel(application) {

    val channel = EventChannel(Channel<Event>(Channel.BUFFERED), viewModelScope)
    val books = app.bookRepository.booksInShelf()

    private val app get() = getApplication<org.readium.r2.testapp.Application>()

    fun deleteBookInShelf(book: Book) = viewModelScope.launch {
        book.id?.let { app.bookRepository.deleteBookInShelf(it) }
    }

    fun addToBookShelf(bookId: Long) = viewModelScope.launch {
        app.bookRepository.addToBookShelf(bookId)
    }


    fun openBook(
        bookId: Long,
        activity: Activity
    ) = viewModelScope.launch {
        val readerRepository = app.readerRepository.await()
        readerRepository.open(bookId, activity)
            .onFailure { exception ->
                if (exception is ReaderRepository.CancellationException)
                    return@launch

                val message = when (exception) {
                    is UserException -> exception.getUserMessage(app)
                    else -> exception.message
                }
                channel.send(Event.OpenBookError(message))
            }
            .onSuccess {
                val arguments = ReaderActivityContract.Arguments(bookId)
                channel.send(Event.LaunchReader(arguments))
            }
    }

    fun closeBook(bookId: Long) = viewModelScope.launch {
        val readerRepository = app.readerRepository.await()
        readerRepository.close(bookId)
    }

    sealed class Event {

        class ImportPublicationFailed(val errorMessage: String?) : Event()

        object UnableToMovePublication : Event()

        object ImportPublicationSuccess : Event()

        object ImportDatabaseFailed : Event()

        class OpenBookError(val errorMessage: String?) : Event()

        class LaunchReader(val arguments: ReaderActivityContract.Arguments) : Event()
    }
}

/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.catalogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import org.readium.r2.shared.extensions.tryOrLog
import org.readium.r2.testapp.MainActivity
import org.readium.r2.testapp.R
import org.readium.r2.testapp.bookshelf.BookshelfFragment
import org.readium.r2.testapp.bookshelf.BookshelfViewModel
import org.readium.r2.testapp.catalogs.CatalogFeedListAdapter.Companion.CATALOGFEED
import org.readium.r2.testapp.databinding.FragmentCatalogBinding
import org.readium.r2.testapp.domain.model.Catalog
import org.readium.r2.testapp.opds.GridAutoFitLayoutManager
import org.readium.r2.testapp.reader.ReaderActivityContract
import org.readium.r2.testapp.utils.viewLifecycle

class CatalogFragment : Fragment() {
    private val catalogViewModel: CatalogViewModel by viewModels()
    private val bookshelfViewModel: BookshelfViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var catalog: Catalog
    private lateinit var readerLauncher: ActivityResultLauncher<ReaderActivityContract.Arguments>
    private var binding: FragmentCatalogBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        catalog = arguments?.get(CATALOGFEED) as Catalog
        binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookshelfViewModel.channel.receive(viewLifecycleOwner) { handleEvent(it) }

        readerLauncher = registerForActivityResult(ReaderActivityContract()) { input ->
            input?.let { tryOrLog { bookshelfViewModel.closeBook(input.bookId) } }
        }

        bookAdapter = BookAdapter {
            it.id?.let { id ->
                bookshelfViewModel.openBook(id, requireActivity())
            }
        }
        setHasOptionsMenu(true)

        binding.catalogBookList.apply {
            layoutManager = GridAutoFitLayoutManager(requireContext(), 120)
            adapter = bookAdapter
            addItemDecoration(
                BookshelfFragment.VerticalSpaceItemDecoration(
                    10
                )
            )
        }

        (activity as MainActivity).supportActionBar?.title = catalog.title

        catalogViewModel.parseCatalog(catalog)?.observe(viewLifecycleOwner) {
            bookAdapter.submitList(it)
        }
    }

    private fun handleEvent(event: BookshelfViewModel.Event) {
        val message =
            when (event) {
                is BookshelfViewModel.Event.ImportPublicationFailed -> {
                    "Error: " + event.errorMessage
                }

                is BookshelfViewModel.Event.UnableToMovePublication ->
                    getString(R.string.unable_to_move_pub)

                is BookshelfViewModel.Event.ImportPublicationSuccess -> getString(R.string.import_publication_success)
                is BookshelfViewModel.Event.ImportDatabaseFailed ->
                    getString(R.string.unable_add_pub_database)

                is BookshelfViewModel.Event.OpenBookError -> {
                    val detail = event.errorMessage
                        ?: "Unable to open publication. An unexpected error occurred."
                    "Error: $detail"
                }

                is BookshelfViewModel.Event.LaunchReader -> {
                    bookshelfViewModel.addToBookShelf(event.arguments.bookId)
                    readerLauncher.launch(event.arguments)
                    null
                }
            }
        message?.let {
            Snackbar.make(
                requireView(),
                it,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}

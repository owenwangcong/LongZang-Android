/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.catalogs

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import org.readium.r2.testapp.databinding.FragmentCatalogFeedListBinding
import org.readium.r2.testapp.utils.viewLifecycle

class CatalogFeedListFragment : Fragment() {

    private val catalogFeedListViewModel: CatalogFeedListViewModel by viewModels()
    private var binding: FragmentCatalogFeedListBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogFeedListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences =
            requireContext().getSharedPreferences("org.readium.r2.testapp", Context.MODE_PRIVATE)

        val catalogsAdapter = CatalogFeedListAdapter()

        binding.catalogFeedList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = catalogsAdapter
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    10
                )
            )
        }

        catalogFeedListViewModel.catalogs.observe(viewLifecycleOwner) {
            catalogsAdapter.submitList(it)
        }

        val version = 2
        val VERSION_KEY = "OPDS_CATALOG_VERSION"

        if (preferences.getInt(VERSION_KEY, 0) < version) {
            context?.assets?.open("sutras.json")?.bufferedReader()?.use { it.readText() }?.let {
                val json = JSONObject(it)
                val buInfoList = json.getJSONArray("buInfoList")
                for (i in 0 until buInfoList.length()) {
                    val catalog = buInfoList.getJSONObject(i)
                    catalogFeedListViewModel.insertCatalog(catalog)
                }
            }
            preferences.edit().putInt(VERSION_KEY, version).apply()
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}

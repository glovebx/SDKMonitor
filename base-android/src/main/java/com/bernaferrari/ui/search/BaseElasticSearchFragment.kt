package com.bernaferrari.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bernaferrari.ui.R
import com.bernaferrari.ui.widgets.ElasticDragDismissFrameLayout
import kotlinx.android.synthetic.main.frag_elastic_search.*

abstract class BaseElasticSearchFragment : BaseSearchFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_elastic_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chromeFader =
            ElasticDragDismissFrameLayout.SystemChromeFader(activity as AppCompatActivity)
        elastic_container.addListener(chromeFader)
    }
}

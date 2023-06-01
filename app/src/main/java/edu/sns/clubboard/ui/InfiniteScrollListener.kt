package edu.sns.clubboard.ui

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteScrollListener: RecyclerView.OnScrollListener()
{
    private var isLoading = false

    private var onLoadingStart: (() -> Unit)? = null

    private var getItemSize: (() -> Int)? = null

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
    {
        super.onScrolled(recyclerView, dx, dy)

        if(getItemSize == null)
            return

        if(!isLoading) {
            if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == getItemSize!!.invoke() - 1){
                isLoading = true
                onLoadingStart?.invoke()
                loadingEnd()
            }
        }
    }

    fun setOnLoadingStart(onLoadingStart: () -> Unit)
    {
        this.onLoadingStart = onLoadingStart
    }

    fun loadingEnd()
    {
        isLoading = false
    }

    fun setItemSizeGettr(getItemSize: () -> Int)
    {
        this.getItemSize = getItemSize
    }
}
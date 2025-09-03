package com.example.himaikfinance.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.himaikfinance.data.model.TransactionData
import com.example.himaikfinance.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException

class TransactionPagingSource(
    private val api: ApiService,
    private val pageSize: Int
) : PagingSource<Int, TransactionData>() {
    override fun getRefreshKey(state: PagingState<Int, TransactionData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionData> {
        val page = params.key ?: 1
        return try {
            val response = api.transactions(page = page, limit = pageSize)
            if (!response.isSuccessful) throw HttpException(response)
            val body = response.body()
            val items = body?.data ?: emptyList()
            val pagination = body?.pagination
            val nextKey = if (pagination != null) {
                if (page < pagination.totalPages) page + 1 else null
            } else {
                if (items.isNotEmpty()) page + 1 else null
            }
            val prevKey = if (page > 1) page - 1 else null

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}


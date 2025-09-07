package com.example.himaikfinance.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.himaikfinance.data.local.db.entities.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY created_at DESC, transaction_id DESC")
    fun pagingSource(): PagingSource<Int, TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Transaction
    suspend fun replaceAll(items: List<TransactionEntity>) {
        clearAll()
        if (items.isNotEmpty()) upsertAll(items)
    }
}


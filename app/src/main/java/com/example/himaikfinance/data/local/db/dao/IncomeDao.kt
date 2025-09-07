package com.example.himaikfinance.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.himaikfinance.data.local.db.entities.IncomeEntity

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY transfer_date DESC, id DESC")
    fun pagingSource(): PagingSource<Int, IncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<IncomeEntity>)

    @Query("DELETE FROM incomes")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM incomes")
    suspend fun count(): Int

    @Transaction
    suspend fun replaceAll(items: List<IncomeEntity>) {
        clearAll()
        if (items.isNotEmpty()) upsertAll(items)
    }
}


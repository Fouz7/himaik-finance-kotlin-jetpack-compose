package com.example.himaikfinance.data.local.db

import com.example.himaikfinance.data.local.db.entities.BalanceEvidenceEntity
import com.example.himaikfinance.data.local.db.entities.BalanceTotalsEntity
import com.example.himaikfinance.data.local.db.entities.IncomeEntity
import com.example.himaikfinance.data.local.db.entities.TransactionEntity
import com.example.himaikfinance.data.model.BalanceEvidenceResponse
import com.example.himaikfinance.data.model.IncomeData
import com.example.himaikfinance.data.model.TotalBalanceResponse
import com.example.himaikfinance.data.model.TotalIncomeResponse
import com.example.himaikfinance.data.model.TotalOutcomeResponse
import com.example.himaikfinance.data.model.TransactionData

private fun parseAmount(s: String?): Long = s?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
private fun longToStr(v: Long): String = v.toString()

fun incomeDataToEntity(model: IncomeData): IncomeEntity = IncomeEntity(
    id = model.id,
    createdAt = model.createdAt,
    createdBy = model.createdBy,
    name = model.name,
    nominal = model.nominal,
    transactionId = model.transactionId,
    transferDate = model.transfer_date,
)

fun incomeEntityToModel(entity: IncomeEntity): IncomeData = IncomeData(
    createdAt = entity.createdAt,
    createdBy = entity.createdBy,
    id = entity.id,
    name = entity.name,
    nominal = entity.nominal,
    transactionId = entity.transactionId,
    transfer_date = entity.transferDate
)

fun transactionDataToEntity(model: TransactionData): TransactionEntity = TransactionEntity(
    transactionId = model.transactionId,
    balance = model.balance,
    createdAt = model.createdAt,
    createdBy = model.createdBy,
    credit = model.credit,
    debit = model.debit,
    notes = model.notes,
)

fun transactionEntityToModel(entity: TransactionEntity): TransactionData = TransactionData(
    balance = entity.balance,
    createdAt = entity.createdAt,
    createdBy = entity.createdBy,
    credit = entity.credit,
    debit = entity.debit,
    notes = entity.notes,
    transactionId = entity.transactionId
)

fun totalBalanceToTotalsEntity(resp: TotalBalanceResponse, prev: BalanceTotalsEntity?): BalanceTotalsEntity =
    BalanceTotalsEntity(
        id = 1,
        balance = resp.balance.toLong(),
        totalIncome = prev?.totalIncome ?: 0,
        totalOutcome = prev?.totalOutcome ?: 0,
        updatedAt = System.currentTimeMillis()
    )

fun totalIncomeToTotalsEntity(resp: TotalIncomeResponse, prev: BalanceTotalsEntity?): BalanceTotalsEntity =
    BalanceTotalsEntity(
        id = 1,
        balance = prev?.balance ?: 0,
        totalIncome = resp.totalIncome.toLong(),
        totalOutcome = prev?.totalOutcome ?: 0,
        updatedAt = System.currentTimeMillis()
    )

fun totalOutcomeToTotalsEntity(resp: TotalOutcomeResponse, prev: BalanceTotalsEntity?): BalanceTotalsEntity =
    BalanceTotalsEntity(
        id = 1,
        balance = prev?.balance ?: 0,
        totalIncome = prev?.totalIncome ?: 0,
        totalOutcome = resp.totalOutcome.toLong(),
        updatedAt = System.currentTimeMillis()
    )

fun evidenceResponseToEntity(resp: BalanceEvidenceResponse): BalanceEvidenceEntity = BalanceEvidenceEntity(
    id = 1,
    key = resp.key,
    url = resp.url,
    updatedAt = System.currentTimeMillis()
)

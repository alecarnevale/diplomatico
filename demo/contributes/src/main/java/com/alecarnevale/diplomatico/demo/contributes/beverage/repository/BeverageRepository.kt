package com.alecarnevale.diplomatico.demo.contributes.beverage.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.alecarnevale.diplomatico.demo.contributes.beverage.BeverageEntity
import com.alecarnevale.diplomatico.demo.contributes.beverage.BeverageEntityDao
import com.alecarnevale.diplomatico.demo.contributes.beverage.Beverage

internal class BeverageRepository(
  private val beverageEntityDao: BeverageEntityDao,
) {
  fun getAll(): LiveData<List<Beverage>> =
    beverageEntityDao.getAll().map { daoResult ->
      daoResult.map { beverage ->
        BeverageConverter.fromString(beverage.anything)
      }
    }

  fun insertBeverage(beverage: Beverage) {
    beverageEntityDao.insertBeverage(
      BeverageEntity(
        name = beverage.name,
        anything = BeverageConverter.toString(beverage),
      ),
    )
  }
}

package com.alecarnevale.diplomatico.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alecarnevale.diplomatico.demo.beverage.Beverage
import com.alecarnevale.diplomatico.demo.beverage.repository.BeverageRepository
import com.alecarnevale.diplomatico.demo.cocktail.BaseSpirit
import com.alecarnevale.diplomatico.demo.cocktail.Cocktail
import com.alecarnevale.diplomatico.demo.cocktail.Distilled
import com.alecarnevale.diplomatico.demo.drink.Drink
import com.alecarnevale.diplomatico.demo.ui.theme.DiplomaticoTheme

internal class MainActivity : ComponentActivity() {
  private val drinkDatabase: DrinkDatabase
    get() = DrinkDatabaseProvider.get(this)
  private val beverageRepository: BeverageRepository by lazy {
    BeverageRepository(drinkDatabase.beverageEntityDao())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val drinks by drinkDatabase.drinkDao().getAll().observeAsState(emptyList())
      val cocktails by drinkDatabase.cocktailDao().getAll().observeAsState(emptyList())
      val beverages by beverageRepository.getAll().observeAsState(emptyList())

      DiplomaticoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainContent(
            drinks = drinks,
            cocktails = cocktails,
            beverages = beverages,
            modifier =
              Modifier
                .fillMaxSize()
                .padding(innerPadding),
          )
        }
      }

      LaunchedEffect(key1 = Unit) {
        drinkDatabase.drinkDao().insertDrink(Drink(name = "Rum"))
        drinkDatabase.cocktailDao().insertCocktail(
          Cocktail(
            name = "Cuba Libre",
            bseSpirit =
              BaseSpirit(
                name = "Rum",
                distilled =
                  Distilled(
                    name = "Sugarcane",
                  ),
              ),
          ),
        )
        beverageRepository.insertBeverage(
          Beverage(
            name = "Coca-Cola",
            isAlcoholic = false,
            brand = "The Coca-Cola Company",
          ),
        )
      }
    }
  }
}

@Composable
private fun MainContent(
  drinks: List<Drink>,
  cocktails: List<Cocktail>,
  beverages: List<Beverage>,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier,
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(20.dp),
  ) {
    items(drinks) { drink ->
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
      ) {
        Text(text = drink.name, modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp))
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
    items(cocktails) { cocktail ->
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
      ) {
        Text(text = cocktail.name, modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp))
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
    items(beverages) { beverage ->
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
      ) {
        Column {
          Text(text = "Name: ${beverage.name}", modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp))
          Spacer(modifier = Modifier.height(5.dp))
          Text(text = "Alcoholic: ${beverage.isAlcoholic}", modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp))
          Spacer(modifier = Modifier.height(5.dp))
          Text(text = "Brand: ${beverage.brand}", modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp))
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
  DiplomaticoTheme {
    MainContent(
      drinks =
        listOf(
          Drink("Diplomatico"),
          Drink("Zacapa"),
          Drink("Kraken"),
        ),
      cocktails =
        listOf(
          Cocktail(
            name = "Cuba Libre",
            bseSpirit =
              BaseSpirit(
                name = "Rum",
                distilled =
                  Distilled(
                    name = "Sugarcane",
                  ),
              ),
          ),
        ),
      beverages =
        listOf(
          Beverage(
            name = "Coca-Cola",
            isAlcoholic = false,
            brand = "The Coca-Cola Company",
          ),
        ),
    )
  }
}

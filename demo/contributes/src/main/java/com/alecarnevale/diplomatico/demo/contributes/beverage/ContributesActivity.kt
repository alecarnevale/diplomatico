package com.alecarnevale.diplomatico.demo.contributes.beverage

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
import com.alecarnevale.diplomatico.demo.contributes.beverage.repository.BeverageRepository

class ContributesActivity : ComponentActivity() {
  private val beverageDatabase: BeverageDatabase
    get() = BeverageDatabaseProvider.get(this)
  private val beverageRepository: BeverageRepository by lazy {
    BeverageRepository(beverageDatabase.beverageEntityDao())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val beverages by beverageRepository.getAll().observeAsState(emptyList())

      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        MainContent(
          beverages = beverages,
          modifier =
            Modifier
              .fillMaxSize()
              .padding(innerPadding),
        )
      }

      LaunchedEffect(key1 = Unit) {
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
  beverages: List<Beverage>,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier,
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(20.dp),
  ) {
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
  MainContent(
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

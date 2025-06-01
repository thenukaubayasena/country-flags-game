package com.example.w2001273

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.w2001273.ui.theme.W2001273Theme
import kotlinx.coroutines.delay
import org.json.JSONObject

class GuessTheCountry : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTimerActivated = intent.getBooleanExtra("TIMER_ACTIVATED", false)
        setContent {
            W2001273Theme {
                GuessCountryScreen(applicationContext, isTimerActivated)
            }
        }
    }
}

@Composable
fun GuessCountryScreen(context: Context, isTimerActivated: Boolean) {
    // Retrieve the map of countries and their names from JSON
    val countriesMapData = remember { getCountriesFromJson(context) }

    var remainingTime by rememberSaveable { mutableStateOf(10) }
    var timerActive by rememberSaveable { mutableStateOf(isTimerActivated) }

    val (currentCountryCode, newSelectedCountryCode) = rememberSaveable { mutableStateOf(countriesMapData.entries.random().key) }
    val currentCountryName = countriesMapData[currentCountryCode] ?: ""
    val flagResourceId = remember(currentCountryCode) { context.resources.getIdentifier(currentCountryCode.lowercase(), "drawable", context.packageName) }

    val enteredAnswer = rememberSaveable { mutableStateOf<String?>(null) }
    val clickSubmit = rememberSaveable { mutableStateOf(false) }
    val userCorrect = rememberSaveable { mutableStateOf<Boolean?>(null) }
    val nextButton = rememberSaveable { mutableStateOf(false) }

    // Timer logic
    if (isTimerActivated && timerActive) {
        LaunchedEffect(remainingTime, timerActive) {
            if (remainingTime > 0) {
                delay(1000) // Wait for a second
                remainingTime-- // Decrement the timer
            } else {
                clickSubmit.value = true
                userCorrect.value = false
                nextButton.value = true
                timerActive = false // Stop the countdown
            }
        }
    }

    // Select a new random country and reset the game state
    val selectNewCountry = {
        val newEntry = countriesMapData.entries.random()
        newSelectedCountryCode(newEntry.key)
        timerActive = isTimerActivated
        remainingTime = 10 // Reset the timer for the next flag

        enteredAnswer.value = null
        clickSubmit.value = false
        userCorrect.value = null
        nextButton.value = false
    }

    Box {
        /*Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )*/
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display timer
            if (isTimerActivated) {
                Text("Time left: $remainingTime seconds", modifier = Modifier.padding(8.dp))
            }

            // Display current flag
            if (flagResourceId != 0) {
                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .border(2.dp, Color.Black)
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = flagResourceId),
                        contentDescription = currentCountryName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(200.dp)
                            .padding(16.dp),
                    )
                }
                Text(
                    text = currentCountryName,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
            } else {
                Text("Flag not found for $currentCountryCode")
            }

            // Display country names for selection
            LazyColumn(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                items(countriesMapData.values.toList()) { countryName ->
                    Column(
                        modifier = Modifier
                            .clickable {
                                enteredAnswer.value = countryName
                                clickSubmit.value = false
                                nextButton.value = false
                                timerActive = false // Pause the countdown
                            }
                            .padding(16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = countryName,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 5.dp))
                    }
                }
            }

            // Submit & Next button
            Button(
                onClick = {
                    if (!nextButton.value) {
                        clickSubmit.value = true
                        userCorrect.value = enteredAnswer.value == currentCountryName
                        timerActive = false // Pause the countdown

                        nextButton.value = true
                    } else {
                        selectNewCountry()
                    }
                }, modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .width(200.dp)
            ) {
                Text(
                    if (nextButton.value) "Next" else "Submit",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                )
            }

            // Display whether the user's guess is Correct or Wrong
            if (clickSubmit.value) {
                Text(
                    text = if (userCorrect.value == true) "CORRECT!" else "WRONG!",
                    color = if (userCorrect.value == true) Color.Green else Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            // Display correct answer
            if (clickSubmit.value) {
                Text(
                    text = "Correct Answer: $currentCountryName",
                    color = Color.Blue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

// Read country names from JSON
fun getCountriesFromJson(context: Context): Map<String, String> =
    context.assets.open("countries.json").bufferedReader().use { reader ->
        JSONObject(reader.readText()).let { jsonObject ->
            jsonObject.keys().asSequence().associateWith { key -> jsonObject.getString(key) }
        }
    }
package com.example.w2001273

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class GuessTheFlag : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTimerActivated = intent.getBooleanExtra("TIMER_ACTIVATED", false)
        setContent {
            val countriesMap = getCountriesFromJson(this)
            GuessFlagScreen(countriesMap, isTimerActivated)
        }
    }
}

@Composable
fun GuessFlagScreen(countries: Map<String, String>, isTimerActivated: Boolean) {
    val context = LocalContext.current
    var randomlySelectedCountries by rememberSaveable { mutableStateOf(listOf<String>()) }
    var correctCountryCode by rememberSaveable { mutableStateOf("") }
    var finalMessage by rememberSaveable { mutableStateOf("") }
    var finalMessageColorArgb by rememberSaveable { mutableStateOf(Color.Black.toArgb()) }
    val messageColor = Color(finalMessageColorArgb)
    var isAttemptAllowed by rememberSaveable { mutableStateOf(true) }
    var resetCounterKey by rememberSaveable { mutableStateOf(0) }
    var remainingTime by rememberSaveable { mutableStateOf(10) }
    var timerActive by rememberSaveable { mutableStateOf(isTimerActivated) }

    // Timer logic
    if (isTimerActivated && timerActive) {
        LaunchedEffect(key1 = timerActive, key2 = resetCounterKey) {
            remainingTime = 10 // Reset the timer for new flags
            while (remainingTime > 0 && timerActive) {
                delay(1000) // Wait for a second
                remainingTime-- // Decrement the timer
            }
            if (remainingTime == 0) {
                finalMessage = "Time's Up!"
                finalMessageColorArgb = Color.Red.toArgb()
                isAttemptAllowed = false // Disallow further attempts if the time runs out
                timerActive = false // Stop the countdown
            }
        }
    }

    // Initialize the list of randomly selected countries
    LaunchedEffect(key1 = resetCounterKey) {
        randomlySelectedCountries = countries.keys.shuffled().take(3).toList()
        correctCountryCode = randomlySelectedCountries.random()
    }

    Box (modifier = Modifier.fillMaxWidth()) {
        /*Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )*/
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the name of the correct country
            Text(
                text = "${countries[correctCountryCode]}",
                fontSize = 25.sp,
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            // Display timer
            if (isTimerActivated) {
                Text("Time left: $remainingTime seconds")
            }

            // Display the flags
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = randomlySelectedCountries) { countryCode ->
                        FlagImage(
                            countryCode = countryCode,
                            context = context,
                            attemptAllowed = isAttemptAllowed,
                            correctAnswer = correctCountryCode,
                            onAttemptResult = { isCorrect ->
                                if (isCorrect) {
                                    finalMessage = "CORRECT!"
                                    finalMessageColorArgb = Color.Green.toArgb()
                                } else {
                                    finalMessage = "WRONG!"
                                    finalMessageColorArgb = Color.Red.toArgb()
                                }

                                isAttemptAllowed = false
                                timerActive = false
                            }
                        )
                    }
                }
            }

            Text(
                text = finalMessage,
                color = messageColor,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Next button
            if (!isAttemptAllowed) {
                Button(
                    onClick = {
                        resetCounterKey++
                        isAttemptAllowed = true
                        timerActive = isTimerActivated
                        finalMessage = ""
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .height(50.dp)
                        .width(180.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next", fontFamily = FontFamily.Serif,
                            fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FlagImage(
    countryCode: String,
    context: Context,
    attemptAllowed: Boolean,
    correctAnswer: String,
    onAttemptResult: (Boolean) -> Unit
) {
    // Get the resource ID for the flag
    val resourceId = context.resources.getIdentifier(
        countryCode.lowercase(),
        "drawable",
        context.packageName
    )
    if (resourceId != 0) {
        Box(
            modifier = Modifier
                .padding(15.dp)
                .border(1.dp, Color.Black)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Flag of $countryCode",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(90.dp)
                        .clickable(enabled = attemptAllowed) { onAttemptResult(correctAnswer == countryCode) }
                )
            }
        }
    }
}
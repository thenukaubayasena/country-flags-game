package com.example.w2001273

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class GuessHints : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTimerActivated = intent.getBooleanExtra("TIMER_ACTIVATED", false)
        setContent {
            GuessHintScreen(this, isTimerActivated)
        }
    }
}

@Composable
fun GuessHintScreen(context: Context, isTimerActivated: Boolean) {
    // Retrieve the map of countries and their names from JSON
    val countriesMapData = getCountriesFromJson(context)

    var recomposeNeeded by rememberSaveable { mutableStateOf(false) }
    var currentCountryCode by rememberSaveable { mutableStateOf(countriesMapData.entries.random().key) }
    val currentCountryName = countriesMapData[currentCountryCode] ?: "Unknown"
    val flagResourceId = remember(currentCountryCode) { context.resources.getIdentifier(currentCountryCode.lowercase(), "drawable", context.packageName) }
    val imagePainter = if (flagResourceId != 0) painterResource(id = flagResourceId) else null
    var maskedCountryName by rememberSaveable { mutableStateOf("_".repeat(currentCountryName.length)) }

    var enteredAnswer by rememberSaveable { mutableStateOf("") }
    var finalMessage by rememberSaveable { mutableStateOf("") }
    var remainingAttempts by rememberSaveable { mutableStateOf(3) }
    var isRoundOver by rememberSaveable { mutableStateOf(false) }

    var remainingTime by rememberSaveable { mutableStateOf(10) }
    var timerActive by rememberSaveable { mutableStateOf(isTimerActivated) }

    var scoreIncrementedForThisRound by rememberSaveable { mutableStateOf(false) }
    var score by rememberSaveable { mutableStateOf(0) }

    // Timer logic
    if (isTimerActivated && timerActive) {
        LaunchedEffect(key1 = recomposeNeeded, key2 = remainingAttempts) {
            if (isTimerActivated && timerActive) {
                remainingTime = 10
                while (remainingTime > 0 && remainingAttempts > 0) {
                    delay(1000)
                    remainingTime--
                }
                if (remainingTime == 0 && remainingAttempts > 0) {
                    remainingAttempts--
                    if (remainingAttempts > 0) {
                        finalMessage = "Incorrect, try again! Attempts left: $remainingAttempts"
                        remainingTime = 10
                    } else {
                        finalMessage = "Time's up! The correct country was $currentCountryName."
                        isRoundOver = true
                        timerActive = false
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        /*Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )*/

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display timer
            if (isTimerActivated) {
                Text(text = "Time left: $remainingTime seconds", Modifier.padding(8.dp))
            }

            if (imagePainter != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(50.dp))
                    // Display flag and masked country name
                    Text(
                        text = "Flag of $currentCountryName",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = maskedCountryName.map { it }.joinToString("  "),
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Display attempts left
                    Text(
                        text = "Attempts left: $remainingAttempts",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp
                    )
                    // Display flag image
                    Box(
                        modifier = Modifier
                            .padding(20.dp)
                            .border(2.dp, Color.Black)
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = imagePainter,
                            contentDescription = "Flag of $currentCountryName",
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(250.dp)
                                .padding(16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Feedback message
                    if (isRoundOver && remainingAttempts == 0) {
                        Text(text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("WRONG! The correct country is ")
                            }
                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append(currentCountryName)
                            }
                        },
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    } else {
                        Text(
                            text = finalMessage,
                            color = when {
                                finalMessage.startsWith("CORRECT! The country is") -> Color.Green
                                else -> Color.Black
                            },
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }

                    // Input field for user guess
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .border(1.dp, Color.Black)
                    ) {
                        TextField(
                            value = enteredAnswer,
                            onValueChange = { enteredAnswer = it.lowercase() },
                            label = { Text("Enter your guess") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }

                    // Submit button
                    if (!isRoundOver) {
                        Button(
                            onClick = {
                                if (enteredAnswer.isNotEmpty() && enteredAnswer.length == 1) {
                                    if (currentCountryName.lowercase().contains(enteredAnswer)) {
                                        val previouslyIncomplete = maskedCountryName.contains('_')
                                        maskedCountryName = maskedCountryName.toCharArray().apply {
                                            currentCountryName.forEachIndexed { index, c ->
                                                if (c.lowercase() == enteredAnswer) this[index] = c
                                            }
                                        }.concatToString()
                                        if (!maskedCountryName.contains('_') && previouslyIncomplete && !scoreIncrementedForThisRound) {
                                            finalMessage = "CORRECT! The country is $currentCountryName"
                                            isRoundOver = true
                                            scoreIncrementedForThisRound = true
                                            if (isTimerActivated) {
                                                score++
                                                timerActive = false
                                            }
                                        }
                                    } else {
                                        remainingAttempts--
                                        enteredAnswer = ""
                                        if (remainingAttempts == 0) {
                                            finalMessage = "WRONG! The correct country is "
                                            isRoundOver = true
                                            timerActive = false
                                        } else {
                                            finalMessage = "Incorrect, try again! Attempts left: $remainingAttempts"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.height(45.dp).width(200.dp),
                            enabled = !isRoundOver
                        ) {
                            Text("Submit", fontFamily = FontFamily.Serif, fontSize = 18.sp)
                        }
                    }

                    // Next button and feedback message
                    if (isRoundOver) {
                        Button(
                            onClick = {
                                recomposeNeeded = !recomposeNeeded
                                currentCountryCode = countriesMapData.entries.random().key
                                val newCountryName = countriesMapData[currentCountryCode] ?: "Unknown"
                                maskedCountryName = "_".repeat(newCountryName.length)
                                remainingAttempts = 3
                                finalMessage = ""
                                isRoundOver = false
                                enteredAnswer = ""
                                if (isTimerActivated) {
                                    remainingTime = 30
                                    timerActive = true
                                }
                            },
                            modifier = Modifier.height(45.dp).width(200.dp),
                            enabled = isRoundOver
                        ) {
                            Text("Next", fontFamily = FontFamily.Serif, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                Text("Flag image not found")
            }
        }
    }
}
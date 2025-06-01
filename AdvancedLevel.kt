package com.example.w2001273

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class AdvancedLevel : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTimerActivated = intent.getBooleanExtra("TIMER_ACTIVATED", false)
        setContent {
            AdvancedLevelGame(getCountriesFromJson(this), this, isTimerActivated)
        }
    }
}

@Composable
fun AdvancedLevelGame(countries: Map<String, String>, context: Context, timerEnabled: Boolean) {
    var randomFlagsAndAnswers by rememberSaveable { mutableStateOf(getRandomFlagsAndAnswers(countries, context)) }
    var userGuesses by rememberSaveable { mutableStateOf(listOf("", "", "")) }
    var score by rememberSaveable { mutableStateOf(0) }
    var guessCorrectness by rememberSaveable { mutableStateOf(listOf<Boolean?>(null, null, null)) }
    var remainingAttempts by rememberSaveable { mutableStateOf(3) }
    var isGameOver by rememberSaveable { mutableStateOf(false) }
    var remainingTime by rememberSaveable { mutableStateOf(10) }
    var timerScore by rememberSaveable { mutableStateOf(0) }

    // Check the correctness of the guesses
    fun checkGuesses() {
        if (!isGameOver) {
            if (remainingAttempts > 0) {
                remainingAttempts--
                remainingTime = 10 // Reset timer for each attempt
                userGuesses.forEachIndexed { index, guess ->
                    if (guessCorrectness[index] != true) {
                        val isCorrect = guess.equals(randomFlagsAndAnswers.second[index], ignoreCase = true)
                        guessCorrectness = guessCorrectness.toMutableList().also { it[index] = isCorrect }
                        if (isCorrect) {
                            score++
                            timerScore++ // Also increment the timer score
                        }
                    }
                }
                if (guessCorrectness.all { it == true } || remainingAttempts == 0) {
                    isGameOver = true
                }
            }
        } else {
            // Reset game state when game is over
            randomFlagsAndAnswers = getRandomFlagsAndAnswers(countries, context)
            userGuesses = listOf("", "", "")
            guessCorrectness = listOf(null, null, null)
            score = 0
            isGameOver = false
            remainingTime = 10
            remainingAttempts = 3
        }
    }

    // Timer logic
    LaunchedEffect(timerEnabled, isGameOver, remainingTime) {
        if (timerEnabled && remainingTime > 0 && !isGameOver) {
            while (remainingTime > 0 && !isGameOver) {
                delay(1000L)
                remainingTime--
            }
            if (remainingTime <= 0) {
                // Handle timer logic when it reaches 0
                checkGuesses()
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Box {
        /*Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )*/
        Column(modifier = Modifier.padding(10.dp)) {
            // Display timer
            if (timerEnabled) {
                Text(
                    text = "Time Left: $remainingTime seconds",
                    color = if (remainingTime <= 3) Color.Red else Color.Blue,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Display the scores and attempts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Attempts Left: $remainingAttempts",
                    fontFamily = FontFamily.Serif,
                    color = Color.Black
                )
                Text(
                    text = "Score: $score",
                    fontFamily = FontFamily.Serif,
                    color = Color.Black
                )
            }

            // Display flags and text fields for guesses
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(randomFlagsAndAnswers.first.zip(randomFlagsAndAnswers.second)) { index, (flagDrawable, correctAnswer) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (flagDrawable != 0) {
                            Image(
                                painter = painterResource(id = flagDrawable),
                                contentDescription = "Flag of $correctAnswer",
                                modifier = Modifier
                                    .height(75.dp)
                                    .fillMaxWidth()
                            )
                            Text(
                                text = correctAnswer,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Text field for user input
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .border(1.dp, Color.Black)
                        ) {
                            TextField(
                                value = userGuesses.getOrElse(index) { "" },
                                onValueChange = { newValue ->
                                    if (!isGameOver && guessCorrectness.getOrElse(index) { false } != true) {
                                        userGuesses =
                                            userGuesses.toMutableList().also { it[index] = newValue }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .height(50.dp),
                                placeholder = { Text("Guess The Country Name") },
                                textStyle = TextStyle(
                                    color = when (guessCorrectness.getOrElse(index) { null }) {
                                        true -> Color.Green
                                        false -> Color.Red
                                        else -> Color.Black
                                    }
                                ),
                                trailingIcon = {
                                    if (userGuesses.getOrElse(index) { "" }.isNotEmpty()) {
                                        IconButton(onClick = {
                                            userGuesses =
                                                userGuesses.toMutableList().also { it[index] = "" }
                                        }) {
                                            Icon(
                                                Icons.Filled.Clear,
                                                contentDescription = "Clear input"
                                            )
                                        }
                                    }
                                },
                                enabled = !isGameOver && guessCorrectness.getOrElse(index) { false } != true
                            )
                        }
                    }
                }

                // Submit & next button
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                    val buttonEnabled = if (!isGameOver) {
                        userGuesses.any { it.isNotBlank() }
                    } else {
                        true
                    }

                    Button(
                        onClick = {
                            if (!isGameOver) {
                                if (userGuesses.any { it.isNotBlank() }) {
                                    remainingAttempts--
                                    userGuesses.forEachIndexed { index, guess ->
                                        if (guess.isNotBlank()) {
                                            val isCorrect = guess.equals(randomFlagsAndAnswers.second[index], ignoreCase = true)
                                            if (isCorrect && guessCorrectness[index] != true) {
                                                score++
                                            }
                                            guessCorrectness = guessCorrectness.toMutableList().also { it[index] = isCorrect }
                                        }
                                    }
                                    isGameOver = guessCorrectness.all { it == true } || remainingAttempts <= 0
                                }
                            } else {
                                // Reset game state when game is over
                                randomFlagsAndAnswers = getRandomFlagsAndAnswers(countries, context)
                                userGuesses = listOf("", "", "")
                                guessCorrectness = listOf(null, null, null)
                                score = 0
                                remainingAttempts = 3
                                isGameOver = false
                                remainingTime = 10
                            }
                        },
                        modifier = Modifier
                            .height(45.dp).width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGameOver) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.LightGray
                        ),
                        border = BorderStroke(1.dp, Color.DarkGray),
                        enabled = buttonEnabled,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "Next" else "Submit", fontFamily = FontFamily.Serif, fontSize = 18.sp,
                            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }
            }

            // Feedback messages
            if (isGameOver) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (guessCorrectness.all { it == true }) "CORRECT!" else "WRONG!",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (guessCorrectness.all { it == true }) Color.Green else Color.Red
                    )
                    repeat(randomFlagsAndAnswers.second.size) { index ->
                        if (guessCorrectness[index] != true) {
                            Text(
                                text = "Flag ${index + 1}: The correct country is ${randomFlagsAndAnswers.second[index]}",
                                color = Color.Blue,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Get random flags and corresponding answers
fun getRandomFlagsAndAnswers(countries: Map<String, String>, context: Context): Pair<List<Int>, List<String>> {
    val randomEntries = countries.entries.shuffled().take(3)
    val flags = mutableListOf<Int>()
    val answers = mutableListOf<String>()

    randomEntries.forEach { (countryCode, countryName) ->
        val resourceIdentifier = context.resources.getIdentifier(countryCode.toLowerCase(), "drawable", context.packageName)
        if (resourceIdentifier != 0) {
            flags.add(resourceIdentifier)
            answers.add(countryName)
        }
    }
    return Pair(flags, answers)
}
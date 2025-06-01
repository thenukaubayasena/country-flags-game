package com.example.w2001273

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen()
        }
    }

    @Composable
    fun HomeScreen() {
        // Timer activation status
        val isTimerActivated = remember { mutableStateOf(false) }

        Box {
            /*Image(
                painter = painterResource(id = R.drawable.background), // Background image
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )*/
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "COUNTRY FLAGS GAME",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(50.dp))

                // Buttons
                ButtonWithIntent("Guess the Country", GuessTheCountry::class.java, isTimerActivated.value)
                Spacer(modifier = Modifier.height(30.dp))
                ButtonWithIntent("Guess-Hints", GuessHints::class.java, isTimerActivated.value)
                Spacer(modifier = Modifier.height(30.dp))
                ButtonWithIntent("Guess the Flag", GuessTheFlag::class.java, isTimerActivated.value)
                Spacer(modifier = Modifier.height(30.dp))
                ButtonWithIntent("Advanced Level", AdvancedLevel::class.java, isTimerActivated.value)
                Spacer(modifier = Modifier.height(30.dp))

                // Countdown Timer Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Countdown Timer",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isTimerActivated.value,
                        onCheckedChange = { newValue ->
                            isTimerActivated.value = newValue
                        }
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Button with intent to start activity
    @Composable
    private fun ButtonWithIntent(text: String, targetActivity: Class<*>, timerActivated: Boolean) {
        Button(
            onClick = {
                val intent = Intent(this@MainActivity, targetActivity)
                intent.putExtra("TIMER_ACTIVATED", timerActivated)
                startActivity(intent)
            },
            modifier = Modifier
                .height(50.dp)
                .width(250.dp)
        ) {
            Text(
                text = text,
                fontFamily = FontFamily.Serif,
                fontSize = 21.sp
            )
        }
    }
}
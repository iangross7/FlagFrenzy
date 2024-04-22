package com.example.assignment1

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

/**
 * Activity for the Guess the Country screen
 */
class GuessTheCountry : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            displayGUI()
        }
    }

    @Preview
    @Composable
    fun displayGUI() {
        // ArrayList containing all pairs of flag drawable & country name
        val masterFlagList = intent.getSerializableExtra("masterFlagList", ArrayList::class.java) as ArrayList<Pair<Int, String>>
        val countryNameList = retrieveCountryNames(masterFlagList)

        // Variable holding orientation status
        val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

        // Current pair of flag/country
        var currPair by rememberSaveable{ mutableStateOf(generateNextPair(masterFlagList)) }
        // Current correct answer
        var currCountry by rememberSaveable{ mutableStateOf(currPair.second) }

        // Currently selected answer by user
        var userAnswer by rememberSaveable{ mutableStateOf("") }
        // Status of the game (guessing/completed)
        var hasGuessed by rememberSaveable{ mutableStateOf(false) }
        // Text judging the user answer
        var judgementText by rememberSaveable{ mutableStateOf("") }
        // Text that shows the correct country if was answered incorrect
        var correctionText by rememberSaveable{ mutableStateOf("") }
        // Text on button for submit/next
        var continueButtonText by rememberSaveable{ mutableStateOf("Submit") }

        // Information concerning pressureMode & timer
        val isPressure = intent.getBooleanExtra("isPressure", false)
        var timeRemaining by rememberSaveable{mutableStateOf(10)}
        var timer: CountDownTimer? by remember { mutableStateOf(null) }

        // Custom hex colors for better UI display
        val cGreen = Color(0xFF32a852)
        val cRed = Color(0xFFd11b15)
        val cBlue = Color(0xFF1c9de0)
        val cGrey = Color(0xFFe3e3e3)

        // Custom font (futuraPT)
        val futuraPT = FontFamily(
            Font(R.font.futurapt, FontWeight.Light)
        )

        // Function to update fields for a submit action
        fun submit() {
            // In use for timed mode
            timer?.cancel()

            // Checks the answer of the user
            val wasCorrect = if (userAnswer == currCountry) true else false

            if (wasCorrect) {
                judgementText = "CORRECT!"
            }
            else {
                judgementText = "WRONG!"
                correctionText = currCountry
            }

            continueButtonText = "Next"
            hasGuessed = true;
        }

        // Starts a new timer with ten seconds to go
        fun createNewTimer(seconds : Int) : CountDownTimer {
            return object : CountDownTimer((seconds.toLong() * 1000), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeRemaining = (millisUntilFinished / 1000).toInt() + 1
                }

                override fun onFinish() {
                    timeRemaining = 0
                    submit()
                }
            }
        }

        // Starting the timer on activity recomposition for time continuity
        // Time remaining is initialized to ten in case of first composition
        LaunchedEffect(Unit) {
            if (timer == null && !hasGuessed && isPressure) {
                timer = createNewTimer(timeRemaining)
                timer?.start()
            }
        }

        // Handles canceling a new timer so another one can be started, single timer continuity
        DisposableEffect(Unit) {
            onDispose {
                timer?.cancel()
            }
        }

        // Orientation for vertical device
        if (isPortrait) {
            // Button to return to home screen & pressure time
            Column() {
                Text(
                    text = "< BACK",
                    color = cBlue,
                    fontFamily = futuraPT,
                    fontSize = 35.sp,
                    modifier = Modifier.clickable(onClick = {
                        finish()
                    })
                )
                if (isPressure) {
                    Text(
                        text = "${timeRemaining}s",
                        color = cBlue,
                        fontFamily = futuraPT,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(top = 10.dp, start = 30.dp)
                    )
                }
            }

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Code for the CORRECT/INCORRECT text
                Text(
                    text = judgementText,
                    fontSize = 45.sp,
                    color = if (judgementText == "CORRECT!") cGreen else cRed
                )
                // Text for the correct answer
                Text(
                    text = correctionText,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = cBlue,
                    fontSize = 22.sp
                )
                Image(
                    painterResource(id = currPair.first),
                    contentDescription = "Flag",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .height(196.dp)
                        .width(280.dp)
                        .border(1.dp, Color.Black)
                )
                // Code for scrollable list & selected item
                LazyColumn(
                    Modifier.height(250.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    for (i in countryNameList) {
                        item {
                            Row(
                                Modifier
                                    .width(300.dp)
                                    .clickable(onClick = { userAnswer = i }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = i,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "SELECTED: ${userAnswer.uppercase()}",
                    textAlign = TextAlign.Center,
                    fontFamily = futuraPT,
                    color = cBlue,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                // Submit/Next Button
                Button(
                    onClick = {
                        if (hasGuessed) {
                            // Flushing gamestate for next round
                            hasGuessed = false;
                            judgementText = ""
                            correctionText = ""
                            userAnswer = ""
                            continueButtonText = "Submit"
                            currPair = generateNextPair(masterFlagList)
                            currCountry = currPair.second
                            if (isPressure) {
                                timer = createNewTimer(10)
                                timer?.start()
                            }
                        }
                        // Round has concluded so resetting state
                        else {
                            submit()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = continueButtonText,
                        color = cGrey,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )
                }
            }
        }
        // Orientation for horizontal device
        else {
            Text(
                text = "< BACK",
                color = cBlue,
                fontFamily = futuraPT,
                fontSize = 35.sp,
                modifier = Modifier.clickable(onClick = {
                    finish()
                })
            )
            Row(
                Modifier.fillMaxSize().padding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Code for the CORRECT/INCORRECT text
                    Text(
                        text = judgementText,
                        fontSize = 45.sp,
                        color = if (judgementText == "CORRECT!") cGreen else cRed
                    )
                    // Text for the correct answer
                    Text(
                        text = correctionText,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = cBlue,
                        fontSize = 22.sp
                    )
                    Image(
                        painterResource(id = currPair.first),
                        contentDescription = "Flag",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(180.dp)
                            .width(280.dp)
                            .border(1.dp, Color.Black)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(300.dp)
                ) {
                    // Code for scrollable list & selected item
                    LazyColumn(
                        Modifier.height(250.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        for (i in countryNameList) {
                            item {
                                Row(
                                    Modifier
                                        .width(300.dp)
                                        .clickable(onClick = { userAnswer = i }),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = i,
                                        fontSize = 22.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "SELECTED: ${userAnswer.uppercase()}",
                        textAlign = TextAlign.Center,
                        fontFamily = futuraPT,
                        color = cBlue,
                        fontSize = 22.sp,
                        modifier = Modifier.padding()
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isPressure) {
                        Text(
                            text = "${timeRemaining}s",
                            color = cBlue,
                            fontFamily = futuraPT,
                            fontSize = 30.sp,
                        )
                    }
                    // Submit/Next Button
                    Button(
                        onClick = {
                            if (hasGuessed) {
                                // Flushing gamestate for next round
                                hasGuessed = false;
                                judgementText = ""
                                correctionText = ""
                                userAnswer = ""
                                continueButtonText = "Submit"
                                currPair = generateNextPair(masterFlagList)
                                currCountry = currPair.second
                                if (isPressure) {
                                    timer = createNewTimer(10)
                                    timer?.start()
                                }
                            }
                            // Round has concluded so resetting state
                            else {
                                submit()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cBlue),
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            text = continueButtonText,
                            color = cGrey,
                            fontSize = 25.sp,
                        )
                    }
                }
            }
        }
    }

    /**
     * Accepts a list of paired flag images with country names, and returns a sorted
     * ArrayList of the names of all countries.
     */
    fun retrieveCountryNames(masterFlagList : ArrayList<Pair<Int, String>>) : List<String>  {
        val countryNameList = mutableListOf<String>()

        for (pair in masterFlagList) {
            countryNameList.add(pair.second)
        }

        countryNameList.sort()
        return countryNameList
    }

    /**
     * Randomly generates a new pair of flag & name for use from the masterList
     */
    fun generateNextPair(masterFlagList: ArrayList<Pair<Int, String>>) : Pair<Int,String> {
        val index = Random.nextInt(0, masterFlagList.size)
        return (masterFlagList[index])
    }
}
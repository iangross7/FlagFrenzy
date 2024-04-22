package com.example.assignment1

import android.content.Intent
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

class GuessTheFlag : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            displayGUI()
        }
    }

    @Preview
    @Composable
    fun displayGUI() {
        // Variable holding orientation status
        val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

        // ArrayList containing all pairs of flag drawable & country name
        val masterFlagList = intent.getSerializableExtra("masterFlagList", ArrayList::class.java) as ArrayList<Pair<Int, String>>

        // Holds the flag/country pairs for the three flags currently on screen
        var flagTrioList by rememberSaveable{ mutableStateOf(generateNextThreeFlags(masterFlagList)) }
        // Contains which country is the one prompted to the user (correct flag/country)
        var correctPair by rememberSaveable{ mutableStateOf(selectCountry(flagTrioList)) }
        // Determines whether the user has clicked a flag (game status)
        var hasGuessed by rememberSaveable{ mutableStateOf(false) }

        // Text judging the user answer
        var judgementText by rememberSaveable{ mutableStateOf("") }
        // Text telling the user what they incorrectly selected (if at all)
        var correctionText by rememberSaveable{ mutableStateOf("") }

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

        fun submit() {
            timer?.cancel()

            judgementText = "WRONG!"
            correctionText = "You didn't select a country in time!"

            hasGuessed = true
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

        if (isPortrait) {
            // Button to return to home screen & pressure time
            Column {
                Text(
                    text = "< BACK",
                    color = cBlue,
                    fontFamily = futuraPT,
                    fontSize = 35.sp,
                    modifier = Modifier.clickable(onClick = {
                        finish()
                    })
                )
            }

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                if (isPressure) {
                    Text(
                        text = "${timeRemaining}s",
                        color = cBlue,
                        fontFamily = futuraPT,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(end = 15.dp, top = 10.dp)
                    )
                }
            }

            Column(
                Modifier.fillMaxSize().padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = correctPair.second.uppercase(),
                    color = cBlue,
                    fontFamily = futuraPT,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp
                )
                Box(Modifier.padding(bottom = 10.dp)) {
                    Image(
                        painterResource(id = flagTrioList[0].first),
                        contentDescription = "Flag 1",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(215.dp)
                            .border(1.dp, Color.Black)
                            .clickable {
                                if (!hasGuessed) {
                                    if (flagTrioList[0].second == correctPair.second) {
                                        judgementText = "CORRECT!"
                                    } else {
                                        judgementText = "WRONG!"
                                        correctionText = "You selected ${flagTrioList[0].second}"
                                    }
                                    hasGuessed = true
                                    timer?.cancel()
                                }
                            }
                    )
                }
                Box(Modifier.padding(bottom = 10.dp)) {
                    Image(
                        painterResource(id = flagTrioList[1].first),
                        contentDescription = "Flag 2",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(215.dp)
                            .border(1.dp, Color.Black)
                            .clickable {
                                if (!hasGuessed) {
                                    if (flagTrioList[1].second == correctPair.second) {
                                        judgementText = "CORRECT!"
                                    } else {
                                        judgementText = "WRONG!"
                                        correctionText = "You selected ${flagTrioList[1].second}"
                                    }
                                    hasGuessed = true
                                    timer?.cancel()
                                }
                            }
                    )
                }
                Image(
                    painterResource(id = flagTrioList[2].first),
                    contentDescription = "Flag 3",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .height(150.dp)
                        .width(215.dp)
                        .border(1.dp, Color.Black)
                        .clickable {
                            if (!hasGuessed) {
                                if (flagTrioList[2].second == correctPair.second) {
                                    judgementText = "CORRECT!"
                                } else {
                                    judgementText = "WRONG!"
                                    correctionText = "You selected ${flagTrioList[2].second}"
                                }
                                hasGuessed = true
                                timer?.cancel()
                            }
                        }
                )
                // Code for the CORRECT/INCORRECT text
                Text(
                    text = judgementText,
                    color = if (judgementText == "CORRECT!") cGreen else cRed,
                    fontSize = 30.sp
                )
                // Code for the correct answer text
                Text(
                    text = correctionText,
                    color = if (judgementText == "CORRECT!") cGreen else cBlue,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                    textAlign = TextAlign.Center
                )
                // Next button
                Button(
                    onClick = {
                        // Round has concluded so resetting state
                        if (hasGuessed) {
                            flagTrioList = generateNextThreeFlags(masterFlagList)
                            correctPair = selectCountry(flagTrioList)
                            judgementText = ""
                            correctionText = ""
                            hasGuessed = false
                            if (isPressure) {
                                timer = createNewTimer(10)
                                timer?.start()
                            }
                        }
                    },
                    // Only shows next button if gamestate permits
                    modifier = if (hasGuessed) Modifier.alpha(1.0f) else Modifier.alpha(0.0f),
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = "Next",
                        color = cGrey,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )
                }
            }
        }
        // If orientation is landscape
        else {
            // Button to return to home screen & pressure time
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "< BACK",
                    color = cBlue,
                    fontFamily = futuraPT,
                    fontSize = 35.sp,
                    modifier = Modifier.clickable(onClick = {
                        finish()
                    })
                )
            }

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (isPressure) {
                    Text(
                        text = "${timeRemaining}s",
                        color = cBlue,
                        fontFamily = futuraPT,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(end = 15.dp, bottom = 10.dp)
                    )
                }
            }

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = correctPair.second.uppercase(),
                    color = cBlue,
                    fontFamily = futuraPT,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp
                )
                Row {
                    Box(Modifier.padding(end = 18.dp)) {
                        Image(
                            painterResource(id = flagTrioList[0].first),
                            contentDescription = "Flag 1",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(215.dp)
                                .border(1.dp, Color.Black)
                                .clickable {
                                    if (!hasGuessed) {
                                        if (flagTrioList[0].second == correctPair.second) {
                                            judgementText = "CORRECT!"
                                        } else {
                                            judgementText = "WRONG!"
                                            correctionText = "You selected ${flagTrioList[0].second}"
                                        }
                                        hasGuessed = true
                                        timer?.cancel()
                                    }
                                }
                        )
                    }
                    Box(Modifier.padding(end = 18.dp)) {
                        Image(
                            painterResource(id = flagTrioList[1].first),
                            contentDescription = "Flag 2",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(215.dp)
                                .border(1.dp, Color.Black)
                                .clickable {
                                    if (!hasGuessed) {
                                        if (flagTrioList[1].second == correctPair.second) {
                                            judgementText = "CORRECT!"
                                        } else {
                                            judgementText = "WRONG!"
                                            correctionText = "You selected ${flagTrioList[1].second}"
                                        }
                                        hasGuessed = true
                                        timer?.cancel()
                                    }
                                }
                        )
                    }
                    Image(
                        painterResource(id = flagTrioList[2].first),
                        contentDescription = "Flag 3",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(215.dp)
                            .border(1.dp, Color.Black)
                            .clickable {
                                if (!hasGuessed) {
                                    if (flagTrioList[2].second == correctPair.second) {
                                        judgementText = "CORRECT!"
                                    } else {
                                        judgementText = "WRONG!"
                                        correctionText = "You selected ${flagTrioList[2].second}"
                                    }
                                    hasGuessed = true
                                    timer?.cancel()
                                }
                            }
                    )
                }
                // Code for the CORRECT/INCORRECT text
                Text(
                    text = judgementText,
                    color = if (judgementText == "CORRECT!") cGreen else cRed,
                    fontSize = 30.sp
                )
                // Code for the correct answer text
                Text(
                    text = correctionText,
                    color = if (judgementText == "CORRECT!") cGreen else cBlue,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                    textAlign = TextAlign.Center
                )
                // Next button
                Button(
                    onClick = {
                        // Round has concluded so resetting state
                        if (hasGuessed) {
                            flagTrioList = generateNextThreeFlags(masterFlagList)
                            correctPair = selectCountry(flagTrioList)
                            judgementText = ""
                            correctionText = ""
                            hasGuessed = false
                            if (isPressure) {
                                timer = createNewTimer(10)
                                timer?.start()
                            }
                        }
                    },
                    // Only shows next button if gamestate permits
                    modifier = if (hasGuessed) Modifier.alpha(1.0f) else Modifier.alpha(0.0f),
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = "Next",
                        color = cGrey,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )
                }
            }
        }
    }

    /**
     * Accepts a list of flag/country pairs and returns three randomly selected, unique flag/country
     * pairs.
     */
    fun generateNextThreeFlags(masterFlagList: ArrayList<Pair<Int, String>>) : List<Pair<Int, String>> {
        val flagTrioList = mutableListOf<Pair<Int,String>>()

        while(flagTrioList.size < 3) {
            var randIndex = Random.nextInt(0, masterFlagList.size)
            if(masterFlagList[randIndex] !in flagTrioList) flagTrioList.add(masterFlagList[randIndex])
        }

        return flagTrioList
    }

    /**
     * Randomly picks a flag/country pair from a list. Used in displayGUI() to select the country
     * of choice.
     */
    fun selectCountry(pairList : List<Pair<Int, String>>) : Pair<Int,String> {
        return pairList[Random.nextInt(0, pairList.size)]
    }
}
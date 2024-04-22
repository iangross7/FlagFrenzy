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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

class AdvancedLevel : ComponentActivity() {
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

        // Status of the game
        var doneGuessing by rememberSaveable{ mutableStateOf(false) }
        // Tracks number of guesses made
        var numGuesses by rememberSaveable{ mutableStateOf(1) }
        // Tracks userScore
        var numPoints by rememberSaveable{ mutableStateOf(0) }

        // Stores the user's answers for the three countries, row-major-order
        var userGuess1 by rememberSaveable{ mutableStateOf("") }
        var userGuess2 by rememberSaveable{ mutableStateOf("") }
        var userGuess3 by rememberSaveable{ mutableStateOf("") }

        // Stores the states of the user's answers, individually
        var isFlag1Correct by rememberSaveable{ mutableStateOf(false)}
        var isFlag2Correct by rememberSaveable{ mutableStateOf(false)}
        var isFlag3Correct by rememberSaveable{ mutableStateOf(false)}

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

            // Updating all answer states
            if (!isFlag1Correct)
                isFlag1Correct = validateAnswer(userGuess1, flagTrioList[0].second)
            if (!isFlag2Correct)
                isFlag2Correct = validateAnswer(userGuess2, flagTrioList[1].second)
            if (!isFlag3Correct)
                isFlag3Correct = validateAnswer(userGuess3, flagTrioList[2].second)

            // Checking for completion
            if (isFlag1Correct && isFlag2Correct && isFlag3Correct) {
                doneGuessing = true
            }
            else {
                if (numGuesses > 2) {
                    doneGuessing = true
                }
                else {
                    numGuesses++
                }
            }

            // Updating score per completion
            if (doneGuessing) {
                numPoints += increaseScore(isFlag1Correct, isFlag2Correct, isFlag3Correct)
            }
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
                    // If the round isn't over, start a new timer
                    if (!doneGuessing) {
                        timer = createNewTimer(10)
                        timer?.start()
                    }
                }
            }
        }

        // Starting the timer on activity recomposition for time continuity
        // Time remaining is initialized to ten in case of first composition
        LaunchedEffect(Unit) {
            if (timer == null && !doneGuessing && isPressure) {
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

        // UI Layout for portrait mode
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

            // Box containing remaining answer & score
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, end = 45.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text ="$numGuesses/3",
                        fontSize = 30.sp,
                        color = cBlue,
                        fontFamily = futuraPT,
                        modifier = Modifier.padding(start = 30.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text ="Points: $numPoints",
                        fontSize = 30.sp,
                        color = cBlue,
                        fontFamily = futuraPT,
                        modifier = Modifier.padding(start = 30.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (isPressure) {
                Text(
                    text = "${timeRemaining}s",
                    fontSize = 30.sp,
                    textAlign = TextAlign.End,
                    color = cBlue,
                    fontFamily = futuraPT,
                    modifier = Modifier.padding(start = 5.dp, top = 420.dp).width(50.dp)
                )
            }

            // Containing all flags, prompts, submit button
            Column(
                Modifier.fillMaxSize().padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (doneGuessing) {
                    if (isFlag1Correct && isFlag2Correct && isFlag3Correct) {
                        Text(
                            text = "CORRECT!",
                            color = cGreen,
                            fontSize = 40.sp
                        )
                    } else {
                        Text(
                            text = "WRONG!",
                            color = cRed,
                            fontSize = 40.sp
                        )
                    }
                } else {
                    Text("", fontSize = 40.sp)
                }
                // Contains the row of two flags & their submit boxes
                Row(Modifier.padding(bottom = 10.dp)) {
                    Column(
                        Modifier.width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(id = flagTrioList[0].first),
                            contentDescription = "Flag 1",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(200.dp)
                                .border(1.dp, Color.Black)
                        )
                        OutlinedTextField(
                            value = userGuess1,
                            onValueChange = { userGuess1 = it },
                            label = { Text("Guess for Flag 1", color = cBlue) },
                            modifier = Modifier.width(200.dp),
                            singleLine = true,
                            // Handling color & edibility per user answer
                            textStyle = TextStyle(
                                color = when {
                                    isFlag1Correct -> cGreen
                                    numGuesses < 2 -> Color.Black
                                    else -> cRed
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cBlue
                            ),
                            enabled = !isFlag1Correct
                        )
                        // Text to display correct country if necessary
                        Text(
                            text = if (doneGuessing && !isFlag1Correct) flagTrioList[0].second else "",
                            color = cBlue,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        Modifier.padding(bottom = 8.dp).width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(id = flagTrioList[1].first),
                            contentDescription = "Flag 2",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(200.dp)
                                .border(1.dp, Color.Black)
                        )
                        OutlinedTextField(
                            value = userGuess2,
                            onValueChange = { userGuess2 = it },
                            label = { Text("Guess for Flag 2", color = cBlue) },
                            modifier = Modifier.width(200.dp),
                            singleLine = true,
                            // Handling color & edibility per user answer
                            textStyle = TextStyle(
                                color = when {
                                    isFlag2Correct -> cGreen
                                    numGuesses < 2 -> Color.Black
                                    else -> cRed
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cBlue
                            ),
                            enabled = !isFlag2Correct
                        )
                        // Text to display correct country if necessary
                        Text(
                            text = if (doneGuessing && !isFlag2Correct) flagTrioList[1].second else "",
                            color = cBlue,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Column(
                    Modifier.width(200.dp).padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(id = flagTrioList[2].first),
                        contentDescription = "Flag 3",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(200.dp)
                            .border(1.dp, Color.Black)
                    )
                    OutlinedTextField(
                        value = userGuess3,
                        onValueChange = { userGuess3 = it },
                        label = { Text("Guess for Flag 3", color = cBlue) },
                        modifier = Modifier.width(200.dp),
                        singleLine = true,
                        // Handling color & edibility per user answer
                        textStyle = TextStyle(
                            color = when {
                                isFlag3Correct -> cGreen
                                numGuesses < 2 -> Color.Black
                                else -> cRed
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cBlue
                        ),
                        enabled = !isFlag3Correct
                    )
                    // Text to display correct country if necessary
                    Text(
                        text = if (doneGuessing && !isFlag3Correct) flagTrioList[2].second else "",
                        color = cBlue,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = {
                        if (!doneGuessing) {
                            submit()
                            if (!doneGuessing && isPressure) {
                                timer = createNewTimer(10)
                                timer?.start()
                            }
                        } else {
                            // Flushing gamestate & procuring next round
                            flagTrioList = generateNextThreeFlags(masterFlagList)
                            numGuesses = 1
                            userGuess1 = ""
                            isFlag1Correct = false
                            userGuess2 = ""
                            isFlag2Correct = false
                            userGuess3 = ""
                            isFlag3Correct = false
                            doneGuessing = false
                            if (isPressure) {
                                timer = createNewTimer(10)
                                timer?.start()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = if (doneGuessing) "Next" else "Submit",
                        color = cGrey,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )
                }
            }
        }
        // If device is landscape
        else {
            // Button to return to home screen
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

            // Box containing time & num guesses
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp, end = 22.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text ="${numGuesses}/3",
                        fontSize = 30.sp,
                        color = cBlue,
                        fontFamily = futuraPT,
                        modifier = Modifier.padding(),
                        textAlign = TextAlign.Center,
                    )
                    if (isPressure) {
                        Text(
                            text = "${timeRemaining}s",
                            fontSize = 30.sp,
                            color = cBlue,
                            fontFamily = futuraPT,
                        )
                    }
                }
            }

            // Containing all flags & submit button
            Column(
                Modifier.fillMaxSize().padding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contains the row of flags
                Row(Modifier.padding(bottom = 10.dp)) {
                    Column(
                        Modifier.width(200.dp).padding(end = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(id = flagTrioList[0].first),
                            contentDescription = "Flag 1",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(200.dp)
                                .border(1.dp, Color.Black)
                        )
                        OutlinedTextField(
                            value = userGuess1,
                            onValueChange = { userGuess1 = it },
                            label = { Text("Guess for Flag 1", color = cBlue) },
                            modifier = Modifier.width(200.dp),
                            singleLine = true,
                            // Handling color & edibility per user answer
                            textStyle = TextStyle(
                                color = when {
                                    isFlag1Correct -> cGreen
                                    numGuesses < 2 -> Color.Black
                                    else -> cRed
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cBlue
                            ),
                            enabled = !isFlag1Correct
                        )
                        // Text to display correct country if necessary
                        Text(
                            text = if (doneGuessing && !isFlag1Correct) flagTrioList[0].second else "",
                            color = cBlue,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        Modifier.padding(bottom = 8.dp, end = 10.dp).width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(id = flagTrioList[1].first),
                            contentDescription = "Flag 2",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(200.dp)
                                .border(1.dp, Color.Black)
                        )
                        OutlinedTextField(
                            value = userGuess2,
                            onValueChange = { userGuess2 = it },
                            label = { Text("Guess for Flag 2", color = cBlue) },
                            modifier = Modifier.width(200.dp),
                            singleLine = true,
                            // Handling color & edibility per user answer
                            textStyle = TextStyle(
                                color = when {
                                    isFlag2Correct -> cGreen
                                    numGuesses < 2 -> Color.Black
                                    else -> cRed
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cBlue
                            ),
                            enabled = !isFlag2Correct
                        )
                        // Text to display correct country if necessary
                        Text(
                            text = if (doneGuessing && !isFlag2Correct) flagTrioList[1].second else "",
                            color = cBlue,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        Modifier.width(200.dp).padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(id = flagTrioList[2].first),
                            contentDescription = "Flag 3",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .height(150.dp)
                                .width(200.dp)
                                .border(1.dp, Color.Black)
                        )
                        OutlinedTextField(
                            value = userGuess3,
                            onValueChange = { userGuess3 = it },
                            label = { Text("Guess for Flag 3", color = cBlue) },
                            modifier = Modifier.width(200.dp),
                            singleLine = true,
                            // Handling color & edibility per user answer
                            textStyle = TextStyle(
                                color = when {
                                    isFlag3Correct -> cGreen
                                    numGuesses < 2 -> Color.Black
                                    else -> cRed
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cBlue
                            ),
                            enabled = !isFlag3Correct
                        )
                        // Text to display correct country if necessary
                        Text(
                            text = if (doneGuessing && !isFlag3Correct) flagTrioList[2].second else "",
                            color = cBlue,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CORRECT/INCORRECT TEXT
                    Box(
                        Modifier.padding(end = 20.dp).width(165.dp)
                    ) {
                        if (doneGuessing) {
                            if (isFlag1Correct && isFlag2Correct && isFlag3Correct) {
                                Text(
                                    text = "CORRECT!",
                                    color = cGreen,
                                    fontSize = 35.sp,
                                    textAlign = TextAlign.End
                                )
                            } else {
                                Text(
                                    text = "WRONG!",
                                    color = cRed,
                                    fontSize = 35.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        else {
                            Text(
                                text = "",
                                fontSize = 35.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Next Button
                    Button(
                        onClick = {
                            if (!doneGuessing) {
                                submit()
                                if (!doneGuessing && isPressure) {
                                    timer = createNewTimer(10)
                                    timer?.start()
                                }
                            } else {
                                // Flushing gamestate & procuring next round
                                flagTrioList = generateNextThreeFlags(masterFlagList)
                                numGuesses = 1
                                userGuess1 = ""
                                isFlag1Correct = false
                                userGuess2 = ""
                                isFlag2Correct = false
                                userGuess3 = ""
                                isFlag3Correct = false
                                doneGuessing = false
                                if (isPressure) {
                                    timer = createNewTimer(10)
                                    timer?.start()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                    ) {
                        Text(
                            text = if (doneGuessing) "Next" else "Submit",
                            color = cGrey,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        )
                    }

                    // Point Counter
                    Box(
                        Modifier.padding(start = 20.dp).width(165.dp)
                    ) {
                        Text(
                            text ="Points: $numPoints",
                            fontSize = 30.sp,
                            color = cBlue,
                            fontFamily = futuraPT,
                            modifier = Modifier.padding(start = 30.dp),
                            textAlign = TextAlign.Left,
                        )
                    }
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
     * Examines if a user's answer is equal to the actual, irrespective of case
     * and leading/trailing spaces.
     */
    fun validateAnswer(userString : String, actualString : String) : Boolean {
        return userString.lowercase().trim() == actualString.lowercase().trim()
    }

    /**
     * Determines by how much score should increase based on if answers were correct/not,
     * with such passed in as booleans.
     */
    fun increaseScore(answer1 : Boolean, answer2 : Boolean, answer3: Boolean) : Int {
        var numPoints = 0
        if (answer1) numPoints++
        if (answer2) numPoints++
        if (answer3) numPoints++
        return numPoints
    }
}
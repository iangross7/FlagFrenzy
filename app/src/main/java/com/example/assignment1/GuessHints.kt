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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import kotlin.random.Random


class GuessHints : ComponentActivity() {
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

        // Current pair of flag/country
        var currPair by rememberSaveable{ mutableStateOf(generateNextPair(masterFlagList)) }
        // Current correct answer
        var currCountry by rememberSaveable{ mutableStateOf(currPair.second.trim()) }

        // Current dashed string
        var currDash by rememberSaveable{
            mutableStateOf(updateDashedString("", "", currCountry))
        }
        // Status of the game
        var doneGuessing by rememberSaveable{ mutableStateOf(false) }
        // Tracks number of guesses made
        var numGuesses by rememberSaveable{ mutableStateOf(1) }

        // String containing the user's current guess
        var userGuess by rememberSaveable{ mutableStateOf("") }
        //
        var allGuesses by rememberSaveable{ mutableStateOf("") }
        // Text judging the user answer
        var judgementText by rememberSaveable{ mutableStateOf("") }
        // Text that shows the correct country if was answered incorrect
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

        // Helper function to update incorrect guesses
        fun updateIncorrect() {
            if (numGuesses >= 3) {
                judgementText = "WRONG!"
                correctionText = "The country is $currCountry"
                doneGuessing = true
            }
            else {
                judgementText = "Incorrect Guess!"
                numGuesses++
            }
        }

        // Helper function to update for a correct letter guess
        fun updateCorrect() {
            currDash = updateDashedString(currDash, userGuess, currCountry)
            if (currDash.lowercase() == currCountry.lowercase()) {
                judgementText = "CORRECT!"
                doneGuessing = true
            }
        }

        // Handles submission sequence for a standard game  (disallows duplicates)
        fun submitStandard() {
            // Flushing the correctionText
            correctionText = ""
            judgementText = ""

            // Accepting only valid input, otherwise will not proceed
            // Valid if: isn't empty, is a letter, and hasn't been guessed yet
            if(!userGuess.isEmpty() && userGuess[0].isLetter() &&
                userGuess.lowercase() !in allGuesses ) {

                // If guess is correct
                if (userGuess.lowercase() in currCountry.lowercase()) {
                    updateCorrect()
                }
                // If guess is incorrect
                else {
                    updateIncorrect()
                }

                allGuesses += userGuess.lowercase()
            }
            else {
                correctionText = "Guess invalid or already guessed!"
            }
            userGuess = ""
        }

        // Handles submission for a pressure game (no leeway for duplicated guesses)
        fun submitPressured() {
            timer?.cancel()

            // Flushing the correctionText
            correctionText = ""
            judgementText = ""

            if (userGuess.isEmpty() || !userGuess[0].isLetter() || userGuess.lowercase() in allGuesses) {
                updateIncorrect()
            }
            else {
                // If guess is correct
                if (userGuess.lowercase() in currCountry.lowercase()) {
                    updateCorrect()
                }
                // If guess is incorrect
                else {
                    updateIncorrect()
                }

                allGuesses += userGuess.lowercase()
            }

            userGuess = ""
        }

        // Starts a new timer with ten seconds to go
        fun createNewTimer(seconds : Int) : CountDownTimer {
            return object : CountDownTimer((seconds.toLong() * 1000), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeRemaining = (millisUntilFinished / 1000).toInt() + 1
                }

                override fun onFinish() {
                    timeRemaining = 0
                    submitPressured()
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
            }

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currDash,
                    fontSize = 40.sp,
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.1.sp
                )
                Image(
                    painterResource(id = currPair.first),
                    contentDescription = "Flag",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .height(150.dp)
                        .width(215.dp)
                        .border(1.dp, Color.Black)
                )

                // Code for the CORRECT/INCORRECT text
                Text(
                    text = judgementText,
                    color = if (judgementText == "CORRECT!") cGreen else cRed,
                    fontSize = 40.sp
                )
                // Code for text telling the user the correct country
                Text(
                    text = correctionText,
                    color = cBlue,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                // Submit / Next Button
                Row {
                    Box(Modifier.width(100.dp)) {
                        Text(
                            text = if (isPressure) "${timeRemaining}s" else "",
                            color = cBlue,
                            fontFamily = futuraPT,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                    Button(
                        onClick = {
                            if (!doneGuessing) {
                                if (isPressure) {
                                    submitPressured()
                                    if (!doneGuessing) {
                                        timer = createNewTimer(10)
                                        timer?.start()
                                    }
                                } else {
                                    submitStandard()
                                }
                            }
                            // Round has concluded so resetting state
                            else {
                                correctionText = ""
                                judgementText = ""
                                currPair = generateNextPair(masterFlagList)
                                currCountry = currPair.second.trim()
                                currDash = updateDashedString("", "", currCountry)
                                numGuesses = 1
                                allGuesses = ""
                                doneGuessing = false
                                if (isPressure) {
                                    timer = createNewTimer(10)
                                    timer?.start()
                                }
                            }
                            // Flushing the textbox
                            userGuess = ""
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
                    Box(Modifier.width(100.dp)) {
                        Text(
                            text = "$numGuesses/3",
                            fontSize = 30.sp,
                            color = cBlue,
                            fontFamily = futuraPT,
                            modifier = Modifier.padding(start = 30.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // User-Input Field. Enter acts just like the enter button.
                OutlinedTextField(
                    value = userGuess,
                    // Only allows the user to type one character
                    onValueChange = {
                        if ((userGuess.isEmpty() && it[0].isLetter()) || it.isEmpty()) userGuess =
                            it
                    },
                    label = {
                        Text(
                            text = "Your Guess",
                            color = cBlue
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cBlue
                    )
                )
            }
        }
        // If orientation is landscape
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
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(250.dp)
                ) {
                    // Code for the CORRECT/INCORRECT text
                    Text(
                        text = judgementText,
                        color = if (judgementText == "CORRECT!") cGreen else cRed,
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center
                    )
                    // Code for text telling the user the correct country
                    Text(
                        text = correctionText,
                        color = cBlue,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(255.dp)
                ){
                    Text(
                        text = currDash,
                        fontSize = 40.sp,
                        softWrap = true,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp
                    )
                    Image(
                        painterResource(id = currPair.first),
                        contentDescription = "Flag",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(150.dp)
                            .width(215.dp)
                            .border(1.dp, Color.Black)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Submit / Next Button
                    Row {
                        Box(Modifier.width(70.dp)) {
                            Text(
                                text = if (isPressure) "${timeRemaining}s" else "",
                                color = cBlue,
                                fontFamily = futuraPT,
                                fontSize = 30.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 15.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (!doneGuessing) {
                                    if (isPressure) {
                                        submitPressured()
                                        if (!doneGuessing) {
                                            timer = createNewTimer(10)
                                            timer?.start()
                                        }
                                    } else {
                                        submitStandard()
                                    }
                                }
                                // Round has concluded so resetting state
                                else {
                                    correctionText = ""
                                    judgementText = ""
                                    currPair = generateNextPair(masterFlagList)
                                    currCountry = currPair.second.trim()
                                    currDash = updateDashedString("", "", currCountry)
                                    numGuesses = 1
                                    allGuesses = ""
                                    doneGuessing = false
                                    if (isPressure) {
                                        timer = createNewTimer(10)
                                        timer?.start()
                                    }
                                }
                                // Flushing the textbox
                                userGuess = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = cBlue),
                            modifier = Modifier.width(150.dp)
                        ) {
                            Text(
                                text = if (doneGuessing) "Next" else "Submit",
                                color = cGrey,
                                fontSize = 25.sp,
                            )
                        }
                        Box(Modifier.width(70.dp)) {
                            Text(
                                text = "$numGuesses/3",
                                fontSize = 30.sp,
                                color = cBlue,
                                fontFamily = futuraPT,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }

                    // User-Input Field. Enter acts just like the enter button.
                    OutlinedTextField(
                        value = userGuess,
                        // Only allows the user to type one character
                        onValueChange = {
                            if ((userGuess.isEmpty() && it[0].isLetter()) || it.isEmpty()) userGuess =
                                it
                        },
                        label = {
                            Text(
                                text = "Your Guess",
                                color = cBlue
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cBlue
                        )
                    )
                }
            }
        }
    }

    /**
     * Randomly generates a new pair of flag & name for use from the masterList
     */
    fun generateNextPair(masterFlagList: ArrayList<Pair<Int, String>>) : Pair<Int,String> {
        val index = Random.nextInt(0, masterFlagList.size)
        return (masterFlagList[index])
    }

    /**
     * Takes in the current dashed string and updates it per the user's new character guess.
     * Disregards case for any of the inputs during inspection, returns properly cased guesses.
     * If the userGuess is not a single character, it will return a fresh set of dashes corresponding
     * to the letters in the correct country.
     */
    fun updateDashedString(currString : String, userGuess : String, correctCountry : String) : String {
        var updatedString = ""
        val userGuessLower = userGuess.lowercase()
        val correctCountryLower = correctCountry.lowercase()

        // Checks if the user's guess is the appropriate length with spaces.
        // If it is not, it returns a fresh set of dashes.
        if (userGuess.length != 1) {
            for (i in 0..correctCountry.length - 1) {
                if (correctCountry[i] == ' ') {
                    updatedString += " "
                }
                else {
                    updatedString += "-"
                }
            }

            return updatedString
        }

        val currStringLower = currString.lowercase()

        // Checking if the character has already been guessed, or if it's an incorrect guess.
        // Either way, it returns the string untouched
        if (userGuessLower in currStringLower || userGuessLower !in correctCountryLower) return currString

        // By this point, we know that the user's guess is a correct and undiscovered letter
        // in the correct country's name
        val currStringBuilder = StringBuilder(currString)

        for (i in 0..correctCountry.length - 1) {
            if (correctCountryLower[i] == userGuessLower[0]) {
                currStringBuilder.setCharAt(i, correctCountry[i])
            }
        }

        return currStringBuilder.toString()
    }
}
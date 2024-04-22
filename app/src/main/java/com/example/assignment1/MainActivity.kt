package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.lang.Exception
import java.io.IOException
import org.json.JSONObject
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * ACTIVITY FOR THE MAIN (HOME) SCREEN
 *
 * IAN GROSS - ID: 20545204
 * VIDEO ATTACHED IN ZIP FILE
 * REACH ME AT IANGROSS707@GMAIL.COM
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            displayGUI()
        }
    }

    @Composable
    @Preview
    /**
     * Responsible for displaying the main GUI elements
     */
    fun displayGUI(){
        // Variable holding orientation status
        val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
        // Variable tracking whether or not device is in countdown mode
        var pressureEnabled by rememberSaveable{mutableStateOf(false)}
        // Tracking context
        val context = LocalContext.current
        // Paired drawable objects by country name, ArrayList<Pair<Int,String>>
        // The reason it is an ArrayList is for ease of java intent passing
        val masterFlagList = loadFlagsViaJSON(context, "countries.json")

        // Custom colors for UI
        val cBlue = Color(0xFF1c9de0)
        val cGrey = Color(0xFFe3e3e3)

        // Custom font (futuraPT)
        val futuraPT = FontFamily(
            Font(R.font.futurapt, FontWeight.Light)
        )

        // UI layout for a portrait device
        if (isPortrait) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "FLAG FRENZY",
                    fontSize = 55.sp,
                    fontFamily = futuraPT,
                    color = cBlue,
                    modifier = Modifier.padding(top = 50.dp, bottom = 20.dp)
                )
                Image(
                    painterResource(id = R.drawable.earths),
                    contentDescription = "Earth Image",
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp)
                        .padding(bottom = 20.dp)
                )
                // Button for GuessTheCountry
                Button(
                    onClick = {
                        // Passes in the master list & pressure mode info
                        val i = Intent(context, GuessTheCountry::class.java)
                        i.putExtra("masterFlagList", masterFlagList)
                        i.putExtra("isPressure", pressureEnabled)
                        context.startActivity(i)
                    },
                    modifier = Modifier.padding(bottom=15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = "Guess The Country",
                        fontSize = 25.sp,
                        color = cGrey
                    )
                }
                // Button for GuessHints
                Button(
                    onClick = {
                        val i = Intent(context, GuessHints::class.java)
                        i.putExtra("masterFlagList", masterFlagList)
                        i.putExtra("isPressure", pressureEnabled)
                        context.startActivity(i)
                    },
                    modifier = Modifier.padding(bottom=15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cGrey)
                ) {
                    Text(
                        text = "Guess-Hints",
                        fontSize = 25.sp,
                        color = cBlue
                    )
                }
                // Button for GuessTheFlag
                Button(
                    onClick = {
                        val i = Intent(context, GuessTheFlag::class.java)
                        i.putExtra("masterFlagList", masterFlagList)
                        i.putExtra("isPressure", pressureEnabled)
                        context.startActivity(i)
                    },
                    modifier = Modifier.padding(bottom=15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                ) {
                    Text(
                        text = "Guess The Flag",
                        fontSize = 25.sp,
                        color = cGrey
                    )
                }
                // Button for AdvancedLevel
                Button(
                    onClick = {
                        val i = Intent(context, AdvancedLevel::class.java)
                        i.putExtra("masterFlagList", masterFlagList)
                        i.putExtra("isPressure", pressureEnabled)
                        context.startActivity(i)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cGrey)
                ) {
                    Text(
                        text = "Advanced Level",
                        fontSize = 25.sp,
                        color = cBlue
                    )
                }
                Text(
                    text = "PRESSURE MODE",
                    fontSize = 25.sp,
                    fontFamily = futuraPT,
                    color = cBlue,
                    modifier = Modifier.padding(top=30.dp)
                )
                // Toggle for pressure mode
                Switch(
                    checked = pressureEnabled,
                    onCheckedChange = { pressureEnabled = it },
                    modifier = Modifier
                        .scale(1.5f)
                        .padding(top = 10.dp),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = cBlue,
                        uncheckedThumbColor = cBlue
                    )
                )
            }
        }
        // IF DEVICE IS LANDSCAPE
        else {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "FLAG FRENZY",
                    fontSize = 55.sp,
                    fontFamily = futuraPT,
                    color = cBlue,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(Modifier.padding(top = 20.dp)){
                    // Button for GuessTheCountry
                    Button(
                        onClick = {
                            // Passes in the master list & pressure mode info
                            val i = Intent(context, GuessTheCountry::class.java)
                            i.putExtra("masterFlagList", masterFlagList)
                            i.putExtra("isPressure", pressureEnabled)
                            context.startActivity(i)
                        },
                        modifier = Modifier.width(265.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                    ) {
                        Text(
                            text = "Guess The Country",
                            fontSize = 25.sp,
                            color = cGrey
                        )
                    }
                    // Button for GuessHints
                    Button(
                        onClick = {
                            val i = Intent(context, GuessHints::class.java)
                            i.putExtra("masterFlagList", masterFlagList)
                            i.putExtra("isPressure", pressureEnabled)
                            context.startActivity(i)
                        },
                        modifier = Modifier.padding(start = 20.dp).width(265.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cGrey)
                    ) {
                        Text(
                            text = "Guess-Hints",
                            fontSize = 25.sp,
                            color = cBlue
                        )
                    }
                }
                Row(Modifier.padding(top = 20.dp)){
                    // Button for GuessTheFlag
                    Button(
                        onClick = {
                            val i = Intent(context, GuessTheFlag::class.java)
                            i.putExtra("masterFlagList", masterFlagList)
                            i.putExtra("isPressure", pressureEnabled)
                            context.startActivity(i)
                        },
                        modifier = Modifier.width(265.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cGrey)
                    ) {
                        Text(
                            text = "Guess The Flag",
                            fontSize = 25.sp,
                            color = cBlue
                        )
                    }
                    // Button for AdvancedLevel
                    Button(
                        onClick = {
                            val i = Intent(context, AdvancedLevel::class.java)
                            i.putExtra("masterFlagList", masterFlagList)
                            i.putExtra("isPressure", pressureEnabled)
                            context.startActivity(i)
                        },
                        modifier = Modifier.padding(start = 20.dp).width(265.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cBlue)
                    ) {
                        Text(
                            text = "Advanced Level",
                            fontSize = 25.sp,
                            color = cGrey
                        )
                    }
                }
                Text(
                    text = "PRESSURE MODE",
                    fontSize = 25.sp,
                    fontFamily = futuraPT,
                    color = cBlue,
                    modifier = Modifier.padding(top = 20.dp)
                )
                // Toggle for pressure mode
                Switch(
                    checked = pressureEnabled,
                    onCheckedChange = { pressureEnabled = it },
                    modifier = Modifier
                        .scale(1.5f)
                        .padding(top = 6.dp),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = cBlue,
                        uncheckedThumbColor = cBlue
                    )
                )
            }
        }
    }

    /**
     * Function that first loads JSON from assets into a string, then parses said string with
     * jsonObject to retrieve both flag file names and country names, and coalesces corresponding
     * drawable flag files with their actual country name for implementation throughout the app.
     *
     * RETURNS: Paired ArrayList with drawable flag file and corresponding country name
    */
    fun loadFlagsViaJSON(context: Context, fileName: String): ArrayList<Pair<Int, String>> {
        val flagList = mutableListOf<Pair<Int, String>>()

        try {
            // Retrieves JSON from assets and converts into a string using buffered reader
            context.assets.open(fileName).bufferedReader().use {reader ->
                val jsonString = reader.readText()
                val jsonObj = JSONObject(jsonString)

                // JSONObject has been created and will now iterate through its keys to retrieve
                // all flags and country names
                val keys = jsonObj.keys()
                while(keys.hasNext()) {
                    // Retrieves pair of filename with corresponding country name
                    val fileNameUpper = keys.next()
                    val countryName = jsonObj.getString(fileNameUpper)

                    // Finds the drawable ID by filename
                    val id = context.resources.getIdentifier(fileNameUpper.lowercase(), "drawable", context.packageName)
                    if (id != 0) {
                        flagList.add(Pair(id,countryName))
                    }
                    else {
                        println("Error loading file for $countryName with fileName $fileNameUpper")
                    }
                }
            }
        }
        catch (e: IOException) {
                e.printStackTrace()
            println("Error loading in flags & country names")
        }

        return ArrayList(flagList)
    }
}

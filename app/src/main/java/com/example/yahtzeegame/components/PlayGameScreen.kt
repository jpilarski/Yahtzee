package com.example.yahtzeegame.components

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.yahtzeegame.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PlayGameScreen(
    isUserLoggedIn: Boolean,
    context: Context,
    onSaveGameFirebase: (myPoints: Int, opponentPoints: Int, result: String, gotBonus: Boolean, gotYahtzee: Boolean) -> Unit,
    onSaveGameLocal: (myPoints: Int, opponentPoints: Int, result: String, gotBonus: Boolean, gotYahtzee: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var gameFinished by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (!gameFinished) showExitDialog = true
    }

    if (showExitDialog) {
        MyAlertDialog(
            onDismiss = { showExitDialog = false },
            title = "End Game",
            alertText = "Do you want to end this game?",
            confirmText = "Yes",
            onConfirm = {
                showExitDialog = false
                onBack()
            },
            onAbandon = { showExitDialog = false }
        )
    }

    val intListSaver = listSaver<MutableList<Int>, Int>(
        save = { state -> state.toList() },
        restore = { list -> list.toMutableStateList() })
    val booleanListSaver = listSaver<MutableList<Boolean>, Boolean>(
        save = { it.toList() },
        restore = { it.toMutableStateList() })

    val diceValues = rememberSaveable(saver = intListSaver) {
        mutableStateListOf<Int>().apply { repeat(5) { add(Random.nextInt(1, 7)) } }
    }
    val diceSelected = rememberSaveable(saver = booleanListSaver) {
        mutableStateListOf(true, true, true, true, true)
    }
    val playerScores = rememberSaveable(saver = intListSaver) {
        mutableStateListOf<Int>().apply { repeat(15) { add(0) } }
    }
    val opponentScores = rememberSaveable(saver = intListSaver) {
        mutableStateListOf<Int>().apply { repeat(15) { add(0) } }
    }
    val playerChosenCategories = rememberSaveable(saver = booleanListSaver) {
        mutableStateListOf<Boolean>().apply { repeat(15) { add(false) } }
    }
    val opponentChosenCategories = rememberSaveable(saver = booleanListSaver) {
        mutableStateListOf<Boolean>().apply { repeat(15) { add(false) } }
    }

    val diceImages = diceValues.map {
        when (it) {
            1 -> R.drawable.dice1
            2 -> R.drawable.dice2
            3 -> R.drawable.dice3
            4 -> R.drawable.dice4
            5 -> R.drawable.dice5
            6 -> R.drawable.dice6
            else -> R.drawable.logo
        }
    }

    var showShakeText by rememberSaveable { mutableStateOf(false) }
    var isPlayerTurn by rememberSaveable { mutableStateOf(Random.nextBoolean()) }
    var rollsLeft by rememberSaveable { mutableIntStateOf(3) }
    var turnsLeft by rememberSaveable { mutableIntStateOf(26) }
    var turnFinished by rememberSaveable { mutableStateOf(false) }

    var finalPlayerScore by rememberSaveable { mutableIntStateOf(0) }
    var finalOpponentScore by rememberSaveable { mutableIntStateOf(0) }

    fun rollSelectedDices() {
        for (i in 0..4) {
            if (diceSelected[i]) diceValues[i] = Random.nextInt(1, 7)
        }
    }

    fun endTurn() {
        rollsLeft = 3
        turnFinished = false
        turnsLeft--
        isPlayerTurn = !isPlayerTurn
        diceSelected.fill(true) // isPlayerTurn
    }

    suspend fun opponentTurn() {
        val preferredMinScores = listOf(3, 6, 9, 12, 15, 18, 20, 18, 25, 30, 40, 20, 50)

        fun selectBestCategory(scores: List<Int>): Int? {
            val available = scores.withIndex()
                .filter { (i, score) -> !opponentChosenCategories[i] && score >= preferredMinScores[i] }
            return available.maxByOrNull { it.value }?.index
        }

        diceSelected.fill(true)
        delay(500)

        rollSelectedDices()
        delay(500)

        repeat(2) {
            val calculated = calculateYahtzeeScores(diceValues)
            val bestCategory = selectBestCategory(calculated)

            if (bestCategory != null) {
                opponentScores[bestCategory] = calculated[bestCategory]
                opponentChosenCategories[bestCategory] = true
                return endTurn()
            }

            diceSelected.indices.forEach { i -> diceSelected[i] = Random.nextBoolean() }
            delay(500)

            rollSelectedDices()
            delay(500)
        }

        val fallback = calculateYahtzeeScores(diceValues).withIndex()
            .filter { (i, _) -> !opponentChosenCategories[i] }
            .maxByOrNull { it.value }

        fallback?.let { (index, value) ->
            opponentScores[index] = value
            opponentChosenCategories[index] = true
        }

        endTurn()
    }

    LaunchedEffect(turnsLeft) {
        if (turnsLeft <= 0 && !gameFinished) {
            val playerSum = playerScores.subList(0, 6).sum()
            val opponentSum = opponentScores.subList(0, 6).sum()

            val playerBonus = if (playerSum >= 63) 35 else 0
            val opponentBonus = if (opponentSum >= 63) 35 else 0

            playerScores[13] = playerBonus
            opponentScores[13] = opponentBonus

            playerScores[14] = playerScores.subList(0, 14).sum()
            opponentScores[14] = opponentScores.subList(0, 14).sum()

            playerChosenCategories[13] = true
            opponentChosenCategories[13] = true
            playerChosenCategories[14] = true
            opponentChosenCategories[14] = true

            val result = when {
                playerScores[14] > opponentScores[14] -> "WIN"
                playerScores[14] < opponentScores[14] -> "LOSE"
                else -> "DRAW"
            }

            val gotYahtzee = playerScores[12] == 50
            val gotBonus = playerBonus > 0

            if (isUserLoggedIn) {
                onSaveGameFirebase(
                    playerScores[14],
                    opponentScores[14],
                    result,
                    gotBonus,
                    gotYahtzee
                )
            } else {
                onSaveGameLocal(playerScores[14], opponentScores[14], result, gotBonus, gotYahtzee)
            }

            diceSelected.fill(true)
            isPlayerTurn = false
            turnFinished = true
            gameFinished = true

            finalPlayerScore = playerScores[14]
            finalOpponentScore = opponentScores[14]
        }
    }

    LaunchedEffect(isPlayerTurn) {
        if (!isPlayerTurn && !gameFinished) {
            delay(1000)
            opponentTurn()
        } else if (rollsLeft == 3) {
            diceSelected.fill(true)
        }
    }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val shakeDetector = remember {
        object : SensorEventListener {
            private var lastShakeTime: Long = 0

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                if (acceleration > 12) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 1000) {
                        lastShakeTime = now
                        sensorManager.unregisterListener(this)
                        coroutineScope.launch {
                            rollSelectedDices()
                            rollsLeft--
                            if (rollsLeft <= 2) diceSelected.fill(false)
                            showShakeText = false
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }


    val onRollClick = {
        if (isPlayerTurn && diceSelected.any { it }) {
            showShakeText = true
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }


    val onCategoryChosen: (Int) -> Unit = { index ->
        val calculatedScores = calculateYahtzeeScores(diceValues)
        playerScores[index] = calculatedScores[index]
        playerChosenCategories[index] = true
        endTurn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isPortrait) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameControlsSection(
                    isPlayerTurn = isPlayerTurn,
                    showSelectDicesText = (isPlayerTurn && rollsLeft < 3),
                    diceSelected = diceSelected,
                    onDiceClick = { index ->
                        if (rollsLeft in 1..2) diceSelected[index] = !diceSelected[index]
                    },
                    diceImages = diceImages,
                    onRollClick = onRollClick,
                    rollsLeft = rollsLeft,
                    showShakeText = showShakeText,
                    gameFinished = gameFinished,
                    minePoints = finalPlayerScore,
                    oppPoints = finalOpponentScore
                )
                Spacer(modifier = Modifier.height(8.dp))
                BackToMenuButton(onClick = {
                    if (!gameFinished) showExitDialog = true
                    else onBack()
                })
                Spacer(modifier = Modifier.height(16.dp))
                ScoresTableSection(
                    playerScores = playerScores,
                    opponentScores = opponentScores,
                    playerChosenCategories = playerChosenCategories,
                    opponentChosenCategories = opponentChosenCategories,
                    playerTurn = isPlayerTurn,
                    diceValues = diceValues,
                    rollsLeft = rollsLeft,
                    onCategoryChosen = onCategoryChosen
                )
            }
        } else {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameControlsSection(
                        isPlayerTurn = isPlayerTurn,
                        showSelectDicesText = (isPlayerTurn && rollsLeft < 3),
                        diceSelected = diceSelected,
                        onDiceClick = { index ->
                            if (rollsLeft in 1..2) diceSelected[index] = !diceSelected[index]
                        },
                        diceImages = diceImages,
                        onRollClick = onRollClick,
                        rollsLeft = rollsLeft,
                        showShakeText = showShakeText,
                        gameFinished = gameFinished,
                        minePoints = finalPlayerScore,
                        oppPoints = finalOpponentScore
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BackToMenuButton(onClick = {
                        if (!gameFinished) showExitDialog = true
                        else onBack()
                    })
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScoresTableSection(
                        playerScores = playerScores,
                        opponentScores = opponentScores,
                        playerChosenCategories = playerChosenCategories,
                        opponentChosenCategories = opponentChosenCategories,
                        playerTurn = isPlayerTurn,
                        diceValues = diceValues,
                        rollsLeft = rollsLeft,
                        onCategoryChosen = onCategoryChosen
                    )
                }
            }
        }
    }
}

@Composable
fun GameControlsSection(
    isPlayerTurn: Boolean,
    showSelectDicesText: Boolean,
    diceSelected: List<Boolean>,
    onDiceClick: (Int) -> Unit,
    diceImages: List<Int>,
    onRollClick: () -> Unit,
    rollsLeft: Int,
    showShakeText: Boolean,
    gameFinished: Boolean,
    minePoints: Int,
    oppPoints: Int
) {
    val selectedColor = greenColor
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!gameFinished) HeaderText(text = if (isPlayerTurn) "Your turn" else "Opponent's turn")
        if (gameFinished) ResultText(minePoints, oppPoints)
        if (showSelectDicesText) SelectDiceText(rollsLeft)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .width(80.dp)
                        .height(80.dp)
                        .background(
                            if (diceSelected[i]) selectedColor.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onDiceClick(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = diceImages[i]),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 3..4) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .width(80.dp)
                        .height(80.dp)
                        .background(
                            if (diceSelected[i]) selectedColor.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onDiceClick(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = diceImages[i]),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        MyButton(
            text = "Roll dices",
            onClick = onRollClick,
            modifier = Modifier.fillMaxWidth(0.5f),
            enabled = isPlayerTurn && diceSelected.any { it } && rollsLeft > 0
        )
        if (showShakeText) ShakeText()
    }
}

@Composable
fun ScoresTableSection(
    playerScores: List<Int>,
    opponentScores: List<Int>,
    playerChosenCategories: List<Boolean>,
    opponentChosenCategories: List<Boolean>,
    playerTurn: Boolean,
    diceValues: List<Int>,
    rollsLeft: Int,
    onCategoryChosen: (Int) -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderText(text = "Scores Table")
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(BorderStroke(1.dp, borderColor))
            ) {
                listOf("Category", "Player", "Opponent").forEach { header ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    borderColor
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TableHead(text = header)
                    }
                }
            }
            categories.forEachIndexed { index, category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            BorderStroke(
                                1.dp,
                                borderColor
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    borderColor
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TableHead(text = category)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    borderColor
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerChosenCategories[index]) TableText(text = playerScores[index].toString())
                        else if (playerTurn && rollsLeft < 3) {
                            val calculatedScores = calculateYahtzeeScores(diceValues)
                            if (index in 0..12) {
                                SelectCategoryButton(
                                    onClick = { onCategoryChosen(index) },
                                    text = calculatedScores[index].toString()
                                )
                            } else {
                                TableText(text = "-")
                            }
                        } else TableText(text = "-")

                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    borderColor
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!opponentChosenCategories[index]) TableText(text = "-")
                        else TableText(text = opponentScores[index].toString())
                    }
                }
            }
        }
    }
}
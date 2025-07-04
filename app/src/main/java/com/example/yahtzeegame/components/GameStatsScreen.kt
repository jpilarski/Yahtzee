package com.example.yahtzeegame.components

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun GameStatsScreen(
    gameResults: List<GameResult>,
    isUserLoggedIn: Boolean,
    userEmail: String?,
    onDeleteAll: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (gameResults.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderText(text = "No games played")
                Spacer(Modifier.height(24.dp))
                BackToMenuButton(onClick = onBack)
            }
        }
        return
    }

    val gamesPlayed = gameResults.size
    val gamesWon = gameResults.count { it.result == "WIN" }
    val gamesDrawn = gameResults.count { it.result == "DRAW" }
    val gamesLost = gameResults.count { it.result == "LOSE" }

    val avgScore = gameResults.map { it.myPoints }.average()
    val maxScore = gameResults.maxOfOrNull { it.myPoints } ?: 0
    val minScore = gameResults.minOfOrNull { it.myPoints } ?: 0

    val biggestWin =
        gameResults.filter { it.result == "WIN" }.maxOfOrNull { it.myPoints - it.opponentPoints }
            ?: -1
    val biggestLoss =
        gameResults.filter { it.result == "LOSE" }.maxOfOrNull { it.opponentPoints - it.myPoints }
            ?: -1

    val bonusesGained = gameResults.count { it.gotBonus }
    val yahtzeeGained = gameResults.count { it.gotYahtzee }

    fun sendStatisticsSms() {
        val userIdText = if (isUserLoggedIn) userEmail ?: "unknown user" else "local user"
        val statsText = buildString {
            appendLine("Yahtzee statistics for $userIdText")
            appendLine("Games played: $gamesPlayed")
            appendLine("Games won: $gamesWon")
            appendLine("Games drawn: $gamesDrawn")
            appendLine("Games lost: $gamesLost")
            appendLine("Average score: ${"%.2f".format(avgScore)}")
            appendLine("Maximum score: $maxScore")
            appendLine("Minimum score: $minScore")
            appendLine("Biggest win: ${if (biggestWin >= 0) biggestWin else "-"}")
            appendLine("Biggest loss: ${if (biggestLoss >= 0) biggestLoss else "-"}")
            appendLine("Bonuses gained: $bonusesGained")
            appendLine("Yahtzee gained: $yahtzeeGained")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:".toUri()
            putExtra("sms_body", statsText)
        }
        context.startActivity(intent)
    }

    if (showDialog) {
        MyAlertDialog(
            onDismiss = { showDialog = false },
            title = "Confirm deletion",
            alertText = "Are you sure you want to delete all game data?",
            confirmText = "Delete",
            onConfirm = {
                onDeleteAll()
                showDialog = false
                Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
            },
            onAbandon = { showDialog = false },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderText(text = "Game statistics")
                Spacer(modifier = Modifier.height(8.dp))
                StatsSurface(
                    gamesPlayed = gamesPlayed,
                    gamesWon = gamesWon,
                    gamesDrawn = gamesDrawn,
                    gamesLost = gamesLost,
                    avgScore = avgScore,
                    maxScore = maxScore,
                    minScore = minScore,
                    biggestWin = biggestWin,
                    biggestLoss = biggestLoss,
                    bonusesGained = bonusesGained,
                    yahtzeeGained = yahtzeeGained
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatsButtons(
                    onDeleteClick = { showDialog = true },
                    onSendClick = { sendStatisticsSms() })
                Spacer(modifier = Modifier.height(8.dp))
                BackToMenuButton(onClick = onBack)
                Spacer(Modifier.height(16.dp))
                HeaderText(text = "List of games")
                Spacer(Modifier.height(8.dp))
                StatsResultsList(gameResults = gameResults)
            }
        } else {
            Row(
                modifier = Modifier
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
                    HeaderText(text = "Game statistics")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatsSurface(
                        gamesPlayed = gamesPlayed,
                        gamesWon = gamesWon,
                        gamesDrawn = gamesDrawn,
                        gamesLost = gamesLost,
                        avgScore = avgScore,
                        maxScore = maxScore,
                        minScore = minScore,
                        biggestWin = biggestWin,
                        biggestLoss = biggestLoss,
                        bonusesGained = bonusesGained,
                        yahtzeeGained = yahtzeeGained
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatsButtons(
                        onDeleteClick = { showDialog = true },
                        onSendClick = { sendStatisticsSms() })
                    Spacer(modifier = Modifier.height(8.dp))
                    BackToMenuButton(onClick = onBack)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeaderText(text = "List of games")
                    Spacer(Modifier.height(8.dp))
                    StatsResultsList(gameResults = gameResults)
                }
            }
        }
    }
}
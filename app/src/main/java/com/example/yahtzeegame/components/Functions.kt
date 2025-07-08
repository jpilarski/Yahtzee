package com.example.yahtzeegame.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

val greenColor = Color(0xFF4CAF50)
val orangeColor = Color(0xFFFFA726)
val redColor = Color(0xFFF44336)

@Composable
fun BackToMenuButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = "Back to main menu",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun OutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.titleLarge) },
        modifier = Modifier.fillMaxWidth(0.8f),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun MyButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) {
    Button(
        onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled
    ) {
        Text(
            text = text, style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun HeaderText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun LoggedText(email: String) {
    Text(
        text = "Logged as $email",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun MyAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    alertText: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onAbandon: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, title = {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }, text = {
            Text(
                text = alertText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }, confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = redColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }, dismissButton = {
            TextButton(onClick = onAbandon) {
                Text(
                    text = "No",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }, containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun StatsSurfaceEntry(
    title: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = "$title: $value", style = MaterialTheme.typography.bodyMedium, color = color
    )
}

@Composable
fun StatsSurface(
    gamesPlayed: Int,
    gamesWon: Int,
    gamesDrawn: Int,
    gamesLost: Int,
    avgScore: Double,
    maxScore: Int,
    minScore: Int,
    biggestWin: Int,
    biggestLoss: Int,
    bonusesGained: Int,
    yahtzeeGained: Int
) {
    Surface(
        tonalElevation = 5.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatsSurfaceEntry(title = "Games played", value = gamesPlayed.toString())
            StatsSurfaceEntry(
                title = "Games won", value = gamesWon.toString(), color = greenColor
            )
            StatsSurfaceEntry(
                title = "Games drawn", value = gamesDrawn.toString(), color = orangeColor
            )
            StatsSurfaceEntry(
                title = "Games lost", value = gamesLost.toString(), color = redColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsSurfaceEntry(
                title = "Average score",
                value = String.format(Locale.getDefault(), format = "%.2f", avgScore)
            )
            StatsSurfaceEntry(title = "Maximum score", value = maxScore.toString())
            StatsSurfaceEntry(title = "Minimum score", value = minScore.toString())
            Spacer(modifier = Modifier.height(8.dp))
            StatsSurfaceEntry(
                title = "Biggest win", value = if (biggestWin >= 0) biggestWin.toString() else "-"
            )
            StatsSurfaceEntry(
                title = "Biggest loss",
                value = if (biggestLoss >= 0) biggestLoss.toString() else "-"
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsSurfaceEntry(
                title = "Bonuses gained", value = bonusesGained.toString()
            )
            StatsSurfaceEntry(
                title = "Yahtzee gained", value = yahtzeeGained.toString()
            )
        }
    }
}

@Composable
fun StatsButtons(
    onDeleteClick: () -> Unit, onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
    ) {
        MyButton(text = "Delete games", onClick = onDeleteClick)
        MyButton(text = "Send statistics", onClick = onSendClick)
    }
}

@Composable
fun GotBonusOrYahtzee(
    what: String, got: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$what ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (got) "✅" else "❌", style = MaterialTheme.typography.bodyMedium
        )
    }

}

@Composable
fun StatsResultsList(
    gameResults: List<GameResult>
) {
    gameResults.forEach { game ->
        val backColor = when (game.result) {
            "WIN" -> greenColor.copy(alpha = 0.25f)
            "DRAW" -> orangeColor.copy(alpha = 0.25f)
            "LOSE" -> redColor.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surface
        }
        val scoreColor = when (game.result) {
            "WIN" -> greenColor
            "DRAW" -> orangeColor
            "LOSE" -> redColor
            else -> MaterialTheme.colorScheme.onSurface
        }
        Surface(
            tonalElevation = 5.dp,
            shape = MaterialTheme.shapes.medium,
            color = backColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    SimpleDateFormat(
                        "dd-MM-yyyy HH:mm", Locale.getDefault()
                    ).format(Date(game.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${game.myPoints} - ${game.opponentPoints}",
                    color = scoreColor,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                GotBonusOrYahtzee(what = "Bonus", got = game.gotBonus)
                GotBonusOrYahtzee(what = "Yahtzee", got = game.gotYahtzee)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SelectDiceText(rollsLeft: Int) {
    Spacer(modifier = Modifier.height(8.dp))
    val text = when (rollsLeft) {
        3 -> ""
        0 -> "Choose category"
        else -> "Select dices to roll again or choose category"
    }
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ResultText(
    minePoints: Int,
    oppPoints: Int
) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = if (minePoints > oppPoints) "You won!" else if (minePoints == oppPoints) "It's a draw!" else "You lost!",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "$minePoints - $oppPoints",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )

}



@Composable
fun ShakeText() {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Shake your phone",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun TableHead(
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun TableText(
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun SelectCategoryButton(onClick: () -> Unit, text: String) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

fun calculateYahtzeeScores(dice: List<Int>): List<Int> {
    val counts = IntArray(6)
    dice.forEach { counts[it - 1]++ }
    val total = dice.sum()
    val scores = mutableListOf<Int>()
    for (i in 0..5) {
        scores.add(counts[i] * (i + 1))
    }
    scores.add(if (counts.any { it >= 3 }) total else 0)
    scores.add(if (counts.any { it >= 4 }) total else 0)
    scores.add(if (counts.contains(3) && counts.contains(2)) 25 else 0)
    val straights = counts.map { if (it > 0) 1 else 0 }.joinToString("")
    val hasSmallStraight = "1111" in straights || "01111" in straights || "11110" in straights
    scores.add(if (hasSmallStraight) 30 else 0)
    val hasLargeStraight = "11111" in straights
    scores.add(if (hasLargeStraight) 40 else 0)
    scores.add(total)
    scores.add(if (counts.any { it == 5 }) 50 else 0)
    return scores
}

val categories = listOf(
    "Ones", "Twos", "Threes", "Fours", "Fives",
    "Sixes", "Three of a Kind", "Four of a Kind",
    "Full House", "Small Straight", "Large Straight",
    "Chance", "Yahtzee", "Bonus", "Total"
)

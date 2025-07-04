package com.example.yahtzeegame

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yahtzeegame.components.GameResult
import com.example.yahtzeegame.components.GameResultViewModel
import com.example.yahtzeegame.components.GameStatsScreen
import com.example.yahtzeegame.components.LoginScreen
import com.example.yahtzeegame.components.MainMenuScreen
import com.example.yahtzeegame.components.PlayGameScreen
import com.example.yahtzeegame.components.RegisterScreen
import com.example.yahtzeegame.components.toGameResult
import com.example.yahtzeegame.ui.theme.YahtzeeTheme
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            YahtzeeTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                var isUserLoggedIn by remember { mutableStateOf(false) }
                val gameResults = remember { mutableStateListOf<GameResult>() }
                val gameResultViewModel: GameResultViewModel = viewModel(
                    factory = GameResultViewModelFactory(application)
                )

                LaunchedEffect(isUserLoggedIn) {
                    gameResults.clear()
                    if (isUserLoggedIn && auth.currentUser != null) {
                        val userId = auth.currentUser?.uid
                        db.collection("yahtzeeResults").whereEqualTo("userId", userId)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener { result, error ->
                                if (error != null || result == null) return@addSnapshotListener

                                val firebaseResults = result.mapNotNull { document ->
                                    try {
                                        GameResult(
                                            myPoints = document.getLong("myPoints")?.toInt()
                                                ?: return@mapNotNull null,
                                            opponentPoints = document.getLong("opponentPoints")
                                                ?.toInt() ?: return@mapNotNull null,
                                            result = document.getString("result")
                                                ?: return@mapNotNull null,
                                            gotBonus = document.getBoolean("gotBonus") ?: false,
                                            gotYahtzee = document.getBoolean("gotYahtzee") ?: false,
                                            timestamp = document.getTimestamp("timestamp")
                                                ?.toDate()?.time ?: 0L,
                                            id = document.id
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                gameResults.clear()
                                gameResults.addAll(firebaseResults)
                            }

                    } else {
                        gameResultViewModel.loadLocalGameResults()
                        gameResultViewModel.localGameResults.observe(this@MainActivity) { localResults ->
                            gameResults.clear()
                            gameResults.addAll(localResults.map { it.toGameResult() })
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    isUserLoggedIn = auth.currentUser != null
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        isUserLoggedIn = firebaseAuth.currentUser != null
                    }
                    auth.addAuthStateListener(listener)
                }

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainMenuScreen(
                            isUserLoggedIn = isUserLoggedIn,
                            userEmail = auth.currentUser?.email,
                            onNvgToLogin = { navController.navigate("login") },
                            onNvgToRegister = { navController.navigate("register") },
                            onNvgToPlayGame = { navController.navigate("playGame") },
                            onNvgToGameStats = { navController.navigate("gameStats") },
                            onLogout = {
                                auth.signOut()
                                isUserLoggedIn = false
                            })
                    }

                    composable("login") {
                        LoginScreen(onLogin = { email, password ->
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this@MainActivity) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context, "Welcome back!", Toast.LENGTH_SHORT
                                        ).show()
                                        isUserLoggedIn = true
                                        navController.popBackStack()
                                    } else {
                                        val message = when (val e = task.exception) {
                                            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                                            is FirebaseNetworkException -> "No internet connection"
                                            else -> e?.localizedMessage ?: "Login failed"
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                        }, onBack = { navController.popBackStack() })
                    }


                    composable("register") {
                        RegisterScreen(onRegister = { email, password ->
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this@MainActivity) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT)
                                            .show()
                                        isUserLoggedIn = true
                                        navController.popBackStack()
                                    } else {
                                        val message = when (val e = task.exception) {
                                            is FirebaseAuthUserCollisionException -> "Email already in use"
                                            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                                            is FirebaseNetworkException -> "No internet connection"
                                            else -> e?.localizedMessage ?: "Registration failed"
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                        }, onBack = { navController.popBackStack() })
                    }

                    composable("playGame") {
                        PlayGameScreen(
                            isUserLoggedIn = isUserLoggedIn,
                            context = context,
                            onSaveGameFirebase = { myPoints, oppPoints, result, gotBonus, gotYahtzee ->
                                val user = auth.currentUser
                                if (user != null) {
                                    val data = hashMapOf(
                                        "userId" to user.uid,
                                        "myPoints" to myPoints,
                                        "opponentPoints" to oppPoints,
                                        "result" to result,
                                        "gotBonus" to gotBonus,
                                        "gotYahtzee" to gotYahtzee,
                                        "timestamp" to Timestamp.now()
                                    )
                                    db.collection("yahtzeeResults").add(data)
                                }
                            },
                            onSaveGameLocal = { myPoints, oppPoints, result, gotBonus, gotYahtzee ->
                                gameResultViewModel.addLocalGameResult(
                                    myPoints, oppPoints, result, gotBonus, gotYahtzee
                                )
                            },
                            onBack = { navController.popBackStack() })
                    }

                    composable("gameStats") {
                        GameStatsScreen(
                            gameResults = gameResults,
                            isUserLoggedIn = isUserLoggedIn,
                            userEmail = auth.currentUser?.email,
                            onDeleteAll = {
                                scope.launch {
                                    if (isUserLoggedIn) {
                                        val userId = auth.currentUser?.uid ?: return@launch
                                        val snapshot = db.collection("yahtzeeResults")
                                            .whereEqualTo("userId", userId).get().await()

                                        val batch = db.batch()
                                        for (doc in snapshot.documents) {
                                            batch.delete(doc.reference)
                                        }
                                        batch.commit().await()
                                        gameResults.clear()
                                    } else {
                                        gameResultViewModel.clearAllLocalGameResults()
                                        gameResults.clear()
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

class GameResultViewModelFactory(private val application: Application) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return GameResultViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

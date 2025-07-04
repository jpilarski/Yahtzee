package com.example.yahtzeegame.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.yahtzeegame.R

@Composable
fun MainMenuScreen(
    isUserLoggedIn: Boolean,
    userEmail: String?,
    onNvgToLogin: () -> Unit,
    onNvgToRegister: () -> Unit,
    onNvgToPlayGame: () -> Unit,
    onNvgToGameStats: () -> Unit,
    onLogout: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val scrollState = rememberScrollState()

    if (isPortrait) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AppLogo()
            Spacer(modifier = Modifier.height(24.dp))
            MainButtons(
                isUserLoggedIn = isUserLoggedIn,
                userEmail = userEmail,
                onNvgToLogin = onNvgToLogin,
                onNvgToRegister = onNvgToRegister,
                onNvgToGameEntry = onNvgToPlayGame,
                onNvgToGameStats = onNvgToGameStats,
                onLogout = onLogout
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AppLogo(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MainButtons(
                    isUserLoggedIn = isUserLoggedIn,
                    userEmail = userEmail,
                    onNvgToLogin = onNvgToLogin,
                    onNvgToRegister = onNvgToRegister,
                    onNvgToGameEntry = onNvgToPlayGame,
                    onNvgToGameStats = onNvgToGameStats,
                    onLogout = onLogout,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "App logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}

@Composable
fun MainButtons(
    isUserLoggedIn: Boolean,
    userEmail: String?,
    onNvgToLogin: () -> Unit,
    onNvgToRegister: () -> Unit,
    onNvgToGameEntry: () -> Unit,
    onNvgToGameStats: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isUserLoggedIn && userEmail != null) {
            LoggedText(email = userEmail)
        }
        MyButton(
            text = "New game", onClick = onNvgToGameEntry, modifier = Modifier.fillMaxWidth(0.5f)
        )
        MyButton(
            text = "Statistics", onClick = onNvgToGameStats, modifier = Modifier.fillMaxWidth(0.5f)
        )
        if (isUserLoggedIn) {
            MyButton(
                text = "Log out", onClick = onLogout, modifier = Modifier.fillMaxWidth(0.5f)
            )
        } else {
            MyButton(
                text = "Log in", onClick = onNvgToLogin, modifier = Modifier.fillMaxWidth(0.5f)
            )
            MyButton(
                text = "Sign up", onClick = onNvgToRegister, modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    }
}

package com.calor.app.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.ui.theme.CoralPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = listOf(
        "Добро пожаловать в Calor!" to "Простой и уютный помощник для подсчёта калорий.",
        "Твой холодильник" to "Добавляй продукты с граммовками — приложение запомнит остатки.",
        "Списывай съеденное" to "Укажи граммы — Calor посчитает калории и покажет, сколько осталось на день.",
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var goal by remember { mutableStateOf("2000") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    pages[page].first,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    pages[page].second,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (page == 0) {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Дневная цель (ккал)") },
                        modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                    )
                }
            }
        }
        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    val kcal = goal.toIntOrNull() ?: 2000
                    viewModel.complete(kcal, onComplete)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
        ) {
            Text(if (pagerState.currentPage < pages.lastIndex) "Далее" else "Начать")
        }
    }
}

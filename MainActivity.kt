package com.example.schoolschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Сейчас") },
                    label = { Text("Сейчас") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Уроки") },
                    label = { Text("Уроки") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Звонки") },
                    label = { Text("Звонки") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> CurrentLessonScreen()
                1 -> ScheduleEditorScreen()
                2 -> BellTimesEditorScreen()
            }
        }
    }
}

@Composable
fun CurrentLessonScreen() {
    val today = LocalDate.now().dayOfWeek
    val now = LocalTime.now()
    val todaySchedule = defaultSchedule[today] ?: emptyList()

    // Логика поиска текущего или следующего урока
    var statusText = "Уроков нет / Выходной"
    var currentLessonName = ""
    var timeLeftText = ""

    if (todaySchedule.isNotEmpty()) {
        for (i in defaultBellTimes.indices) {
            if (i >= todaySchedule.size) break // если уроков меньше, чем звонков
            
            val start = LocalTime.parse(defaultBellTimes[i].first, DateTimeFormatter.ofPattern("HH:mm"))
            val end = LocalTime.parse(defaultBellTimes[i].second, DateTimeFormatter.ofPattern("HH:mm"))
            val nextStart = if (i + 1 < defaultBellTimes.size) LocalTime.parse(defaultBellTimes[i + 1].first, DateTimeFormatter.ofPattern("HH:mm")) else null

            if (now >= start && now <= end) {
                statusText = "Идет ${i + 1}-й урок"
                currentLessonName = todaySchedule[i]
                val minutesLeft = java.time.Duration.between(now, end).toMinutes() + 1
                timeLeftText = "До конца: $minutesLeft мин."
                break
            } else if (now > end && nextStart != null && now < nextStart) {
                statusText = "Перемена"
                currentLessonName = "Следующий: ${if (i + 1 < todaySchedule.size) todaySchedule[i + 1] else "Нет"}"
                val minutesLeft = java.time.Duration.between(now, nextStart).toMinutes() + 1
                timeLeftText = "До звонка: $minutesLeft мин."
                break
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Сегодня: ${getDayName(today)}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = statusText, fontSize = 20.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentLessonName, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = timeLeftText, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Расписание на сегодня:", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(todaySchedule.size) { index ->
                val isCurrent = statusText.contains("${index + 1}-й")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${index + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                        Text(text = defaultBellTimes.getOrNull(index)?.let { "${it.first} - ${it.second}" } ?: "", modifier = Modifier.width(110.dp), fontSize = 14.sp)
                        Text(text = todaySchedule[index], fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

// Заглушки для вкладок редактирования (чтобы код компилировался и работал)
@Composable
fun ScheduleEditorScreen() {
    Text("Редактор расписания (в разработке, пока использует данные по умолчанию)", modifier = Modifier.padding(16.dp))
}

@Composable
fun BellTimesEditorScreen() {
    Text("Редактор звонков (в разработке, пока использует данные по умолчанию)", modifier = Modifier.padding(16.dp))
}

fun getDayName(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "Понедельник"
        DayOfWeek.TUESDAY -> "Вторник"
        DayOfWeek.WEDNESDAY -> "Среда"
        DayOfWeek.THURSDAY -> "Четверг"
        DayOfWeek.FRIDAY -> "Пятница"
        DayOfWeek.SATURDAY -> "Суббота"
        DayOfWeek.SUNDAY -> "Воскресенье"
    }
}

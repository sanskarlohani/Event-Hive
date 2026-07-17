package com.sanskar.eventhive.Notification

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddScheduleScreen(
    initialText: String = "",
    onDateChange: (String) -> Unit = {},
    onTimeChange: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // -- internal state instead of reassigning a parameter --
    var scheduleText by remember { mutableStateOf(initialText) }
    var scheduleDate by remember {
        mutableStateOf(
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Calendar.getInstance().time)
        )
    }
    var scheduleTime by remember {
        mutableStateOf(
            "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:" +
                    Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0')
        )
    }

    // pickers
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().timeInMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true // or false for AM/PM
    )

    var showTimePicker by remember { mutableStateOf(false) }

    // notification permission
    val postNotificationPermission = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )
    LaunchedEffect(Unit) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = scheduleText,
            onValueChange = { scheduleText = it },
            label = { Text("Reminder text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { showDatePicker = true }) {
            Text("Date: $scheduleDate")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { showTimePicker = true }) {
            Text("Time: $scheduleTime")
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            // tell parent, if needed
            onDateChange(scheduleDate)
            onTimeChange(scheduleTime)
            ScheduleNotification().scheduleNotification(
                context,
                timePickerState,
                datePickerState,
                scheduleText
            )
        }, Modifier.fillMaxWidth()) {
            Text("Add reminder")
        }
    }

    // --- DatePickerDialog ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        scheduleDate = SimpleDateFormat(
                            "dd-MM-yyyy", Locale.getDefault()
                        ).format(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- TimePickerDialog ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    scheduleTime = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

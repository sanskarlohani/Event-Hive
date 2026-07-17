package com.sanskar.eventhive.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.College
import com.sanskar.eventhive.data.model.User
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.viewModel.AuthViewModel
import com.sanskar.eventhive.ui.viewModel.SignUpFormViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    signUpFormViewModel: SignUpFormViewModel = hiltViewModel(),
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var registrationNo by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var yearOfJoining by remember { mutableStateOf("") }
    var yearOfPassing by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedCollege by remember { mutableStateOf<College?>(null) }
    var collegeCode by remember { mutableStateOf("") }
    var collegeMenuExpanded by remember { mutableStateOf(false) }
    var courseMenuExpanded by remember { mutableStateOf(false) }

    val colleges by signUpFormViewModel.colleges.collectAsState()
    val signUpState by authViewModel.signUpState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is Resource.Loading -> snackbarHost.showSnackbar("Creating account...")
            is Resource.Error -> snackbarHost.showSnackbar(
                (signUpState as Resource.Error).exception.localizedMessage ?: "Sign-up failed",
            )
            is Resource.Success -> {
                navController.navigate(NavigationItem.Verify.route) {
                    popUpTo(NavigationItem.SignUp.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background,
                            Color(0xFFEAF3FF),
                        ),
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("College Email / Email") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            ExposedDropdownMenuBox(
                expanded = collegeMenuExpanded,
                onExpandedChange = { collegeMenuExpanded = !collegeMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedCollege?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("College (Listed only)") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.School, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                )
                DropdownMenu(
                    expanded = collegeMenuExpanded,
                    onDismissRequest = { collegeMenuExpanded = false },
                ) {
                    colleges.forEach { college ->
                        DropdownMenuItem(
                            text = { Text("${college.name} (${college.collegeCode})") },
                            onClick = {
                                selectedCollege = college
                                if (course.isNotBlank() && course !in college.listedCourses) {
                                    course = ""
                                }
                                collegeMenuExpanded = false
                            },
                        )
                    }
                }
            }

            val selected = selectedCollege
            val validDomain = selected?.emailDomain
                ?.let { email.lowercase().endsWith("@${it.lowercase()}") }
                ?: false

            if (!validDomain) {
                OutlinedTextField(
                    value = collegeCode,
                    onValueChange = { collegeCode = it.uppercase().take(6) },
                    label = { Text("College Code (Required if email domain doesn't match)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = registrationNo,
                onValueChange = { registrationNo = it },
                label = { Text("Registration No / Roll No") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            val availableCourses = selectedCollege?.listedCourses.orEmpty()
            if (availableCourses.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = courseMenuExpanded,
                    onExpandedChange = { courseMenuExpanded = !courseMenuExpanded },
                ) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course (Listed)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseMenuExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    DropdownMenu(
                        expanded = courseMenuExpanded,
                        onDismissRequest = { courseMenuExpanded = false },
                    ) {
                        availableCourses.forEach { courseName ->
                            DropdownMenuItem(
                                text = { Text("${selectedCollege?.name} - $courseName") },
                                onClick = {
                                    course = courseName
                                    courseMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course (BTech / MTech / ... )") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = yearOfJoining,
                onValueChange = { yearOfJoining = it.filter(Char::isDigit).take(4) },
                label = { Text("Year of Joining") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = yearOfPassing,
                onValueChange = { yearOfPassing = it.filter(Char::isDigit).take(4) },
                label = { Text("Year of Passing") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = {
                    val college = selectedCollege
                    if (fullName.isBlank() || email.isBlank() || registrationNo.isBlank() || course.isBlank()
                        || yearOfJoining.length != 4 || yearOfPassing.length != 4 || password.isBlank()
                        || confirmPassword.isBlank() || college == null
                    ) {
                        scope.launch { snackbarHost.showSnackbar("Fill all required fields") }
                        return@TextButton
                    }

                    if (password != confirmPassword) {
                        scope.launch { snackbarHost.showSnackbar("Password and confirm password must match") }
                        return@TextButton
                    }

                    val isDomainMatched = college.emailDomain
                        ?.let { email.lowercase().endsWith("@${it.lowercase()}") }
                        ?: false
                    if (!isDomainMatched && collegeCode.uppercase() != college.collegeCode.uppercase()) {
                        scope.launch { snackbarHost.showSnackbar("Invalid college code for selected college") }
                        return@TextButton
                    }

                    val user = User(
                        name = fullName.trim(),
                        email = email.trim(),
                        phone = phone.ifBlank { null },
                        sic = registrationNo.trim(),
                        registrationNo = registrationNo.trim(),
                        course = course.trim(),
                        yearOfJoining = yearOfJoining,
                        yearOfPassing = yearOfPassing,
                        collegeName = college.name,
                        collegeId = college.id,
                    )
                    authViewModel.signUp(user, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sign Up")
                }
            }

            TextButton(onClick = { navController.navigate(NavigationItem.Login.route) }) {
                Text("Already have an account? Log in")
            }
        }
    }
}

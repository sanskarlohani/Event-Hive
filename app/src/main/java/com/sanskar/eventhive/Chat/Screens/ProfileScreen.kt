package com.sanskar.eventhive.Chat.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sanskar.eventhive.Chat.CommonDivider
import com.sanskar.eventhive.Chat.CommonImage
import com.sanskar.eventhive.Chat.CommonProgressBar
import com.sanskar.eventhive.Chat.LCViewModel
import com.sanskar.eventhive.Chat.Navigation.DestinationScreen
import com.sanskar.eventhive.Chat.navigateTo

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel){

    val signIn = vm.signIn.value

    LaunchedEffect (signIn){
        if(!signIn){
            navController.navigate(DestinationScreen.Login.route) {
                popUpTo(DestinationScreen.ChatList.route) { inclusive = true }
            }
        }
    }

    val inProgress = vm.inProcess.value

    if(inProgress){
        CommonProgressBar()
    }else{
        val userData = vm.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name?: "")
        }

        var number by rememberSaveable {
            mutableStateOf(userData?.number?:"")
        }

        Column (modifier = Modifier.padding(16.dp)) {
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                vm = vm,
                name = name,
                number = number,
                onNameChange = {name = it},
                onNumberChange = {number = it},
                onSave = {
                    vm.CreateOrUpdateProfile(name,number)
                },
                onBack = {
                    navigateTo(navController,DestinationScreen.ChatList.route)
                },
                onLogout = {vm.signOut()}
            )
            BottomNavigationMenu(selectedItem = BottomNavigationItem.PROFILE, navController)
        }
    }


}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    vm: LCViewModel,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = vm.userData.value?.imageUrl

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section with Back and Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                modifier = Modifier.clickable { onBack() },
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Blue)
            )
            Text(
                text = "Save",
                modifier = Modifier.clickable { onSave() },
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Blue)
            )
        }

        // Divider
        CommonDivider()

        // Profile Image Section
        ProfileImage(imageUrl = imageUrl, vm = vm)

        // Divider
        CommonDivider()

        // Name Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        // Number Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Number",
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            TextField(
                value = number,
                onValueChange = onNumberChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        // Divider
        CommonDivider()

        // Logout Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Logout",
                modifier = Modifier.clickable { onLogout() },
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red)
            )
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?,vm: LCViewModel){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri->
        uri?.let {
            vm.uploadProfileImage(uri)
        }
    }

    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)){
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier.padding(8.dp).size(100.dp)
            ) {
                CommonImage(imageUrl)
            }
            Text("Change Profile Image")
        }

        if(vm.inProcess.value){
            CommonProgressBar()
        }

    }
}
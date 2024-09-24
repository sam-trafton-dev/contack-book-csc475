package dev.samtrafton.contactbook

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.samtrafton.contactbook.ui.theme.ContactBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactBookTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ContactBookApp()
                }
                }
            }
        }
    }

@Composable
fun ContactBookApp() {
    // Retrieve app context to create the model view from the factory design pattern
    val context: Context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(context))
    // mutable states for switching the UI state values
    var showDialog by remember { mutableStateOf(false) }
    var currentContact by remember { mutableStateOf<Contact?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        LazyColumn( // Container for list of contact items
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
                .padding(top = 32.dp)
        ) { // Jetpack Compose only observes new instances so mutating the same object will not trigger a state observation
            items(contactViewModel.contacts) {contact ->
                ContactItem(
                    contact = contact,
                    onEdit = {
                        currentContact = contact
                        showDialog = true
                    },
                    onDelete = {contactViewModel.deleteContact(contact)}
                )
            }
        }

        Button(
            onClick = {showDialog = true},
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(0.dp,0.dp,0.dp,20.dp)
        ) {
            Text("Add Contact")
        }
    }

    if (showDialog) {
        AddEditContactDialog(contact = currentContact, onDismiss = { showDialog = false }) { contact ->
            if (currentContact == null) {
                contactViewModel.addContact(contact)
            } else {
                contactViewModel.editContact(contact)
            }
            showDialog = false
            currentContact = null
        }
    }
}

// The UI component that populates the list with contact information
@Composable
fun ContactItem(contact: Contact, onEdit: () -> Unit, onDelete: () -> Unit) {

    val colorScheme = MaterialTheme.colorScheme
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
        colors = CardColors(
            containerColor = colorScheme.inversePrimary,
            contentColor = colorScheme.onSurface,
            disabledContentColor = Color.LightGray,
            disabledContainerColor =Color.LightGray
        )

    ) {
        Row(modifier = Modifier
            .fillMaxWidth().padding(2.dp)
            .border(border = BorderStroke(2.dp, color = colorScheme.onPrimaryContainer) , shape = RoundedCornerShape(10))
            , horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = contact.name, modifier = Modifier
                    .padding(start = 24.dp))
                Text(text = contact.phoneNumber, modifier = Modifier
                    .padding(start = 24.dp))
                Text(text = contact.company, modifier = Modifier
                    .padding(start = 24.dp))
            }
            Row {
                IconButton(onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    ),
                    content = { Icon(Icons.Filled.Edit, contentDescription = null)
                    }, modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(top= 10.dp)
                )
                IconButton(onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    ),
                    content = { Icon(Icons.Filled.Delete, contentDescription = null)
                    }, modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(top= 10.dp)
                )
            }
        }

    }
}

@Composable
fun AddEditContactDialog(contact: Contact?, onDismiss: () -> Unit, onSave: (Contact) -> Unit) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var company by remember { mutableStateOf(contact?.company ?: "") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = if (contact == null) "Add Contact" else "Edit Contact") },
        text = {
            Column {
                Text(text = "Name")
                Text(text = "Phone Number")
                Text(text = "Company")
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(value = name, onValueChange = { name = it }, modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End)
                    .background(Color.LightGray)
                    )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End)
                    .background(Color.LightGray)
                    )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(value = company, onValueChange = { company = it}, modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End)
                    .background(Color.LightGray)
                    )
            }
        },
        confirmButton = {
            Button(onClick = {
                val id = contact?.id ?: (0..1000).random() // Create a random ID if it's a new contact
                // If we are using a remote database the object ID keys can be generated by the RDMS
                onSave(Contact(id, name, phoneNumber, company))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ContactBookTheme {
        ContactBookApp()
    }
}
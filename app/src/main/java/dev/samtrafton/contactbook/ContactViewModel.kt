package dev.samtrafton.contactbook
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ContactViewModel(private val context: Context) : ViewModel() {
    var contacts = mutableStateListOf<Contact>()
        private set

    private val fileName = "contacts.json"

    init {
        loadContacts()
        Log.d("flowcheck", "init of view model")
    }


    fun addContact(contact: Contact) {
        contacts.add(contact)
        saveContacts()
    }

    fun deleteContact(contact: Contact) {
        contacts.remove(contact)
        saveContacts()
    }

    fun editContact(updatedContact: Contact) {
        val index = contacts.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            contacts[index] = updatedContact
            saveContacts()
        }
    }

    private fun saveContacts() {
        try {
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            // Serialize the contact list
            val json = Json.encodeToString(contacts.toList())
            fileOutputStream.write(json.toByteArray())
            fileOutputStream.close()
            // adding my own tag "flowcheck" to filter out unwanted debug statements in cat log
            Log.d("flowcheck", "Saving contacts")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadContacts() {
        try {
            val file = File(context.filesDir, fileName)

            // check if data file exists
            if (file.exists() && file.length() > 0) {
                val fileInputStream = context.openFileInput(fileName)
                val size = fileInputStream.available()
                val buffer = ByteArray(size)
                fileInputStream.read(buffer)
                fileInputStream.close()

                val json = String(buffer)

                // Deserialization
                val savedContacts: List<Contact> = Json.decodeFromString(json)
                contacts.clear()
                contacts.addAll(savedContacts)

                Log.d("flowcheck", "Loading contacts")
            } else {
                // handle empty file case
                contacts = mutableStateListOf()
                Log.d("flowcheck","Making new json file")
            }
        } catch (e: FileNotFoundException) {
            contacts = mutableStateListOf()
            Log.e("loadContacts", "File not found, initializing empty contact list.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("loadContacts", "IOException occurred during file read.")
        } catch (e: SerializationException) {
            contacts = mutableStateListOf()
            Log.e("loadContacts", "Error decoding JSON, initializing empty contact list. JSON might be corrupted.")
        } catch (e: Exception) {
            e.printStackTrace() // Catch any other unforeseen errors
            Log.e("loadContacts", "Unexpected error occurred: ${e.message}")
        }
    }
}
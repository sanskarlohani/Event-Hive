package com.sanskar.eventhive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Note(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = ""
)

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("notes")

    fun getNotes(): Flow<List<Note>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val notes = snapshot?.toObjects(Note::class.java) ?: emptyList()
            trySend(notes)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addNote(note: Note) {
        collection.add(note).await()
    }

    suspend fun updateNote(note: Note) {
        collection.document(note.id).set(note).await()
    }

    suspend fun deleteNote(id: String) {
        collection.document(id).delete().await()
    }
}

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    // Expose notes as StateFlow
    val notes: StateFlow<List<Note>> = repository.getNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )


    private val _isOperationLoading = MutableStateFlow(false)
    val isOperationLoading: StateFlow<Boolean> = _isOperationLoading

    fun add(note: Note) = viewModelScope.launch {
        repository.addNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        _isOperationLoading.value = true
        try {
            repository.updateNote(note)
        } finally {
            _isOperationLoading.value = false
        }
    }

    fun delete(id: String) = viewModelScope.launch {
        _isOperationLoading.value = true
        try {
            repository.deleteNote(id)
        } finally {
            _isOperationLoading.value = false
        }
    }
}


@Composable
fun NoteScreen(viewModel: NoteViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isOperationLoading.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.add(Note(title = title, content = content))
                    title = ""
                    content = ""
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Note")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        isLoading = isLoading,
                        onUpdate = { viewModel.update(it) },
                        onDelete = { viewModel.delete(it.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Global loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    isLoading: Boolean,
    onUpdate: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Button(
                onClick = { onUpdate(note.copy(title = title, content = content)) },
                enabled = !isLoading
            ) {
                Text("Update")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onDelete(note) },
                enabled = !isLoading
            ) {
                Text("Delete")
            }
        }
    }
}
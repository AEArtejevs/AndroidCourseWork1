package com.example.coursework

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.coursework.database.Note
import com.example.coursework.database.NoteDatabase
import kotlinx.coroutines.*


class AddNoteActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use your existing note editor layout
        setContentView(R.layout.fragment_add)

        val backButton = findViewById<android.widget.ImageButton>(R.id.btnBackFromAddNote)
        titleInput = findViewById(R.id.editTextHeaderTitle)
        contentInput = findViewById(R.id.editTextContent)
        saveButton = findViewById(R.id.buttonSave)

        val db = NoteDatabase.getDatabase(this)
        val noteDao = db.noteDao()

        backButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()

            if (title.isNotEmpty() || content.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    noteDao.insert(Note(title = title, content = content))
                    withContext(Dispatchers.Main) {
                        finish() // go back after saving
                    }
                }
            } else {
                finish() // If both fields are empty, just go back without saving
            }
        }
    }
}

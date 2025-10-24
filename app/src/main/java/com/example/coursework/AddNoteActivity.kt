package com.example.coursework

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.coursework.database.Note
import com.example.coursework.database.NoteDatabase
import jp.wasabeef.richeditor.RichEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddNoteActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
//    private lateinit var contentInput: EditText
    private lateinit var editor: RichEditor

    private lateinit var saveButton: Button
    private var currentNoteId: Int? = null
    val db = NoteDatabase.getDatabase(this)
    val noteDao = db.noteDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use your existing note editor layout
        setContentView(R.layout.fragment_add)

        val backButton = findViewById<android.widget.ImageButton>(R.id.btnBackFromAddNote)
        titleInput = findViewById(R.id.editTextHeaderTitle)
        editor = findViewById(R.id.editor)
        saveButton = findViewById(R.id.buttonSave)

        // RichEditor setup
        editor.setEditorFontSize(16)
        editor.setPadding(10, 10, 10, 10)
        editor.setPlaceholder("Write your note here...")

        currentNoteId = intent.getIntExtra("note_id", -1).takeIf { it != -1 }

        if (currentNoteId != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val note = noteDao.getNoteById(currentNoteId!!)
                note?.let {
                    withContext(Dispatchers.Main) {
                        titleInput.setText(it.title)
                        editor.html = it.content
                    }
                }
            }
        }
        // Toolbar actions buttons
        findViewById<ImageButton>(R.id.buttonBold)?.setOnClickListener { editor.setBold() }
        findViewById<ImageButton>(R.id.buttonItalic)?.setOnClickListener { editor.setItalic() }
        findViewById<ImageButton>(R.id.buttonList)?.setOnClickListener { editor.setBullets() }

        backButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val contentHtml = editor.html ?: "" // Save HTML format

            lifecycleScope.launch(Dispatchers.IO) {
                if (currentNoteId != null) {
                    // Update existing note
                    noteDao.update(
                        Note(
                            id = currentNoteId!!,
                            title = title,
                            content = contentHtml,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Insert new note
                    noteDao.insert(Note(title = title, content = contentHtml))
                }

                withContext(Dispatchers.Main) { finish() }
            }
        }
    }
}

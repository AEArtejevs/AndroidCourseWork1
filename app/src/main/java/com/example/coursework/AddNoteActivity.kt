package com.example.coursework

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use your existing note editor layout
        setContentView(R.layout.fragment_add)
        val db = NoteDatabase.getDatabase(this)
        val noteDao = db.noteDao()
        val backButton = findViewById<android.widget.ImageButton>(R.id.btnBackFromAddNote)
        titleInput = findViewById(R.id.editTextHeaderTitle)
        editor = findViewById(R.id.editor)
        saveButton = findViewById(R.id.buttonSave)

        // Toolbar actions buttons
        val buttonBold = findViewById<ImageButton>(R.id.buttonBold)
        val buttonItalic = findViewById<ImageButton>(R.id.buttonItalic)
        val buttonList = findViewById<ImageButton>(R.id.buttonList)

        buttonBold.setOnClickListener { editor.setBold() }
        buttonItalic.setOnClickListener { editor.setItalic() }
        buttonList.setOnClickListener { editor.setBullets() }

        // --- tint icons based on dark mode ---
        val formatButtons = listOf(buttonBold, buttonItalic, buttonList)
        val isDarkMode = isNightModeActive(this)
        val tintColor = if (isDarkMode) Color.WHITE else Color.BLACK
        formatButtons.forEach { it.setColorFilter(tintColor) }

        //  editor setup
        if (isDarkMode) {
            editor.setEditorBackgroundColor("#1E1E1E".toColorInt())
            editor.setEditorFontColor(Color.WHITE)
        } else {
            editor.setEditorBackgroundColor(Color.WHITE)
            editor.setEditorFontColor(Color.BLACK)
        }

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
    private fun isNightModeActive(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

}

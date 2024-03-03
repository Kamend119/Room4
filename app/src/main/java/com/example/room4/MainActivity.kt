package com.example.room4

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val db = Room.databaseBuilder(
                LocalContext.current,
                AppDataBase::class.java, "AppDataBase"
            ).allowMainThreadQueries().build()

            MainScreen(db)
        }
    }
}

enum class Input{
    Rus, Eng
}

@Composable
fun MainScreen(db: AppDataBase) {

    var showAddDialog by remember { mutableStateOf(false) }
    var lang by remember { mutableStateOf(Input.Eng) }
    var word by remember { mutableStateOf("") }
    var findWords = remember { mutableListOf<Glossary>() }
    var wordEng by remember { mutableStateOf("") }
    var wordRus by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    Scaffold (
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddDialog = true
                wordEng = ""
                wordRus = ""
            }, shape = CircleShape
            ){
                Icon(Icons.Default.Add, "")
            }
        }
    ){pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            Text(
                "Словарик",
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { if (lang == Input.Rus) Text("Rus -> Eng") else Text("Eng -> Rus") },
                    enabled = !showAddDialog
                )

                Switch(
                    checked = (lang == Input.Eng),
                    onCheckedChange = { isChecked ->
                        lang = if (isChecked) Input.Eng else Input.Rus
                    },
                    modifier = Modifier.padding(8.dp),
                    enabled = !showAddDialog
                )
            }

            LazyColumn {
                if (word != "") {
                    findWords = if (lang == Input.Eng) db.glossaryDao().getEngWords(word)
                    else db.glossaryDao().getRusWords(word)
                }
                items(findWords.size){
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .border(2.dp, Color.Black)
                            .fillMaxWidth()
                    ){
                        if (lang == Input.Eng){
                            Text("${findWords[it].eng} - ${findWords[it].rus}",
                                fontSize = 40.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        else{
                            Text("${findWords[it].rus} - ${findWords[it].eng}",
                                fontSize = 40.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Column(
                Modifier
                    .border(2.dp, Color.Black)
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Добавить слово")

                OutlinedTextField(
                    value = wordEng,
                    onValueChange = {wordEng = it},
                    label = { Text("Английское слово") }
                )

                OutlinedTextField(
                    value = wordRus,
                    onValueChange = {wordRus = it},
                    label = { Text("Русское слово") }
                )

                Button(onClick = {
                    if (wordEng != "" && wordRus != "")
                    {
                        val existingWords = db.glossaryDao().getEngWords(wordEng)

                        if (existingWords.isNotEmpty()) {
                            existingWords[0].rus = wordRus
                            db.glossaryDao().insertGlossary(existingWords[0])
                            showAddDialog = false
                        } else {
                            db.glossaryDao().insertGlossary(Glossary(wordEng, wordRus))
                            showAddDialog = false
                        }
                    }
                    else Toast.makeText( ctx ,"Слова не должны быть пустыми",Toast.LENGTH_SHORT).show()
                }) {
                    Text("Добавить")
                }

            }
        }
    }
}

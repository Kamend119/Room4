package com.example.room4

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "glossary")
class Glossary(
    var eng: String,
    var rus: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
interface GlossaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGlossary(glossary: Glossary)

    @Query(" SELECT * FROM glossary WHERE eng like :engWords || '%' ")
    fun getEngWords(engWords : String) : MutableList<Glossary>

    @Query(" SELECT * FROM glossary WHERE rus like :rusWords || '%' ")
    fun getRusWords(rusWords : String) : MutableList<Glossary>
}

@Database(
    entities = [Glossary::class],
    version = 1
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun glossaryDao(): GlossaryDao
}

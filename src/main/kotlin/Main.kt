package dev.bltucker

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Genres : IntIdTable(){
    val name = varchar("name", 255).uniqueIndex()
    val description = varchar("description", 255).nullable()
}

fun main() {
    println("Hello Exposed!")

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Genres)

        //CREATE
        val fantasyGenreId = Genres.insert {
            it[name] = "Fantasy"
            it[description] = "Magic, Sorcery, Elves and Stuff"
        } get Genres.id

        //Note the lack of a description here. We allow it to be nullable so that's ok!
        val scienceFictionGenreId = Genres.insert {
            it[name] = "Science Fiction"
        } get Genres.id

        println("Inserted Fantasy with ID: $fantasyGenreId")
        println("Inserted Science Fiction with ID: $scienceFictionGenreId")

        //READ
        val allGenres: Query = Genres.selectAll()

        allGenres.forEach{
            println("${it[Genres.id]}: ${it[Genres.name]} - ${it[Genres.description]}")
        }

        val fantasy = Genres.select(Genres.name, Genres.description).where { Genres.name eq "Fantasy" }.single()
        println("Fantasy genre: ${fantasy[Genres.name]} - ${fantasy[Genres.description]}")


        //UPDATE
        Genres.update({ Genres.id eq scienceFictionGenreId }) {
            it[description] = "A genre of speculative fiction dealing with imaginative and futuristic concepts"
        }

        val updatedSciFi = Genres.select(Genres.name, Genres.description).where { Genres.id eq scienceFictionGenreId }.single()
        println("Updated Sci-Fi: ${updatedSciFi[Genres.name]} - ${updatedSciFi[Genres.description]}")


        //DELETE
        val knitting: EntityID<Int> = Genres.insert {
            it[name] = "Cyberpunk Knitting"
            it[description] = "A genre doomed to never catch on"
        } get Genres.id

        val genreCountBefore = Genres.selectAll().count()
        println("There are $genreCountBefore genres")

        Genres.deleteWhere { Genres.id eq knitting }

        val genreCountAfter = Genres.selectAll().count()
        println("There are $genreCountAfter genres")

    }

}
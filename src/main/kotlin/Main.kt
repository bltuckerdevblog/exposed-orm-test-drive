package dev.bltucker

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.print.Book
import java.math.BigDecimal


fun main() {
    println("Hello Exposed!")

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        // Create tables
        SchemaUtils.create(Books, Authors, Genres, Users, UserBooks)


        val fantasyId = Genres.insert {
            it[name] = "Fantasy"
            it[description] = "Fantasy literature"
        } get Genres.id

        val authorId = Authors.insert {
            it[name] = "J.R.R. Tolkien"
            it[biography] = "English writer and philologist"
        } get Authors.id

        val bookId = Books.insert {
            it[title] = "The Lord of the Rings"
            it[isbn] = "9780618640157"
            it[price] = BigDecimal("29.99")
            it[publishDate] = LocalDate(1954, 7, 29)
            it[this.authorId] = authorId
            it[genreId] = fantasyId
            it[metadata] = BookMetadata(
                tags = listOf("awesome", "classic"),
                editions = listOf(),
                translations = listOf()
            )
        } get Books.id

        val book = Books.selectAll().where { Books.id eq bookId }.single()
        val metaData = book[Books.metadata]

        println("Book Meta Data: $metaData")
    }
}


private fun getCurrentDate(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
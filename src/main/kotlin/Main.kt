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
        } get Books.id

        val userId = Users.insert {
            it[username] = "bookworm"
            it[email] = "bookworm@example.com"
            it[registrationDate] = getCurrentDate()
        } get Users.id

        UserBooks.insert {
            it[this.userId] = userId
            it[this.bookId] = bookId
            it[purchaseDate] = getCurrentDate()
        }

        //Get all books with their authors and genres
        val booksWithDetails = (Books innerJoin Authors innerJoin Genres)
            .select(Books.title, Authors.name, Genres.name, Books.price)


        println("Books with details:")
        booksWithDetails.forEach {
            println("${it[Books.title]} by ${it[Authors.name]} (${it[Genres.name]}) - $${it[Books.price]}")
        }

        //Find all books purchased by a specific user
        val userPurchases = (UserBooks innerJoin Books innerJoin Users)
            .select(Users.username, Books.title, UserBooks.purchaseDate)
            .where { Users.id eq userId }

        println("\nBooks purchased by user:")
        userPurchases.forEach {
            println("${it[Users.username]} bought ${it[Books.title]} on ${it[UserBooks.purchaseDate]}")
        }

        //Get average book price
        val avgPrice = Books.select(Books.price.avg()).single()[Books.price.avg()]
        println("\nAverage book price: $${avgPrice?.setScale(2)}")

        //Delete a user (cascading delete will remove their purchases)
        Users.deleteWhere { Users.id eq userId }

        val remainingUsers = Users.selectAll().count()
        println("\nRemaining users after deletion: $remainingUsers")


    }
}


private fun getCurrentDate(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
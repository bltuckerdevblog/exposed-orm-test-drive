package dev.bltucker

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Books : IntIdTable() {
    val title = varchar("title", 255)
    val isbn = varchar("isbn", 13).uniqueIndex()
    val price = decimal("price", precision = 10, scale = 2)
    val publishDate = date("publish_date")
    val authorId = reference("author_id", Authors)
    val genreId = reference("genre_id", Genres)
}

object Authors : IntIdTable() {
    val name = varchar("name", 100)
    val biography = text("biography").nullable()
}

object Genres : IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description").nullable()
}

object Users : IntIdTable() {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val registrationDate = date("registration_date")
}

object UserBooks : IntIdTable() {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val bookId = reference("book_id", Books, onDelete = ReferenceOption.CASCADE)
    val purchaseDate = date("purchase_date")
}
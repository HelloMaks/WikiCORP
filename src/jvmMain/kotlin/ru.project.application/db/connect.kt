package ru.project.application.db

import access.User
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import data.WikiPage
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

val client: MongoClient = KMongo.createClient("mongodb://127.0.0.1:27017")
val mongoDB: MongoDatabase = client.getDatabase("wiki") // База данных MongoDB

val users = mongoDB.getCollection<User>() // MongoDB-коллекция пользователей
val pages = mongoDB.getCollection<WikiPage>() // MongoDB-коллекция вики-страниц
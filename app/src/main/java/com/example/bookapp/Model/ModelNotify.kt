package com.example.bookapp.Model

class ModelNotify {
    var uid:String = ""
    var id:String = ""
    var bookId:String = ""
    var url:String = ""
    var titleChapter:String = ""
    var titleBook:String = ""
    var timestamp:String = ""

    constructor()
    constructor(
        uid: String,
        id: String,
        bookId: String,
        url: String,
        titleChapter: String,
        titleBook: String,
        timestamp: String
    ) {
        this.uid = uid
        this.id = id
        this.bookId = bookId
        this.url = url
        this.titleChapter = titleChapter
        this.titleBook = titleBook
        this.timestamp = timestamp
    }


}
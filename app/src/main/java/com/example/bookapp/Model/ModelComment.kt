package com.example.bookapp.Model

class ModelComment {
    var id:String = ""
    var uid:String = ""
    var comment:String = ""
    var timestamp:String = ""
    var bookId:String = ""

    constructor()

    constructor(id: String, uid: String, comment: String, timestamp: String, bookId: String) {
        this.id = id
        this.uid = uid
        this.comment = comment
        this.timestamp = timestamp
        this.bookId = bookId
    }


}
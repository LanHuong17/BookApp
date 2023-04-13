package com.example.bookapp.Model

class ModelChapter {
    var bookId: String = ""
    var id:String = ""
    var timestamp:String = ""
//    var timestamp:Long = 0
    var titleBook:String = ""
    var titleChapter:String = ""
    var uid:String = ""
    var url:String = ""
    var viewCount:Int = 0
    var downloadCount: Int = 0

    constructor()
    constructor(
        bookId: String,
        id: String,
//        timestamp: Long,
        timestamp: String,
        titleBook: String,
        titleChapter: String,
        uid: String,
        url: String,
        viewCount: Int,
        downloadCount: Int
    ) {
        this.bookId = bookId
        this.id = id
        this.timestamp = timestamp
        this.titleBook = titleBook
        this.titleChapter = titleChapter
        this.uid = uid
        this.url = url
        this.viewCount = viewCount
        this.downloadCount = downloadCount
    }


}
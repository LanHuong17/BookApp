package com.example.bookapp.Model

class ModelBook {
    var categoryId:String= ""
    var description:String =""
    var id:String = ""
    var timestamp:String = ""
    var title:String = ""
    var uid:String = ""
    var url:String = ""
    var viewCount:Int = 0
    var downloadCount:Int = 0
    var isFavorite = false

    constructor()
    constructor(
        categoryId: String,
        description: String,
        id: String,
        timestamp: String,
        title: String,
        uid: String,
        url: String,
        viewCount: Int,
        downloadCount: Int,
        isFavorite: Boolean
    ) {
        this.categoryId = categoryId
        this.description = description
        this.id = id
        this.timestamp = timestamp
        this.title = title
        this.uid = uid
        this.url = url
        this.viewCount = viewCount
        this.downloadCount = downloadCount
        this.isFavorite = isFavorite
    }


}
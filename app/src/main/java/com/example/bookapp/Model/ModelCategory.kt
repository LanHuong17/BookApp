package com.example.bookapp.Model

class ModelCategory {
    var id:String = ""
    var category:String = ""
    var timestamp:Long = 0
    var uid:String = ""

    constructor() //required in firebase

    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }


}
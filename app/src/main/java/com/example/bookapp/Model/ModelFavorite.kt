package com.example.bookapp.Model

class ModelFavorite {
    var userType:String = ""
    var uid:String = ""
    var timestamp:String = ""
    var profileImage:String = ""
    var name:String = ""
    var email:String = ""

    constructor()
    constructor(
        userType: String,
        uid: String,
        timestamp: String,
        profileImage: String,
        name: String,
        email: String
    ) {
        this.userType = userType
        this.uid = uid
        this.timestamp = timestamp
        this.profileImage = profileImage
        this.name = name
        this.email = email
    }


}

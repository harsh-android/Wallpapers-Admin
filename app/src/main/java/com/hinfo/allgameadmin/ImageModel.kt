package com.hinfo.allgameadmin

class ImageModel {

    lateinit var key:String
    lateinit var category:String
    lateinit var image:String

    constructor()
    constructor(key: String, category: String, image: String) {
        this.key = key
        this.category = category
        this.image = image
    }
}
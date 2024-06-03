package com.example.cameramap

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class PharmacyDeserializer : JsonDeserializer<Pharmacy> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Pharmacy {
        val jsonObject = json.asJsonObject

        val name = jsonObject.get("name").asString
        val latitude = jsonObject.get("latitude").asDouble
        val longitude = jsonObject.get("longitude").asDouble
        val day = jsonObject.get("day")?.asString?.takeIf { it.isNotBlank() } ?: "정보 없음"
        val number = jsonObject.get("number")?.asString?.takeIf { it.isNotBlank() } ?: "정보 없음"
        val time = jsonObject.get("time")?.asString?.takeIf { it.isNotBlank() } ?: "정보 없음"

        return Pharmacy(name, latitude, longitude, day, number, time)
    }
}
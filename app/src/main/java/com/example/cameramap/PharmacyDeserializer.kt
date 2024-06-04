package com.example.cameramap

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class PharmacyDeserializer : JsonDeserializer<Pharmacy> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Pharmacy {
        val jsonObject = json?.asJsonObject ?: JsonObject()
        return Pharmacy(
            name = jsonObject.get("name")?.asString ?: "",
            latitude = jsonObject.get("latitude")?.asDouble ?: 0.0,
            longitude = jsonObject.get("longitude")?.asDouble ?: 0.0,
            day = jsonObject.get("day")?.asString ?: "",
            number = jsonObject.get("number")?.asString ?: "",
            time = jsonObject.get("time")?.asString ?: "",
            road_name_address = jsonObject.get("road_name_address")?.asString ?: "",
            local_address = jsonObject.get("local_address")?.asString ?: "",
        )
    }
}
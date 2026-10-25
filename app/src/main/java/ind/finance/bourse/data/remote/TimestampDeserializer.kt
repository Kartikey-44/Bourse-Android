package ind.finance.bourse.data.remote

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TimestampDeserializer : JsonDeserializer<String> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: com.google.gson.JsonDeserializationContext
    ): String {

        // Normal JSON string
        if (json.isJsonPrimitive) {
            return json.asString
        }

        // Java LocalDateTime serialized by Jackson as:
        // [2026, 10, 25, 21, 30, 25]
        if (json.isJsonArray) {

            val array = json.asJsonArray

            if (array.size() < 3) {
                return ""
            }

            val year = array[0].asInt
            val month = array[1].asInt
            val day = array[2].asInt

            val hour =
                if (array.size() > 3) array[3].asInt else 0

            val minute =
                if (array.size() > 4) array[4].asInt else 0

            val second =
                if (array.size() > 5) array[5].asInt else 0

            return String.format(
                "%04d-%02d-%02dT%02d:%02d:%02d",
                year,
                month,
                day,
                hour,
                minute,
                second
            )
        }

        return ""
    }
}
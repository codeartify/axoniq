package ch.fitnesslab.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Suppress("UNCHECKED_CAST")
@Component
class JsonLoader {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun loadJsonFromFile(path: String): String =
        Files.readString(
            Paths.get(
                javaClass.classLoader.getResource(path)?.toURI()
                    ?: throw IllegalArgumentException("File $path not found"),
            ),
        )

    fun <T> loadObjectFromFile(path: String, type: Class<T>): T = objectMapper.readValue(loadJsonFromFile(path), type)

    fun <T> loadObjectFromFileAndReplace(path: String, type: Class<T>, keyToReplace: String, valueToReplace: String): T {
        val content = loadJsonAndReplace(path, keyToReplace, valueToReplace)
        return objectMapper.readValue(content, type)
    }

    fun loadJsonAndReplaceId(filePathWithExtension: String, valueToAttach: String?): String {
        if (valueToAttach?.isEmpty()!!) {
            return loadJsonFromFile(filePathWithExtension)
        }

        return loadJsonAndReplace(filePathWithExtension, "id", valueToAttach)
    }

    private fun loadJsonAndReplace(filePathWithExtension: String, KeyOfValueToReplace: String, valueToReplace: String): String {
        val mapper = jacksonObjectMapper()
        val jsonMap: MutableMap<String, Any> = mapper.readValue(loadJsonFromFile(filePathWithExtension))
        jsonMap[KeyOfValueToReplace] = valueToReplace
        return mapper.writeValueAsString(jsonMap)
    }

    fun <T> loadObjectFromFileAndReplace(path: String, type: Class<T>, replacements: Map<String, String>): T {
        val mapper = jacksonObjectMapper()
        val jsonMap: MutableMap<String, Any> = mapper.readValue(loadJsonFromFile(path))

        replacements.forEach { (key, value) ->
            val keys = key.split(".")
            var currentMap: MutableMap<String, Any> = jsonMap

            for (i in 0 until keys.size - 1) {
                val nestedKey = keys[i]
                currentMap = currentMap.getOrPut(nestedKey) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
            }

            currentMap[keys.last()] = value
        }

        return objectMapper.readValue(mapper.writeValueAsString(jsonMap), type)
    }
}

package com.dertyp7214.applicationmanager.helpers

import com.dertyp7214.applicationmanager.props.Application
import org.json.JSONArray
import org.json.JSONObject

class JsonParser {
    companion object {
        private fun JSONArray.forEach(unit: (obj: JSONObject) -> Unit) {
            for (i in 0 until length())
                unit(this.getJSONObject(i))
        }

        fun applicationToJSON(applications: List<Application>): JSONArray {
            val array = JSONArray()
            applications.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("version", it.version)
                obj.put("author", it.author)
                obj.put("packageName", it.packageName)
                obj.put("description", it.description)
                obj.put("descriptionShort", it.descriptionShort)
                obj.put("latestChanges", it.latestChanges)
                obj.put("latestApk", it.latestApk)
                obj.put("latestUpdate", it.latestUpdate)
                obj.put("divider", it.divider)
                array.put(obj)
            }
            return array
        }

        fun JSONtoApplication(array: JSONArray): List<Application> {
            val list = ArrayList<Application>()
            array.forEach {
                val id = it.getInt("id")
                val name = it.getString("name")
                val version = it.getString("version")
                val author = it.getString("author")
                val packageName = it.getString("packageName")
                val description = it.getString("description")
                val descriptionShort = it.getString("descriptionShort")
                val latestChanges = it.getString("latestChanges")
                val latestApk = it.getString("latestApk")
                val latestUpdate = it.getString("latestUpdate")
                val divider = it.getBoolean("divider")
                list.add(
                    Application(
                        id,
                        name,
                        version,
                        author,
                        packageName,
                        description,
                        descriptionShort,
                        latestChanges,
                        latestApk,
                        latestUpdate,
                        divider
                    )
                )
            }
            return list
        }
    }
}
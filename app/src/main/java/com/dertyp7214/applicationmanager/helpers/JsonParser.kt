/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

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
                array.put(it.getData())
            }
            return array
        }

        fun jsonToApplication(array: JSONArray): List<Application> {
            val list = ArrayList<Application>()
            array.forEach {
                list.add(Application.setData(it))
            }
            return list
        }
    }
}
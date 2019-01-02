package com.dertyp7214.applicationmanager.github

class Application(
    val id: Int,
    val name: String,
    val version: String,
    val packageName: String,
    val description: String,
    val latestChanges: String,
    val latestApk: String,
    val latestUpdate: String
) {
    override fun toString(): String {
        return "id: $id\t" +
                "name: $name\t" +
                "version: $version\t" +
                "packageName: $packageName\t" +
                "description: $description\t" +
                "latestChanges: $latestChanges\t" +
                "latestApk: $latestApk\t" +
                "latestUpdate: $latestUpdate"
    }
}

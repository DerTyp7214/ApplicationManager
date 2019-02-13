package com.dertyp7214.applicationmanager.core

import android.content.Context
import com.dertyp7214.applicationmanager.helpers.Api
import com.dertyp7214.applicationmanager.props.Application
import com.dertyp7214.applicationmanager.thirdparty.Repo3
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

fun List<Repo3>.loadApplications(context: Context): List<Application> {
    return map {
        val renderer = HtmlRenderer.builder().build()
        val parser = Parser.builder().build()
        val release = Api(context).releases(it.name, true, it.host).first()
        val id = it.id
        val name = it.name
        val version = release.tag_name
        val author = it.repoProp.author
        val packageName = it.repoProp.id
        val description =
            "https://raw.githubusercontent.com/${it.host}/${it.name}/${if (it.description.isEmpty()) "master" else it.description}/README.md"
        val descriptionShort = it.repoProp.description
        val latestChanges = renderer.render(parser.parse(release.body))
        val latestApk = release.asset
        val latestUpdate = it.parsedUpdatedAt()
        val zipUrl =
            "${Api.github}/${it.host}/$name/zipball/${if (it.description.isEmpty()) "master" else it.description}"
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
            zipUrl
        )
    }
}
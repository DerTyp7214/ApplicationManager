package com.dertyp7214.applicationmanager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.activity_main.*
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.SpannableFactoryDef
import ru.noties.markwon.html.impl.HtmlEmptyTagReplacement
import ru.noties.markwon.html.impl.MarkwonHtmlParserImpl
import ru.noties.markwon.il.AsyncDrawableLoader

class MainActivity : AppCompatActivity() {

    private lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        api = Api(this)

        val config = SpannableConfiguration.builder(this)
            .factory(SpannableFactoryDef())
            .softBreakAddsNewLine(true)
            .htmlParser(MarkwonHtmlParserImpl.create(HtmlEmptyTagReplacement.create()))
            .asyncDrawableLoader(AsyncDrawableLoader.builder().build()).build()

        Thread {
            api.loadApplications(api.getRepos("")).forEach {
                runOnUiThread {
                    val text = TextView(this)
                    text.setPadding(20)
                    Markwon.setMarkdown(text, config, it.description)
                    layout.addView(text)
                }
            }
        }.start()
    }
}

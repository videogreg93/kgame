package kweb.template

import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import kweb.util.json
import org.jsoup.Jsoup

fun main(args: Array<String>) {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
        val site = "https://www.skidrowreloaded.com/"
        val web = Jsoup.connect(site).get()
        val posts = web.select(".post-excerpt").map {
            it.child(1).child(0)
        }.filterNotNull()
        doc.body.new {
            route {
                path("/") { params ->
                    val searchText = KVar("")
                    val form = form {
                        div(fomantic.ui.icon.input) {
                            input(type = InputType.text, placeholder = "Search...").value = searchText
                            i(fomantic.search.icon)
                        }
                        button(fomantic.ui.primary.button).apply {
                            text("Search")
                            on.click {
                                url.value = "/search/${searchText.value}"
                            }
                        }
                    }
                    form.on(preventDefault = true).submit {
                        url.value = "/search/${searchText.value}"
                    }

                    br()
                    h2().text("Latest Releases")
                    posts.forEach { post ->
                        a(href = post.attr("href")) {
                            img(
                                mapOf(
                                    "src" to post.child(0).attr("data-lazy-src").json
                                )
                            )
                        }
                    }
                }
                path("/search/{query}") { params ->
                    val query = params.getValue("query")
                    val newUrl = "https://www.skidrowreloaded.com/?s=${query.value.replace(" ", "+")}"
                    val searchResults = Jsoup.connect(newUrl).get().select(".post").drop(1)
                    searchResults.forEach { result ->
                        val title = result.child(0).child(0).text()
                        val date = result.selectFirst(".meta")!!.text()
                        val link = result.child(0).child(0).attr("href")
                        val imageSource = result.selectFirst(".lazy-hidden")!!.attr("data-lazy-src")
                        a(href = link) {
                            h1().text(title)
                            p().text(date)
                            img(
                                mapOf(
                                    "src" to imageSource.json
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


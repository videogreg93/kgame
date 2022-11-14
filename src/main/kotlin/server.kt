package kweb.template

import kweb.*
import kweb.html.BodyElement
import kweb.state.KVar
import kweb.util.json
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

fun main(args: Array<String>) {
    Kweb(port = 16097) {
        val site = "https://www.skidrowreloaded.com/"
        val web = Jsoup.connect(site).get()
        val posts = web.select(".post-excerpt").map {
            it.child(1).child(0)
        }.filterNotNull()
        doc.body.new {
            route {
                path("/") { params ->
                    val searchText = KVar("")
                    input(type = InputType.text).value = searchText
                    button().apply {
                        text("Search")
                        on.click {
                            url.value = "/search/${searchText.value}"
                        }
                    }
                    br()
                    h1().text = searchText
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
                    val searchResult = Jsoup.connect(newUrl).get()
                    println(searchResult)
                }
            }
        }
    }
}


package kweb.template

import com.google.gson.Gson
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipFile


fun downloadFile(url: URL, fileName: String) {
    url.openStream().use { Files.copy(it, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING) }
}

fun unzip(zipFile: String) {
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                File(entry.name).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}

private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    if (lhs == rhs) {
        return 0
    }
    if (lhs.isEmpty()) {
        return rhs.length
    }
    if (rhs.isEmpty()) {
        return lhs.length
    }

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1..rhsLength - 1) {
        newCost[0] = i

        for (j in 1..lhsLength - 1) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = Integer.min(Integer.min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}

private fun getPanneReason(code: Int): String {
    val codesPannes = Gson().fromJson(File("codes.json").readText(), CodesPanne::class.java)
    return codesPannes.causePanne.firstOrNull { it.code == code }?.name ?: "Raison Incounnu"
}

private fun getPanneEtat(code: Int): String {
    val codesPannes = Gson().fromJson(File("codes.json").readText(), CodesPanne::class.java)
    return codesPannes.etatPanne.firstOrNull { it.code == code }?.name ?: "État Incounnu"
}

private fun getData(): PanneResponse {
    val site = "https://cartes.ville.sherbrooke.qc.ca/Pannes/data/data.zip"
    downloadFile(URL(site), "data.zip")
    unzip("data.zip")
    return Gson().fromJson(File("data.json").readText(), PanneResponse::class.java)
}

fun main(args: Array<String>) {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {

        println("Download complete")
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
                            text("Adresse...")
                            on.click {
                                url.value = "/search/${searchText.value}"
                            }
                        }
                    }
                    form.on(preventDefault = true).submit {
                        url.value = "/search/${searchText.value}"
                    }
                }
                path("/search/{query}") { params ->
                    val query = params.getValue("query").value.replace("%20", " ")
                    val response = getData()
                    val panne =
                        response.features.filter { levenshtein(it.attributes.adresse, query) < 20 }.sortedBy {
                            levenshtein(it.attributes.adresse, query)
                        }.firstOrNull()
                    if (panne == null) {
                        h1().text("Aucune adresse trouvée, réponses possibles...")
                        response.features.sortedBy {
                            levenshtein(it.attributes.adresse, query)
                        }.take(3).map { adresse ->
                            a(href = "/search/${adresse.attributes.adresse}") {
                                h3().text(adresse.attributes.adresse)
                            }
                        }
                    } else {
                        with(panne.attributes) {
                            h1().text(adresse)
                            p().text("Date mise à jour: ${convertLongToTime(dateMiseAJour)}")
                            p().text("Cause de la panne: ${getPanneReason(causePanne)}")
                            p().text("Status de la panne: ${getPanneEtat(statutPanne)}")
                            p().text("Début de la Panne: ${convertLongToTime(debutPanne)}")
                            p().text("Date prévue du retour: ${convertLongToTime(retablissementPanne)}")
                        }
                    }
                }
            }
        }
    }
}


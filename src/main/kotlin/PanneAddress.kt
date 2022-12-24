package kweb.template

import com.google.gson.annotations.SerializedName

data class PanneAddress(
//    val geometry: String,
    val attributes: Attributes
) {
    data class Attributes(
        @SerializedName("cause_panne")
        val causePanne: Int,
        val adresse: String,
        @SerializedName("datemiseajour")
        val dateMiseAJour: Long,
        @SerializedName("statut_panne")
        val statutPanne: Int,
        @SerializedName("retablissement_panne")
        val retablissementPanne: Long,
        @SerializedName("debut_panne")
        val debutPanne: Long,
    )
}
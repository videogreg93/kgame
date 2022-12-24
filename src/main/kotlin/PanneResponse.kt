package kweb.template

data class PanneResponse(
    val creationDate: String,
    val features: List<PanneAddress>
)
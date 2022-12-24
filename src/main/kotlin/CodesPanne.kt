package kweb.template

data class CodesPanne(val causePanne: List<Code>, val etatPanne: List<Code>) {
    data class Code(val name: String, val code: Int)
}
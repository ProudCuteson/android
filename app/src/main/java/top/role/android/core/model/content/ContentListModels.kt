package top.role.android.core.model.content

enum class ContentListMode(
    val value: String,
) {
    Featured("featured"),
    Latest("latest");

    companion object {
        fun fromValue(
            value: String?,
        ): ContentListMode {
            return when (value) {
                "featured" ->
                    Featured

                "latest" ->
                    Latest

                else ->
                    Latest
            }
        }
    }
}

enum class MobileContentType(
    val value: String,
) {
    Slide("slide"),
    Doc("doc");

    companion object {
        fun fromValue(
            value: String?,
        ): MobileContentType {
            return when (value) {
                "slide" ->
                    Slide

                "doc" ->
                    Doc

                else ->
                    Doc
            }
        }
    }
}

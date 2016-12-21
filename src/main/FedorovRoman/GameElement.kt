package FedorovRoman

enum class GameElement(val letter: Char) {
    START('S'),
    TARGET('T'),
    LIGHT_CELL('.'),
    REGULAR_CELL('*'),
    BLOCK('x'),
    SWITCH('_'),  // letters for switch and bridge shouldn't be displayed
    BRIDGE('_');  //

    companion object {
        private val SWITCH_LETTERS: CharRange = 'A'..'R'
        private val BRIDGE_LETTERS: CharRange = 'a'..'r'

        fun from(letter: Char): GameElement? =
                if (letter in SWITCH_LETTERS) {
                    SWITCH
                } else if (letter in BRIDGE_LETTERS) {
                    BRIDGE
                } else
                    GameElement.values().firstOrNull { it.letter == letter }
    }
}

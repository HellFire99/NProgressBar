package com.github.hellfire99.nprogressbar

import javax.swing.ImageIcon

enum class MBCharacter(val icon: ImageIcon) {
    MARIO(Icons.MARIO);

    val displayName: String
        get() = icon.description
}
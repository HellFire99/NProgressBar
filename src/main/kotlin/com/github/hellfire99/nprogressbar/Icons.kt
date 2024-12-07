package com.github.hellfire99.nprogressbar

import javax.swing.ImageIcon

interface Icons {
    companion object {
        @JvmField
        val MARIO: ImageIcon = ImageIcon(Icons::class.java.getResource("/mario.gif"), "Mario")

        @JvmField
        val LUIGI: ImageIcon = ImageIcon(Icons::class.java.getResource("/luigi.gif"), "Luigi")

        @JvmField
        val SHELL: ImageIcon = ImageIcon(Icons::class.java.getResource("/shell.gif"))
    }
}

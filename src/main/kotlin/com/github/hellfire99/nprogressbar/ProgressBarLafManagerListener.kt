package com.github.hellfire99.nprogressbar

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import javax.swing.UIManager

class ProgressBarLafManagerListener : LafManagerListener, ApplicationActivationListener {
    init {
        updateProgressBarUI()
    }

    override fun lookAndFeelChanged(lafManager: LafManager) {
        updateProgressBarUI()
    }

    override fun applicationActivated(ideFrame: IdeFrame) {
        updateProgressBarUI()
    }

    companion object {
        private fun updateProgressBarUI() {
            UIManager.put("ProgressBarUI", ProgressBarUi::class.java.name)
            UIManager.getDefaults()[ProgressBarUi::class.java.name] = ProgressBarUi::class.java
        }
    }
}

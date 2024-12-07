package com.github.hellfire99.nprogressbar

import com.intellij.openapi.ui.GraphicsConfig
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.TexturePaint
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicGraphicsUtils
import javax.swing.plaf.basic.BasicProgressBarUI
import kotlin.concurrent.Volatile

class ProgressBarUi : BasicProgressBarUI() {
    var bimage: BufferedImage? = null

    override fun getPreferredSize(c: JComponent): Dimension {
        return Dimension(super.getPreferredSize(c).width, JBUIScale.scale(20))
    }

    override fun installListeners() {
        super.installListeners()
        progressBar.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) {
                super.componentShown(e)
            }

            override fun componentHidden(e: ComponentEvent) {
                super.componentHidden(e)
            }
        })
    }

    @Volatile
    private var offset = 0

    @Volatile
    private var offset2 = 0

    @Volatile
    private var velocity = 1

    init {
        try {
            bimage = ImageIO.read(javaClass.getResource("/bricks.png"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun paintIndeterminate(g2d: Graphics, c: JComponent) {
        if (g2d !is Graphics2D) {
            return
        }
        val g = g2d
        val b = progressBar.insets
        val barRectWidth = progressBar.width - (b.right + b.left)
        val barRectHeight = progressBar.height - (b.top + b.bottom)
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return
        }
        g.color = JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50))
        val w = c.width
        var h = c.preferredSize.height
        if (!isEven(c.height - h)) h++
        if (c.isOpaque) {
            g.fillRect(0, (c.height - h) / 2, w, h)
        }
        g.color = JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50))
        val config: GraphicsConfig = GraphicsUtil.setupAAPainting(g)
        g.translate(0, (c.height - h) / 2)

        val x = -offset
        val R: Float = JBUIScale.scale(8f)
        val R2: Float = JBUIScale.scale(9f)
        val containingRoundRect = Area(RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R))
        g.fill(containingRoundRect)
        offset = (offset + 1) % periodLength
        offset2 += velocity
        if (offset2 <= 2) {
            offset2 = 2
            velocity = 1
        } else if (offset2 >= w - JBUIScale.scale(15)) {
            offset2 = w - JBUIScale.scale(15)
            velocity = -1
        }
        val area = Area(Rectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat()))
        area.subtract(Area(RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R)))
        if (c.isOpaque) {
            g.fill(area)
        }
        area.subtract(Area(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), R2, R2)))
        val parent = c.parent
        if (c.isOpaque) {
            g.fill(area)
        }

        Icons.SHELL.paintIcon(progressBar, g, offset2 - JBUIScale.scale(3), -JBUIScale.scale(-2))

        g.draw(RoundRectangle2D.Float(1f, 1f, w - 2f - 1f, h - 2f - 1f, R, R))
        g.translate(0, -(c.height - h) / 2)

        if (progressBar.isStringPainted) {
            if (progressBar.orientation == SwingConstants.HORIZONTAL) {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width)
            } else {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.y, boxRect.height)
            }
        }
        config.restore()
    }

    override fun paintDeterminate(g: Graphics, c: JComponent) {
        if (g !is Graphics2D) {
            return
        }

        if (progressBar.orientation != SwingConstants.HORIZONTAL || !c.componentOrientation.isLeftToRight) {
            super.paintDeterminate(g, c)
            return
        }
        val config: GraphicsConfig = GraphicsUtil.setupAAPainting(g)
        val b = progressBar.insets // area for border
        val w = progressBar.width
        var h = progressBar.preferredSize.height
        if (!isEven(c.height - h)) h++
        val barRectWidth = w - (b.right + b.left)
        val barRectHeight = h - (b.top + b.bottom)
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return
        }
        val amountFull = getAmountFull(b, barRectWidth, barRectHeight)
        val parent = c.parent
        val background = if (parent != null) parent.background else UIUtil.getPanelBackground()
        g.setColor(background)
        val g2 = g
        if (c.isOpaque) {
            g.fillRect(0, 0, w, h)
        }

        val R: Float = JBUIScale.scale(8f)
        val R2: Float = JBUIScale.scale(9f)
        val off: Float = JBUIScale.scale(1f)
        g2.translate(0, (c.height - h) / 2)
        g2.color = progressBar.foreground
        g2.fill(RoundRectangle2D.Float(0f, 0f, w - off, h - off, R2, R2))
        g2.color = background
        g2.fill(RoundRectangle2D.Float(off, off, w - 2f * off - off, h - 2f * off - off, R, R))

        if (bimage != null) {
            val tp = TexturePaint(
                bimage,
                Rectangle2D.Double(0.0, 1.0, (h - 2f * off - off).toDouble(), (h - 2f * off - off).toDouble())
            )
            g2.paint = tp
        }

        g2.fill(
            RoundRectangle2D.Float(
                2f * off,
                2f * off,
                amountFull - JBUIScale.scale(5f),
                h - JBUIScale.scale(5f),
                JBUIScale.scale(7f),
                JBUIScale.scale(7f)
            )
        )

        MBCharacter.MARIO.icon.paintIcon(progressBar, g2, amountFull - JBUIScale.scale(5), -JBUIScale.scale(1))
        g2.translate(0, -(c.height - h) / 2)

        if (progressBar.isStringPainted) {
            paintString(
                g, b.left, b.top,
                barRectWidth, barRectHeight,
                amountFull, b
            )
        }
        config.restore()
    }

    private fun paintString(g: Graphics, x: Int, y: Int, w: Int, h: Int, fillStart: Int, amountFull: Int) {
        if (g !is Graphics2D) {
            return
        }

        val g2 = g
        val progressString = progressBar.string
        g2.font = progressBar.font
        var renderLocation = getStringPlacement(
            g2, progressString,
            x, y, w, h
        )
        val oldClip = g2.clipBounds

        if (progressBar.orientation == SwingConstants.HORIZONTAL) {
            g2.color = selectionBackground
            BasicGraphicsUtils.drawString(
                progressBar,
                g2,
                progressString,
                renderLocation.x.toFloat(),
                renderLocation.y.toFloat()
            )

            g2.color = selectionForeground
            g2.clipRect(fillStart, y, amountFull, h)
            BasicGraphicsUtils.drawString(
                progressBar,
                g2,
                progressString,
                renderLocation.x.toFloat(),
                renderLocation.y.toFloat()
            )
        } else {
            g2.color = selectionBackground
            val rotate =
                AffineTransform.getRotateInstance(Math.PI / 2)
            g2.font = progressBar.font.deriveFont(rotate)
            renderLocation = getStringPlacement(
                g2, progressString,
                x, y, w, h
            )
            BasicGraphicsUtils.drawString(
                progressBar,
                g2,
                progressString,
                renderLocation.x.toFloat(),
                renderLocation.y.toFloat()
            )
            g2.color = selectionForeground
            g2.clipRect(x, fillStart, w, amountFull)
            BasicGraphicsUtils.drawString(
                progressBar,
                g2,
                progressString,
                renderLocation.x.toFloat(),
                renderLocation.y.toFloat()
            )
        }
        g2.clip = oldClip
    }

    override fun getBoxLength(availableLength: Int, otherDimension: Int): Int {
        return availableLength
    }

    private val periodLength: Int
        get() = JBUIScale.scale(16)

    companion object {
        fun createUI(c: JComponent): ComponentUI {
            c.border = com.intellij.util.ui.JBUI.Borders.empty().asUIResource()
            return ProgressBarUi()
        }

        private fun isEven(value: Int): Boolean {
            return value % 2 == 0
        }
    }
}


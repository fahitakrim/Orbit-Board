package org.futo.inputmethod.keyboard.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class TapAnimationType {
    NONE,
    RADAR_WAVE,
    PARTICLE_BURST,
    CONSTELLATION_WEB
}

interface TapEffect {
    fun updateAndDraw(canvas: Canvas, currentTimeMs: Long): Boolean // returns true if still alive
}

/**
 * Glyph Radar Pulse (Nothing Radar Wave):
 * 3 concentric dot-matrix rings expanding outward with ease-out friction and a vivid Nothing Red spark core.
 */
class RadarPulseEffect(
    private val originX: Float,
    private val originY: Float,
    private val startTimeMs: Long = SystemClock.uptimeMillis()
) : TapEffect {

    private val durationMs = 540f
    private val maxRadius = 220f // expansive radiating radius across key row

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    companion object {
        // Precomputed unit circle trigonometry for 3 concentric dot-matrix rings (12, 18, 24 dots)
        private val COS_12 = FloatArray(12) { i -> cos(i * 2f * Math.PI.toFloat() / 12) }
        private val SIN_12 = FloatArray(12) { i -> sin(i * 2f * Math.PI.toFloat() / 12) }

        private val COS_18 = FloatArray(18) { i -> cos(i * 2f * Math.PI.toFloat() / 18) }
        private val SIN_18 = FloatArray(18) { i -> sin(i * 2f * Math.PI.toFloat() / 18) }

        private val COS_24 = FloatArray(24) { i -> cos(i * 2f * Math.PI.toFloat() / 24) }
        private val SIN_24 = FloatArray(24) { i -> sin(i * 2f * Math.PI.toFloat() / 24) }
    }

    // Ease-out cubic curve representing friction
    private fun easeOutCubic(t: Float): Float {
        val f = 1f - t.coerceIn(0f, 1f)
        return 1f - f * f * f
    }

    override fun updateAndDraw(canvas: Canvas, currentTimeMs: Long): Boolean {
        val elapsed = currentTimeMs - startTimeMs
        if (elapsed >= durationMs) {
            return false
        }
        val safeElapsed = elapsed.coerceAtLeast(0L)
        val t = safeElapsed / durationMs // 0..1

        // 1. Vivid Nothing Red spark core
        val sparkAlpha = (1f - (t * 2.2f).coerceAtMost(1f))
        if (sparkAlpha > 0.01f) {
            // Soft outer glow
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((sparkAlpha * 140).toInt(), 0xD7, 0x19, 0x21)
            canvas.drawCircle(originX, originY, 18f * (1f - t * 0.5f), paint)

            // Vivid red spark core
            paint.color = Color.argb((sparkAlpha * 255).toInt(), 0xFF, 0x1E, 0x27)
            canvas.drawCircle(originX, originY, 9.5f * (1f - t * 0.4f), paint)

            // White central micro spark
            val whiteAlpha = (1f - (t * 3.5f).coerceAtMost(1f))
            if (whiteAlpha > 0.01f) {
                paint.color = Color.argb((whiteAlpha * 240).toInt(), 0xFF, 0xFF, 0xFF)
                canvas.drawCircle(originX, originY, 4.0f, paint)
            }
        }

        // 2. Three Concentric Dot-Matrix Rings
        paint.style = Paint.Style.FILL

        for (ringIdx in 0..2) {
            val ringT = when (ringIdx) {
                0 -> t
                1 -> ((t - 0.10f) / 0.90f).coerceAtLeast(0f)
                else -> ((t - 0.20f) / 0.80f).coerceAtLeast(0f)
            }
            if (ringT <= 0f || ringT >= 1f) continue

            val expansion = easeOutCubic(ringT)
            val currentRadius = maxRadius * expansion
            if (currentRadius < 4f) continue

            // Fade out as it expands
            val ringAlpha = (1f - ringT) * 0.9f
            if (ringAlpha <= 0.01f) continue

            val alphaInt = (ringAlpha * 255).toInt().coerceIn(0, 255)
            val dotRadius = (3.6f * (1f - ringT * 0.32f)).coerceAtLeast(1.8f)

            val cosArr: FloatArray
            val sinArr: FloatArray
            val count: Int
            when (ringIdx) {
                0 -> {
                    cosArr = COS_12
                    sinArr = SIN_12
                    count = 12
                }
                1 -> {
                    cosArr = COS_18
                    sinArr = SIN_18
                    count = 18
                }
                else -> {
                    cosArr = COS_24
                    sinArr = SIN_24
                    count = 24
                }
            }

            for (i in 0 until count) {
                val dotX = originX + currentRadius * cosArr[i]
                val dotY = originY + currentRadius * sinArr[i]

                // Every 4th or 5th dot on inner rings has vivid Nothing Red highlight, remainder are crisp dot-matrix white
                if (ringIdx == 0 && i % 4 == 0) {
                    paint.color = Color.argb(alphaInt, 0xD7, 0x19, 0x21)
                } else if (ringIdx == 1 && i % 6 == 0) {
                    paint.color = Color.argb(alphaInt, 0xFF, 0x44, 0x44)
                } else {
                    paint.color = Color.argb(alphaInt, 0xFA, 0xFA, 0xFA)
                }
                canvas.drawCircle(dotX, dotY, dotRadius, paint)
            }
        }

        return true
    }
}

/**
 * Dot Particle Burst (Micro Glyph Spark):
 * 15–35 radiating micro-dots governed by velocity, drag, and gravity in the Bauhaus palette
 * (Nothing Red, Bright White, Ochre Yellow, Cobalt Blue).
 */
class ParticleBurstEffect(
    private val originX: Float,
    private val originY: Float,
    private val startTimeMs: Long = SystemClock.uptimeMillis()
) : TapEffect {

    private data class MicroDot(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val radius: Float,
        val colorArgb: Int,
        val lifeMs: Float
    )

    private val dots: List<MicroDot>
    private var lastTimeMs: Long = startTimeMs
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    companion object {
        // Bauhaus Color Palette
        private val BAUHAUS_COLORS = intArrayOf(
            0xFFD71921.toInt(), // Nothing Red
            0xFFFFFFFF.toInt(), // Bright White
            0xFFE2B438.toInt(), // Ochre Yellow
            0xFF3B72BA.toInt()  // Cobalt Blue
        )
    }

    init {
        // 20 refined, balanced bubble particles in Bauhaus palette
        val count = 20
        dots = (0 until count).map {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            // Sleek radiating velocity
            val speed = 240f + Random.nextFloat() * 420f
            val vx = speed * cos(angle)
            val vy = speed * sin(angle) - 30f // gentle buoyant float
            // Refined, elegant bubble radius (2.6px to 5.4px)
            val dotRadius = 2.6f + Random.nextFloat() * 2.8f
            val color = BAUHAUS_COLORS[Random.nextInt(BAUHAUS_COLORS.size)]
            // Balanced animation duration (750ms to 1050ms)
            val life = 750f + Random.nextFloat() * 300f

            MicroDot(
                x = originX,
                y = originY,
                vx = vx,
                vy = vy,
                radius = dotRadius,
                colorArgb = color,
                lifeMs = life
            )
        }
    }

    override fun updateAndDraw(canvas: Canvas, currentTimeMs: Long): Boolean {
        val dt = ((currentTimeMs - lastTimeMs).coerceAtLeast(0L).coerceAtMost(50L)) / 1000f
        lastTimeMs = currentTimeMs

        var anyAlive = false
        val gravity = 160f // buoyant floating gravity (pixels / sec^2)
        val drag = 1.25f // smooth air resistance

        val totalElapsed = (currentTimeMs - startTimeMs).coerceAtLeast(0L).toFloat()

        for (dot in dots) {
            val dotAge = totalElapsed
            if (dotAge >= dot.lifeMs) continue

            anyAlive = true

            // Physics update: drag and gentle gravity
            dot.vx *= (1f - (drag * dt).coerceAtMost(0.9f))
            dot.vy = (dot.vy + gravity * dt) * (1f - (drag * dt).coerceAtMost(0.9f))

            dot.x += dot.vx * dt
            dot.y += dot.vy * dt

            // Alpha curve: bright initial burst (first 35%), then gracefully fades out
            val lifeRatio = (dotAge / dot.lifeMs).coerceIn(0f, 1f)
            val alpha = if (lifeRatio < 0.35f) {
                1f
            } else {
                val f = (1f - lifeRatio) / 0.65f
                f * f
            }
            val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)

            if (alphaInt > 1) {
                val r = (dot.colorArgb shr 16) and 0xFF
                val g = (dot.colorArgb shr 8) and 0xFF
                val b = dot.colorArgb and 0xFF
                val currentRadius = dot.radius * (1f - lifeRatio * 0.15f)

                // Draw outer particle bubble body
                paint.color = Color.argb(alphaInt, r, g, b)
                canvas.drawCircle(dot.x, dot.y, currentRadius, paint)

                // Draw inner micro-highlight for glassy bubble effect
                if (currentRadius >= 3.4f && alphaInt > 30) {
                    paint.color = Color.argb((alphaInt * 0.85f).toInt(), 0xFF, 0xFF, 0xFF)
                    canvas.drawCircle(dot.x - currentRadius * 0.28f, dot.y - currentRadius * 0.28f, currentRadius * 0.35f, paint)
                }
            }
        }

        return anyAlive
    }
}

/**
 * Constellation Grid / Node Web Effect (Option B):
 * 6 geometric node points radiate outward from key center with interconnected glowing
 * digital threads/lines forming an ephemeral cyber constellation that fades away.
 */
class ConstellationWebEffect(
    private val originX: Float,
    private val originY: Float,
    private val startTimeMs: Long = SystemClock.uptimeMillis()
) : TapEffect {

    private val durationMs = 580f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private class WebNode(
        val baseAngle: Float,
        val targetDistance: Float,
        val radius: Float,
        val isAccent: Boolean
    )

    private val nodes: List<WebNode>
    private val nodeX = FloatArray(6)
    private val nodeY = FloatArray(6)

    companion object {
        private const val MAX_CONNECT_DIST = 96f
    }

    init {
        val nodeCount = 6
        // Distribute around 360 degrees with slight natural variance
        val angleStep = (2f * Math.PI.toFloat()) / nodeCount
        nodes = (0 until nodeCount).map { i ->
            val angle = i * angleStep + (Random.nextFloat() - 0.5f) * (angleStep * 0.45f)
            val distance = 100f + Random.nextFloat() * 60f // radiates 100-160px
            val dotRadius = if (i == 0) 3.5f else (2.4f + Random.nextFloat() * 1.0f)
            val isAccent = (i == 0 || i == 3) // Nothing Red accents
            WebNode(angle, distance, dotRadius, isAccent)
        }
    }

    private fun easeOutCubic(t: Float): Float {
        val f = 1f - t.coerceIn(0f, 1f)
        return 1f - f * f * f
    }

    override fun updateAndDraw(canvas: Canvas, currentTimeMs: Long): Boolean {
        val elapsed = currentTimeMs - startTimeMs
        if (elapsed >= durationMs) return false

        val t = (elapsed.coerceAtLeast(0L) / durationMs).coerceIn(0f, 1f)
        val progress = easeOutCubic(t)
        val overallAlpha = (1f - t * t).coerceIn(0f, 1f)

        if (overallAlpha <= 0.01f) return false

        // Compute current positions into preallocated arrays
        for (i in nodes.indices) {
            val dist = nodes[i].targetDistance * progress
            nodeX[i] = originX + dist * cos(nodes[i].baseAngle)
            nodeY[i] = originY + dist * sin(nodes[i].baseAngle)
        }

        // 1. Draw connecting web lines between nodes within distance threshold
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f

        val currentMaxDist = MAX_CONNECT_DIST * (1f + 0.35f * progress)
        val maxD2 = currentMaxDist * currentMaxDist

        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val dx = nodeX[i] - nodeX[j]
                val dy = nodeY[i] - nodeY[j]
                val d2 = dx * dx + dy * dy
                if (d2 < maxD2) {
                    val dist = kotlin.math.sqrt(d2)
                    val proximity = (1f - dist / currentMaxDist).coerceIn(0f, 1f)
                    val lineAlpha = (proximity * overallAlpha * 0.75f).coerceIn(0f, 1f)
                    val alphaInt = (lineAlpha * 255).toInt()
                    if (alphaInt > 2) {
                        if (nodes[i].isAccent || nodes[j].isAccent) {
                            // Subtle red glowing thread
                            paint.color = Color.argb(alphaInt, 0xD7, 0x19, 0x21)
                        } else {
                            // Crisp cyber white thread
                            paint.color = Color.argb(alphaInt, 0xEE, 0xEE, 0xEE)
                        }
                        canvas.drawLine(nodeX[i], nodeY[i], nodeX[j], nodeY[j], paint)
                    }
                }
            }
        }

        // 2. Draw radial lines from key center during initial burst
        val centerLineAlpha = ((1f - t * 2.2f).coerceIn(0f, 1f) * overallAlpha * 0.6f)
        if (centerLineAlpha > 0.02f) {
            val centerAlphaInt = (centerLineAlpha * 255).toInt()
            for (i in nodes.indices) {
                if (nodes[i].isAccent) {
                    paint.color = Color.argb(centerAlphaInt, 0xD7, 0x19, 0x21)
                } else {
                    paint.color = Color.argb(centerAlphaInt, 0xCC, 0xCC, 0xCC)
                }
                canvas.drawLine(originX, originY, nodeX[i], nodeY[i], paint)
            }
        }

        // 3. Draw nodes (constellation stars)
        paint.style = Paint.Style.FILL
        for (i in nodes.indices) {
            val nodeAlpha = overallAlpha
            val alphaInt = (nodeAlpha * 255).toInt()
            if (alphaInt > 2) {
                if (nodes[i].isAccent) {
                    // Nothing Red node with subtle glow
                    paint.color = Color.argb((alphaInt * 0.45f).toInt(), 0xD7, 0x19, 0x21)
                    canvas.drawCircle(nodeX[i], nodeY[i], nodes[i].radius * 1.8f, paint)

                    paint.color = Color.argb(alphaInt, 0xFF, 0x25, 0x2E)
                    canvas.drawCircle(nodeX[i], nodeY[i], nodes[i].radius, paint)
                } else {
                    // Crisp white dot-matrix node
                    paint.color = Color.argb(alphaInt, 0xFA, 0xFA, 0xFA)
                    canvas.drawCircle(nodeX[i], nodeY[i], nodes[i].radius, paint)
                }
            }
        }

        // Central pulse micro-spark
        val sparkAlpha = (1f - t * 3.0f).coerceIn(0f, 1f)
        if (sparkAlpha > 0.02f) {
            paint.color = Color.argb((sparkAlpha * 240).toInt(), 0xD7, 0x19, 0x21)
            canvas.drawCircle(originX, originY, 5.0f * (1f - t), paint)
        }

        return true
    }
}

/**
 * Manager that holds and coordinates active tap animations on the keyboard canvas.
 * UI-thread optimized, bounded, thread-safe, and allocation-free on frame updates.
 */
class KeyboardTapAnimationManager {
    private val activeEffects = ArrayList<TapEffect>()
    private val maxConcurrent = 10

    @Synchronized
    fun spawnAnimation(type: TapAnimationType, x: Float, y: Float) {
        if (type == TapAnimationType.NONE) return

        // Prune older effects if queue exceeds limit
        while (activeEffects.size >= maxConcurrent) {
            activeEffects.removeAt(0)
        }

        val effect: TapEffect = when (type) {
            TapAnimationType.RADAR_WAVE -> RadarPulseEffect(x, y)
            TapAnimationType.PARTICLE_BURST -> ParticleBurstEffect(x, y)
            TapAnimationType.CONSTELLATION_WEB -> ConstellationWebEffect(x, y)
            TapAnimationType.NONE -> return
        }
        activeEffects.add(effect)
    }

    /**
     * Updates and draws all active effects.
     * Returns true if there are still active animations to continue redrawing.
     */
    @Synchronized
    fun draw(canvas: Canvas): Boolean {
        if (activeEffects.isEmpty()) return false

        val now = SystemClock.uptimeMillis()
        var stillRunning = false
        var i = 0

        while (i < activeEffects.size) {
            val effect = activeEffects[i]
            val isAlive = effect.updateAndDraw(canvas, now)
            if (!isAlive) {
                activeEffects.removeAt(i)
            } else {
                stillRunning = true
                i++
            }
        }

        return stillRunning
    }

    @Synchronized
    fun hasActiveEffects(): Boolean = activeEffects.isNotEmpty()

    @Synchronized
    fun clear() {
        activeEffects.clear()
    }
}

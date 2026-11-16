package org.futo.inputmethod.keyboard.internal

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class TapAnimationType {
    NONE,
    RADAR_WAVE,
    PARTICLE_BURST,
    CONSTELLATION_WEB,
    LASER_RETICLE
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
 * Border Collision & Gutter Flow Touch Effect (কী-এর বর্ডারে ধাক্কা খেয়ে ফাঁকা দিয়ে ছড়িয়ে পড়া):
 * Organic, asymmetric neon energy bursts outward from the tap origin, collides with the keycap
 * perimeters, and courses dynamically along the channels, alleys, and crevices between key borders.
 *
 * Design Specifications:
 * 1. Asymmetric & Organic Randomness: 2 to 4 random asymmetric branches along key border gutters,
 *    randomized reach lengths (~1.2 to 2.4 keys reach, ~140-180dp total span), randomized speeds,
 *    and slight crevice branching angles/bends.
 * 2. Border Collision & Gutter Traversal: Energy bursts outward from tap origin, collides with
 *    the keycap perimeter with an impact spark, and flows strictly through inter-key channels/gaps.
 *    Keycap faces (letters/symbols) remain clean and unobstructed.
 * 3. Multi-Color Vibrant Futuristic Neon: Random vibrant neon palette (Cyan, Neon Pink, Cyber Yellow,
 *    Electric Green, Nothing Red, Electric Violet, Sky Blue, Electric Orange) glowing intensely.
 * 4. Layered Neon Aesthetics: Soft glowing diffusion halo + saturated vibrant body + intense core.
 * 5. Allocation-free execution during updateAndDraw for ultra-smooth 120 FPS frame rate.
 */
typealias BorderCollisionEffect = LaserReticleEffect

class LaserReticleEffect @JvmOverloads constructor(
    private val originX: Float,
    private val originY: Float,
    private val colorMode: String = "neon", // "white", "yellow", "cyan", "pink", "green", "red", "purple", "blue", "orange", "mix", "neon", "random"
    private val startTimeMs: Long = SystemClock.uptimeMillis(),
    private val durationMs: Float = 450f,
    val chosenColor: Int = resolveColor(colorMode),
    private val keyWidth: Float = 0f,
    private val keyHeight: Float = 0f
) : TapEffect {

    companion object {
        // 3 Signature Colors: Pure White, Nothing Red, Cyber Yellow
        val SIGNATURE_COLORS = intArrayOf(
            0xFFFFFFFF.toInt(), // Pure White
            0xFFD71921.toInt(), // Nothing Red
            0xFFFED700.toInt()  // Cyber Yellow
        )

        // Multi-color vibrant futuristic neon palette for random selection per tap:
        // Cyan #00E5FF, Neon Pink #FF1493, Cyber Yellow #FED700, Electric Green #00E676,
        // Nothing Red #D71921, Electric Violet #D500F9, Sky Blue #00B0FF, Electric Orange #FF6D00
        val NEON_PALETTE = intArrayOf(
            0xFF00E5FF.toInt(), // Cyan
            0xFFFF1493.toInt(), // Neon Pink
            0xFFFED700.toInt(), // Cyber Yellow
            0xFF00E676.toInt(), // Electric Green
            0xFFD71921.toInt(), // Nothing Red
            0xFFD500F9.toInt(), // Electric Violet
            0xFF00B0FF.toInt(), // Sky Blue
            0xFFFF6D00.toInt()  // Electric Orange
        )

        @JvmStatic
        fun resolveColor(mode: String): Int = when (mode.lowercase()) {
            "white" -> 0xFFFFFFFF.toInt()
            "yellow" -> 0xFFFED700.toInt()
            "cyan" -> 0xFF00E5FF.toInt()
            "pink" -> 0xFFFF1493.toInt()
            "green" -> 0xFF00E676.toInt()
            "red" -> 0xFFD71921.toInt()
            "purple", "violet" -> 0xFFD500F9.toInt()
            "blue", "skyblue" -> 0xFF00B0FF.toInt()
            "orange" -> 0xFFFF6D00.toInt()
            "mix" -> 0 // Chromatic Rainbow
            "random" -> SIGNATURE_COLORS.random()
            "neon" -> NEON_PALETTE.random()
            else -> NEON_PALETTE.random()
        }
    }

    constructor(originX: Float, originY: Float, startTimeMs: Long) : this(originX, originY, "neon", startTimeMs, 450f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private fun easeOutCubic(t: Float): Float {
        val f = 1f - t.coerceIn(0f, 1f)
        return 1f - f * f * f
    }

    private class BranchData(
        val p0x: Float, val p0y: Float,
        val p1x: Float, val p1y: Float,
        val p2x: Float, val p2y: Float,
        val p3x: Float, val p3y: Float,
        val len0: Float,
        val len1: Float,
        val len2: Float,
        val totalGutterLen: Float,
        val speedMult: Float,
        val startDelayMs: Float,
        val r: Int, val g: Int, val b: Int,
        val coreR: Int, val coreG: Int, val coreB: Int
    )

    private val branches: Array<BranchData>
    private val safeOriginX: Float = if (originX.isNaN() || originX.isInfinite()) 0f else originX
    private val safeOriginY: Float = if (originY.isNaN() || originY.isInfinite()) 0f else originY
    private val density: Float

    init {
        density = try {
            Resources.getSystem().displayMetrics.density.coerceIn(1.0f, 4.0f)
        } catch (_: Exception) {
            2.5f
        }

        val kw = if (keyWidth > 10f) keyWidth else 38f * density
        val kh = if (keyHeight > 10f) keyHeight else 52f * density
        val halfW = kw * 0.5f
        val halfH = kh * 0.5f

        val topEdgeY = safeOriginY - halfH
        val bottomEdgeY = safeOriginY + halfH
        val leftEdgeX = safeOriginX - halfW
        val rightEdgeX = safeOriginX + halfW

        // 1. Asymmetric & Organic Randomness: Pick 2 to 4 distinct branches per tap
        val branchCount = Random.nextInt(2, 5)
        val routeIndices = (0..11).shuffled().take(branchCount)

        val isMix = colorMode.equals("mix", ignoreCase = true)

        branches = Array(branchCount) { i ->
            val route = routeIndices[i]
            val branchColor = if (isMix) {
                NEON_PALETTE[i % NEON_PALETTE.size]
            } else if (chosenColor == 0xFFFFFFFF.toInt() || chosenColor == 0 || colorMode.equals("white", ignoreCase = true) || colorMode.equals("default", ignoreCase = true)) {
                NEON_PALETTE.random()
            } else {
                chosenColor
            }
            val bR = Color.red(branchColor)
            val bG = Color.green(branchColor)
            val bB = Color.blue(branchColor)

            // Center highlight core: subtle 20% lightness blend, preserving intense neon chromaticity
            val cR = (bR * 0.80f + 255 * 0.20f).toInt().coerceIn(0, 255)
            val cG = (bG * 0.80f + 255 * 0.20f).toInt().coerceIn(0, 255)
            val cB = (bB * 0.80f + 255 * 0.20f).toInt().coerceIn(0, 255)

            // Asymmetric randomized gutter reach (~1.2 to 2.2 keys reach)
            val runLen = 1.15f + Random.nextFloat() * 1.05f
            // Asymmetric crevice bend (~0.5 to 1.1 keys into perpendicular alley)
            val bendLen = 0.50f + Random.nextFloat() * 0.55f
            val jitter = (Random.nextFloat() * 4f - 2f) * density

            val p0x = safeOriginX
            val p0y = safeOriginY
            val p1x: Float
            val p1y: Float
            val p2x: Float
            val p2y: Float
            val p3x: Float
            val p3y: Float

            // 12 Organic Gutter Routes (along key borders, inter-key alleys & crevices)
            when (route) {
                0 -> { // Top gutter -> courses Right -> bends Down into column crevice
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = topEdgeY
                    p2x = rightEdgeX + runLen * kw
                    p2y = topEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y + bendLen * kh
                }
                1 -> { // Top gutter -> courses Left -> bends Down into column crevice
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = topEdgeY
                    p2x = leftEdgeX - runLen * kw
                    p2y = topEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y + bendLen * kh
                }
                2 -> { // Bottom gutter -> courses Right -> bends Up into column crevice
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = bottomEdgeY
                    p2x = rightEdgeX + runLen * kw
                    p2y = bottomEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y - bendLen * kh
                }
                3 -> { // Bottom gutter -> courses Left -> bends Up into column crevice
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = bottomEdgeY
                    p2x = leftEdgeX - runLen * kw
                    p2y = bottomEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y - bendLen * kh
                }
                4 -> { // Right crevice -> courses Up -> bends Right into horizontal row gutter
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kh
                    p1x = rightEdgeX
                    p1y = (safeOriginY + collisionOffset).coerceIn(topEdgeY, bottomEdgeY)
                    p2x = rightEdgeX + jitter * 0.4f
                    p2y = topEdgeY - runLen * kh
                    p3x = p2x + bendLen * kw
                    p3y = p2y + jitter * 0.5f
                }
                5 -> { // Right crevice -> courses Down -> bends Right into horizontal row gutter
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kh
                    p1x = rightEdgeX
                    p1y = (safeOriginY + collisionOffset).coerceIn(topEdgeY, bottomEdgeY)
                    p2x = rightEdgeX + jitter * 0.4f
                    p2y = bottomEdgeY + runLen * kh
                    p3x = p2x + bendLen * kw
                    p3y = p2y + jitter * 0.5f
                }
                6 -> { // Left crevice -> courses Up -> bends Left into horizontal row gutter
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kh
                    p1x = leftEdgeX
                    p1y = (safeOriginY + collisionOffset).coerceIn(topEdgeY, bottomEdgeY)
                    p2x = leftEdgeX + jitter * 0.4f
                    p2y = topEdgeY - runLen * kh
                    p3x = p2x - bendLen * kw
                    p3y = p2y + jitter * 0.5f
                }
                7 -> { // Left crevice -> courses Down -> bends Left into horizontal row gutter
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kh
                    p1x = leftEdgeX
                    p1y = (safeOriginY + collisionOffset).coerceIn(topEdgeY, bottomEdgeY)
                    p2x = leftEdgeX + jitter * 0.4f
                    p2y = bottomEdgeY + runLen * kh
                    p3x = p2x - bendLen * kw
                    p3y = p2y + jitter * 0.5f
                }
                8 -> { // Top gutter -> courses Right -> bends Up into row above
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = topEdgeY
                    p2x = rightEdgeX + runLen * kw
                    p2y = topEdgeY - jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y - bendLen * kh
                }
                9 -> { // Top gutter -> courses Left -> bends Up into row above
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = topEdgeY
                    p2x = leftEdgeX - runLen * kw
                    p2y = topEdgeY - jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y - bendLen * kh
                }
                10 -> { // Bottom gutter -> courses Right -> bends Down into row below
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = bottomEdgeY
                    p2x = rightEdgeX + runLen * kw
                    p2y = bottomEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y + bendLen * kh
                }
                else -> { // Bottom gutter -> courses Left -> bends Down into row below
                    val collisionOffset = (Random.nextFloat() * 0.4f - 0.2f) * kw
                    p1x = (safeOriginX + collisionOffset).coerceIn(leftEdgeX, rightEdgeX)
                    p1y = bottomEdgeY
                    p2x = leftEdgeX - runLen * kw
                    p2y = bottomEdgeY + jitter * 0.4f
                    p3x = p2x + jitter * 0.5f
                    p3y = p2y + bendLen * kh
                }
            }

            val l0 = Math.hypot((p1x - p0x).toDouble(), (p1y - p0y).toDouble()).toFloat().coerceAtLeast(1f)
            val l1 = Math.hypot((p2x - p1x).toDouble(), (p2y - p1y).toDouble()).toFloat().coerceAtLeast(1f)
            val l2 = Math.hypot((p3x - p2x).toDouble(), (p3y - p2y).toDouble()).toFloat().coerceAtLeast(1f)

            BranchData(
                p0x = p0x, p0y = p0y,
                p1x = p1x, p1y = p1y,
                p2x = p2x, p2y = p2y,
                p3x = p3x, p3y = p3y,
                len0 = l0,
                len1 = l1,
                len2 = l2,
                totalGutterLen = l1 + l2,
                speedMult = 0.85f + Random.nextFloat() * 0.35f,
                startDelayMs = Random.nextFloat() * 25f,
                r = bR, g = bG, b = bB,
                coreR = cR, coreG = cG, coreB = cB
            )
        }
    }

    override fun updateAndDraw(canvas: Canvas, currentTimeMs: Long): Boolean {
        val elapsed = currentTimeMs - startTimeMs
        if (elapsed >= durationMs) return false

        val safeElapsed = elapsed.coerceAtLeast(0L)
        val progress = (safeElapsed / durationMs).coerceIn(0f, 1f)

        // Smooth ease-out fade curve with glowing luminescence
        val alpha = ((1f - progress) * (1f - progress * 0.25f)).coerceIn(0f, 1f)
        if (alpha <= 0.005f) return false

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        if (width <= 0f || height <= 0f) return true

        val haloStroke = (6.0f * density).coerceIn(14f, 26f)
        val bodyStroke = (2.6f * density).coerceIn(6f, 11f)
        val coreStroke = (1.0f * density).coerceIn(2.2f, 4.2f)

        // Phase 1: Tap Contact Burst to Border (First 65ms):
        // Fast electric filament darts from tap origin to perimeter collision point p1,
        // then completely disappears so keycap face (letter/symbol) remains 100% clean!
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed in 1f..65f) {
                val burstProgress = (branchElapsed / 50f).coerceIn(0f, 1f)
                val burstAlpha = (1f - branchElapsed / 65f) * alpha
                val burstDist = b.len0 * easeOutCubic(burstProgress)
                val ratio = (burstDist / b.len0).coerceIn(0f, 1f)
                val curX = b.p0x + (b.p1x - b.p0x) * ratio
                val curY = b.p0y + (b.p1y - b.p0y) * ratio

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.2f * density
                paint.color = Color.argb((burstAlpha * 0.95f * 255).toInt(), b.r, b.g, b.b)
                canvas.drawLine(b.p0x, b.p0y, curX, curY, paint)

                paint.strokeWidth = 1.0f * density
                paint.color = Color.argb((burstAlpha * 0.95f * 255).toInt(), b.coreR, b.coreG, b.coreB)
                canvas.drawLine(b.p0x, b.p0y, curX, curY, paint)
            }
        }

        // Phase 2: Border Collision Impact Sparks on perimeter (কী-এর বর্ডারে ধাক্কা খেয়ে)
        paint.style = Paint.Style.FILL
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed in 35f..160f) {
                val impactElapsed = branchElapsed - 35f
                val impactProgress = (impactElapsed / 125f).coerceIn(0f, 1f)
                val impactAlpha = (1f - impactProgress) * alpha

                // Outer collision flare: saturated neon bloom
                paint.color = Color.argb((impactAlpha * 0.65f * 255).toInt(), b.r, b.g, b.b)
                canvas.drawCircle(b.p1x, b.p1y, 4.8f * density * (1f - impactProgress * 0.35f), paint)
                // Inner collision core: luminous neon core
                paint.color = Color.argb((impactAlpha * 0.95f * 255).toInt(), b.coreR, b.coreG, b.coreB)
                canvas.drawCircle(b.p1x, b.p1y, 2.0f * density, paint)
            }
        }

        // Phase 3: Gutter Flow Traversal strictly along inter-key crevices & alleys (p1 -> p2 -> p3)
        // Keycap face remains clean and unobstructed.
        // Pass 1: Glowing Soft Diffusion Halo along the alleys
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = haloStroke
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed <= 35f) continue
            val gutterElapsed = branchElapsed - 35f
            val gutterProgress = (gutterElapsed / (durationMs * 0.78f * b.speedMult)).coerceIn(0f, 1f)
            val currentGutterDist = b.totalGutterLen * easeOutCubic(gutterProgress)
            if (currentGutterDist <= 0.5f) continue

            paint.color = Color.argb((alpha * 0.55f * 255).toInt(), b.r, b.g, b.b)
            drawGutterPath(canvas, b, currentGutterDist)
        }

        // Pass 2: Saturated Vibrant Neon Line Body
        paint.strokeWidth = bodyStroke
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed <= 35f) continue
            val gutterElapsed = branchElapsed - 35f
            val gutterProgress = (gutterElapsed / (durationMs * 0.78f * b.speedMult)).coerceIn(0f, 1f)
            val currentGutterDist = b.totalGutterLen * easeOutCubic(gutterProgress)
            if (currentGutterDist <= 0.5f) continue

            paint.color = Color.argb((alpha * 0.95f * 255).toInt(), b.r, b.g, b.b)
            drawGutterPath(canvas, b, currentGutterDist)
        }

        // Pass 3: Intense Center Luminescence (subtle 25% lightness blend, vibrant neon)
        paint.strokeWidth = coreStroke
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed <= 35f) continue
            val gutterElapsed = branchElapsed - 35f
            val gutterProgress = (gutterElapsed / (durationMs * 0.78f * b.speedMult)).coerceIn(0f, 1f)
            val currentGutterDist = b.totalGutterLen * easeOutCubic(gutterProgress)
            if (currentGutterDist <= 0.5f) continue

            paint.color = Color.argb((alpha * 0.95f * 255).toInt(), b.coreR, b.coreG, b.coreB)
            drawGutterPath(canvas, b, currentGutterDist)
        }

        // Pass 4: Advancing Tip Sparks / Electric Beads at leading edge along gutter
        paint.style = Paint.Style.FILL
        for (i in branches.indices) {
            val b = branches[i]
            val branchElapsed = (safeElapsed - b.startDelayMs).coerceAtLeast(0f)
            if (branchElapsed <= 35f) continue
            val gutterElapsed = branchElapsed - 35f
            val gutterProgress = (gutterElapsed / (durationMs * 0.78f * b.speedMult)).coerceIn(0f, 1f)
            val currentGutterDist = b.totalGutterLen * easeOutCubic(gutterProgress)
            if (currentGutterDist <= 0.5f) continue

            val tipX: Float
            val tipY: Float
            if (currentGutterDist <= b.len1) {
                val ratio = currentGutterDist / b.len1
                tipX = b.p1x + (b.p2x - b.p1x) * ratio
                tipY = b.p1y + (b.p2y - b.p1y) * ratio
            } else {
                val rem = currentGutterDist - b.len1
                val ratio = (rem / b.len2).coerceAtMost(1f)
                tipX = b.p2x + (b.p3x - b.p2x) * ratio
                tipY = b.p2y + (b.p3y - b.p2y) * ratio
            }

            // Outer tip glow: saturated neon bloom
            paint.color = Color.argb((alpha * 0.70f * 255).toInt(), b.r, b.g, b.b)
            canvas.drawCircle(tipX, tipY, 4.0f * density, paint)
            // Core spark: luminous neon core
            paint.color = Color.argb((alpha * 0.95f * 255).toInt(), b.coreR, b.coreG, b.coreB)
            canvas.drawCircle(tipX, tipY, 1.8f * density, paint)
        }

        return true
    }

    private fun drawGutterPath(canvas: Canvas, b: BranchData, dist: Float) {
        if (dist <= 0.5f) return
        if (dist <= b.len1) {
            val ratio = dist / b.len1
            val curX = b.p1x + (b.p2x - b.p1x) * ratio
            val curY = b.p1y + (b.p2y - b.p1y) * ratio
            canvas.drawLine(b.p1x, b.p1y, curX, curY, paint)
        } else {
            canvas.drawLine(b.p1x, b.p1y, b.p2x, b.p2y, paint)
            val rem = dist - b.len1
            val ratio = (rem / b.len2).coerceAtMost(1f)
            val curX = b.p2x + (b.p3x - b.p2x) * ratio
            val curY = b.p2y + (b.p3y - b.p2y) * ratio
            canvas.drawLine(b.p2x, b.p2y, curX, curY, paint)
        }
    }
}

/**
 * Manager that holds and coordinates active tap animations on the keyboard canvas.
 * UI-thread optimized, bounded, thread-safe, and allocation-free on frame updates.
 */
class KeyboardTapAnimationManager {
    private val activeEffects = ArrayList<TapEffect>()
    private val maxConcurrent = 10
    var currentLaserColor: String = "neon"
    private var lastColorIndex: Int = -1

    @JvmOverloads
    @Synchronized
    fun spawnAnimation(
        type: TapAnimationType,
        x: Float,
        y: Float,
        laserColor: String = currentLaserColor,
        keyWidth: Float = 0f,
        keyHeight: Float = 0f
    ) {
        if (type == TapAnimationType.NONE) return

        // Prune older effects if queue exceeds limit
        while (activeEffects.size >= maxConcurrent) {
            activeEffects.removeAt(0)
        }

        val effect: TapEffect = when (type) {
            TapAnimationType.RADAR_WAVE -> RadarPulseEffect(x, y)
            TapAnimationType.PARTICLE_BURST -> ParticleBurstEffect(x, y)
            TapAnimationType.CONSTELLATION_WEB -> ConstellationWebEffect(x, y)
            TapAnimationType.LASER_RETICLE -> {
                val isRandomOrNeon = laserColor.isEmpty() ||
                        laserColor.equals("random", ignoreCase = true) ||
                        laserColor.equals("neon", ignoreCase = true) ||
                        laserColor.equals("white", ignoreCase = true) ||
                        laserColor.equals("default", ignoreCase = true) ||
                        laserColor.equals("theme", ignoreCase = true)
                var chosenNeon = if (isRandomOrNeon) {
                    var newIndex = (0 until LaserReticleEffect.NEON_PALETTE.size).random()
                    if (newIndex == lastColorIndex && LaserReticleEffect.NEON_PALETTE.size > 1) {
                        newIndex = (newIndex + 1) % LaserReticleEffect.NEON_PALETTE.size
                    }
                    lastColorIndex = newIndex
                    LaserReticleEffect.NEON_PALETTE[newIndex]
                } else {
                    LaserReticleEffect.resolveColor(laserColor)
                }
                if (chosenNeon == 0xFFFFFFFF.toInt() || chosenNeon == 0) {
                    chosenNeon = LaserReticleEffect.NEON_PALETTE.random()
                }
                val mode = if (isRandomOrNeon) "neon" else laserColor
                LaserReticleEffect(
                    originX = x,
                    originY = y,
                    colorMode = mode,
                    chosenColor = chosenNeon,
                    keyWidth = keyWidth,
                    keyHeight = keyHeight
                )
            }
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
    fun getActiveCount(): Int = activeEffects.size

    @Synchronized
    fun clear() {
        activeEffects.clear()
    }
}

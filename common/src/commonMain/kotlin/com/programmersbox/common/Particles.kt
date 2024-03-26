package com.programmersbox.common


import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.datetime.Clock
import kotlin.jvm.JvmField

//Code from https://github.com/CuriousNikhil/compose-particle-system/tree/main

const val PI = kotlin.math.PI
const val HALF_PI = PI / 2
const val TWO_PI = PI * 2
const val TWO_THIRD_PI = PI * 2 / 3
const val QUARTER_PI = PI / 4

fun Float.roundTo(n: Int): Float {
    return this
}

fun Double.roundTo(n: Int): Double {
    return this
}

open class Vector2D(var x: Float = 0f, var y: Float = 0f) {
}

fun Vector2D.add(other: Vector2D): Vector2D {
    this.x += other.x
    this.y += other.y
    return this
}

fun Vector2D.add(other: Vector2D, scalar: Float): Vector2D {
    this.x += other.x * scalar
    this.y += other.y * scalar
    return this
}

fun Vector2D.scalarMultiply(scalar: Float): Vector2D {
    this.x *= scalar
    this.y *= scalar
    return this
}

fun Vector2D.inc(factor: Float): Vector2D {
    this.x += factor
    this.y += factor
    return this
}

internal abstract class Emitter(
    private val particleConfigData: ParticleConfigData,
) {

    val particlePool = mutableListOf<Particle>()

    abstract fun generateParticles(numberOfParticles: Int)

    fun addParticle() {
        val particle = createFreshParticle()
        particlePool.add(particle)
    }

    private fun createFreshParticle(): Particle {
        return Particle(
            initialX = particleConfigData.x,
            initialY = particleConfigData.y,
            color = particleConfigData.particleColor.getExactColor(),
            size = particleConfigData.particleSize.getExactSize(),
            velocity = particleConfigData.velocity.createVelocityVector(),
            acceleration = particleConfigData.acceleration.createAccelerationVector(),
            lifetime = particleConfigData.lifeTime.maxLife,
            agingFactor = particleConfigData.lifeTime.agingFactor,
        )
    }

    fun applyForce(force: Vector2D) {
        for (particle in particlePool) {
            particle.applyForce(force)
        }
    }

    abstract fun update(dt: Float)

    fun render(drawScope: DrawScope) {
        for (particle in particlePool) {
            particle.show(drawScope)
        }
    }
}

internal class ParticleExplodeEmitter(
    numberOfParticles: Int,
    particleConfigData: ParticleConfigData,
) : Emitter(particleConfigData) {

    init {
        generateParticles(numberOfParticles)
    }

    override fun generateParticles(numberOfParticles: Int) {
        repeat(numberOfParticles) { addParticle() }
    }

    override fun update(dt: Float) {
        for (particle in particlePool) {
            particle.update(dt)
        }
        particlePool.removeAll { it.finished() }
    }
}

internal class ParticleFlowEmitter(
    private val durationMillis: Int,
    private val emissionConfig: EmissionType.FlowEmission,
    particleConfigData: ParticleConfigData,
) : Emitter(particleConfigData) {

    private var particleCount = 0
    private var elapsed = 0f
    private var elapsedTimeParticleCreation = 0f

    override fun generateParticles(numberOfParticles: Int) {
        if (this.isFull()) {
            return
        }
        particleCount++
        repeat(numberOfParticles) { addParticle() }
    }

    private fun isTimeElapsed(): Boolean {
        return when (durationMillis) {
            0 -> false
            EmissionType.FlowEmission.INDEFINITE -> false
            else -> elapsed >= durationMillis
        }
    }

    private fun isFull(): Boolean = emissionConfig.maxParticlesCount in 1..(particleCount)

    override fun update(dt: Float) {
        elapsedTimeParticleCreation += dt
        if (elapsedTimeParticleCreation >= 1 && !isTimeElapsed()) {
            val amount = (emissionConfig.emissionRate * elapsedTimeParticleCreation).toInt()
            generateParticles(amount)
            elapsedTimeParticleCreation %= 1
        }
        elapsed += dt

        for (i in particlePool.size - 1 downTo 0) {
            val particle = particlePool[i]
            particle.update(dt)
        }
        particlePool.removeAll { it.finished() }
    }
}

internal class Particle constructor(
    var initialX: Float = 0f, var initialY: Float = 0f,
    val color: Color = Color.Yellow,
    var size: Float = 25f,
    var velocity: Vector2D = Vector2D(0f, 0f),
    var acceleration: Vector2D = Vector2D(0f, 0f),
    var lifetime: Float = 255f,
    var agingFactor: Float = 20f,
) : Vector2D(initialX, initialY) {

    private val originalLife = lifetime
    private var alpha = 1f

    fun finished(): Boolean = this.lifetime < 0

    fun applyForce(force: Vector2D) {
        this.acceleration.add(force)
    }

    fun update(dt: Float) {
        lifetime -= agingFactor

        if (lifetime >= 0) {
            this.alpha = (lifetime / originalLife).roundTo(3)
        }

        // Add acceleration to velocity vector
        this.velocity.add(acceleration)

        // add velocity vector to positions
        this.add(velocity, scalar = dt)

        //set acceleration back to 0
        this.acceleration.scalarMultiply(0f)
    }

    fun show(drawScope: DrawScope) {
        drawScope.drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 360f,
            alpha = alpha,
            topLeft = Offset(x, y),
            size = Size(size, size),
            useCenter = true
        )
    }
}

data class Velocity(
    val xDirection: Float = 0f,
    val yDirection: Float = 0f,
    val angle: Double = TWO_PI,
    val randomize: Boolean = true,
)

internal fun Velocity.createVelocityVector(): Vector2D {
    return if (this.randomize) {
        Vector2D(
            x = (this.xDirection * cos(angle * Random.nextFloat())).toFloat(),
            y = (this.yDirection * sin(angle * Random.nextFloat())).toFloat()
        )
    } else {
        Vector2D(
            x = (this.xDirection * cos(angle)).toFloat(),
            y = (this.yDirection * sin(angle)).toFloat()
        )
    }
}

data class Acceleration(val xComponent: Float = 0f, val yComponent: Float = 0f, val uniform: Boolean = false)

internal fun Acceleration.createAccelerationVector(): Vector2D {
    return if (!uniform) {
        Vector2D(xComponent * Random.nextFloat(), yComponent * Random.nextFloat())
    } else {
        Vector2D(this.xComponent, this.yComponent)
    }
}

sealed class Force {
    data class Gravity(val magnitude: Float = 0f) : Force()
    data class Wind(val xDirection: Float = 0f, val yDirection: Float = 0f) : Force()
}

internal fun Force.createForceVector(): Vector2D {
    return when (this) {
        is Force.Gravity -> {
            Vector2D(0f, this.magnitude)
        }

        is Force.Wind -> {
            Vector2D(this.xDirection, this.yDirection)
        }
    }
}

sealed class ParticleSize {
    data class ConstantSize(val size: Float = 25f) : ParticleSize()
    data class RandomSizes(val range: IntRange = 25..50) : ParticleSize()
}

internal fun ParticleSize.getExactSize(): Float {
    return when (this) {
        is ParticleSize.ConstantSize -> this.size
        is ParticleSize.RandomSizes -> (Random.nextInt(this.range.first, this.range.last)).toFloat()
    }
}

sealed class ParticleColor {
    data class SingleColor(val color: Color = Color.Yellow) : ParticleColor()
    data class RandomColors(val colors: List<Color>) : ParticleColor()
}

internal fun ParticleColor.getExactColor(): Color {
    return when (this) {
        is ParticleColor.SingleColor -> this.color
        is ParticleColor.RandomColors -> this.colors[Random.nextInt(0, this.colors.size)]
    }
}


data class LifeTime(val maxLife: Float = 255f, val agingFactor: Float = 15f)

sealed class EmissionType {
    data class ExplodeEmission(
        val numberOfParticles: Int = 30,
    ) : EmissionType()

    data class FlowEmission(
        val maxParticlesCount: Int = 50,
        val emissionRate: Float = 0.5f,
    ) : EmissionType() {
        companion object {
            @JvmField
            val INDEFINITE = -2
        }
    }
}

internal data class ParticleConfigData(
    val x: Float = 0f,
    val y: Float = 0f,
    val velocity: Velocity,
    val force: Force,
    val acceleration: Acceleration,
    val particleSize: ParticleSize,
    val particleColor: ParticleColor,
    val lifeTime: LifeTime,
    val emissionType: EmissionType,
)

@Composable
fun CreateParticles(
    modifier: Modifier = Modifier,
    x: Float = 0f,
    y: Float = 0f,
    velocity: Velocity = Velocity(xDirection = 1f, yDirection = 1f),
    force: Force = Force.Gravity(0.0f),
    acceleration: Acceleration = Acceleration(0f, 0f),
    particleSize: ParticleSize = ParticleSize.ConstantSize(),
    particleColor: ParticleColor = ParticleColor.SingleColor(),
    lifeTime: LifeTime = LifeTime(255f, 1f),
    emissionType: EmissionType = EmissionType.ExplodeEmission(),
    durationMillis: Int = 10000,
) {

    val dt = remember { mutableStateOf(0f) }

    var startTime by remember { mutableLongStateOf(0L) }
    var previousTime by remember { mutableLongStateOf(Clock.System.now().nanosecondsOfSecond.toLong()) }

    val emitter = remember {
        val particleConfigData = ParticleConfigData(
            x, y, velocity, force, acceleration, particleSize, particleColor, lifeTime, emissionType
        )
        when (emissionType) {
            is EmissionType.ExplodeEmission -> {
                ParticleExplodeEmitter(emissionType.numberOfParticles, particleConfigData)
            }

            is EmissionType.FlowEmission -> {
                ParticleFlowEmitter(
                    durationMillis,
                    emissionType,
                    particleConfigData
                )
            }
        }
    }

    startTime = Clock.System.now().toEpochMilliseconds()
    LaunchedEffect(Unit) {
        val condition = if (emissionType is EmissionType.FlowEmission &&
            emissionType.maxParticlesCount == EmissionType.FlowEmission.INDEFINITE
        ) {
            true
        } else {
            Clock.System.now().toEpochMilliseconds() - startTime < durationMillis
        }
        while (condition) {
            withFrameNanos {
                dt.value = ((it - previousTime) / 1E7).toFloat()
                previousTime = it
            }
        }
    }

    Canvas(modifier) {
        emitter.render(this)
        emitter.applyForce(force.createForceVector())
        emitter.update(dt.value)
    }
}
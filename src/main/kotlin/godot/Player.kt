package godot

import godot.annotation.Emit
import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.api.AnimatedSprite2D
import godot.api.AnimationPlayer
import godot.api.Area2D
import godot.api.CollisionShape2D
import godot.api.Input
import godot.api.Node2D
import godot.core.StringName
import godot.core.Vector2
import godot.core.signal0
import godot.coroutines.async
import godot.coroutines.await
import godot.extension.getNodeAs

@Script
class Player : Area2D() {
	@Emit
	val hit by signal0()

	var isHurt: Boolean = false

	var animation: AnimatedSprite2D? = null
	var collision: CollisionShape2D? = null
	var hitFlashAnimation: AnimationPlayer? = null

	@Export
	var speed: Int = 400

	var screenSize: Vector2 = Vector2(0, 0)

	// Called when the node enters the scene tree for the first time.
	override fun _ready() {
		val viewPortRect = getViewport()?.getVisibleRect()
		if (viewPortRect !== null) {
			screenSize = viewPortRect.size
		}
		animation = getNodeAs<AnimatedSprite2D>("AnimatedSprite2D")
		collision = getNodeAs<CollisionShape2D>("CollisionShape2D")
		hitFlashAnimation = getNodeAs<AnimationPlayer>("HitFlashAnimationPlayer")
		hide()
	}

	// Called every frame. 'delta' is the elapsed time since the previous frame.
	override fun _process(delta: Double) {
		var velocity = Vector2.ZERO // The player's movement vector.

		if (Input.isActionPressed("move_right")) {
			velocity.x += 1
		}

		if (Input.isActionPressed("move_left")) {
			velocity.x -= 1
		}

		if (Input.isActionPressed("move_down")) {
			velocity.y += 1
		}

		if (Input.isActionPressed("move_up")) {
			velocity.y -= 1
		}

		if (velocity.length() > 0) {
			velocity = velocity.normalized() * speed
			animation?.play()
		} else {
			animation?.stop()
		}
		if (velocity.x != 0.0) {
			animation?.animation = StringName("walk")
			animation?.flipV = false
			animation?.flipH = velocity.x < 0
		} else if (velocity.y != 0.00) {
			animation?.animation = StringName("up")
			animation?.flipV = velocity.y > 0
		}

		position += velocity * delta
		position = position.clamp(Vector2(0, 0), screenSize)

	}

	fun start(pos: Vector2) {
		position = pos
		show()
		collision?.disabled = false
	}

	@Register
	fun onBodyEntered(body: Node2D) {
		if (!isHurt) {
			hit.emit()
			isHurt = true
			async { recover() }
			hitFlashAnimation?.play("hit_flash")
		}
	}

	private suspend fun recover() {
		getTree()?.createTimer(0.5)?.timeout?.await()
		isHurt = false
	}

	fun die() {
		hide()
		// Must be deferred as we can't change physics properties on a physics callback.
		collision?.setDeferred("disabled", true)
	}
}

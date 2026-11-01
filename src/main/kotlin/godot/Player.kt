package godot

import godot.annotation.Emit
import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.api.AnimatedSprite2D
import godot.api.Area2D
import godot.api.CollisionShape2D
import godot.api.Input
import godot.api.Node2D
import godot.core.StringName
import godot.core.Vector2
import godot.core.signal0
import godot.extension.getNodeAs

@Script
class Player : Area2D() {
	@Emit
	val hit by signal0()

	@Export
	var speed: Int = 400

	// Declare member variables here. Examples:
	// val a = 2
	// val b = "text"
	var screenSize: Vector2 = Vector2(0, 0)

	// Called when the node enters the scene tree for the first time.
	override fun _ready() {
		val viewPortRect = getViewport()?.getVisibleRect()
		if (viewPortRect !== null) {
			screenSize = viewPortRect.size
		}
		hide()
	}

	// Called every frame. 'delta' is the elapsed time since the previous frame.
	override fun _process(delta: Double) {
		var velocity = Vector2.ZERO; // The player's movement vector.

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

		val animatedSprite2D = getNodeAs<AnimatedSprite2D>("AnimatedSprite2D")

		if (animatedSprite2D != null) {
			if (velocity.length() > 0) {
				velocity = velocity.normalized() * speed
				animatedSprite2D.play()
			} else {
				animatedSprite2D.stop()
			}
			if (velocity.x != 0.0) {
				animatedSprite2D.animation = StringName("walk")
				animatedSprite2D.flipV = false
				animatedSprite2D.flipH = velocity.x < 0;
			} else if (velocity.y != 0.00) {
				animatedSprite2D.animation = StringName("up")
				animatedSprite2D.flipV = velocity.y > 0
			}
		}

		position += velocity * delta
		position = position.clamp(Vector2(0, 0), screenSize)

	}

	fun start(pos: Vector2) {
		position = pos
		show()
		getNodeAs<CollisionShape2D>("CollisionShape2D")?.disabled = false
	}

	@Register
	fun onBodyEntered(body: Node2D) {
		hide()
		hit.emit()
		// Must be deferred as we can't change physics properties on a physics callback.
		getNodeAs<CollisionShape2D>("CollisionShape2D")?.setDeferred("disabled", true)
	}
}

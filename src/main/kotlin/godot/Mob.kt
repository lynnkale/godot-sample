import godot.annotation.Register
import godot.annotation.Script
import godot.api.AnimatedSprite2D
import godot.api.RigidBody2D
import godot.core.PackedStringArray
import godot.core.StringName
import godot.extension.getNodeAs
import kotlin.random.Random

@Script
class Mob: RigidBody2D() {

	// Declare member variables here. Examples:
	// val a = 2
	// val b = "text"

	// Called when the node enters the scene tree for the first time.
	override fun _ready() {
		val as2d = getNodeAs<AnimatedSprite2D>("AnimatedSprite2D")
		val mobTypes: PackedStringArray = as2d?.spriteFrames?.getAnimationNames() ?: PackedStringArray()
		as2d?.animation = StringName(mobTypes[Random.nextInt(mobTypes.size)])
		as2d?.play()
	}

	// Called every frame. 'delta' is the elapsed time since the previous frame.
	override fun _process(delta: Double) {
		
	}

	@Register
	fun onVisibleOnScreenNotifier2DScreenExited()
	{
		queueFree()
	}
}

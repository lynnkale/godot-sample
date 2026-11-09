package godot

import Mob
import godot.api.Node
import godot.annotation.Script
import godot.annotation.Export
import godot.annotation.Register
import godot.api.AudioStreamPlayer2D
import godot.api.Marker2D
import godot.api.PackedScene
import godot.api.PathFollow2D
import godot.api.Timer
import godot.core.Vector2
import godot.coroutines.async
import godot.extension.getNodeAs
import godot.extension.instantiateAs
import godot.global.GD
import kotlin.math.PI
import kotlin.random.Random

const val MAX_LIVES: Int = 3

@Script
class Main : Node() {


	@Export
	lateinit var mobScene: PackedScene

	var music: AudioStreamPlayer2D? = null
	var gameOverSound: AudioStreamPlayer2D? = null

	private var _score: Int = 0

	private var _lives: Int = MAX_LIVES

	// Called when the node enters the scene tree for the first time.
	override fun _ready() {
		music = getNodeAs("Music")
		gameOverSound = getNodeAs("GameOver")
	}

	// Called every frame. 'delta' is the elapsed time since the previous frame.
	override fun _process(delta: Double) {

	}

	@Register
	fun loseALife() {
		_lives--
		if (_lives < 1) {
			gameOver()
		}
		GD.print("Losing a life")
		updateLives()
	}

	fun addALife() {
		if (_lives < MAX_LIVES) {
			_lives++
		}
		GD.print("Adding a life")
		updateLives()
	}

	private fun updateLives() {
		getNodeAs<Hud>("HUD")?.updateLivesSprite(_lives)
	}

	fun gameOver() {
		getNodeAs<Player>("Player")?.die()
		getNodeAs<Timer>("MobTimer")?.stop()
		getNodeAs<Timer>("ScoreTimer")?.stop()
		async { getNodeAs<Hud>("HUD")?.showGameOver() }
		music?.stop()
		gameOverSound?.play()
	}

	@Register
	fun newGame() {
		music?.play()
		_score = 0
		_lives = MAX_LIVES
		updateLives()

		getTree()?.callGroup("mobs", "queue_free")

		val player = getNodeAs<Player>("Player");
		val startPosition = getNodeAs<Marker2D>("StartPosition");
		player?.start(startPosition?.position ?: Vector2(0, 0));

		getNodeAs<Timer>("StartTimer")?.start();

		val hud = getNodeAs<Hud>("HUD");
		hud?.updateScore(_score);
		hud?.showMessage("Get Ready!");
	}

	@Register
	fun onMobTimerTimeout() {

		val mob: Mob? = mobScene.instantiateAs<Mob>()
		if (mob === null) return

		// Choose a random location on Path2D.
		val mobSpawnLocation = getNodeAs<PathFollow2D>("MobPath/MobSpawnLocation")
		if (mobSpawnLocation === null) return
		mobSpawnLocation.progressRatio = Random.nextFloat()

		// Set the mob's direction perpendicular to the path direction.
		var direction: Double = mobSpawnLocation.rotation + PI / 2

		// Set the mob's position to a random location.
		mob.position = mobSpawnLocation.position

		// Add some randomness to the direction.
		direction += Random.nextDouble(PI / -4.0, PI / 4.0)
		mob.rotation = direction.toFloat()

		// Choose the velocity.
		val velocity = Vector2(Random.nextDouble(150.0, 250.0), 0)
		mob.linearVelocity = velocity.rotated(direction)

		// Spawn the mob by adding it to the Main scene.
		addChild(mob)
	}

	@Register
	fun onScoreTimerTimeout() {
		_score++
		if (_score % 10 == 0) {
			addALife()
		}
		getNodeAs<Hud>("HUD")?.updateScore(_score);
	}

	@Register
	fun onStartTimerTimeout() {

		GD.print("Start Timer Timeout")
		getNodeAs<Timer>("MobTimer")?.start();
		getNodeAs<Timer>("ScoreTimer")?.start();
	}
}

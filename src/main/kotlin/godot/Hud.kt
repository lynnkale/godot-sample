package godot

import godot.annotation.Emit
import godot.annotation.Register
import godot.api.CanvasLayer
import godot.annotation.Script
import godot.api.Button
import godot.api.Label
import godot.api.Timer
import godot.core.signal0
import godot.coroutines.await
import godot.extension.getNodeAs

@Script
class Hud : CanvasLayer() {

	@Emit
	val startGame by signal0()

	// Called when the node enters the scene tree for the first time.
	override fun _ready() {

	}

	// Called every frame. 'delta' is the elapsed time since the previous frame.
	override fun _process(delta: Double) {

	}

	fun showMessage(messageText: String) {
		val message = getNodeAs<Label>("Message");
		message?.text = messageText;
		message?.show();

		getNodeAs<Timer>("MessageTimer")?.start();
	}

	suspend fun showGameOver() {
		showMessage("Game Over");

		val messageTimer = getNodeAs<Timer>("MessageTimer");
		messageTimer?.timeout?.await()

		val message = getNodeAs<Label>("Message");
		message?.text = "Dodge the Creeps!";
		message?.show();

		getTree()?.createTimer(1.0)?.timeout?.await()
		getNodeAs<Button>("StartButton")?.show();
	}

	fun updateScore(score: Int) {
		getNodeAs<Label>("ScoreLabel")?.text = score.toString()
	}

	@Register
	fun OnStartButtonPressed() {
		getNodeAs<Button>("StartButton")?.hide();
		startGame.emit()
	}

	@Register
	fun OnMessageTimerTimeout() {
		getNodeAs<Label>("Message")?.hide();
	}
}

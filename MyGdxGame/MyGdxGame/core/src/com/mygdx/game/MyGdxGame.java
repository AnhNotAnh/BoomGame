package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {

	public enum GameState { PLAYING, COMPLETE }

	public static final float MOVEMENT_COOLDOWN_TIME = 0.3f;

	GameState gameState = GameState.PLAYING;

	// Map and rendering
	SpriteBatch batch;
	SpriteBatch uiBatch;
	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;
	OrthographicCamera camera;

	// Game clock
	long lastTime;
	float elapsedTime;

	// Player Character
	Player player;
	float movementCooldown;

	// Enemy
	Animation enemyRightAnimation;
	Animation enemyLeftAnimation;
	Animation enemyFrontAnimation;
	Animation enemyBackAnimation;
	Animation enemyDeathAnimation;

	TextureRegion[] enemyFrameRight;
	TextureRegion[] enemyFrameLeft;
	TextureRegion[] enemyFramesBack;
	TextureRegion[] enemyFramesFront;
	TextureRegion[] enemyFramesDeath;
	Texture enemyDeath;
	Texture enemyFlyRight;
	Texture enemyFlyLeft;
	Texture enemyFlyBack;
	Texture enemyFlyFront;
	SpriteBatch spriteBatch;
	float stateTime;

	private static final int FRAME_COLS = 3;
	private static final int FRAME_COLSDEATH = 5;
	private static final int FRAME_ROWS = 1;

	Array<Enemy> enemies;

	// UI textures
	Texture buttonSquareTexture;
	Texture buttonSquareDownTexture;
	Texture buttonLongTexture;
	Texture buttonLongDownTexture;

	// UI Buttons
	Button moveLeftButton;
	Button moveRightButton;
	Button moveDownButton;
	Button moveUpButton;
	Button restartButton;
	boolean restartActive;

	// Bomb
	private Texture bombTexture;
	private ArrayList<Bomb> bombs;
	private float bombCooldown;
	private static final float BOMB_COOLDOWN_TIME = 1.0f;
	private static final float BOMB_EXPLOSION_TIME = 2.0f;

	private Texture explosionTexture;

	Button placeBombButton;

	@Override
	public void create() {
		// Rendering
		batch = new SpriteBatch();
		uiBatch = new SpriteBatch();

		tiledMap = new TmxMapLoader().load("map/map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		// Camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w / 2, h / 2);

		// Textures
		buttonSquareTexture = new Texture("button/buttonSquare_blue.png");
		buttonSquareDownTexture = new Texture("button/buttonSquare_beige_pressed.png");
		buttonLongTexture = new Texture("button/buttonLong_blue.png");
		buttonLongDownTexture = new Texture("button/buttonLong_beige_pressed.png");

		// Enemy
		enemyFlyRight = new Texture(Gdx.files.internal("enemies/fly-right.png"));
		enemyFlyLeft = new Texture(Gdx.files.internal("enemies/fly-left.png"));
		enemyFlyFront = new Texture(Gdx.files.internal("enemies/fly-front.png"));
		enemyFlyBack = new Texture(Gdx.files.internal("enemies/fly-back.png"));
		enemyDeath = new Texture(Gdx.files.internal("enemies/death-front.png"));

		TextureRegion[][] tempRight = TextureRegion.split(enemyFlyRight, enemyFlyRight.getWidth() / FRAME_COLS, enemyFlyRight.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempLeft = TextureRegion.split(enemyFlyLeft, enemyFlyLeft.getWidth() / FRAME_COLS, enemyFlyLeft.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempFront = TextureRegion.split(enemyFlyFront, enemyFlyFront.getWidth() / FRAME_COLS, enemyFlyFront.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempBack = TextureRegion.split(enemyFlyBack, enemyFlyBack.getWidth() / FRAME_COLS, enemyFlyBack.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempDeath = TextureRegion.split(enemyDeath, enemyDeath.getWidth() / FRAME_COLSDEATH, enemyDeath.getHeight() / FRAME_ROWS);

		enemyFrameRight = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		enemyFrameLeft = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		enemyFramesBack = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		enemyFramesFront = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		enemyFramesDeath = new TextureRegion[FRAME_COLSDEATH * FRAME_COLS];

		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				enemyFrameRight[index] = tempRight[i][j];
				enemyFrameLeft[index] = tempLeft[i][FRAME_COLS - j - 1];
				enemyFramesFront[index] = tempFront[i][j];
				enemyFramesBack[index] = tempBack[i][j];
				index++;
			}
		}

		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLSDEATH; j++) {
				enemyFramesDeath[index] = tempDeath[i][j];
				index++;
			}
		}

		spriteBatch = new SpriteBatch();
		enemyRightAnimation = new Animation(0.5f, enemyFrameRight);
		enemyLeftAnimation = new Animation(0.5f, enemyFrameLeft);
		enemyFrontAnimation = new Animation(0.5f, enemyFramesFront);
		enemyBackAnimation = new Animation(0.5f, enemyFramesBack);
		enemyDeathAnimation = new Animation(0.5f, enemyFramesDeath);
		stateTime = 0.33f;

		enemies = new Array<Enemy>();

		// Spawn enemies at fixed locations
		MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
		TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

		// Spawn enemy 1
		Vector2 spawnPosition1 = new Vector2(5, 10); // Replace with desired spawn position
		float worldX1 = spawnPosition1.x * tileLayer.getTileWidth() + camera.position.x - camera.viewportWidth / 2;
		float worldY1 = spawnPosition1.y * tileLayer.getTileHeight() + camera.position.y - camera.viewportHeight / 2;
		Vector2 worldSpawnPosition1 = new Vector2(worldX1, worldY1);
		Enemy enemy1 = new Enemy(worldSpawnPosition1, 45f, enemyRightAnimation, enemyLeftAnimation,
				enemyFrontAnimation, enemyBackAnimation, enemyDeathAnimation, this);
		enemies.add(enemy1);

		// Spawn enemy 2
		Vector2 spawnPosition2 = new Vector2(10, 5); // Replace with desired spawn position
		float worldX2 = spawnPosition2.x * tileLayer.getTileWidth() + camera.position.x - camera.viewportWidth / 2;
		float worldY2 = spawnPosition2.y * tileLayer.getTileHeight() + camera.position.y - camera.viewportHeight / 2;
		Vector2 worldSpawnPosition2 = new Vector2(worldX2, worldY2);
		Enemy enemy2 = new Enemy(worldSpawnPosition2, 45f, enemyRightAnimation, enemyLeftAnimation,
		         enemyFrontAnimation, enemyBackAnimation, enemyDeathAnimation, this);
		enemies.add(enemy2);

		// Buttons
		float buttonSize = h * 0.2f;
		moveLeftButton = new Button(0.0f, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveRightButton = new Button(buttonSize * 2, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveDownButton = new Button(buttonSize, 0.0f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveUpButton = new Button(buttonSize, buttonSize * 2, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		restartButton = new Button(w / 2 - buttonSize * 2, h * 0.2f, buttonSize * 4, buttonSize, buttonLongTexture, buttonLongDownTexture);

		// Player
		player = new Player(new Vector2(1, 18));

		// Place bomb button
		placeBombButton = new Button(w / 2 - buttonSize, h * 0.8f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		// Load bomb texture
		bombTexture = new Texture("items/bomb.png");
		bombs = new ArrayList<Bomb>();
		bombCooldown = 0;

		// Bomb explosion
		explosionTexture = new Texture("fxs/explosion.png");
		bombs = new ArrayList<Bomb>();

		newGame();
	}

	private void newGame() {
		gameState = GameState.PLAYING;

		// Translate camera to center of screen
		camera.position.x = 16; // The 16 is half the size of a tile
		camera.position.y = 16;

		lastTime = System.currentTimeMillis();
		elapsedTime = 0.0f;

		// Player start location, you can have this stored in the tiled map using an object layer
		player.setPosition(new Vector2(1, 18));
		movementCooldown = 0.0f;

		camera.translate(player.getPosition().x * 32, player.getPosition().y * 32);
		restartActive = false;
	}

	@Override
	public void render() {
		long currentTime = System.currentTimeMillis();
		// Divide by a thousand to convert from milliseconds to seconds
		elapsedTime = (currentTime - lastTime) / 1000.0f;
		lastTime = currentTime;
		stateTime += elapsedTime;

		// Update the Game State
		update();

		// Clear the screen before drawing
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Allows transparent sprites/tiles
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		// Render Map
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		// Draw Enemy
		spriteBatch.begin();
		for (Enemy enemy : enemies) {
			enemy.render(spriteBatch);
		}
		spriteBatch.end();

		// Draw player
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		player.update(Gdx.graphics.getDeltaTime());
		player.render(batch);
		batch.end();

		// Render bombs and explosions
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Bomb bomb : bombs) {
			bomb.render(batch);
		}
		batch.end();

		// Draw UI
		uiBatch.begin();
		switch (gameState) {
			case PLAYING:
				moveLeftButton.draw(uiBatch);
				moveRightButton.draw(uiBatch);
				moveDownButton.draw(uiBatch);
				moveUpButton.draw(uiBatch);
				placeBombButton.draw(uiBatch);
				break;
			case COMPLETE:
				restartButton.draw(uiBatch);
				break;
		}
		uiBatch.end();
	}

	private void update() {
		// Touch Input Info
		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		// Update Game State based on input
		switch (gameState) {
			case PLAYING:
				// Poll user for input
				moveLeftButton.update(checkTouch, touchX, touchY);
				moveRightButton.update(checkTouch, touchX, touchY);
				moveDownButton.update(checkTouch, touchX, touchY);
				moveUpButton.update(checkTouch, touchX, touchY);
				placeBombButton.update(checkTouch, touchX, touchY);

				float moveX = 0;
				float moveY = 0;
				if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || moveLeftButton.isDown) {
					moveLeftButton.isDown = true;
					moveX = -1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || moveRightButton.isDown) {
					moveRightButton.isDown = true;
					moveX = 1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || moveDownButton.isDown) {
					moveDownButton.isDown = true;
					moveY = -1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.UP) || moveUpButton.isDown) {
					moveUpButton.isDown = true;
					moveY = 1;
				}

				player.setVelocity(moveX, moveY);

				// Movement update
				if (player.canMove()) {
					// Retrieve Collision layer
					MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
					TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

					if ((moveX != 0 || moveY != 0)
							&& moveX + player.getPosition().x >= 0
							&& moveX + player.getPosition().x < tileLayer.getWidth()
							&& moveY + player.getPosition().y >= 0
							&& moveY + player.getPosition().y < tileLayer.getHeight()) {

						// Retrieve Target Tile
						TiledMapTileLayer.Cell targetCell = tileLayer.getCell((int) (player.getPosition().x + moveX),
								(int) (player.getPosition().y + moveY));
						// Move only if the target cell is empty
						if (targetCell == null) {
							camera.translate(moveX * 32, moveY * 32);
							player.move((int) moveX, (int) moveY);
						}
					}
				}

				for (Enemy enemy : enemies) {
					Vector2 enemyPosition = enemy.getPosition();
					TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
					enemy.update(collisionLayer);

					if (enemyPosition.epsilonEquals(player.getPosition(), 0.1f)) {
						killPlayer();
					}
				}

				if (player.getPosition().x == 18 && player.getPosition().y == 1) {
					gameState = GameState.COMPLETE;
				}

				// Handle bomb placement
				if (placeBombButton.isDown && bombCooldown <= 0) {
					Vector2 bombPosition = new Vector2(player.getPosition().x, player.getPosition().y);
					TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
					bombs.add(new Bomb(bombPosition, bombTexture, explosionTexture, BOMB_EXPLOSION_TIME, 0.5f, collisionLayer));
					bombCooldown = BOMB_COOLDOWN_TIME;
					placeBombButton.isDown = false; // Reset button state
				}

				if (bombCooldown > 0) {
					bombCooldown -= elapsedTime;
				}

				// Update bombs
				Iterator<Bomb> bombIterator = bombs.iterator();
				while (bombIterator.hasNext()) {
					Bomb bomb = bombIterator.next();
					bomb.update(elapsedTime, (TiledMapTileLayer) tiledMap.getLayers().get("Maze"));
					if (bomb.isFinished()) {
						bombIterator.remove();
					}
				}

				break;

			case COMPLETE:
				restartButton.update(checkTouch, touchX, touchY);

				if (Gdx.input.isKeyPressed(Input.Keys.DPAD_CENTER) || restartButton.isDown) {
					restartButton.isDown = true;
					restartActive = true;
				} else if (restartActive) {
					newGame();
				}
				break;
		}

		if (player.getCooldown() > 0.0f)
			player.reduceCooldown(elapsedTime);
	}

	@Override
	public void dispose() {
		buttonSquareTexture.dispose();
		buttonSquareDownTexture.dispose();
		buttonLongTexture.dispose();
		buttonLongDownTexture.dispose();
		tiledMap.dispose();
		bombTexture.dispose();
		explosionTexture.dispose();
	}

	public Vector2 getPlayerPosition() {
		return player.getPosition();
	}

	public void killPlayer() {
		gameState = GameState.COMPLETE;
		player.setPosition(new Vector2(1, 18)); // Reset player position
	}

	public void removeEnemy(Enemy enemy) {
		enemies.removeValue(enemy, true);
	}
}

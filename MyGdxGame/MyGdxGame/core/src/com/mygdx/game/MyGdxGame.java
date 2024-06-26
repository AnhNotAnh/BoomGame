package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {

	public enum GameState { MAIN_MENU, PLAYING, GAME_OVER, WIN }

	public static final float FRAME_COLS = 3;
	public static final float FRAME_COLSDEATH = 5;
	public static final float FRAME_ROWS = 1;
	public static final float BOMB_EXPLOSION_TIME = 2.0f;
	public static final float BOMB_COOLDOWN_TIME = 1.0f;
	public static final float MOVEMENT_COOLDOWN_TIME = 0.3f;

	private static MyGdxGame instance;

	public static MyGdxGame getInstance() {
		return instance;
	}

	public Sound getExplosionSound() {
		return explosionSound;
	}

	GameState gameState = GameState.MAIN_MENU;

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
	Animation<TextureRegion> enemyRightAnimation;
	Animation<TextureRegion> enemyLeftAnimation;
	Animation<TextureRegion> enemyFrontAnimation;
	Animation<TextureRegion> enemyBackAnimation;
	Animation<TextureRegion> enemyDeathAnimation;

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

	Array<Enemy> enemies;

	// UI textures
	Texture buttonSquareTexture;
	Texture buttonSquareDownTexture;
	Texture buttonLongTexture;
	Texture buttonLongDownTexture;
	Texture playButtonTexture;
	Texture exitButtonTexture;
	Texture retryButtonTexture;
	Texture menuButtonTexture;
	Texture winTexture;
	Texture heartTexture;

	// UI Buttons
	Button moveLeftButton;
	Button moveRightButton;
	Button moveDownButton;
	Button moveUpButton;
	Button placeBombButton;
	Button playButton;
	Button exitButton;
	Button retryButton;
	Button menuButton;

	// Bomb
	private Texture bombTexture;
	private ArrayList<Bomb> bombs;
	private float bombCooldown;

	private Texture explosionTexture;

	// Sounds and Music
	private Music backgroundMusic;
	private Sound movementSound;
	private Sound placeBombSound;
	private Sound buttonClickSound;
	private Sound explosionSound;

	private ShapeRenderer shapeRenderer;

	@Override
	public void create() {
		// Rendering
		batch = new SpriteBatch();
		uiBatch = new SpriteBatch();

		// Load map
		tiledMap = new TmxMapLoader().load("map/map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		// Camera setup
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(w, h);
		TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
		float mapWidth = layer.getTileWidth() * layer.getWidth();
		float mapHeight = layer.getTileHeight() * layer.getHeight();
		camera.zoom = Math.max(mapWidth / w, mapHeight / h);
		camera.update();

		// Textures
		buttonSquareTexture = new Texture("button/buttonSquare_blue.png");
		buttonSquareDownTexture = new Texture("button/buttonSquare_beige_pressed.png");
		buttonLongTexture = new Texture("button/buttonLong_blue.png");
		buttonLongDownTexture = new Texture("button/buttonLong_beige_pressed.png");
		playButtonTexture = new Texture("button/PlayBtn.png");
		exitButtonTexture = new Texture("button/CloseBtn.png");
		retryButtonTexture = new Texture("button/RestartBtn.png");
		menuButtonTexture = new Texture("button/MenuBtn.png");
		winTexture = new Texture("items/win.png"); // Load win texture
		heartTexture = new Texture("items/heart.png"); // Load heart texture

		// Initialize buttons
		float buttonSize = h * 0.2f;
		moveLeftButton = new Button(0.0f, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveRightButton = new Button(buttonSize * 2, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveDownButton = new Button(buttonSize, 0.0f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveUpButton = new Button(buttonSize, buttonSize * 2, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		placeBombButton = new Button(w - buttonSize, 0, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);

		float playButtonWidth = w * 0.1f;
		float playButtonHeight = h * 0.1f;
		float centerX = w / 2 - playButtonWidth / 2;
		float centerY = h / 2;
		playButton = new Button(centerX, centerY, playButtonWidth, playButtonHeight, playButtonTexture, playButtonTexture);
		exitButton = new Button(centerX, centerY - playButtonHeight - 20, playButtonWidth, playButtonHeight, exitButtonTexture, exitButtonTexture);

		retryButton = new Button(centerX, centerY, playButtonWidth, playButtonHeight, retryButtonTexture, retryButtonTexture);
		menuButton = new Button(centerX, centerY - playButtonHeight - 20, playButtonWidth, playButtonHeight, menuButtonTexture, menuButtonTexture);

		// Player
		player = new Player(new Vector2(1, 18));

		// Bomb
		bombTexture = new Texture("items/bomb.png");
		explosionTexture = new Texture("fxs/explosion.png");
		bombs = new ArrayList<>();
		bombCooldown = 0;

		// Enemy
		createEnemyAnimations();

		enemies = new Array<>();

		// Spawn enemies
		spawnEnemies();

		// Game state variables
		lastTime = System.currentTimeMillis();
		elapsedTime = 0.0f;
		stateTime = 0.0f;

		// Load sounds and music
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Sounds/background1.mp3"));
		movementSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/blink.wav"));
		placeBombSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/shoot.wav"));
		buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/jump.wav"));
		explosionSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/collision.wav"));
		instance = this;

		// Play background music
		backgroundMusic.setLooping(true);
		backgroundMusic.play();

		//Bounding box
		this.shapeRenderer = new ShapeRenderer();
	}

	private void createEnemyAnimations() {
		enemyFlyRight = new Texture(Gdx.files.internal("enemies/fly-right.png"));
		enemyFlyLeft = new Texture(Gdx.files.internal("enemies/fly-left.png"));
		enemyFlyFront = new Texture(Gdx.files.internal("enemies/fly-front.png"));
		enemyFlyBack = new Texture(Gdx.files.internal("enemies/fly-back.png"));
		enemyDeath = new Texture(Gdx.files.internal("enemies/death-front.png"));

		int frameCols = (int) FRAME_COLS;
		int frameRows = (int) FRAME_ROWS;
		int frameColsDeath = (int) FRAME_COLSDEATH;

		TextureRegion[][] tempRight = TextureRegion.split(enemyFlyRight, enemyFlyRight.getWidth() / frameCols, enemyFlyRight.getHeight() / frameRows);
		TextureRegion[][] tempLeft = TextureRegion.split(enemyFlyLeft, enemyFlyLeft.getWidth() / frameCols, enemyFlyLeft.getHeight() / frameRows);
		TextureRegion[][] tempFront = TextureRegion.split(enemyFlyFront, enemyFlyFront.getWidth() / frameCols, enemyFlyFront.getHeight() / frameRows);
		TextureRegion[][] tempBack = TextureRegion.split(enemyFlyBack, enemyFlyBack.getWidth() / frameCols, enemyFlyBack.getHeight() / frameRows);
		TextureRegion[][] tempDeath = TextureRegion.split(enemyDeath, enemyDeath.getWidth() / frameColsDeath, enemyDeath.getHeight() / frameRows);

		enemyFrameRight = new TextureRegion[frameCols * frameRows];
		enemyFrameLeft = new TextureRegion[frameCols * frameRows];
		enemyFramesBack = new TextureRegion[frameCols * frameRows];
		enemyFramesFront = new TextureRegion[frameCols * frameRows];
		enemyFramesDeath = new TextureRegion[frameColsDeath * frameCols];

		int index = 0;
		for (int i = 0; i < frameRows; i++) {
			for (int j = 0; j < frameCols; j++) {
				enemyFrameRight[index] = tempRight[i][j];
				enemyFrameLeft[index] = tempLeft[i][frameCols - j - 1];
				enemyFramesFront[index] = tempFront[i][j];
				enemyFramesBack[index] = tempBack[i][j];
				index++;
			}
		}

		index = 0;
		for (int i = 0; i < frameRows; i++) {
			for (int j = 0; j < frameColsDeath; j++) {
				enemyFramesDeath[index] = tempDeath[i][j];
				index++;
			}
		}

		spriteBatch = new SpriteBatch();
		enemyRightAnimation = new Animation<>(0.5f, enemyFrameRight);
		enemyLeftAnimation = new Animation<>(0.5f, enemyFrameLeft);
		enemyFrontAnimation = new Animation<>(0.5f, enemyFramesFront);
		enemyBackAnimation = new Animation<>(0.5f, enemyFramesBack);
		enemyDeathAnimation = new Animation<>(0.5f, enemyFramesDeath);
	}

	private void spawnEnemies() {
		MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
		TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

		Vector2 spawnPosition1 = new Vector2(5, 18);
		float worldX1 = spawnPosition1.x * tileLayer.getTileWidth();
		float worldY1 = spawnPosition1.y * tileLayer.getTileHeight();
		Vector2 worldSpawnPosition1 = new Vector2(worldX1, worldY1);
		Enemy enemy1 = new Enemy(worldSpawnPosition1, enemyRightAnimation, enemyLeftAnimation, enemyFrontAnimation, enemyBackAnimation, enemyDeathAnimation, this, player, tileLayer);
		enemies.add(enemy1);

		Vector2 spawnPosition2 = new Vector2(9, 5);
		float worldX2 = spawnPosition2.x * tileLayer.getTileWidth();
		float worldY2 = spawnPosition2.y * tileLayer.getTileHeight();
		Vector2 worldSpawnPosition2 = new Vector2(worldX2, worldY2);
		Enemy enemy2 = new Enemy(worldSpawnPosition2, enemyRightAnimation, enemyLeftAnimation, enemyFrontAnimation, enemyBackAnimation, enemyDeathAnimation, this, player, tileLayer);
		enemies.add(enemy2);
	}

	@Override
	public void render() {
		long currentTime = System.currentTimeMillis();
		elapsedTime = (currentTime - lastTime) / 1000.0f;
		lastTime = currentTime;
		stateTime += elapsedTime;

		switch (gameState) {
			case MAIN_MENU:
				renderMainMenu();
				break;
			case PLAYING:
				renderPlaying();
				break;
			case GAME_OVER:
				renderGameOver();
				break;
			case WIN:
				renderWin();
				break;
		}
	}

	private void renderMainMenu() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		playButton.update(checkTouch, touchX, touchY);
		exitButton.update(checkTouch, touchX, touchY);

		uiBatch.begin();
		playButton.draw(uiBatch);
		exitButton.draw(uiBatch);
		uiBatch.end();

		if (playButton.isDown) {
			Gdx.app.log("Button", "Play button clicked");
			buttonClickSound.play();
			gameState = GameState.PLAYING;
			newGame();
		} else if (exitButton.isDown) {
			Gdx.app.log("Button", "Exit button clicked");
			buttonClickSound.play();
			Gdx.app.exit();
		}
	}

	private void renderPlaying() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		updateGame();

		// Center the camera on the map
		float mapWidth = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getWidth() * ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getTileWidth();
		float mapHeight = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getHeight() * ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getTileHeight();
		camera.position.set(mapWidth / 2, mapHeight / 2, 0);
		camera.update();

		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		//spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		for (Enemy enemy : enemies) {
			enemy.render(spriteBatch);
		}
		spriteBatch.end();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		player.render(batch);
		for (Bomb bomb : bombs) {
			bomb.render(batch);
		}
		batch.end();

		//For measuring the bounding box of play, enemy.
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		if (player.getBoundingBox() != null) {
			shapeRenderer.rect(player.getBoundingBox().x, player.getBoundingBox().y,
					player.getBoundingBox().width, player.getBoundingBox().height);
		}
		for (Enemy enemy : enemies) {
			if (enemy.getBoundingBox() != null) {
				shapeRenderer.rect(enemy.getBoundingBox().x, enemy.getBoundingBox().y,
						enemy.getBoundingBox().width, enemy.getBoundingBox().height);
			}
		}
		shapeRenderer.end();

		// Render UI elements (e.g., buttons, player lives)
		uiBatch.begin();
		moveLeftButton.draw(uiBatch);
		moveRightButton.draw(uiBatch);
		moveDownButton.draw(uiBatch);
		moveUpButton.draw(uiBatch);
		placeBombButton.draw(uiBatch);

		// Draw player lives (hearts)
		for (int i = 0; i < player.getLives(); i++) {
			float heartX = 20 + i * 40;
			float heartY = Gdx.graphics.getHeight() - 40;
			uiBatch.draw(heartTexture, heartX, heartY, 32, 32);
		}
		uiBatch.end();
	}


	private void renderGameOver() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		retryButton.update(checkTouch, touchX, touchY);
		menuButton.update(checkTouch, touchX, touchY);

		uiBatch.begin();
		retryButton.draw(uiBatch);
		menuButton.draw(uiBatch);
		uiBatch.end();

		if (retryButton.isDown) {
			Gdx.app.log("Button", "Retry button clicked");
			buttonClickSound.play();
			gameState = GameState.PLAYING;
			newGame();
		} else if (menuButton.isDown) {
			Gdx.app.log("Button", "Menu button clicked");
			buttonClickSound.play();
			gameState = GameState.MAIN_MENU;
		}
	}

	private void renderWin() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		retryButton.update(checkTouch, touchX, touchY);
		menuButton.update(checkTouch, touchX, touchY);

		uiBatch.begin();
		uiBatch.draw(winTexture, Gdx.graphics.getWidth() / 2 - winTexture.getWidth() / 2, Gdx.graphics.getHeight() / 2 - winTexture.getHeight() / 2);
		retryButton.draw(uiBatch);
		menuButton.draw(uiBatch);
		uiBatch.end();

		if (retryButton.isDown) {
			Gdx.app.log("Button", "Retry button clicked");
			buttonClickSound.play();
			gameState = GameState.PLAYING;
			newGame();
		} else if (menuButton.isDown) {
			Gdx.app.log("Button", "Menu button clicked");
			buttonClickSound.play();
			gameState = GameState.MAIN_MENU;
		}
	}

	private void updateGame() {
		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

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

		if (moveX != 0 || moveY != 0) {
			movementSound.play();
		}

		player.setVelocity(moveX, moveY);

		if (player.canMove()) {
			TiledMapTileLayer tileLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");

			if ((moveX != 0 || moveY != 0)
					&& moveX + player.getPosition().x >= 0
					&& moveX + player.getPosition().x < tileLayer.getWidth()
					&& moveY + player.getPosition().y >= 0
					&& moveY + player.getPosition().y < tileLayer.getHeight()) {

				TiledMapTileLayer.Cell targetCell = tileLayer.getCell((int) (player.getPosition().x + moveX), (int) (player.getPosition().y + moveY));
				if (targetCell == null) {
					player.move((int) moveX, (int) moveY);
				}
			}
		}

		player.update(Gdx.graphics.getDeltaTime());

		for (Enemy enemy : enemies) {
			Vector2 enemyPosition = enemy.getPosition();
			enemy.update(elapsedTime);

			// Check for collision using bounding boxes
			/*if (player.getBoundingBox().overlaps(enemy.getBoundingBox())) {
				player.handleCollision(enemy.getPosition());
			}*/

			/*if (checkCollision(player, enemy)) {
				// Handle collision logic here (e.g., player loses a life)
				player.handleCollision(enemy.getPosition());

			}*/

			float playerRadius = player.getRadius(); // Or player's bounding box size
			float enemyRadius = enemy.getRadius(); // Or enemy's bounding box size

			// Calculate distance between player and enemy
			float distance = player.getPosition().dst(enemy.getPosition());

			// Check collision (simplified check)
			if (distance < playerRadius + enemyRadius) {
				// Handle collision (e.g., reduce player's lives)
				player.handleCollision(enemy.getPosition());
				// Print a message indicating collision
				Gdx.app.log("Collision", "Player collided with enemy!");
			}

		}

		if (placeBombButton.isDown && bombCooldown <= 0) {
			placeBombSound.play();
			Vector2 bombPosition = new Vector2(player.getPosition().x, player.getPosition().y);
			TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
			bombs.add(new Bomb(bombPosition, bombTexture, explosionTexture, BOMB_EXPLOSION_TIME, 0.5f, collisionLayer, this));
			bombCooldown = BOMB_COOLDOWN_TIME;
			placeBombButton.isDown = false;
		}

		if (bombCooldown > 0) {
			bombCooldown -= elapsedTime;
		}

		Iterator<Bomb> bombIterator = bombs.iterator();
		while (bombIterator.hasNext()) {
			Bomb bomb = bombIterator.next();
			bomb.update(elapsedTime, (TiledMapTileLayer) tiledMap.getLayers().get("Maze"));
			if (bomb.isFinished()) {
				bombIterator.remove();
			}
		}

		// Check if all enemies are killed
		if (enemies.size == 0) {
			// Check the map name property
			String mapName = tiledMap.getProperties().get("mapName", String.class);
			if ("secondMap".equals(mapName)) {
				gameState = GameState.WIN;
			} else if ("firstMap".equals(mapName)) {
				newMap("map/secondMap.tmx");
			}
		}

		if (player.getCooldown() > 0.0f)
			player.reduceCooldown(elapsedTime);
	}


	private void newGame() {
		gameState = GameState.PLAYING;
		lastTime = System.currentTimeMillis();
		elapsedTime = 0.0f;

		player.setPosition(new Vector2(1, 18));
		movementCooldown = 0.0f;

		camera.translate(player.getPosition().x * 32, player.getPosition().y * 32);

		bombs.clear();
		bombCooldown = 0;

		enemies.clear();
		spawnEnemies();
	}

	private void newMap(String mapPath){
		newGame();
		if (tiledMap != null) {
			tiledMap.dispose();
		}
		tiledMap = new TmxMapLoader().load(mapPath);
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
	}

	@Override
	public void dispose() {
		buttonSquareTexture.dispose();
		buttonSquareDownTexture.dispose();
		buttonLongTexture.dispose();
		buttonLongDownTexture.dispose();
		playButtonTexture.dispose();
		exitButtonTexture.dispose();
		retryButtonTexture.dispose();
		menuButtonTexture.dispose();
		winTexture.dispose();
		heartTexture.dispose();
		tiledMap.dispose();
		bombTexture.dispose();
		explosionTexture.dispose();
		backgroundMusic.dispose();
		movementSound.dispose();
		placeBombSound.dispose();
		buttonClickSound.dispose();
	}

	public Vector2 getPlayerPosition() {
		return player.getPosition();
	}

	public void killPlayer() {
		gameState = GameState.GAME_OVER;
		player.setPosition(new Vector2(1, 18));
	}

	public void removeEnemy(Enemy enemy) {
		enemies.removeValue(enemy, true);
	}

	public Array<Enemy> getEnemies() {
		return enemies;
	}

	private boolean checkCollision(Player player, Enemy enemy) {
		// Get positions and sizes of player and enemy
		Vector2 playerPosition = player.getPosition();
		float playerWidth = player.getWidth();
		float playerHeight = player.getHeight();

		Vector2 enemyPosition = enemy.getPosition();
		float enemyWidth = enemy.getWidth();
		float enemyHeight = enemy.getHeight();

		// Simple AABB collision detection
		if (playerPosition.x < enemyPosition.x + enemyWidth &&
				playerPosition.x + playerWidth > enemyPosition.x &&
				playerPosition.y < enemyPosition.y + enemyHeight &&
				playerPosition.y + playerHeight > enemyPosition.y) {
			Gdx.app.log("Collision", "Player hitted by enemy");
			return true; // Collision detected
		}

		return false; // No collision
	}
}

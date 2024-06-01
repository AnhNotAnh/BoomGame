package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {


	public enum GameState { PLAYING, COMPLETE };

	public static final float MOVEMENT_COOLDOWN_TIME = 0.3f;

	GameState gameState = GameState.PLAYING;

	//Map and rendering
	SpriteBatch batch;
	SpriteBatch uiBatch; //Second SpriteBatch without camera transforms, for drawing UI
	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;
	OrthographicCamera camera;


	//Game clock
	long lastTime;
	float elapsedTime;

	//Player Character
	Texture characterTexture;

	Player player;


	int characterX;
	int characterY;
	float movementCooldown;


	//Enemy
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
	Enemy enemy;

	//UI textures
	Texture buttonSquareTexture;
	Texture buttonSquareDownTexture;
	Texture buttonLongTexture;
	Texture buttonLongDownTexture;

	//UI Buttons
	Button moveLeftButton;
	Button moveRightButton;
	Button moveDownButton;
	Button moveUpButton;
	Button restartButton;
	//Just use this to only restart when the restart button is released instead of immediately as it's pressed
	boolean restartActive;

	//Bomb
	private Texture bombTexture;
	private ArrayList<Bomb> bombs;
	private float bombCooldown;
	private static final float BOMB_COOLDOWN_TIME = 1.0f; // Cooldown time in seconds
	private static final float BOMB_EXPLOSION_TIME = 2.0f; // Explosion time in seconds

	Button placeBombButton;


	@Override
	public void create() {
		// Rendering
		batch = new SpriteBatch();
		uiBatch = new SpriteBatch();

		tiledMap = new TmxMapLoader().load("map/SimpleMaze.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		// Camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w / 2, h / 2);

		// Textures
		characterTexture = new Texture("map/character.png");
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

		TextureRegion[][] tempRight = TextureRegion.split(enemyFlyRight, enemyFlyRight.getWidth()
				/ FRAME_COLS, enemyFlyRight.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempLeft = TextureRegion.split(enemyFlyLeft, enemyFlyLeft.getWidth()
				/ FRAME_COLS, enemyFlyLeft.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempFront = TextureRegion.split(enemyFlyFront, enemyFlyFront.getWidth()
				/ FRAME_COLS, enemyFlyFront.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempBack = TextureRegion.split(enemyFlyBack, enemyFlyBack.getWidth()
				/ FRAME_COLS, enemyFlyBack.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempDeath = TextureRegion.split(enemyDeath, enemyDeath.getWidth()
				/ FRAME_COLSDEATH, enemyDeath.getHeight() / FRAME_ROWS);

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

		for(int i = 0; i < FRAME_ROWS; i++){
			for(int j = 0; j < FRAME_COLSDEATH; j++){
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

		//Vector2 enemyPosition = new Vector2(50, 50);
		//enemy = new Enemy(enemyPosition, 50f, flyAnimationRight, flyAnimationLeft, flyAnimationFront, flyAnimationBack, this);
		enemies = new Array<Enemy>();


		// Buttons
		float buttonSize = h * 0.2f;
		moveLeftButton = new Button(0.0f, buttonSize, buttonSize, buttonSize, buttonSquareTexture,
				buttonSquareDownTexture);
		moveRightButton = new Button(buttonSize * 2, buttonSize, buttonSize, buttonSize, buttonSquareTexture,
				buttonSquareDownTexture);
		moveDownButton = new Button(buttonSize, 0.0f, buttonSize, buttonSize, buttonSquareTexture,
				buttonSquareDownTexture);
		moveUpButton = new Button(buttonSize, buttonSize * 2, buttonSize, buttonSize, buttonSquareTexture,
				buttonSquareDownTexture);
		restartButton = new Button(w / 2 - buttonSize * 2, h * 0.2f, buttonSize * 4, buttonSize, buttonLongTexture,
				buttonLongDownTexture);

		// Player
		player = new Player(new Vector2(1, 18));

		//Place bomb button
		placeBombButton = new Button(w / 2 - buttonSize, h * 0.8f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		//Load bomb texture
		bombTexture = new Texture("bomb.png");
		bombs = new ArrayList<Bomb>();
		bombCooldown = 0;

		newGame();
	}

	private void newGame() {
		gameState = GameState.PLAYING;

		//Translate camera to center of screen
		camera.position.x = 16; //The 16 is half the size of a tile
		camera.position.y = 16;

		lastTime = System.currentTimeMillis();
		elapsedTime = 0.0f;

		//Player start location, you can have this stored in the tilemaze using an object layer.
		characterX = 1;
		characterY = 18;
		movementCooldown = 0.0f;

		//Enemy start location

		camera.translate(characterX * 32, characterY * 32);
		restartActive = false;
	}

	@Override
	public void render () {
		long currentTime = System.currentTimeMillis();
		//Divide by a thousand to convert from milliseconds to seconds
		elapsedTime = (currentTime - lastTime) / 1000.0f;
		lastTime = currentTime;
		stateTime += elapsedTime;

		//Update the Game State
		update();


		//Clear the screen before drawing.
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //Allows transparent sprites/tiles
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		//TODO Render Map Here
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		//Draw Character
		//Apply the camera's transform to the SpriteBatch so the character is drawn in the correct
		//position on screen.
//		batch.setProjectionMatrix(camera.combined);
//		batch.begin();
//		batch.draw(characterTexture, characterX * 32, characterY * 32, 32, 32);
//		batch.end();

		//Draw Enemy
		spriteBatch.begin();
		for(Enemy enemy : enemies){
			enemy.render(spriteBatch);
		}
		spriteBatch.end();

		//Draw player
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		player.update(Gdx.graphics.getDeltaTime());
		player.render(batch);
		batch.end();

		// Render bombs
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Bomb bomb : bombs) {
			bomb.render(batch);
		}
		batch.end();

		//Draw UI
		uiBatch.begin();
		switch(gameState) {
			//if gameState is Running: Draw Controls
			case PLAYING:
				moveLeftButton.draw(uiBatch);
				moveRightButton.draw(uiBatch);
				moveDownButton.draw(uiBatch);
				moveUpButton.draw(uiBatch);
				placeBombButton.draw(uiBatch);
				break;
			//if gameState is Complete: Draw Restart button
			case COMPLETE:
				restartButton.draw(uiBatch);
				break;
		}
		uiBatch.end();
	}




//	private void update() {
//		//Touch Input Info
//		boolean checkTouch = Gdx.input.isTouched();
//		int touchX = Gdx.input.getX();
//		int touchY = Gdx.input.getY();
//
//		//Update Game State based on input
//		switch (gameState) {
//
//			case PLAYING:
//				//Poll user for input
//				moveLeftButton.update(checkTouch, touchX, touchY);
//				moveRightButton.update(checkTouch, touchX, touchY);
//				moveDownButton.update(checkTouch, touchX, touchY);
//				moveUpButton.update(checkTouch, touchX, touchY);
//
//				int moveX = 0;
//				int moveY = 0;
//				if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || moveLeftButton.isDown) {
//					moveLeftButton.isDown = true;
//					moveX -= 1;
//				} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || moveRightButton.isDown) {
//					moveRightButton.isDown = true;
//					moveX += 1;
//				} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || moveDownButton.isDown) {
//					moveDownButton.isDown = true;
//					moveY -= 1;
//				} else if (Gdx.input.isKeyPressed(Input.Keys.UP) || moveUpButton.isDown) {
//					moveUpButton.isDown = true;
//					moveY += 1;
//				}
//
//				//Movement update
//				if (movementCooldown <= 0.0f) { //Don't move every frame
//
//					//TODO Retrieve Collision layer
//					MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
//					TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;
//
//					//Don't do anything if we're not moving
//					if ((moveX != 0 || moveY != 0)
//							//TODO Also check map bounds to prevent exceptions when accessing map cells
//							&& moveX + characterX >= 0 && moveX + characterX < tileLayer.getWidth()
//							&& moveY + characterY >= 0 && moveY + characterY < tileLayer.getHeight()
//					) {
//						//TODO Retrieve Target Tile
//						TiledMapTileLayer.Cell targetCell = tileLayer.getCell(characterX + moveX, characterY + moveY);
//						//TODO Move only if the target cell is empty
//						if (targetCell == null) {
//							camera.translate(moveX * 32, moveY * 32);
//							characterX += moveX;
//							characterY += moveY;
//							movementCooldown = MOVEMENT_COOLDOWN_TIME;
//						} //Restrict movement for a moment
//					}
//				}
//
//				//Enemy update
//				Vector2 enemyPosition = enemy.getPosition();
//				enemy.update(new Vector2(characterX, characterY));
//
//				// Check for collisions between the enemy and the player
//				if (enemyPosition.epsilonEquals(new Vector2(characterX, characterY), 0.1f)) {
//					enemy.onTouchPlayer();
//				}
//
//				// Check for collisions between the enemy and walls
//				//if (isWall(enemyPosition)) {
//					// Enemy has collided with a wall
//				//	enemy.onTouchWall();
//				//}
//
//				//Check if player has met the winning condition
//				if (characterX == 18 && characterY == 1) {
//					//Player has won!
//					gameState = GameState.COMPLETE;
//				}
//				break;
//
//			case COMPLETE:
//				//Poll for input
//				restartButton.update(checkTouch, touchX, touchY);
//
//				if (Gdx.input.isKeyPressed(Input.Keys.DPAD_CENTER) || restartButton.isDown) {
//					restartButton.isDown = true;
//					restartActive = true;
//				} else if (restartActive) {
//					newGame();
//				}
//				break;
//		}
//
//		if (movementCooldown > 0.0f)
//			movementCooldown -= elapsedTime;
//	}



	private void update() {
		//Touch Input Info
		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		//Update Game State based on input
		switch (gameState) {
			case PLAYING:
				//Poll user for input
				spawnNewEnemy();
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

				//Movement update
				if (player.canMove()) {

					//TODO Retrieve Collision layer
					MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
					TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

					//TODO Also check map bounds to prevent exceptions when accessing map cells
					if ((moveX != 0 || moveY != 0)
							&& moveX + player.getPosition().x >= 0
							&& moveX + player.getPosition().x < tileLayer.getWidth()
							&& moveY + player.getPosition().y >= 0
							&& moveY + player.getPosition().y < tileLayer.getHeight()) {

						//TODO Retrieve Target Tile
						TiledMapTileLayer.Cell targetCell = tileLayer.getCell((int) (player.getPosition().x + moveX),
								(int) (player.getPosition().y + moveY));
						//TODO Move only if the target cell is empty
						if (targetCell == null) {
							camera.translate(moveX * 32, moveY * 32);
							player.move((int) moveX, (int) moveY);
						}
					}
				}

				for (Enemy enemy : enemies) {
					Vector2 enemyPosition = enemy.getPosition();
					enemy.update(new Vector2(characterX, characterY));

					if (enemyPosition.epsilonEquals(new Vector2(player.getPosition().x, player.getPosition().y), 0.1f)) {
						killPlayer();
					}

				}

				if (player.getPosition().x == 18 && player.getPosition().y == 1) {
					gameState = GameState.COMPLETE;
				}

				// Add bomb placement logic
				placeBombButton.update(checkTouch, touchX, touchY);

				if (placeBombButton.isDown && bombCooldown <= 0) {
					Vector2 bombPosition = new Vector2(player.getPosition().x, player.getPosition().y);
					bombs.add(new Bomb(bombPosition, bombTexture, BOMB_EXPLOSION_TIME));
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
					bomb.update(elapsedTime);
					if (bomb.hasExploded()) {
						// Handle bomb explosion effects
						bombIterator.remove();
						// Add explosion logic here
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
	public void dispose () {
		characterTexture.dispose();
		buttonSquareTexture.dispose();
		buttonSquareDownTexture.dispose();
		buttonLongTexture.dispose();
		buttonLongDownTexture.dispose();
		tiledMap.dispose();
		bombTexture.dispose();

	}

	public boolean isWall(Vector2 position) {
		MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
		if (collisionLayer instanceof TiledMapTileLayer) {
			TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;
			return tileLayer.getCell((int) position.x, (int) position.y) != null;
		}
		return false;
	}

	public Vector2 getPlayerPosition() {
		return  new Vector2(characterX * 32, characterY * 32);
	}

	public void killPlayer() {
		gameState = GameState.COMPLETE;
		characterX = 0;
		characterY = 0;
	}

	public void removeEnemy(Enemy enemy) {
		enemies.removeValue(enemy, true);
	}

	public void spawnNewEnemy() {
		if (enemies.size >= 1 && enemies.size <= 3) {
			return;
		}
		MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
		TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;
		// TODO: spawn inside the tiledMap, check if isWall() enemy cant spawn

		enemy = Add functionality to place bombs.new Enemy(player.getPosition(), 45f, enemyRightAnimation, enemyLeftAnimation, enemyFrontAnimation, enemyBackAnimation, enemyDeathAnimation, this);
	}
}



package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {


	public enum GameState { PLAYING, COMPLETE, GAMEOVER };

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
	int characterX;
	int characterY;
	float movementCooldown;


	//Enemy
	Animation flyAnimationRight;
	Animation flyAnimationLeft;
	Animation flyAnimationFront;
	Animation flyAnimationBack;

	TextureRegion currentFrame;
	TextureRegion[] flyFramesRight;
	TextureRegion[] flyFramesLeft;
	TextureRegion[] flyFramesBack;
	TextureRegion[] flyFramesFront;
	Texture enemyFlyRight;
	Texture enemyFlyLeft;
	Texture enemyFlyBack;
	Texture enemyFlyFront;
	SpriteBatch spriteBatch;
	int frameIndex;
	float stateTime;

	private static final int FRAME_COLS = 3;
	private static final int FRAME_ROWS = 1;

	Enemy enemy;
	MyGdxGame game;

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

		TextureRegion[][] tempRight = TextureRegion.split(enemyFlyRight, enemyFlyRight.getWidth()
				/ FRAME_COLS, enemyFlyRight.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempLeft = TextureRegion.split(enemyFlyLeft, enemyFlyLeft.getWidth()
				/ FRAME_COLS, enemyFlyLeft.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempFront = TextureRegion.split(enemyFlyFront, enemyFlyFront.getWidth()
				/ FRAME_COLS, enemyFlyFront.getHeight() / FRAME_ROWS);
		TextureRegion[][] tempBack = TextureRegion.split(enemyFlyBack, enemyFlyBack.getWidth()
				/ FRAME_COLS, enemyFlyBack.getHeight() / FRAME_ROWS);

		flyFramesRight = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		flyFramesLeft = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		flyFramesBack = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		flyFramesFront = new TextureRegion[FRAME_COLS * FRAME_ROWS];

		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				flyFramesRight[index] = tempRight[i][j];
				flyFramesLeft[index] = tempLeft[i][FRAME_COLS - j - 1];
				flyFramesFront[index] = tempFront[i][j];
				flyFramesBack[index] = tempBack[i][j];
				index++;
			}
		}

		spriteBatch = new SpriteBatch();
		flyAnimationRight = new Animation(0.5f, flyFramesRight);
		flyAnimationLeft = new Animation(0.5f, flyFramesLeft);
		flyAnimationFront = new Animation(0.5f, flyFramesFront);
		flyAnimationBack = new Animation(0.5f, flyFramesBack);
		stateTime = 0.33f;

		Vector2 enemyPosition = new Vector2(50, 50);
		enemy = new Enemy(enemyPosition, 50f, flyAnimationRight, flyAnimationLeft, flyAnimationFront, flyAnimationBack, this);

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
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(characterTexture, characterX * 32, characterY * 32, 32, 32);
		batch.end();

		//Draw Enemy
		spriteBatch.begin();
		enemy.render(spriteBatch);
		spriteBatch.end();


		//Draw UI
		uiBatch.begin();
		switch(gameState) {
			//if gameState is Running: Draw Controls
			case PLAYING:
				moveLeftButton.draw(uiBatch);
				moveRightButton.draw(uiBatch);
				moveDownButton.draw(uiBatch);
				moveUpButton.draw(uiBatch);
				break;
			//if gameState is Complete: Draw Restart button
			case COMPLETE:
				restartButton.draw(uiBatch);
				break;
		}
		uiBatch.end();
	}




	private void update() {
		//Touch Input Info
		boolean checkTouch = Gdx.input.isTouched();
		int touchX = Gdx.input.getX();
		int touchY = Gdx.input.getY();

		//Update Game State based on input
		switch (gameState) {

			case PLAYING:
				//Poll user for input
				moveLeftButton.update(checkTouch, touchX, touchY);
				moveRightButton.update(checkTouch, touchX, touchY);
				moveDownButton.update(checkTouch, touchX, touchY);
				moveUpButton.update(checkTouch, touchX, touchY);

				int moveX = 0;
				int moveY = 0;
				if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || moveLeftButton.isDown) {
					moveLeftButton.isDown = true;
					moveX -= 1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || moveRightButton.isDown) {
					moveRightButton.isDown = true;
					moveX += 1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || moveDownButton.isDown) {
					moveDownButton.isDown = true;
					moveY -= 1;
				} else if (Gdx.input.isKeyPressed(Input.Keys.UP) || moveUpButton.isDown) {
					moveUpButton.isDown = true;
					moveY += 1;
				}

				//Movement update
				if (movementCooldown <= 0.0f) { //Don't move every frame

					//TODO Retrieve Collision layer
					MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
					TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

					//Don't do anything if we're not moving
					if ((moveX != 0 || moveY != 0)
							//TODO Also check map bounds to prevent exceptions when accessing map cells
							&& moveX + characterX >= 0 && moveX + characterX < tileLayer.getWidth()
							&& moveY + characterY >= 0 && moveY + characterY < tileLayer.getHeight()
					) {
						//TODO Retrieve Target Tile
						TiledMapTileLayer.Cell targetCell = tileLayer.getCell(characterX + moveX, characterY + moveY);
						//TODO Move only if the target cell is empty
						if (targetCell == null) {
							camera.translate(moveX * 32, moveY * 32);
							characterX += moveX;
							characterY += moveY;
							movementCooldown = MOVEMENT_COOLDOWN_TIME;
						} //Restrict movement for a moment
					}
				}
				
				//Enemy update
				Vector2 enemyPosition = enemy.getPosition();
				enemy.update(new Vector2(characterX, characterY));

				// Check for collisions between the enemy and the player
				if (enemyPosition.epsilonEquals(new Vector2(characterX, characterY), 0.1f)) {
					enemy.onTouchPlayer();
				}

				// Check for collisions between the enemy and walls
				//if (isWall(enemyPosition)) {
					// Enemy has collided with a wall
				//	enemy.onTouchWall();
				//}

				//Check if player has met the winning condition
				if (characterX == 18 && characterY == 1) {
					//Player has won!
					gameState = GameState.COMPLETE;
				}
				break;

			case COMPLETE:
				//Poll for input
				restartButton.update(checkTouch, touchX, touchY);

				if (Gdx.input.isKeyPressed(Input.Keys.DPAD_CENTER) || restartButton.isDown) {
					restartButton.isDown = true;
					restartActive = true;
				} else if (restartActive) {
					newGame();
				}
				break;
		}

		if (movementCooldown > 0.0f)
			movementCooldown -= elapsedTime;
	}


	@Override
	public void dispose () {
		characterTexture.dispose();
		buttonSquareTexture.dispose();
		buttonSquareDownTexture.dispose();
		buttonLongTexture.dispose();
		buttonLongDownTexture.dispose();
		tiledMap.dispose();

	}

	public Vector2 getPlayerPosition() {
		return  new Vector2(characterX * 32, characterY * 32);
	}

	public void killPlayer() {
		gameState = GameState.GAMEOVER;

		characterX = 0;
		characterY = 0;
	}

}

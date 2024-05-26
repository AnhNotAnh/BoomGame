package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
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
	int characterX;
	int characterY;
	float movementCooldown;

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
	public void create () {
		//Rendering
		batch = new SpriteBatch();
		uiBatch = new SpriteBatch();

		tiledMap = new TmxMapLoader().load( "map/SimpleMaze.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		//Camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w / 2, h / 2);

		//Textures
		characterTexture = new Texture("map/character.png");
		buttonSquareTexture = new Texture("button/buttonSquare_blue.png");
		buttonSquareDownTexture = new Texture("button/buttonSquare_beige_pressed.png");
		buttonLongTexture = new Texture("button/buttonLong_blue.png");
		buttonLongDownTexture = new Texture("button/buttonLong_beige_pressed.png");

		//Buttons
		float buttonSize = h * 0.2f;
		moveLeftButton = new Button(0.0f, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveRightButton = new Button(buttonSize*2, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveDownButton = new Button(buttonSize, 0.0f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		moveUpButton = new Button(buttonSize, buttonSize*2, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
		restartButton = new Button(w/2 - buttonSize*2, h * 0.2f, buttonSize*4, buttonSize, buttonLongTexture, buttonLongDownTexture);

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

		camera.translate(characterX * 32, characterY * 32);
		restartActive = false;
	}

	@Override
	public void render () {
		long currentTime = System.currentTimeMillis();
		//Divide by a thousand to convert from milliseconds to seconds
		elapsedTime = (currentTime - lastTime) / 1000.0f;
		lastTime = currentTime;

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
}

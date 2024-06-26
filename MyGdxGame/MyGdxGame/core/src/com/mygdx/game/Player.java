package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player implements CollidableObject {
    private Vector2 position;
    private Vector2 velocity;
    private float movementCooldown;
    private MyGdxGame game;

    private Texture deathFront;
    private Texture idleBack;
    private Texture idleFront;
    private Texture idleLeft;
    private Texture idleRight;
    private Texture takeOffFront;
    private Texture walkBack;
    private Texture walkFront;
    private Texture walkLeft;
    private Texture walkRight;
    private Texture winFront;

    private Animation<TextureRegion> walkAnimationFront;
    private Animation<TextureRegion> walkAnimationBack;
    private Animation<TextureRegion> walkAnimationLeft;
    private Animation<TextureRegion> walkAnimationRight;
    private Animation<TextureRegion> idleAnimationFront;
    private Animation<TextureRegion> idleAnimationBack;
    private Animation<TextureRegion> idleAnimationLeft;
    private Animation<TextureRegion> idleAnimationRight;
    private Animation<TextureRegion> winAnimationFront;
    private Animation<TextureRegion> takeOffAnimationFront;

    private Animation<TextureRegion> deathAnimationFront;

    private float stateTime;

    private float width;
    private float height;
    private Rectangle boundingBox;

    public enum State {
        IDLE, WALKING, TAKING_OFF, WINNING, DYING, DEAD
    }

    private State currentState;
    private State previousState;

    private float bombCooldown;
    private static final float BOMB_COOLDOWN_TIME = 1.0f; // Cooldown time in seconds

    private int lives;

    private float frame = 0;

    public Player(Vector2 startPosition) {
        this.position = startPosition;
        this.velocity = new Vector2(0, 0);
        this.movementCooldown = 0;
        this.bombCooldown = 0;
        this.lives = 3; // Initialize with 3 lives
        this.width = 32;
        this.height = 32;
        this.boundingBox = new Rectangle(position.x, position.y, width, height); // No offset

        // Load player textures
        deathFront = new Texture("character/death-front.png");
        idleBack = new Texture("character/idle-back.png");
        idleFront = new Texture("character/idle-front.png");
        idleLeft = new Texture("character/idle-left.png");
        idleRight = new Texture("character/idle-right.png");
        takeOffFront = new Texture("character/take-off-front.png");
        walkBack = new Texture("character/walk-back.png");
        walkFront = new Texture("character/walk-front.png");
        walkLeft = new Texture("character/walk-left.png");
        walkRight = new Texture("character/walk-right.png");
        winFront = new Texture("character/win-front.png");

        // Create animations
        walkAnimationFront = createAnimation(walkFront, 4, 1);
        walkAnimationBack = createAnimation(walkBack, 4, 1);
        walkAnimationLeft = createAnimation(walkLeft, 4, 1);
        walkAnimationRight = createAnimation(walkRight, 4, 1);
        idleAnimationFront = createAnimation(idleFront, 4, 1);
        idleAnimationBack = createAnimation(idleBack, 4, 1);
        idleAnimationLeft = createAnimation(idleLeft, 4, 1);
        idleAnimationRight = createAnimation(idleRight, 4, 1);
        winAnimationFront = createAnimation(winFront, 2, 1);
        takeOffAnimationFront = createAnimation(takeOffFront, 2, 1);
        deathAnimationFront = createAnimation(deathFront, 5, 1);

        stateTime = 0f;

        currentState = State.IDLE;
        previousState = State.IDLE;
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int frameCols, int frameRows) {
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / frameCols, texture.getHeight() / frameRows);
        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];
        int index = 0;
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return new Animation<TextureRegion>(0.1f, frames);
    }

    public void move(int moveX, int moveY) {
        this.position.add(moveX, moveY);
        this.movementCooldown = MyGdxGame.MOVEMENT_COOLDOWN_TIME;
        this.boundingBox.setPosition(position.x, position.y); // Update bounding box position
    }

    public void setPosition(Vector2 newPosition) {
        this.position = newPosition;
        this.boundingBox.setPosition(position.x, position.y); // Update bounding box position
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getRadius() {
        return Math.max(width, height) / 2;
    }

    public void resetCooldown() {
        this.movementCooldown = 0;
    }

    public void reduceCooldown(float amount) {
        this.movementCooldown -= amount;
    }

    public boolean canMove() {
        return movementCooldown <= 0;
    }

    public float getCooldown() {
        return movementCooldown;
    }

    public boolean canPlaceBomb() {
        return bombCooldown <= 0;
    }

    public void placeBomb() {
        this.bombCooldown = BOMB_COOLDOWN_TIME;
    }

    public void render(SpriteBatch batch) {
        if(!(getFrame() == null)) {
            TextureRegion currentFrame = getFrame();
            batch.draw(currentFrame, position.x * 32, position.y * 32, 32, 32);
        }
    }

    private TextureRegion getFrame() {
        TextureRegion region;

        switch (currentState) {
            case WALKING:
                if (velocity.y > 0) {
                    region = walkAnimationBack.getKeyFrame(stateTime, true);
                } else if (velocity.y < 0) {
                    region = walkAnimationFront.getKeyFrame(stateTime, true);
                } else if (velocity.x < 0) {
                    region = walkAnimationLeft.getKeyFrame(stateTime, true);
                } else {
                    region = walkAnimationRight.getKeyFrame(stateTime, true);
                }
                break;
            case DYING:
                region = deathAnimationFront.getKeyFrame(stateTime, true);
                break;
            case DEAD:
                region = null;
                break;
            case IDLE:
            default:
                region = idleAnimationFront.getKeyFrame(stateTime, true); // Default idle animation
                break;
        }

        return region;
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        if (bombCooldown > 0) {
            bombCooldown -= deltaTime;
        }

        boundingBox.setPosition(position.x, position.y); // Ensure bounding box is updated

        if (this.currentState == State.DYING){
            this.frame += 5 * deltaTime;
            if (this.frame >= 20) {
                this.currentState = Player.State.DEAD;
            }
        }
        if (this.currentState == State.DEAD) {
            respawn();
        }
        if (lives <= 0) {
            game.killPlayer(); // Call the method to handle game over
        }

        // Update state based on movement
        if (velocity.x == 0 && velocity.y == 0) {
            currentState = State.IDLE;
        } else {
            currentState = State.WALKING;
        }

        // Change animation based on state and direction
        if (currentState != previousState) {
            stateTime = 0; // reset animation time
        }
        previousState = currentState;
    }

    public int getLives() {
        return lives;
    }

    public void dispose() {
        deathFront.dispose();
        idleBack.dispose();
        idleFront.dispose();
        idleLeft.dispose();
        idleRight.dispose();
        takeOffFront.dispose();
        walkBack.dispose();
        walkFront.dispose();
        walkLeft.dispose();
        walkRight.dispose();
        winFront.dispose();
    }

    @Override
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void handleCollision(Vector2 position) {
        if (currentState != State.DYING) {
            currentState = State.DYING;
            stateTime = 0; // Reset the animation time for the death animation
            velocity.set(0, 0); // Stop the player's movement
            movementCooldown = 0; // Reset the movement cooldown
            bombCooldown = BOMB_COOLDOWN_TIME; // Reset the bomb cooldown
            // Decrease lives
            lives--;
        }
    }

    public void respawn(){
        setPosition(new Vector2(1, 18));
        stateTime = 0f;
        currentState = State.IDLE;
        previousState = State.IDLE;
        this.movementCooldown = 0;
        this.bombCooldown = 0;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }
}

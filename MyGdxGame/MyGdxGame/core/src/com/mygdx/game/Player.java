// src/com/mygdx/game/Player.java

package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Vector2 position;
    private Vector2 velocity;
    private float movementCooldown;

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

    private float stateTime;

    public enum State {
        IDLE, WALKING, TAKING_OFF, WINNING, DYING
    }

    private State currentState;
    private State previousState;

    private float bombCooldown;
    private static final float BOMB_COOLDOWN_TIME = 1.0f; // Cooldown time in seconds

    public Player(Vector2 startPosition) {
        this.position = startPosition;
        this.velocity = new Vector2(0, 0);
        this.movementCooldown = 0;
        this.bombCooldown = 0;



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
    }

    public void setPosition(Vector2 newPosition) {
        this.position = newPosition;
    }

    public Vector2 getPosition() {
        return position;
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
        TextureRegion currentFrame = getFrame();
        batch.draw(currentFrame, position.x * 32, position.y * 32, 32, 32);
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
}

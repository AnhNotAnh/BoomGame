package com.mygdx.game;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Enemy implements CollidableObject {
    private MyGdxGame game;
    private Player player;
    private Vector2 position;
    private float speed;
    private float stateTime;
    private Animation<TextureRegion> animationRight;
    private Animation<TextureRegion> animationLeft;
    private Animation<TextureRegion> animationFront;
    private Animation<TextureRegion> animationBack;
    private Animation<TextureRegion> animationDeath;
    private EnemyState currentState;
    private float width = 32f;
    private float height = 32f;
    private Random random;
    private long lastDirectionChangeTime;
    private TiledMapTileLayer collisionLayer;

    public Enemy(Vector2 position, Animation<TextureRegion> animationRight, Animation<TextureRegion> animationLeft,
                 Animation<TextureRegion> animationFront, Animation<TextureRegion> animationBack, Animation<TextureRegion> animationDeath, MyGdxGame game, Player player, TiledMapTileLayer collisionLayer) {
        this.position = position;
        this.speed = 30f;
        this.animationRight = animationRight;
        this.animationLeft = animationLeft;
        this.animationFront = animationFront;
        this.animationBack = animationBack;
        this.animationDeath = animationDeath;
        this.stateTime = 0.33f;
        this.currentState = EnemyState.MOVING_RIGHT; // Initial state
        this.game = game;
        this.random = new Random();
        this.player = player;
        this.collisionLayer = collisionLayer;
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getRadius() {
        return Math.max(width, height) / 2;
    }

    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(this.position.x, this.position.y, width, height);
    }

    @Override
    public void handleCollision(Vector2 position) {
        if (currentState != EnemyState.DIE) {
            currentState = EnemyState.DIE;
            stateTime = 0; // Reset the animation time for the death animation
            Gdx.app.log("Bomb", "Enemy hit by explosion at position: " + position);
        }
    }

    public void update(float dt) {
        stateTime += dt;

        // Calculate the next position based on the current state and speed
        Vector2 nextPosition = new Vector2(position);
        switch (currentState) {
            case MOVING_UP:
                nextPosition.y += speed * dt;
                break;
            case MOVING_DOWN:
                nextPosition.y -= speed * dt;
                break;
            case MOVING_RIGHT:
                nextPosition.x += speed * dt;
                break;
            case MOVING_LEFT:
                nextPosition.x -= speed * dt;
                break;
            case DIE:
                game.removeEnemy(this);
                return; // Stop further updates for a dying enemy
        }

        // Check if the player is in line of sight
        if (isPlayerInLineOfSight()){
            chase(player.getPosition());
            // Check for collisions with walls
            if (isCollision(nextPosition, currentState, collisionLayer)) {
                changeDirectionRandomly();
            }
            else{
                position.set(nextPosition);
            }
        }
        else {
            if (isCollision(nextPosition, currentState, collisionLayer)) {
                changeDirectionRandomly();
            }
            else {
                position.set(nextPosition);
            }
        }
    }

    private boolean isCollision(Vector2 nextPosition, EnemyState direction, TiledMapTileLayer collisionLayer) {
        float tileWidth = collisionLayer.getTileWidth();
        float tileHeight = collisionLayer.getTileHeight();

        int tileX = (int) (nextPosition.x / tileWidth);
        int tileY = (int) (nextPosition.y / tileHeight);

        switch (direction) {
            case MOVING_UP:
                tileY = (int) ((nextPosition.y + height) / tileHeight);
                break;
            case MOVING_DOWN:
                tileY = (int) (nextPosition.y / tileHeight);
                break;
            case MOVING_RIGHT:
                tileX = (int) ((nextPosition.x + width) / tileWidth);
                break;
            case MOVING_LEFT:
                tileX = (int) (nextPosition.x / tileWidth);
                break;
            case DIE:
                // No need to check for collisions when the enemy is dying
                break;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }

    private void changeDirectionRandomly() {
        EnemyState[] states = {EnemyState.MOVING_UP, EnemyState.MOVING_DOWN, EnemyState.MOVING_LEFT, EnemyState.MOVING_RIGHT};
        currentState = states[random.nextInt(states.length)];
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame;

        switch (currentState) {
            case MOVING_UP:
                currentFrame = animationFront.getKeyFrame(stateTime, true);
                break;
            case MOVING_DOWN:
                currentFrame = animationBack.getKeyFrame(stateTime, true);
                break;
            case MOVING_RIGHT:
                currentFrame = animationRight.getKeyFrame(stateTime, true);
                break;
            case MOVING_LEFT:
                currentFrame = animationLeft.getKeyFrame(stateTime, true);
                break;
            case DIE:
                currentFrame = animationDeath.getKeyFrame(stateTime, true);
                break;
            default:
                currentFrame = animationRight.getKeyFrame(stateTime, true);
                break;
        }

        Vector3 position3D = new Vector3(position.x, position.y, 0);
        game.camera.project(position3D);

        batch.draw(currentFrame, position3D.x, position3D.y, currentFrame.getRegionWidth() * 2f,
                currentFrame.getRegionHeight() * 2f);
    }

    private boolean isPlayerInLineOfSight() {
        Vector2 playerPosition = player.getPosition();
        float distance = position.dst(playerPosition);
        return distance < 50;
    }

    public void chase(Vector2 playerPosition) {
        float dx = playerPosition.x - position.x;
        float dy = playerPosition.y - position.y;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx > 0) {
                currentState = EnemyState.MOVING_RIGHT;
            } 
            else {
                currentState = EnemyState.MOVING_LEFT;
            }
        } 
        else {
            if (dy > 0) {
                currentState = EnemyState.MOVING_UP;
            } 
            else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
    }

    public enum EnemyState {
        MOVING_UP,
        MOVING_RIGHT,
        MOVING_LEFT,
        MOVING_DOWN,
        DIE
    }
}

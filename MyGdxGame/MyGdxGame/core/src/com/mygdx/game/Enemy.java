package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
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
    private Vector2 position;
    private float speed = 50f;
    private float stateTime;
    private Animation<TextureRegion> animationRight;
    private Animation<TextureRegion> animationLeft;
    private Animation<TextureRegion> animationFront;
    private Animation<TextureRegion> animationBack;
    private Animation<TextureRegion> animationDeath;
    private EnemyState currentState;
    private float width = 32f;
    private float height = 32f;

    public Enemy(Vector2 position, float speed, Animation<TextureRegion> animationRight, Animation<TextureRegion> animationLeft,
                 Animation<TextureRegion> animationFront, Animation<TextureRegion> animationBack, Animation<TextureRegion> animationDeath, MyGdxGame game) {
        this.position = position;
        this.speed = speed;
        this.animationRight = animationRight;
        this.animationLeft = animationLeft;
        this.animationFront = animationFront;
        this.animationBack = animationBack;
        this.animationDeath = animationDeath;
        this.stateTime = 0.33f;
        this.currentState = EnemyState.MOVING_RIGHT; // Initial state
        this.game = game;
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
        return new Rectangle(this.position.x + 5, this.position.y + 5, 40, 35);
    }

    @Override
    public void handleCollision(Vector2 position, float radius) {
        if (currentState != EnemyState.DIE) {
            Vector2 enemyPosition = getPosition();
            float distance = enemyPosition.dst(position);

            if (distance <= radius) {
                currentState = EnemyState.DIE;
                stateTime = 0; // Reset the animation time for the death animation
            }
        }
    }

    public void update(TiledMapTileLayer collisionLayer) {
        float dt = Gdx.graphics.getDeltaTime();
        stateTime += dt;

        Vector2 nextPosition = new Vector2(position);
        Vector2 playerPosition = game.player.getPosition();

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
                nextPosition.x = 0;
                nextPosition.y = 0;
                speed = 0;
        }

        if (!isCollision(nextPosition, collisionLayer)) {
            position.set(nextPosition);
        } else {
            // Handle collision
            changeDirection(collisionLayer);
        }

        // Update the enemy's direction based on the player's position
        updateDirection(playerPosition);
    }

    private void updateDirection(Vector2 playerPosition) {
        float dx = playerPosition.x - position.x;
        float dy = playerPosition.y - position.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                currentState = EnemyState.MOVING_RIGHT;
            } else {
                currentState = EnemyState.MOVING_LEFT;
            }
        } else {
            if (dy > 0) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
    }

    private void changeDirection(TiledMapTileLayer collisionLayer) {
    List<EnemyState> availableDirections = new ArrayList<>();

    // Check which directions are available (no collision)
    for (EnemyState state : EnemyState.values()) {
        if (state != EnemyState.DIE) {
            Vector2 nextPosition = new Vector2(position);
            switch (state) {
                case MOVING_UP:
                    nextPosition.y += 1;
                    break;
                case MOVING_DOWN:
                    nextPosition.y -= 1;
                    break;
                case MOVING_RIGHT:
                    nextPosition.x += 1;
                    break;
                case MOVING_LEFT:
                    nextPosition.x -= 1;
                    break;
            }
            if (!isCollision(nextPosition, collisionLayer)) {
                availableDirections.add(state);
            }
        }
    }

    // If there are available directions, choose one randomly
    if (!availableDirections.isEmpty()) {
        currentState = availableDirections.get(new Random().nextInt(availableDirections.size()));
    }
}

    private boolean isCollision(Vector2 nextPosition, TiledMapTileLayer collisionLayer) {
        int tileX = (int) (nextPosition.x / collisionLayer.getTileWidth());
        int tileY = (int) (nextPosition.y / collisionLayer.getTileHeight());
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
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

    public enum EnemyState {
        MOVING_UP,
        MOVING_RIGHT,
        MOVING_LEFT,
        MOVING_DOWN,
        DIE
    }
}

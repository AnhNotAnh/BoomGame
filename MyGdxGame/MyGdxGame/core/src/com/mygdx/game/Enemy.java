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
    private Random random;

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
        this.random = new Random();
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

        Vector2 playerPosition = game.player.getPosition();

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

        // Check collision with walls
        if (!isCollision(nextPosition, collisionLayer)) {
            position.set(nextPosition); // Move to the next position if no collision
        } else {
            // Handle collision by changing direction
            changeDirection(collisionLayer);

            // Update position based on the new direction
            switch (currentState) {
                case MOVING_UP:
                    position.y += speed * dt;
                    break;
                case MOVING_DOWN:
                    position.y -= speed * dt;
                    break;
                case MOVING_RIGHT:
                    position.x += speed * dt;
                    break;
                case MOVING_LEFT:
                    position.x -= speed * dt;
                    break;
                case DIE:
                    game.removeEnemy(this);
                    return; // Stop further updates for a dying enemy
            }
        }
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

        // Check all possible directions except DIE and current state
        for (EnemyState state : EnemyState.values()) {
            if (state != EnemyState.DIE && state != currentState) {
                Vector2 direction = getDirectionVector(state);
                Vector2 nextPosition = new Vector2(position).add(direction.scl(speed * Gdx.graphics.getDeltaTime()));

                if (!isCollision(nextPosition, collisionLayer)) {
                    availableDirections.add(state);
                }
            }
        }

        // Choose a random direction from available directions
        if (!availableDirections.isEmpty()) {
            currentState = availableDirections.get(random.nextInt(availableDirections.size()));
        } else {
            // If no available directions, stop moving
            currentState = EnemyState.DIE;
        }
    }

    private EnemyState getOppositeDirection(EnemyState state) {
        switch (state) {
            case MOVING_UP:
                return EnemyState.MOVING_DOWN;
            case MOVING_DOWN:
                return EnemyState.MOVING_UP;
            case MOVING_RIGHT:
                return EnemyState.MOVING_LEFT;
            case MOVING_LEFT:
                return EnemyState.MOVING_RIGHT;
            default:
                return EnemyState.DIE;
        }
    }

    private Vector2 getDirectionVector(EnemyState state) {
        switch (state) {
            case MOVING_UP:
                return new Vector2(0, 1);
            case MOVING_DOWN:
                return new Vector2(0, -1);
            case MOVING_RIGHT:
                return new Vector2(1, 0);
            case MOVING_LEFT:
                return new Vector2(-1, 0);
            default:
                return Vector2.Zero;
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

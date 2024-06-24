package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Enemy implements CollidableObject{
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

    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(this.position.x + 5,this.position.y + 5 ,40,35);
    }

    @Override
    public void handleCollision() {
        //maybe change to state DIE here
    }

    public enum EnemyState {
        MOVING_UP,
        MOVING_RIGHT,
        MOVING_LEFT,
        MOVING_DOWN,
        DIE
    }

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

    public void update(TiledMapTileLayer collisionLayer) {
        float dt = Gdx.graphics.getDeltaTime();
        stateTime += dt;

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
                nextPosition.x = 0;
                nextPosition.y = 0;
                speed = 0;
        }

        if (!isCollision(nextPosition)) {
            position.set(nextPosition);
        } else {
            // Handle collision
            changeDirection();
        }
    }

    private void changeDirection() {
        switch (currentState) {
            case MOVING_UP:
                currentState = EnemyState.MOVING_RIGHT;
                break;
            case MOVING_RIGHT:
                currentState = EnemyState.MOVING_DOWN;
                break;
            case MOVING_DOWN:
                currentState = EnemyState.MOVING_LEFT;
                break;
            case MOVING_LEFT:
                currentState = EnemyState.MOVING_UP;
                break;
            case DIE:
                // Do nothing
                break;
        }
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

        batch.draw(currentFrame, position3D.x, position3D.y, currentFrame.getRegionWidth() * 2.5f,
                currentFrame.getRegionHeight() * 2.5f);
    }

    private boolean isCollision(Vector2 nextPosition) {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) game.tiledMap.getLayers().get("Collision");
        int tileX = (int) (nextPosition.x / collisionLayer.getTileWidth());
        int tileY = (int) (nextPosition.y / collisionLayer.getTileHeight());
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }
}

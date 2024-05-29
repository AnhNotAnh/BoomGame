package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy {
    MyGdxGame game;
    Vector2 position;
    float speed = 50f;
    float stateTime;

    Animation<TextureRegion> animationRight;
    Animation<TextureRegion> animationLeft;
    Animation<TextureRegion> animationUp;
    Animation<TextureRegion> animationDown;

    EnemyState currentState;

    public enum EnemyState {
        MOVING_UP,
        MOVING_DOWN,
        PATROLLING,
        BOOSTING,
        CHASING,
        DODGING,
        FLEEING
    }

    public Enemy(Vector2 position, float speed, Animation<TextureRegion> animationRight, Animation<TextureRegion> animationLeft,
                 Animation<TextureRegion> animationUp, Animation<TextureRegion> animationDown, MyGdxGame game) {
        this.position = position;
        this.speed = speed;
        this.animationRight = animationRight;
        this.animationLeft = animationLeft;
        this.animationUp = animationUp;
        this.animationDown = animationDown;
        this.stateTime = 0.33f;
        this.currentState = EnemyState.PATROLLING;
        this.game = game;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getAngle(Vector2 target) {
        float angle = (float) Math.toDegrees(Math.atan2(target.y - this.position.y, target.x - this.position.x));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public boolean canSeePlayer() {
        Vector2 playerPosition = game.getPlayerPosition();
        float angle = this.getAngle(playerPosition);
        if (playerPosition.x < this.position.x) {
            if (angle > 170 && angle < 190) {
                return true;
            }
        }
        return false;
    }

    public void update(Vector2 targetPosition) {
        float dt = Gdx.graphics.getDeltaTime();
        stateTime += dt;
        

        switch (this.currentState) {
            case MOVING_UP:
                this.position.y += this.speed * dt;
                break;
            case MOVING_DOWN:
                this.position.y -= this.speed * dt;
                break;
            case PATROLLING:
                this.position.x -= this.speed * dt;
                break;
            case BOOSTING:
                this.position.x -= this.speed * dt * 5;
                if (!this.canSeePlayer()) {
                    this.currentState = EnemyState.PATROLLING;
                }
                break;
            case CHASING:
                if (this.position.y < game.getPlayerPosition().y)
                    this.position.y += this.speed * dt;
                if (this.position.y > game.getPlayerPosition().y)
                    this.position.y -= this.speed * dt;
                this.position.x -= this.speed * dt * 3;
                if (!this.canSeePlayer()) {
                    this.currentState = EnemyState.PATROLLING;
                }
                break;
            case DODGING:
                if (this.position.y < game.getPlayerPosition().y)
                    this.position.y -= this.speed * dt * 3;
                if (this.position.y > game.getPlayerPosition().y)
                    this.position.y += this.speed * dt * 3;
                this.position.x -= this.speed * dt;
                break;
            case FLEEING:
                this.position.x += this.speed * dt * 5;
                break;
        }
    }

    public void render(SpriteBatch batch) {

        TextureRegion currentFrame;
        switch (this.currentState) {
            case MOVING_UP:
                currentFrame = animationUp.getKeyFrame(stateTime, true);
                break;
            case MOVING_DOWN:
                currentFrame = animationDown.getKeyFrame(stateTime, true);
                break;
            case PATROLLING:
            case BOOSTING:
            case CHASING:
            case DODGING:
            case FLEEING:
                if (this.position.x > game.getPlayerPosition().x) {
                    currentFrame = animationLeft.getKeyFrame(stateTime, true);
                } else {
                    currentFrame = animationRight.getKeyFrame(stateTime, true);
                }
                break;
            default:
                currentFrame = animationRight.getKeyFrame(stateTime, true);
                break;
        }
        batch.draw(currentFrame, this.position.x, this.position.y);
    }


    public void onTouchPlayer() {
        game.killPlayer();

        Vector2 playerPosition = game.getPlayerPosition();
        Vector2 direction = position.cpy().sub(playerPosition);
        position.add(direction.scl(speed * Gdx.graphics.getDeltaTime()));
    }

    public void onTouchWall() {
       this.currentState = EnemyState.PATROLLING;
    }
}


package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Enemy {
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

    public enum EnemyState {
        MOVING_UP,
        MOVING_DOWN,
        PATROLLING,
        BOOSTING,
        CHASING,
        DODGING,
        FLEEING,
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
            case PATROLLING:
                nextPosition.x -= speed * dt;
                break;
            case BOOSTING:
                nextPosition.x -= speed * dt * 5;
                if (!canSeePlayer()) {
                    currentState = EnemyState.PATROLLING;
                }
                break;
            case CHASING:
                if (nextPosition.y < game.getPlayerPosition().y)
                    nextPosition.y += speed * dt;
                if (nextPosition.y > game.getPlayerPosition().y)
                    nextPosition.y -= speed * dt;
                nextPosition.x -= speed * dt * 3;
                if (!canSeePlayer()) {
                    currentState = EnemyState.PATROLLING;
                }
                break;
            case DODGING:
                if (nextPosition.y < game.getPlayerPosition().y)
                    nextPosition.y -= speed * dt * 3;
                if (nextPosition.y > game.getPlayerPosition().y)
                    nextPosition.y += speed * dt * 3;
                nextPosition.x -= speed * dt;
                break;
            case FLEEING:
                nextPosition.x += speed * dt * 5;
                break;
            case DIE:
                game.removeEnemy(this);
                nextPosition.x = 0;
                nextPosition.y = 0;
                speed = 0;
        }

        TiledMapTileLayer.Cell nextCell = collisionLayer.getCell((int) nextPosition.x, (int) nextPosition.y);
        if (nextCell == null || nextCell.getTile() == null) {
            position = nextPosition;
        } else {
            currentState = EnemyState.PATROLLING;
            findNewPath(collisionLayer);
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
            case PATROLLING:
            case BOOSTING:
            case CHASING:
            case DODGING:
            case FLEEING:
                if (position.x > game.getPlayerPosition().x) {
                    currentFrame = animationLeft.getKeyFrame(stateTime, true);
                } else {
                    currentFrame = animationRight.getKeyFrame(stateTime, true);
                }
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

    private void findNewPath(TiledMapTileLayer collisionLayer) {
        Vector2 playerPosition = game.getPlayerPosition();
        float angle = getAngle(playerPosition);
        if (playerPosition.x < position.x) {
            if (angle > 170 && angle < 190) {
                currentState = EnemyState.CHASING;
            } else {
                currentState = EnemyState.DODGING;
            }
        } else {
            currentState = EnemyState.FLEEING;
        }
        if (currentState == EnemyState.CHASING || currentState == EnemyState.DODGING) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.BOOSTING) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.FLEEING) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.PATROLLING) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.MOVING_UP) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.MOVING_DOWN) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
        if (currentState == EnemyState.DIE) {
            if (position.y > playerPosition.y) {
                currentState = EnemyState.MOVING_UP;
            } else {
                currentState = EnemyState.MOVING_DOWN;
            }
        }
    }
}

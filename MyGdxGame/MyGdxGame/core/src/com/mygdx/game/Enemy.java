package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Spaceship {

    private float speed = 50f;

    public Enemy(String texturePath) {
        this.texture = new Texture(texturePath);

        // Place it in the middle of the screen
        this.x = Gdx.graphics.getWidth() + 100;
        this.y = (Gdx.graphics.getHeight() - this.texture.getHeight()) / 2.0f;

        this.currentState = Spaceship.STATE.PATROLLING;
    }

    public float getAngle(Vector2 target) {
        float angle = (float) Math.toDegrees(Math.atan2(target.y - this.getPosition().y, target.x - this.getPosition().x));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    public boolean canSeePlayer(Player player) {
        float angle = this.getAngle(player.getPosition());
        if (player.getPosition().x < this.x) {
            if (angle > 170 && angle < 190) {
                return true;
            }
        }
        return false;
    }

    public float distanceFrom(Player player) {
        return this.getPosition().dst(player.getPosition());
    }

    public void update(Player player) {
        // Grab deltatime to calculate movement over time
        float dt = Gdx.graphics.getDeltaTime();

        switch(this.currentState) {
            case MOVING_UP:
                break;

            case MOVING_DOWN:
                break;

            case PATROLLING:
                this.x -= this.speed * dt;
                break;

            case BOOSTING:
                this.x -= this.speed * dt * 5;
                if (!this.canSeePlayer(player)) {
                    this.currentState = STATE.PATROLLING;
                }
                break;

            case CHASING:
                if (this.getPosition().y < player.getPosition().y)
                    this.y += this.speed * dt;
                if (this.getPosition().y > player.getPosition().y)
                    this.y -= this.speed * dt;
                this.x -= this.speed * dt *3;
                if (!this.canSeePlayer(player)) {
                    this.currentState = STATE.PATROLLING;
                }
                break;

            case DODGING:
                if (this.getPosition().y < player.getPosition().y)
                    this.y -= this.speed * dt * 3;
                if (this.getPosition().y > player.getPosition().y)
                    this.y += this.speed * dt * 3;
                this.x -= this.speed * dt;
                break;

            case FLEEING:
                this.x += this.speed * dt * 5;
                break;
            default:
                // code block
        }
    }
}
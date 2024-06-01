package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Bomb {
    private Vector2 position;
    private Texture texture;
    private float timer;
    private float explosionTime;
    private boolean exploded;

    public Bomb(Vector2 position, Texture texture, float explosionTime) {
        this.position = position;
        this.texture = texture;
        this.timer = 0;
        this.explosionTime = explosionTime;
        this.exploded = false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean hasExploded() {
        return exploded;
    }

    public void update(float deltaTime) {
        timer += deltaTime;
        if (timer >= explosionTime) {
            exploded = true;
        }
    }

    public void render(SpriteBatch batch) {
        if (!exploded) {
            batch.draw(texture, position.x * 32, position.y * 32, 32, 32);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}

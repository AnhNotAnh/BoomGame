package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public interface CollidableObject {
    Rectangle getBoundingBox();
    void handleCollision(Vector2 position, float radius);
    Vector2 getPosition();
    float getRadius();
}
package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

public interface CollidableObject {
    public Rectangle getBoundingBox();

    public void handleCollision();
}

// src/com/mygdx/game/Bomb.java

package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class Bomb {
    private Vector2 position;
    private Texture bombTexture;
    private Texture explosionTexture;
    private float timer;
    private float explosionTime;
    private float explosionDuration;
    private boolean exploded;

    public Bomb(Vector2 position, Texture bombTexture, Texture explosionTexture, float explosionTime, float explosionDuration) {
        this.position = position;
        this.bombTexture = bombTexture;
        this.explosionTexture = explosionTexture;
        this.timer = 0;
        this.explosionTime = explosionTime;
        this.explosionDuration = explosionDuration;
        this.exploded = false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isFinished() {
        return exploded && timer >= explosionDuration;
    }

    public void update(float deltaTime, TiledMapTileLayer tileLayer) {
        timer += deltaTime;
        if (timer >= explosionTime && !exploded) {
            explode(tileLayer);
            exploded = true;
            timer = 0; // Reset timer for explosion duration
        } else if (exploded) {
            timer += deltaTime;
        }
    }

    private void explode(TiledMapTileLayer tileLayer) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        for (int i = 0; i < 4; i++) {
            int x = (int) position.x;
            int y = (int) position.y;

            for (int j = 0; j < 2; j++) {
                x += dx[i];
                y += dy[i];

                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    int tileId = cell.getTile().getId();
                    if (tileId == 2) {
                        tileLayer.setCell(x, y, null);
                    } else if (tileId == 1) {
                        break;
                    }
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!exploded) {
            batch.draw(bombTexture, position.x * 32, position.y * 32, 32, 32);
        } else if (timer < explosionDuration) {
            batch.draw(explosionTexture, position.x * 32, position.y * 32, 32, 32);
        }
    }

    public void dispose() {
        bombTexture.dispose();
        explosionTexture.dispose();
    }
}

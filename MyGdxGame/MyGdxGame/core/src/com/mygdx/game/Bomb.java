// src/com/mygdx/game/Bomb.java

package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class Bomb {
    private Vector2 position;
    private Texture bombTexture;
    private Texture explosionTexture;
    private float timer;
    private float explosionTime;
    private float explosionDuration;
    private boolean exploded;
    private TiledMapTileLayer collisionLayer;
    private MyGdxGame game;

    private static final int TILE_SIZE = 32;
    private static final int EXPLOSION_RADIUS = 1;

    public Bomb(Vector2 position, Texture bombTexture, Texture explosionTexture, float explosionTime, float explosionDuration, TiledMapTileLayer collisionLayer, MyGdxGame game) {
        this.position = position;
        this.bombTexture = bombTexture;
        this.explosionTexture = explosionTexture;
        this.timer = 0;
        this.explosionTime = explosionTime;
        this.explosionDuration = explosionDuration;
        this.exploded = false;
        this.collisionLayer = collisionLayer;
        this.game = game;
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
        MyGdxGame.getInstance().getExplosionSound().play();
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        // Explode in the center
        handleTileDestruction(tileLayer, (int) position.x, (int) position.y);
        handleCollisionLayerDestruction(collisionLayer, (int) position.x, (int) position.y);

        // Explode in the cross pattern
        for (int i = 0; i < 4; i++) {
            int x = (int) position.x;
            int y = (int) position.y;

            for (int j = 0; j < EXPLOSION_RADIUS; j++) {
                x += dx[i];
                y += dy[i];

                if (!isWithinBounds(tileLayer, x, y) || !handleTileDestruction(tileLayer, x, y)) {
                    break;
                }
                if (!handleCollisionLayerDestruction(collisionLayer, x, y)) {
                    break;
                }
            }
        }
        checkEnemyCollisions();
    }

    private boolean handleTileDestruction(TiledMapTileLayer tileLayer, int x, int y) {
        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
        if (cell != null && cell.getTile() != null) {
            int tileId = cell.getTile().getId();
            if (tileId == 2 || tileId == 3 || tileId == 7) { // Destroyable objects
                tileLayer.setCell(x, y, null);
                return true;
            } else if (tileId == 4) { // Solid walls
                return false;
            }
        }
        return true;
    }

    private boolean handleCollisionLayerDestruction(TiledMapTileLayer collisionLayer, int x, int y) {
        if (collisionLayer != null) {
            TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
            if (cell != null && cell.getTile() != null) {
                int tileId = cell.getTile().getId();
                if (tileId == 2 || tileId == 3 || tileId == 7) { // Destroyable objects
                    collisionLayer.setCell(x, y, null);
                    return true;
                } else if (tileId == 4) { // Solid walls
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isWithinBounds(TiledMapTileLayer tileLayer, int x, int y) {
        return x >= 0 && x < tileLayer.getWidth() && y >= 0 && y < tileLayer.getHeight();
    }

    public void render(SpriteBatch batch) {
        if (!exploded) {
            batch.draw(bombTexture, position.x * TILE_SIZE, position.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        } else if (timer < explosionDuration) {
            batch.draw(explosionTexture, position.x * TILE_SIZE, position.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            renderExplosion(batch);
        }
    }

    private void renderExplosion(SpriteBatch batch) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        for (int i = 0; i < 4; i++) {
            int x = (int) position.x;
            int y = (int) position.y;

            for (int j = 0; j < EXPLOSION_RADIUS; j++) {
                x += dx[i];
                y += dy[i];

                if (isWithinBounds(collisionLayer, x, y)) {
                    batch.draw(explosionTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public void dispose() {
        bombTexture.dispose();
        explosionTexture.dispose();
    }

    private void checkEnemyCollisions() {
        Array<Enemy> enemies = MyGdxGame.getInstance().getEnemies();
        Rectangle explosionArea = new Rectangle(position.x * TILE_SIZE, position.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        for (Enemy enemy : enemies) {
            if (enemy.getBoundingBox().overlaps(explosionArea)) {
                enemy.handleCollision(position);
            }
        }

        // Check in the cross pattern
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        for (int i = 0; i < 4; i++) {
            int x = (int) position.x;
            int y = (int) position.y;

            for (int j = 0; j < EXPLOSION_RADIUS; j++) {
                x += dx[i];
                y += dy[i];
                explosionArea.setPosition(x * TILE_SIZE, y * TILE_SIZE);

                for (Enemy enemy : enemies) {
                    if (enemy.getBoundingBox().overlaps(explosionArea)) {
                        enemy.handleCollision(new Vector2(x, y));
                    }
                }
            }
        }
    }
}

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
        // Play the explosion sound
        MyGdxGame.getInstance().getExplosionSound().play();

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
                    if (tileId == 2) // destroy ground object
                    {
                        tileLayer.setCell(x, y, null);
                        if (collisionLayer != null) {
                            collisionLayer.setCell(x, y, null); // Check if collisionLayer is not null before setting cell
                        }
                    } else if (tileId == 3) // destroy rock object
                    {
                        tileLayer.setCell(x, y, null);
                        if (collisionLayer != null) {
                            collisionLayer.setCell(x, y, null); // Check if collisionLayer is not null before setting cell
                        }
                    } else if (tileId == 7) // destroy wood object
                    {
                        tileLayer.setCell(x, y, null);
                        if (collisionLayer != null) {
                            collisionLayer.setCell(x, y, null); // Check if collisionLayer is not null before setting cell
                        }
                    } else if (tileId == 1) {
                        break;
                    }
                }
            }
        }
        checkEnemyCollisions();
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

    private void checkEnemyCollisions() {
        // Iterate through the list of enemies in the game
        Array<Enemy> enemies = MyGdxGame.getInstance().getEnemies();
        for (Enemy enemy : enemies) {
            // Check if the enemy's bounding box intersects with the explosion area
            Rectangle explosionArea = new Rectangle(position.x * 32, position.y * 32, 32, 32);
            if (enemy.getBoundingBox().overlaps(explosionArea)) {
                // Kill the enemy
                enemy.handleCollision(position);
            }
        }
    }

}

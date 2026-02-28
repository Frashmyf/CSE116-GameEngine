package app.games.snake;

import java.util.ArrayList;

import app.display.common.Background;
import app.gameengine.Game;
import app.gameengine.Level;
import app.gameengine.model.physics.PhysicsEngine;
import app.gameengine.model.physics.Vector2D;
import app.gameengine.utils.Randomizer;
import app.gameengine.utils.Timer;

/**
 * A level within a game of snake.
 * <p>
 * This level manages logic including timing of player movement, spawning new
 * food, and keeping track of the snake positions.
 * 
 * @see Level
 * @see Snake
 * @see SnakeFood
 */
public class SnakeLevel extends Level {

    private Timer timer;
    private ArrayList<SnakeFood> food = new ArrayList<>();
    private ArrayList<SnakeBody> tail = new ArrayList<>();
    private int lengthIncrease;
    private int startingLength;
    private int numFood;

    public SnakeLevel(Game game, int size, Timer timer, String name) {
        this(game, size, timer, name, 1, 3, 1);
    }

    public SnakeLevel(Game game, int size, Timer timer, String name, int lengthIncrease, int startingLength,
            int numFood) {
        super(game, new PhysicsEngine(), size, size, name);
        this.timer = timer;
        this.lengthIncrease = Math.min(lengthIncrease, size * size - startingLength);
        this.startingLength = Math.min(startingLength, size * size);
        this.numFood = Math.min(numFood, size * size - 2);
        this.keyboardControls = new SnakeControls(game);
        this.setBackground(new Background("snake/snakeColors.png", 3, 0));
    }

    /**
     * Returns the list of {@link SnakeFood} objects currently in the level.
     * 
     * @return the list of food
     */
    public ArrayList<SnakeFood> getFood() {
        return this.food;
    }

    /**
     * Returns the list of {@link SnakeBody} objects currently in the level. This
     * does not include the head of the snake.
     * 
     * @return the list of body segments
     */
    public ArrayList<SnakeBody> getTail() {
        return this.tail;
    }

    /**
     * Surrounds the level with {@link SnakeWall} objects, just out of bounds. This
     * is to prevent the snake from leaving the confines of the level.
     */
    public void wallOffBoundary() {
        for (int x = -1; x <= this.width; x++) {
            addStaticObject(new SnakeWall(x, -1));
            addStaticObject(new SnakeWall(x, this.height));
        }
        for (int y = 0; y < this.height; y++) {
            addStaticObject(new SnakeWall(-1, y));
            addStaticObject(new SnakeWall(this.width, y));
        }
    }

    /**
     * Randomly choose a new location for food, that is not already occupied. If the
     * snake head and tail fill all available space, it will advance to the next
     * level. If there is no room to spawn food, it will do nothing.
     * <p>
     * Note that food is added to this class's list of food, as well as its list of
     * static objects.
     */
    public void spawnFood() {
        int totalTiles = this.width * this.height;

        if (1 + this.tail.size() >= totalTiles) {
            this.game.advanceLevel();
            return;
        }

        ArrayList<Vector2D> exceptions = new ArrayList<>();
        for (SnakeBody body : this.tail) {
            exceptions.add(body.getLocation().copy());
        }
        exceptions.add(this.getPlayer().getLocation().copy());
        for (SnakeFood f : this.food) {
            exceptions.add(f.getLocation().copy());
        }

        Vector2D pos = Randomizer.randomIntVector2D(new Vector2D(this.width, this.height), exceptions);
        if (pos == null) {
            return;
        }

        SnakeFood newFood = new SnakeFood(pos.getX(), pos.getY(), this);
        this.food.add(newFood);
        this.addStaticObject(newFood);
    }

    /**
     * Increase the length of the snake by creating new body segments. The amount
     * which the length increases by is determined by the lengthIncrease passed into
     * the constructor.
     * <p>
     * Note that body segments are added to this class's list of body segments, as
     * well as its list of static objects.
     */
    public void lengthenSnake() {
        for (int i = 0; i < this.lengthIncrease; i++) {
            this.addBodySegment();
        }
    }

    /**
     * Create the snake at the center of the level. All body segments start in a
     * stack behind the head of the snake.
     */
    public void spawnSnake() {
        for (int i = 0; i < this.startingLength - 1; i++) {
            this.addBodySegment();
        }
    }

    private void addBodySegment() {
        SnakeBody body;
        if (this.tail.isEmpty()) {
            Vector2D headLoc = this.getPlayer().getLocation();
            Vector2D orientation = this.getPlayer().getOrientation();
            body = new SnakeBody(headLoc.getX() - orientation.getX(), headLoc.getY() - orientation.getY());
        } else {
            Vector2D frontLoc = this.tail.get(0).getLocation();
            body = new SnakeBody(frontLoc.getX(), frontLoc.getY());
        }
        this.tail.add(0, body);
        this.addStaticObject(body);
    }

    /**
     * Move each segment of the snake forward in its direction of travel by one
     * tile, including both body segments and the head of the snake.
     */
    private void moveSnake() {
        if (!this.tail.isEmpty()) {
            SnakeBody tip = this.tail.remove(0);
            tip.setLocation(this.getPlayer().getLocation().getX(), this.getPlayer().getLocation().getY());
            this.tail.add(tip);
        }
        Vector2D headLoc = this.getPlayer().getLocation();
        Vector2D orientation = this.getPlayer().getOrientation();
        this.getPlayer().setLocation(headLoc.getX() + orientation.getX(), headLoc.getY() + orientation.getY());
    }

    @Override
    public void update(double dt) {
        this.food.removeIf(SnakeFood::isDestroyed);
        this.tail.removeIf(SnakeBody::isDestroyed);
        // Move body
        if (this.timer.tick(dt) > 0) {
            this.keyboardControls.processInput(0);
            this.moveSnake();
        }
        super.update(dt);
    }

    @Override
    public void load() {
        super.load();
        this.food.forEach(SnakeFood::destroy);
        this.tail.forEach(SnakeBody::destroy);
        this.food.clear();
        this.tail.clear();

        this.spawnSnake();
        for (int i = 0; i < this.numFood; i++) {
            this.spawnFood();
        }
        this.wallOffBoundary();
    }

    @Override
    public void reset() {
        this.load();
    }

    @Override
    public String getUIString() {
        return String.format("Score: %.0f", this.score);
    }

}

package app.games.commonobjects;

import app.display.common.SpriteLocation;
import app.gameengine.model.gameobjects.DynamicGameObject;
import app.gameengine.model.gameobjects.StaticGameObject;

/**
 * A {@code StaticGameObject} that prevents collision by moving any
 * {@code DynamicGameObject}s that collide with it.
 */
public class Wall extends StaticGameObject {

    public Wall(double x, double y) {
        super(x, y);
        this.spriteSheetFilename = "MiniWorldSprites/Ground/Cliff.png";
        this.defaultSpriteLocation = new SpriteLocation(3, 0);
    }

    @Override
    public void collideWithDynamicObject(DynamicGameObject otherObject) {
        double wx = this.getHitbox().getLocation().getX();
        double wy = this.getHitbox().getLocation().getY();
        double ww = this.getHitbox().getDimensions().getX();
        double wh = this.getHitbox().getDimensions().getY();

        double ox = otherObject.getHitbox().getLocation().getX();
        double oy = otherObject.getHitbox().getLocation().getY();
        double ow = otherObject.getHitbox().getDimensions().getX();
        double oh = otherObject.getHitbox().getDimensions().getY();

        double xOverlap = Math.min(ox + ow, wx + ww) - Math.max(ox, wx);
        double yOverlap = Math.min(oy + oh, wy + wh) - Math.max(oy, wy);

        double offX = otherObject.getHitbox().getOffset().getX();
        double offY = otherObject.getHitbox().getOffset().getY();

        if (xOverlap <= yOverlap) {
            double objCenterX = ox + ow / 2;
            double wallCenterX = wx + ww / 2;
            if (objCenterX >= wallCenterX) {
                otherObject.setLocation(wx + ww - offX, otherObject.getLocation().getY());
            } else {
                otherObject.setLocation(wx - ow - offX, otherObject.getLocation().getY());
            }
            otherObject.setVelocity(0, otherObject.getVelocity().getY());
        } else {
            double objCenterY = oy + oh / 2;
            double wallCenterY = wy + wh / 2;
            if (objCenterY >= wallCenterY) {
                otherObject.setLocation(otherObject.getLocation().getX(), wy + wh - offY);
            } else {
                otherObject.setLocation(otherObject.getLocation().getX(), wy - oh - offY);
            }
            otherObject.setVelocity(otherObject.getVelocity().getX(), 0);
        }
    }

}

package tk.amberide.ide.gui.editor.map.tool;

import tk.amberide.engine.data.map.Direction;
import tk.amberide.engine.data.map.Layer;
import tk.amberide.engine.data.map.Tile;

import static tk.amberide.engine.data.map.Angle.*;

import tk.amberide.engine.data.map.TileModel;
import tk.amberide.engine.data.math.Angles;
import tk.amberide.ide.data.res.Tileset;
import tk.amberide.engine.gl.camera.EulerCamera;
import tk.amberide.engine.gl.tess.ITesselator;
import tk.amberide.engine.gl.tess.ImmediateTesselator;
import tk.amberide.ide.gui.editor.map.MapContext;

import java.awt.Component;

import static org.lwjgl.opengl.GL11.*;
import static tk.amberide.engine.input.AbstractKeyboard.*;

import org.lwjgl.util.vector.Vector2f;
import tk.amberide.engine.data.map.Angle;
import tk.amberide.engine.data.map.TileType;

/**
 * @author Tudor
 */
public class BrushTool extends AbstractTool {

    protected EulerCamera camera;
    protected Angle angle = HORIZONTAL;
    protected ITesselator tess = new ImmediateTesselator();

    public BrushTool(MapContext context, EulerCamera camera) {
        super(context);
        this.camera = camera;
    }

    @Override
    public void doScroll(int delta) {
        if (delta >= 1) {
            angle = angle == SLANTED ? HORIZONTAL : (angle == VERTICAL ? SLANTED : angle);

        } else if (delta <= -1) {
            angle = angle == SLANTED ? VERTICAL : (angle == HORIZONTAL ? SLANTED : angle);
        }
    }

    public boolean apply(int x, int y, int z) {
        boolean modified = false;
        switch (context.drawType) {
            case MapContext.TYPE_TILE:
                if (context.tileSelection != null) {
                    Tileset.TileSprite[][] sel = context.tileSelection.clone();
                    int w = sel.length;
                    int h = sel[0].length;
                    for (int sx = 0; sx != w; sx++) {
                        for (int sy = 0; sy != h; sy++) {
                            boolean tiled = setAngledTile(x, y, z, sx, sy, h, w, camera.getFacingDirection(), angle, sel);
                            if (!modified) {
                                modified = tiled;
                            }
                        }
                    }
                }
                break;
            case MapContext.EXT_TYPE_MODEL:
                if (context.EXT_modelSelection != null) {
                    Layer l = context.map.getLayer(context.layer);
                    if (l instanceof Layer) {
                        Layer l3d = (Layer) l;
                        if (isInBounds(x, y)) {
                            TileModel p = l3d.getModel(x, y, z);
                            if (p == null || p.getModel() != context.EXT_modelSelection) {
                                l3d.setModel(x, y, z, new TileModel(context.EXT_modelSelection));
                                modified = true;
                            }
                        }
                    }
                }
                break;
        }

        return modified;
    }

    protected final boolean setAngledTile(int x, int y, int z, int sx, int sy, int h, int w, Direction dir, Angle angle, Tileset.TileSprite[][] sel) {
        // It works, that's all you need to know.
        switch (angle) {
            case HORIZONTAL:
                switch (dir.toCardinal()) {
                    case NORTH:
                        return setTile(x + sy, y + sx, z, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile(sx + x - w + 1, sy + y, z, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile(x + sy - h + 1, y + sx - w + 1, z, sel[w - sx - 1][sy], dir, angle);
                    case WEST:
                        return setTile(sx + x, sy + y - h + 1, z, sel[sx][sy], dir, angle);
                }
            case VERTICAL:
                switch (dir.toCardinal()) {
                    case NORTH:
                        return setTile(x, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile(sx + x - w + 1, y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile(x, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case WEST:
                        return setTile(sx + x, y, z + sy, sel[sx][h - sy - 1], dir, angle);
                }
            case SLANTED:
                switch (dir.toCardinal()) {
                    case NORTH:
                        return setTile(x + sy, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile(sx + x - w + 1, sy + y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile(x - sy, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case WEST:
                        return setTile(sx + x, y - sy, z + sy, sel[sx][h - sy - 1], dir, angle);
                }
        }
        return false;
    }

    protected boolean setTile(int x, int y, int z, Tileset.TileSprite tile, Direction dir, Angle angle) {
        Layer lay = context.map.getLayer(context.layer);
        boolean modified = false;
        if (isInBounds(x, y)) {
            Tile r = lay.getTile(x, y, z);
            if (tile != null) {
                modified = r == null || !tile.equals(r.getSprite());
                TileType type;
                if (isKeyDown(KEY_Z) && !isKeyDown(KEY_X)) {
                    type = TileType.TILE_CORNER;
                } else if (isKeyDown(KEY_X) && !isKeyDown(KEY_Z)) {
                    type = TileType.TILE_CORNER_INVERSED;
                } else {
                    type = TileType.TILE_NORMAL;
                }
                lay.setTile(x, y, z, new Tile(tile, dir, angle, type));
            } else {
                modified = r != null;
                lay.setTile(x, y, z, null);
            }
        }
        return modified;
    }

    public void draw(int x, int y, int z) {
        if (context.drawType == MapContext.EXT_TYPE_MODEL && context.EXT_modelSelection != null) {
            glPushAttrib(GL_CURRENT_BIT | GL_POLYGON_BIT);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            tess.drawModel(new TileModel(context.EXT_modelSelection), x, z, y);
            glPopAttrib();
        } else if (context.tileSelection != null) {
            glPushMatrix();
            float tx = x, tz = z, rot = 0;

            switch (camera.getFacingDirection().toCardinal()) {
                case SOUTH:
                    tx+=1;
                    tz+=1;
                    rot = 180;
                    break;
                case WEST:
                    tz+=1;
                    rot = 90;
                    break;
                case EAST:
                    tx+=1;
                    rot = 270;
                    break;
            }
            glTranslatef(tx, (float) y, tz);
            float y1, y2, y3, y4;
            y1 = y2 = y3 = y4 = 0;
            Vector2f ix;
            Direction dir = camera.getFacingDirection();
            if (isKeyDown(KEY_Z) && !isKeyDown(KEY_X) && !dir.cardinal()) {
                switch (dir) {
                    case NORTH_EAST:
                    case SOUTH_WEST:
                        y3++;
                        break;
                    case NORTH_WEST:
                    case SOUTH_EAST:
                        y2++;
                        break;
                }
                ix = Angles.circleIntercept(HORIZONTAL.intValue(), 0, 0, context.tileSelection[0].length);
            } else if (isKeyDown(KEY_X) && !isKeyDown(KEY_Z) && !dir.cardinal()) {
                switch (dir) {
                    case NORTH_EAST:
                    case SOUTH_WEST:
                        y4++;
                        y3++;
                        y2++;
                        break;
                    case NORTH_WEST:
                    case SOUTH_EAST:
                        y1++;
                        y2++;
                        y3++;
                        break;
                }
                ix = Angles.circleIntercept(HORIZONTAL.intValue(), 0, 0, context.tileSelection[0].length);
            } else {
                ix = Angles.circleIntercept(angle.intValue(), 0, 0, context.tileSelection[0].length);
            }
            glRotatef(rot, 0, 1, 0);

            glBegin(GL_LINE_LOOP);
            {
                int w = context.tileSelection.length;
                glVertex3f(0, 0 + y1, 0);
                glVertex3f(ix.x, ix.y + y2, 0);
                glVertex3f(ix.x, ix.y + y3, w);
                glVertex3f(0, 0 + y4, w);
            }
            glEnd();
            glPopMatrix();
        }
    }

    public Component getContextComponent() {
        return null;
    }
}

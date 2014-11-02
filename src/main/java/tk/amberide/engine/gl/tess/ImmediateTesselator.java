package tk.amberide.engine.gl.tess;

import tk.amberide.engine.data.math.Angles;
import tk.amberide.engine.data.map.Tile;
import tk.amberide.engine.data.map.Tile3D;
import static tk.amberide.engine.data.map.Angle.*;
import tk.amberide.engine.data.map.TileModel;
import tk.amberide.ide.data.res.Tileset;
import tk.amberide.engine.gl.atlas.ITextureAtlas;
import tk.amberide.engine.gl.atlas.TextureAtlasFactory;
import tk.amberide.engine.gl.model.ModelScene;
import tk.amberide.engine.gl.model.obj.WavefrontObject;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.WeakHashMap;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;

/**
 * Renders tiles in immediate mode.
 *
 * @author Tudor
 */
public class ImmediateTesselator implements ITesselator {

    protected WeakHashMap<Tileset, ITextureAtlas> textureCache = new WeakHashMap<Tileset, ITextureAtlas>();
    protected WeakHashMap<WavefrontObject, ModelScene> modelCache = new WeakHashMap<WavefrontObject, ModelScene>();
    protected ITextureAtlas atl;

    public void drawTile3D(Tile3D tile, float x, float y, float z) {
        atl = getTexture(tile);

        glPushMatrix();
        float[] tr = {x, z, y, 0};
        float[] t0 = {0, 1};
        float[] t1 = {0, 0};
        float[] t2 = {1, 0};
        float[] t3 = {1, 1};

        switch (tile.getDirection().toCardinal()) {
            case SOUTH:
                tr[0]++;
                tr[2]++;
                tr[3] = 180;
                break;
            case WEST:
                tr[2]++;
                tr[3] = 90;
                break;
            case EAST:
                tr[0]++;
                tr[3] = 270;
                break;
        }
        float y1, y2, y3, y4;
        y1 = y2 = y3 = y4 = 0;
        Vector2f ix = null;
        switch (tile.getType()) {
            case TILE_CORNER:
                switch (tile.getDirection()) {
                    case NORTH_EAST:
                    case SOUTH_WEST:
                        t2 = new float[]{0, 0};
                        t3 = new float[]{1, 0};
                        t0 = new float[]{1, 1};
                        t1 = new float[]{0, 1};
                        y3++;
                        break;
                    case NORTH_WEST:
                    case SOUTH_EAST:
                        y2++;
                        break;
                }
                ix = Angles.circleIntercept(_180.intValue(), 0, 0, 1);
                break;
            case TILE_CORNER_INVERSED:
                switch (tile.getDirection()) {
                    case NORTH_EAST:
                        y4++;
                        y3++;
                        y2++;
                        t3 = new float[]{0, 0};
                        break;
                    case NORTH_WEST:
                        y1++;
                        y2++;
                        y3++;
                        //t3 = new float[]{0, 1};
                        break;
                    case SOUTH_EAST:
                        y1++;
                        y2++;
                        y3++;
                        break;
                    case SOUTH_WEST:
                        y4++;
                        y3++;
                        y2++;
                        t3 = new float[]{0, 0};
                        break;
                }
                ix = Angles.circleIntercept(_180.intValue(), 0, 0, 1);
                break;
            case TILE_NORMAL:
                ix = Angles.circleIntercept(tile.getAngle().intValue(), 0, 0, 1);
                break;
        }
        glTranslatef(tr[0], tr[1], tr[2]);

        Point start = tile.getSprite().getStart();
        Dimension size = tile.getSprite().getSize();

        atl.bindTextureRegion(start.x, start.y, size.height, size.width);

        glRotatef(tr[3], 0, 1, 0);
        glBegin(GL_TRIANGLES);
        {
            //0      
            atl.atlasCoord(t0[0], t0[1]);
            glVertex3f(0, y1, 0);
            //1  
            atl.atlasCoord(t1[0], t1[1]);
            glVertex3f(ix.x, ix.y + y2, 0);
            //2
            atl.atlasCoord(t2[0], t2[1]);
            glVertex3f(ix.x, ix.y + y3, 1);

            //3
            atl.atlasCoord(t3[0], t3[1]);
            glVertex3f(0, y4, 1);
            //2
            atl.atlasCoord(t2[0], t2[1]);
            glVertex3f(ix.x, ix.y + y3, 1);
            //0
            atl.atlasCoord(t0[0], t0[1]);
            glVertex3f(0, y1, 0);
        }
        glEnd();
        glPopMatrix();
    }

    public void drawTile2D(Tile tile, float x, float y) {
        atl = getTexture(tile);

        Point start = tile.getSprite().getStart();
        Dimension size = tile.getSprite().getSize();

        atl.bindTextureRegion(start.x, start.y, size.width, size.height);

        float dx = x * 32;
        float dy = y * 32;

        glBegin(GL_TRIANGLES);
        {
            //0
            atl.atlasCoord(0, 1);
            glVertex2f(dx, dy);
            //1
            atl.atlasCoord(0, 0);
            glVertex2f(dx, dy + 32);
            //2
            atl.atlasCoord(1, 0);
            glVertex2f(dx + 32, dy + 32);

            //3 
            atl.atlasCoord(1, 1);
            glVertex2f(dx + 32, dy);
            //2
            atl.atlasCoord(1, 0);
            glVertex2f(dx + 32, dy + 32);
            //0
            atl.atlasCoord(0, 1);
            glVertex2f(dx, dy);
        }
        glEnd();
    }

    public void startTileBatch() {
        glPushMatrix();
    }

    public void endTileBatch() {
        glPopMatrix();
        if (atl != null) {
            atl.unbind();
        }
    }

    public void startModelBatch() {
        glPushMatrix();
    }

    public void drawModel3D(TileModel model, float x, float y, float z) {
        ModelScene scene = getModel(model);
        if (scene != null) {
            glPushMatrix();
            glPushAttrib(GL_CURRENT_BIT | GL_TRANSFORM_BIT);
            glTranslatef(x, z, y);
            scene.draw();
            glTranslatef(-x, -z, -y);
            glPopAttrib();
            glPopMatrix();
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    public void endModelBatch() {
        glPopMatrix();
    }

    public void invalidate() {
        textureCache.clear();
        modelCache.clear();
    }

    protected ITextureAtlas getTexture(Tile t) {
        Tileset sheet = t.getSprite().getTileset();
        ITextureAtlas atlas;
        if (textureCache.containsKey(sheet)) {
            atlas = textureCache.get(sheet);
        } else {
            textureCache.put(sheet, atlas = TextureAtlasFactory.createAtlas(sheet.getImage()));
        }
        return atlas;
    }

    protected ModelScene getModel(TileModel t) {
        WavefrontObject m = t.getModel();
        ModelScene scene = null;
        if (m != null) {
            if (modelCache.containsKey(m)) {
                scene = modelCache.get(m);
            } else {
                try {
                    modelCache.put(m, scene = new ModelScene(m));
                } catch (IOException ex) {
                }
            }
        }
        return scene;
    }
}

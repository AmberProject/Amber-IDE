package amber.gui.editor.map;

import amber.Amber;
import amber.data.math.Angles;
import amber.data.res.Tileset;
import amber.data.res.Tileset.TileSprite;
import amber.data.map.Direction;
import static amber.data.map.Direction.*;
import amber.data.map.Layer;
import amber.data.map.Layer3D;
import amber.data.map.LevelMap;
import amber.data.map.Tile;
import amber.data.map.Tile3D;
import amber.data.map.Tile3D.Angle;
import static amber.data.map.Tile3D.Angle.*;
import amber.data.map.TileModel;
import amber.data.sparse.SparseMatrix;
import amber.data.sparse.SparseVector;
import amber.data.math.vec.Ray;
import amber.data.math.vec.Vec3d;
import amber.input.AbstractKeyboard;
import amber.gl.FrameTimer;
import amber.gl.GLColor;
import static amber.gl.GLE.*;
import amber.gl.Sprite;
import amber.gl.TrueTypeFont;
import amber.gl.camera.EulerCamera;
import amber.gl.tess.ImmediateTesselator;
import amber.gl.tess.ITesselator;
import static amber.gui.editor.map.MapContext.*;
import amber.input.AbstractMouse;
import amber.swing.MenuBuilder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;
import static amber.input.AbstractKeyboard.*;
import static amber.input.AbstractMouse.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import javax.swing.UIManager;
import org.lwjgl.opengl.GLContext;

/**
 *
 * @author Tudor
 */
public class GLMapComponent3D extends AbstractGLMapComponent {

    protected FrameTimer timer = new FrameTimer();
    protected Vec3d cursorPos = new Vec3d();
    protected Tile3D.Angle currentAngle = _180;
    protected EulerCamera cam = new EulerCamera.Builder()
            .setPosition(0, 3, 0)
            .setFieldOfView(60)
            .setRotation(50, 135, 0)
            .setFarClippingPane(1000f)
            .build();
    protected TrueTypeFont font;
    protected Sprite compassRose;
    protected ITesselator tess = new ImmediateTesselator();
    protected Panel display = new Panel(new BorderLayout());

    public GLMapComponent3D(LevelMap map) throws LWJGLException {
        super(map);
        setMinimumSize(new Dimension(50, 50));
        setPreferredSize(new Dimension(50, 50));
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        context.EXT_cardinalSupported = true;
        context.EXT_modelSelectionSupported = true;
        display.add(this);
    }

    @Override
    public void initGL() {
        gleClearColor(UIManager.getColor("MapEditor.background"));
        font = new TrueTypeFont(UIManager.getFont("MapEditor.font"), true);

        cam.applyOptimalStates();
        cam.applyPerspectiveMatrix();

        glEnable(GL_COLOR_MATERIAL);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_DITHER);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.1f);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        glShadeModel(GL_SMOOTH);

        timer.start();

        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                // Hierarchy change can signify the AWTGLCanvas destroying the original GL context,
                // so we have to clear the now invalid texture cache.
                tess.invalidate();
                compassRose = null;
                AbstractKeyboard.destroy(); // Prevent double events
                AbstractMouse.destroy();
            }
        });
    }

    @Override
    protected void pollInput() {
        super.pollInput();
        if (isGrabbed()) {
            cam.processMouse(1, 80, -80);
        }
        if (!(AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL) || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL))) {
            // Frame-rate independant movement        
            float dxyz = (float) timer.getDelta() * 8f * 0.1f;
            cam.processKeyboard(12, dxyz, dxyz, dxyz);
        }

        // Cast ray from mouse, then use the properties of
        // similar triangles to find the xy-plane intercept, 
        // or an offset of it based off the current layer Y.
        Ray ray = Ray.getRay(AbstractMouse.getX(this), getHeight() - AbstractMouse.getY(this));
        float ratio = -((ray.point.y - cursorPos.y) / ray.dir.y);
        Vec3d intercept = new Vec3d((ray.dir.x * ratio) + cam.x(), 0, (ray.dir.z * ratio) + cam.z());
        cursorPos = new Vec3d((int) Math.floor(intercept.x), cursorPos.y, (int) Math.floor(intercept.z));

        Point mouse = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouse, Amber.getUI());
        if (Amber.getUI().findComponentAt(mouse) == this) {
            if (isButtonDown(0)) {
                if (isKeyDown(Keyboard.KEY_LCONTROL)) {
                    AbstractMouse.setGrabbed(true);
                } else {
                    LevelMap pre = context.map.clone();
                    switch (context.drawType) {
                        case TYPE_TILE:
                            switch (context.drawMode) {
                                case MODE_BRUSH:
                                    if (setTileAt((int) cursorPos.x, (int) cursorPos.z, context.EXT_cardinal ? cam.getFacingDirection() : Direction.NORTH, currentAngle)) {
                                        context.undoStack.push(pre);
                                    }
                                    break;
                                case MODE_FILL:
                                    if (floodFillAt((int) cursorPos.x, (int) cursorPos.z, context.EXT_cardinal ? cam.getFacingDirection() : Direction.NORTH, currentAngle)) {
                                        context.undoStack.push(pre);
                                    }
                                    break;
                            }
                            break;
                        case EXT_TYPE_MODEL:
                            if (context.EXT_modelSelection != null) {
                                Layer l = context.map.getLayer(context.layer);
                                if (l instanceof Layer3D) {
                                    Layer3D l3d = (Layer3D) l;
                                    int mapX = (int) cursorPos.x;
                                    int mapY = (int) cursorPos.z;
                                    TileModel p = l3d.getModel(mapX, mapY, (int) cursorPos.y);
                                    if (isInBounds(mapX, mapY) && p == null || p.getModel() != context.EXT_modelSelection) {
                                        context.undoStack.push(pre);
                                        l3d.setModel(mapX, mapY, (int) cursorPos.y, new TileModel(context.EXT_modelSelection));
                                    }
                                }
                            }
                            break;
                    }
                }
            } else if (isButtonDown(1)) {
                AbstractMouse.setGrabbed(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

            AbstractMouse.poll();
        }
    }

    @Override
    protected void doKey(int keycode) {
        switch (keycode) {
            case Keyboard.KEY_MULTIPLY:
                currentAngle = currentAngle == _45 ? _90 : (currentAngle == _180 ? _45 : currentAngle);
                break;
            case Keyboard.KEY_DIVIDE:
                currentAngle = currentAngle == _45 ? _180 : (currentAngle == _90 ? _45 : currentAngle);
                break;
            case Keyboard.KEY_SUBTRACT:
                if (cursorPos.y > 0) {
                    cursorPos.y--;
                }
                break;
            case Keyboard.KEY_ADD:
                cursorPos.y++;
                break;
        }
    }

    /**
     * Paints the preview
     */
    @Override
    public void paintGL() {
        if (!GLContext.getCapabilities().GL_ARB_texture_rectangle) {
            running = false;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    display.remove(GLMapComponent3D.this);
                    ScrollPane scroller = new ScrollPane();
                    scroller.setMinimumSize(new Dimension(0, 0));
                    scroller.add(new Label("ARB_TEXTURE_RECTANGLE not supported. Try updating your graphics drivers.", Label.CENTER));
                    display.add(scroller);
                    display.validate();
                }
            });
            return;
        }
        super.paintGL();
        float aspect = (float) getWidth() / (float) getHeight();
        if (aspect != cam.aspectRatio()) {
            glViewport(0, 0, getWidth(), getHeight());
            cam.setAspectRatio(aspect);
            cam.applyPerspectiveMatrix();

        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        if (wireframe) {
            if (glGetInteger(GL_POLYGON_MODE) == GL_FILL) {
                gleToggleWireframe();
            }
        } else if (glGetInteger(GL_POLYGON_MODE) == GL_LINE) {
            gleToggleWireframe();
        }

        cam.applyTranslations();
        glEnable(GL_TEXTURE_RECTANGLE_ARB);
        glEnable(GL_DEPTH_TEST);

        List<Layer> layers = context.map.getLayers();
        // Fix for z-buffer fighting        
        glPolygonOffset(1, 1);
        for (int i = 0; i != layers.size(); i++) {
            drawLayer(layers.get(i));
        }

        drawGrid();
        glDisable(GL_TEXTURE_RECTANGLE_ARB);

        if (info || compass) {
            glePushOrthogonalMode(0, getWidth(), 0, getHeight());
            if (compass) {
                if (compassRose == null) {
                    compassRose = new Sprite("icon/MapEditor.Compass-Rose.png");
                }
                glPushMatrix();
                glTranslatef(getWidth() / 2 + getWidth() / 3.5f, getHeight() / 2 + getHeight() / 3.5f, 0);
                glRotatef(cam.yaw() - 90, 0, 0, 1);
                float ratio = ((float) getWidth()) / ((float) getHeight()) * .7f;
                glScalef(ratio, ratio, ratio);
                compassRose.draw(compassRose.getWidth() / 2, -compassRose.getHeight() / 2);
                glPopMatrix();
            }
            if (info) {
                glPushAttrib(GL_CURRENT_BIT | GL_POLYGON_BIT);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Ensure we're not in wireframe mode
                GLColor.BLACK.bind();

                font.drawString(0, getHeight() - font.getHeight(), "FPS: " + timer.fps() + "\n"
                        + "Position: (" + (int) cam.x() + ", " + (int) cam.y() + ", " + (int) cam.z() + ")\n"
                        + "Altitude: " + cursorPos.y + "\n"
                        + (!AbstractMouse.isGrabbed() ? "Cursor: (" + (int) cursorPos.x + ", " + (int) cursorPos.z + ")" : ""), 1f, 1f, TrueTypeFont.ALIGN_LEFT);
                glPopAttrib();
            }
            glePushFrustrumMode();
        }

        if (context.drawType == EXT_TYPE_MODEL && context.EXT_modelSelection != null && !AbstractMouse.isGrabbed()) {
            glPushAttrib(GL_CURRENT_BIT | GL_POLYGON_BIT);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            tess.drawModel3D(new TileModel(context.EXT_modelSelection), cursorPos.x, cursorPos.z, cursorPos.y);
            glPopAttrib();
        }

        try {
            swapBuffers();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        timer.updateFPS();
    }

    protected void drawLayer(Layer layer) {
        SparseVector<SparseMatrix<Tile>> tileVector = layer.tileMatrix();
        SparseVector<SparseMatrix<TileModel>> modelVector = layer instanceof Layer3D ? ((Layer3D) layer).modelMatrix()
                : new SparseVector<SparseMatrix<TileModel>>();
        tess.startTileBatch();
        SparseVector.SparseVectorIterator tileIterator = tileVector.iterator();
        while (tileIterator.hasNext()) {
            SparseMatrix.SparseMatrixIterator matrixIterator = ((SparseMatrix<Tile>) tileIterator.next()).iterator();
            while (matrixIterator.hasNext()) {
                Tile3D t = (Tile3D) matrixIterator.next();
                if (t != null) {
                    tess.drawTile3D(t, matrixIterator.realX(), matrixIterator.realY(), tileIterator.realIndex());
                }
            }
        }
        tess.endTileBatch();

        tess.startModelBatch();
        SparseVector.SparseVectorIterator modelIterator = modelVector.iterator();
        while (modelIterator.hasNext() && modelVector.size() > 0) {
            SparseMatrix<TileModel> matrix = (SparseMatrix<TileModel>) modelIterator.next();
            SparseMatrix.SparseMatrixIterator matrixIterator = matrix.iterator();
            int z = modelIterator.realIndex();
            while (matrixIterator.hasNext()) {
                TileModel t = (TileModel) matrixIterator.next();
                if (t != null) {
                    tess.drawModel3D(t, matrixIterator.realX(), matrixIterator.realY(), z);
                }
            }
        }
        tess.endModelBatch();
    }

    protected void drawGrid() {
        glPushAttrib(GL_CURRENT_BIT | GL_LINE_BIT);
        if (grid) {
            glBegin(GL_LINES);
            {
                GLColor.GRAY.bind();
                for (int x = 0; x <= context.map.getWidth(); x++) {
                    gleLine(x, cursorPos.y, 0, x, cursorPos.y, context.map.getLength());
                }
                for (int y = 0; y <= context.map.getLength(); y++) {
                    gleLine(0, cursorPos.y, y, context.map.getWidth(), cursorPos.y, y);
                }
            }
            glEnd();
        }
        glLineWidth(3);
        GLColor.BLACK.bind();
        glBegin(GL_LINES);
        {
            gleLine(0, cursorPos.y, 0, context.map.getWidth(), cursorPos.y, 0);
            gleLine(0, cursorPos.y, 0, 0, cursorPos.y, context.map.getLength());
            gleLine(context.map.getWidth(), cursorPos.y, 0, context.map.getWidth(), cursorPos.y, context.map.getLength());
            gleLine(0, cursorPos.y, context.map.getLength(), context.map.getWidth(), cursorPos.y, context.map.getWidth());
        }
        glEnd();
        glLineWidth(2);
        if (!AbstractMouse.isGrabbed() && context.drawType == TYPE_TILE && cursorPos != null && context.tileSelection != null) {
            glPushMatrix();
            float[] tr = new float[]{cursorPos.x, cursorPos.y, cursorPos.z, 0};

            switch (cam.getFacingDirection()) {
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
            glTranslatef(tr[0], tr[1], tr[2]);
            glRotatef(tr[3], 0, 1, 0);
            Vector2f ix = Angles.circleIntercept(currentAngle.intValue(), 0, 0, context.tileSelection[0].length);
            glBegin(GL_LINE_LOOP);
            {
                int w = context.tileSelection.length;
                glVertex3f(0, 0, 0);
                glVertex3f(ix.x, ix.y, 0);
                glVertex3f(ix.x, ix.y, w);
                glVertex3f(0, 0, w);
            }
            glEnd();
            glPopMatrix();
        }
        glPopAttrib();
    }

    protected boolean floodFillAt(int x, int y, Direction dir, Angle angle) {
        boolean modified = false;
        if (isInBounds(x, y)) {
            Tileset.TileSprite target = spriteAt(x, y);
            Stack<Point> stack = new Stack<Point>() {
                Set<Point> visited = new HashSet<Point>();

                @Override
                public Point push(Point t) {
                    return visited.add(t) ? super.push(t) : t;
                }
            };

            stack.push(new Point(x, y));
            while (!stack.empty()) {
                Point p = stack.pop();
                if (spriteAt(p.x, p.y) != target) {
                    continue;
                }
                if (setTileAt(p.x, p.y, dir, angle)) {
                    modified = true;
                }

                if (target == spriteAt(p.x - 1, p.y)) {
                    stack.push(new Point(p.x - 1, p.y));
                }
                if (target == spriteAt(p.x + 1, p.y)) {
                    stack.push(new Point(p.x + 1, p.y));
                }
                if (target == spriteAt(p.x, p.y - 1)) {
                    stack.push(new Point(p.x, p.y - 1));
                }
                if (target == spriteAt(p.x, p.y + 1)) {
                    stack.push(new Point(p.x, p.y + 1));
                }
            }
        }
        return modified;
    }

    private Tileset.TileSprite spriteAt(int x, int y) {
        if (isInBounds(x, y)) {
            Tile tile = context.map.getLayer(context.layer).getTile(x, y, (int) cursorPos.y);
            return tile != null ? tile.getSprite() : TileSprite.NULL_SPRITE;
        }
        return null;
    }

    protected boolean setTileAt(int x, int y, Direction dir, Angle angle) {
        boolean modified = false;
        if (context.tileSelection != null) {
            Tileset.TileSprite[][] sel = context.tileSelection.clone();
            int w = sel.length;
            int h = sel[0].length;
            for (int sx = 0; sx != w; sx++) {
                for (int sy = 0; sy != h; sy++) {
                    boolean tiled = setAngledTile(x, y, (int) cursorPos.y, sx, sy, h, w, dir, angle, sel);
                    if (!modified) {
                        modified = tiled;
                    }
                }
            }
        }
        return modified;
    }

    protected final boolean setAngledTile(int x, int y, int z, int sx, int sy, int h, int w, Direction dir, Angle angle, TileSprite[][] sel) {
        // It works, that's all you need to know.
        switch (angle) {
            case _180:
                switch (dir) {
                    case NORTH:
                        return setTile0(x + sy, y + sx, z, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile0(sx + x - w + 1, sy + y, z, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile0(x + sy - h + 1, y + sx - w + 1, z, sel[w - sx - 1][sy], dir, angle);
                    case WEST:
                        return setTile0(sx + x, sy + y - h + 1, z, sel[sx][sy], dir, angle);
                }
            case _90:
                switch (dir) {
                    case NORTH:
                        return setTile0(x, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile0(sx + x - w + 1, y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile0(x, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case WEST:
                        return setTile0(sx + x, y, z + sy, sel[sx][h - sy - 1], dir, angle);
                }
            case _45:
                switch (dir) {
                    case NORTH:
                        return setTile0(x + sy, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                    case EAST:
                        return setTile0(sx + x - w + 1, sy + y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case SOUTH:
                        return setTile0(x - sy, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                    case WEST:
                        return setTile0(sx + x, y - sy, z + sy, sel[sx][h - sy - 1], dir, angle);
                }
        }
        return false;
    }

    private final boolean setTile0(int x, int y, int z, TileSprite tile, Direction dir, Angle angle) {
        Layer lay = context.map.getLayer(context.layer);
        boolean modified = false;
        if (isInBounds(x, y)) {
            Tile r = lay.getTile(x, y, z);
            modified = r == null || !tile.equals(r.getSprite());
            lay.setTile(x, y, z, new Tile3D(tile, dir, angle));
        }
        return modified;
    }

    @Override
    public Component getComponent() {
        return display;
    }
    protected boolean info = true, compass = true, wireframe = false, grid = true;

    public JMenu[] getContextMenus() {
        return new JMenu[]{new MenuBuilder("View").addCheckbox("Info", true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    info = !info;
                    repaint();
                }
            }).addCheckbox("Grid", true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    grid = !grid;
                    repaint();
                }
            }).addCheckbox("Compass", true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    compass = !compass;
                    repaint();
                }
            }).addCheckbox("Wireframe", false, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    wireframe = !wireframe;
                    repaint();
                }
            }).create()};
    }
}

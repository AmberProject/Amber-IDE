package amber.gui.editor.map;

import amber.data.OS;
import amber.data.res.Tileset;
import amber.data.map.Layer;
import amber.data.map.LevelMap;
import amber.data.map.Tile;
import amber.data.map.codec.Codec;
import amber.data.sparse.SparseMatrix;
import amber.data.sparse.SparseVector;
import amber.gl.FrameTimer;
import amber.gl.GLColor;
import static amber.gl.GLE.*;
import amber.gl.Texture;
import amber.gl.TextureLoader;
import amber.gl.TrueTypeFont;
import amber.gl.camera.OrthographicCamera;
import static amber.gui.editor.map.MapContext.*;
import amber.gui.exc.ErrorHandler;
import amber.input.AbstractKeyboard;
import amber.input.AbstractMouse;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Tudor
 */
public class GLMapComponent2D extends AbstractGLMapComponent {

    protected FrameTimer timer = new FrameTimer();
    protected Vector2f cursorPos = new Vector2f();
    protected WeakHashMap<Tileset, Texture> textureCache = new WeakHashMap<Tileset, Texture>();
    protected TrueTypeFont font;
    protected float aspectRatio;
    protected boolean showDetails = true;
    protected OrthographicCamera cam = new OrthographicCamera();
    protected Thread renderer;
    protected boolean running;

    public GLMapComponent2D() throws LWJGLException {
    }

    public GLMapComponent2D(LevelMap map) throws LWJGLException {
        super(map);
        setMinimumSize(new Dimension(50, 50));
        setPreferredSize(new Dimension(50, 50));
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void initGL() {
        System.out.println("GL " + glGetString(GL_VERSION));
        try {
            AbstractKeyboard.create(AbstractKeyboard.AWT);
            AbstractMouse.create(AbstractMouse.AWT);
        } catch (LWJGLException ex) {
            ErrorHandler.alert(ex);
        }

        gleClearColor(Color.WHITE);
        font = new TrueTypeFont(new Font("Courier", Font.PLAIN, 15), true);

        glEnable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glEnable(GL_COLOR_MATERIAL);
        glEnable(GL_TEXTURE_2D); // Enable Texture Mapping       
        glDisable(GL_DITHER);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_NORMALIZE); // calculated normals when scaling      
        glEnable(GL_BLEND); // Enabled blending
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // selects blending method
        glEnable(GL_ALPHA_TEST); // allows alpha channels or transperancy
        glAlphaFunc(GL_GREATER, 0.1f); // sets aplha function

        timer.start();

        (renderer = new Thread() {
            @Override
            public void run() {
                running = true;
                while (running) {
                    if (isShowing() && isFocusOwner()) {
                        timer.updateFPS();
                        repaint();
                        Display.sync(120);
                    } else {
                        OS.sleep(300); // Prevent useless CPU cycles when not showing
                    }
                }
            }
        }).start();

        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                // Hierarchy change can signify the AWTGLCanvas destroying the original GL context,
                // so we have to clear the now invalid texture cache.
                textureCache.clear();
            }
        });
    }

    //@Override
    protected void pollInput() {
        float dxy = (float) timer.getDelta() * 2f * 0.1f;
        cam.processKeyboard(1, dxy, dxy);

        cursorPos.x = (int) (AbstractMouse.getX(this) + cam.x()) / 32;
        cursorPos.y = (int) (AbstractMouse.getY(this) + cam.y()) / 32;

        if (AbstractMouse.isButtonDown(0)) {
            LevelMap pre = context.map.clone();
            switch (context.drawMode) {
                case MODE_BRUSH:
                    if (setTileAt((int) cursorPos.x, (int) cursorPos.y)) {
                        context.undoStack.push(pre);
                    }
                    break;
                case MODE_FILL:
                    if (floodFillAt((int) cursorPos.x, (int) cursorPos.y)) {
                        context.undoStack.push(pre);
                    }
                    break;
            }
        }
        while (AbstractKeyboard.next()) {
            if (AbstractKeyboard.getEventKeyState()) {
                switch (AbstractKeyboard.getEventKey()) {
                    case Keyboard.KEY_P:
                        gleToggleWireframe();
                        break;
                    case Keyboard.KEY_I:
                        showDetails = !showDetails;
                        break;
                    case Keyboard.KEY_Z:
                        if (AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL)
                                || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !context.undoStack.empty()) {
                            context.redoStack.push(context.map.clone());
                            context.map = context.undoStack.pop();
                        }
                        break;
                    case Keyboard.KEY_Y:
                        if (AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL)
                                || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                                && !context.redoStack.empty()) {
                            context.undoStack.push(context.map.clone());
                            context.map = context.redoStack.pop();
                        }
                        break;
                    case Keyboard.KEY_S:
                        if (AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL) || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        FileOutputStream fos = new FileOutputStream(context.outputFile);
                                        Codec.getLatestCodec().compileMap(context.map, new DataOutputStream(fos));
                                        fos.close();
                                    } catch (Exception ex) {
                                        ErrorHandler.alert(ex);
                                    }
                                }
                            }.start();
                        }
                }
            }
        }
        AbstractMouse.poll();
    }

    @Override
    protected void paintGL() {
        pollInput();
        float aspect = (float) getWidth() / (float) getHeight();
        if (aspect != aspectRatio) {
            glViewport(0, 0, getWidth(), getHeight());
            aspectRatio = aspect;
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, getWidth(), 0, getHeight(), -1, 1);
        }
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        cam.applyTranslations();

        glEnable(GL_TEXTURE_RECTANGLE_ARB);
        List<Layer> layers = context.map.getLayers();
        for (int i = 0; i != layers.size(); i++) {
            drawLayer(layers.get(i));
        }
        drawGrid();

        if (showDetails) {
            glPushMatrix();
            glLoadIdentity();
            glDisable(GL_TEXTURE_RECTANGLE_ARB);
            glPushAttrib(GL_CURRENT_BIT | GL_POLYGON_BIT);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Ensure we're not in wireframe mode
            GLColor.BLACK.bind();
            font.drawString(0, getHeight() - font.getHeight(), "FPS: " + timer.fps() + "\n"
                    + (!AbstractMouse.isGrabbed() ? "Cursor: (" + (int) cursorPos.x + ", " + (int) cursorPos.y + ")" : ""), 1f, 1f, TrueTypeFont.ALIGN_LEFT);
            glPopAttrib();
            glPopMatrix();
        }

        try {
            swapBuffers();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    protected void drawLayer(Layer layer) {
        int bound = -1;
        SparseVector<SparseMatrix<Tile>> tileVector = layer.tileMatrix();
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getLength(); y++) {
                SparseVector.SparseVectorIterator iterator = tileVector.iterator();
                while (iterator.hasNext()) {
                    SparseMatrix<Tile> matrix = (SparseMatrix<Tile>) iterator.next();
                    Tile t = matrix.get(x, y);

                    if (t != null) {
                        Tileset.TileSprite sprite = t.getSprite();
                        Tileset sheet = sprite.getTileset();
                        Texture txt;
                        if (textureCache.containsKey(sheet)) {
                            txt = textureCache.get(sheet);
                        } else {
                            textureCache.put(sheet, txt = TextureLoader.getTexture(sheet.getImage(), GL_TEXTURE_RECTANGLE_ARB, GL_RGBA));
                        }
                        if (txt.getID() != bound) {
                            glBindTexture(txt.getTarget(), bound = txt.getID());
                        }

                        Point start = sprite.getStart();
                        Dimension size = sprite.getSize();

                        // This coordinate offsetting is a horrible, hacky way to solve
                        // the issue of texture borders bleeding.
                        float tx = start.x + .5f;
                        float ty = start.y + .5f;
                        float th = size.height - 1;
                        float tw = size.width - 1;

                        Vector2f v0 = new Vector2f(tx, ty + th);
                        Vector2f v1 = new Vector2f(tx, ty);
                        Vector2f v2 = new Vector2f(tx + tw, ty);
                        Vector2f v3 = new Vector2f(tx + tw, ty + th);

                        int dx = x * 32;
                        int dy = y * 32;

                        glBegin(GL_TRIANGLES);
                        {
                            //0
                            glTexCoord2f(v0.x, v0.y);
                            glVertex2f(dx, dy);
                            //1
                            glTexCoord2f(v1.x, v1.y);
                            glVertex2f(dx, dy + 32);
                            //2
                            glTexCoord2f(v2.x, v2.y);
                            glVertex2f(dx + 32, dy + 32);

                            //3 
                            glTexCoord2f(v3.x, v3.y);
                            glVertex2f(dx + 32, dy);
                            //2
                            glTexCoord2f(v2.x, v2.y);
                            glVertex2f(dx + 32, dy + 32);
                            //0
                            glTexCoord2f(v0.x, v0.y);
                            glVertex2f(dx, dy);
                        }
                        glEnd();
                    }
                }
            }
        }
        glBindTexture(GL_TEXTURE_RECTANGLE_ARB, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    protected void drawGrid() {
        glPushAttrib(GL_CURRENT_BIT | GL_LINE_BIT);
        if (context.drawGrid) {
            glBegin(GL_LINES);
            {
                GLColor.GRAY.bind();
                for (int x = 0; x <= context.map.getWidth(); x++) {
                    gleLine2d(x * 32, 0, x * 32, context.map.getLength() * 32);
                }
                for (int y = 0; y <= context.map.getLength(); y++) {
                    gleLine2d(0, y * 32, context.map.getWidth() * 32, y * 32);
                }
            }
            glEnd();
        }

        glLineWidth(3);
        GLColor.BLACK.bind();
        glBegin(GL_LINES);
        {
            gleLine2d(0, 0, context.map.getWidth() * 32, 0);
            gleLine2d(0, 0, 0, context.map.getLength() * 32);
            gleLine2d(context.map.getWidth() * 32, 0, context.map.getWidth() * 32, context.map.getLength() * 32);
            gleLine2d(0, context.map.getLength() * 32, context.map.getWidth() * 32, context.map.getWidth() * 32);
        }
        glEnd();
        glLineWidth(2);
        if (cursorPos != null && context.tileSelection != null && context.tileSelection.length > 0 && context.tileSelection[0].length > 0) {
            gleRect2d(cursorPos.x * 32, cursorPos.y * 32, context.tileSelection.length * 32, context.tileSelection[0].length * 32);
        }
        glPopAttrib();
    }

    /**
     * Handles errors that were thrown during a paint
     *
     * @param exception The exception that was thrown
     */
    protected void exceptionOccurred(LWJGLException exception) {
        exception.printStackTrace();
    }

    protected boolean floodFillAt(int x, int y) {
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

                if (setTileAt(p.x, p.y)) {
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

    protected boolean setTileAt(int x, int y) {
        boolean modified = false;
        if (context.tileSelection != null) {
            Layer lay = context.map.getLayer(context.layer);
            for (int cx = 0; cx != context.tileSelection.length; cx++) {
                for (int cy = 0; cy != context.tileSelection[0].length; cy++) {
                    int mapX = cx + x, mapY = cy + y;
                    if (isInBounds(mapX, mapY)) {
                        // We need to flip the array horizontally, so inverse the y
                        Tile t = new Tile(context.tileSelection[cx][context.tileSelection[0].length - cy - 1]);
                        Tile r = lay.getTile(mapX, mapY, 0);
                        modified = r == null || !t.getSprite().equals(r.getSprite());
                        lay.setTile(mapX, mapY, 0, t);
                    }
                }
            }
        }
        return modified;
    }

    protected Tileset.TileSprite spriteAt(int x, int y) {
        if (isInBounds(x, y)) {
            Tile tile = context.map.getLayer(context.layer).getTile(x, y, 0);
            return tile != null ? tile.getSprite() : Tileset.TileSprite.NULL_SPRITE;
        }
        return null;
    }

    public JMenu[] getContextMenus() {
        return new JMenu[0];
    }
}

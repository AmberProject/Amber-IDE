package tk.amberide.ide.gui.editor.map;

import tk.amberide.engine.renderer.GLMapRenderer3D;
import tk.amberide.Amber;
import tk.amberide.engine.data.map.LevelMap;

import static tk.amberide.engine.data.map.Angle.*;

import tk.amberide.engine.data.math.vec.Ray;
import tk.amberide.engine.data.math.vec.Vec3d;
import tk.amberide.engine.input.AbstractKeyboard;
import tk.amberide.engine.gl.FrameTimer;
import tk.amberide.engine.gl.GLColor;

import static tk.amberide.engine.gl.GLE.*;

import tk.amberide.engine.gl.Sprite;
import tk.amberide.engine.gl.TrueTypeFont;
import tk.amberide.engine.gl.camera.EulerCamera;

import static tk.amberide.ide.gui.editor.map.MapContext.*;

import tk.amberide.ide.gui.editor.map.tool.BrushTool;
import tk.amberide.ide.gui.editor.map.tool.EraserTool;
import tk.amberide.ide.gui.editor.map.tool.FillTool;
import tk.amberide.ide.gui.editor.map.tool.Tool;
import tk.amberide.engine.input.AbstractMouse;
import tk.amberide.ide.swing.MenuBuilder;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;
import static tk.amberide.engine.input.AbstractMouse.*;

import tk.amberide.ide.swing.misc.TransferableImage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javax.swing.UIManager;

import org.lwjgl.BufferUtils;
import tk.amberide.engine.data.map.Angle;

/**
 * @author Tudor
 */
public class GLMapComponent3D extends AbstractGLMapComponent {

    protected FrameTimer timer = new FrameTimer();
    protected Vec3d cursorPos = new Vec3d();
    protected Angle currentAngle = HORIZONTAL;
    protected EulerCamera cam = new EulerCamera.Builder()
            .setPosition(0, 3, 0)
            .setFieldOfView(60)
            .setRotation(50, 135, 0)
            .setFarClippingPane(1000f)
            .build();
    protected TrueTypeFont font;
    protected Sprite compassRose;
    protected GLMapRenderer3D renderer;
    protected Panel display = new Panel(new BorderLayout());

    public GLMapComponent3D(LevelMap map) throws LWJGLException {
        super(map);
        setMinimumSize(new Dimension(0, 0));
        setPreferredSize(new Dimension(0, 0));
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        context.EXT_cardinalSupported = true;
        context.EXT_modelSelectionSupported = true;
        display.add(this);
        renderer = new GLMapRenderer3D(map);
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
        glEnable(GL_POLYGON_OFFSET_FILL);

        timer.start();

        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                // Hierarchy change can signify the AWTGLCanvas destroying the original GL context,
                // so we have to clear the now invalid texture cache.
                renderer.invalidate();
                compassRose = null;
                AbstractKeyboard.destroy(); // Prevent double events
                AbstractMouse.destroy();
            }
        });
    }

    @Override
    protected void pollInput() {
        super.pollInput();
        if (!isFocusOwner()) {
            while (AbstractKeyboard.next()) ;
            while (AbstractMouse.next()) ;
            return;
        }

        if (isGrabbed()) {
            cam.processMouse(1, 80, -80);
        }

        if (!(AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL) || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL))) {
            // Frame-rate independant movement        
            float dxyz = (float) timer.getDelta() * 8f * 0.1f;
            dxyz = Math.min(dxyz, 100);
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
                LevelMap pre = context.map.clone();
                if (currentTool().apply((int) cursorPos.x, (int) cursorPos.z, (int) cursorPos.y)) {
                    context.undoStack.push(pre);
                    modified = true;
                }
            } else if (isButtonDown(1)) {
                if (AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL) || AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    AbstractMouse.setGrabbed(true);
                } else {
                    AbstractMouse.setGrabbed(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }

            AbstractMouse.poll();
        }
    }

    @Override
    protected void doKey(int keycode) {
        switch (keycode) {
            case Keyboard.KEY_SUBTRACT:
                if (cursorPos.y > 0) {
                    cursorPos.y--;
                }
                break;
            case Keyboard.KEY_ADD:
                cursorPos.y++;
                break;
            case Keyboard.KEY_I:
                if (AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL) || AbstractKeyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                    int w = getWidth();
                    int h = getHeight();
                    glReadBuffer(GL_FRONT);
                    int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
                    ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * bpp);
                    glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
                    BufferedImage shot = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            int i = (x + (w * y)) * bpp;
                            int r = buffer.get(i) & 0xFF;
                            int g = buffer.get(i + 1) & 0xFF;
                            int b = buffer.get(i + 2) & 0xFF;
                            shot.setRGB(x, h - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                        }
                    }
                    TransferableImage trans = new TransferableImage(shot);
                    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                    c.setContents(trans, new ClipboardOwner() {
                        public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        }
                    });
                }
                break;
            default:
                if (currentTool() != null) currentTool().doKey(keycode);
        }
    }

    @Override
    protected void doScroll(int delta) {
        if (currentTool() != null) currentTool().doScroll(delta);
    }

    /**
     * Paints the preview
     */
    @Override
    public void paintGL() {
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
        if (renderer.getMap() != context.map) {
            renderer = new GLMapRenderer3D(context.map);
        }
        renderer.render();

        glDisable(GL_TEXTURE_2D);
        drawGrid();
        glEnable(GL_TEXTURE_2D);

        glPushAttrib(GL_CURRENT_BIT | GL_LINE_BIT);
        GLColor.BLACK.bind();
        glLineWidth(2);
        if (!AbstractMouse.isGrabbed() && currentTool() != null) {
            currentTool().draw((int) cursorPos.x, (int) cursorPos.y, (int) cursorPos.z);
        }
        glPopAttrib();

        if (info || compass) {
            glePushOrthogonalMode(0, getWidth(), 0, getHeight());
            if (compass) {
                if (compassRose == null) {
                    compassRose = new Sprite("icon/MapEditor.Compass-Rose-small.png");
                }
                glPushMatrix();
                glTranslatef(getWidth() / 2 + getWidth() / 3f, getHeight() / 2 + getHeight() / 3.5f, 0);
                glRotatef(cam.yaw() - 90, 0, 0, 1);
                float ratio = ((float) getWidth()) / ((float) getHeight()) * .7f;
                //System.out.println(ratio);
                ratio = 0.7f;
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
                        + "Direction: " + cam.getFacingDirection() + "\n"
                        + (!AbstractMouse.isGrabbed() ? "Cursor: (" + (int) cursorPos.x + ", " + (int) cursorPos.z + ")" : ""), 1f, 1f, TrueTypeFont.ALIGN_LEFT);
                glPopAttrib();
            }
            glePushFrustrumMode();
        }

        try {
            swapBuffers();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        timer.updateFPS();
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
        glPopAttrib();
    }

    @Override
    public Component getComponent() {
        return display;
    }

    protected Tool brushTool = new BrushTool(context, cam);
    protected Tool eraseTool = new EraserTool(context, cam);
    protected Tool fillTool = new FillTool(context, cam);

    private Tool currentTool() {
        switch (context.drawMode) {
            case MODE_BRUSH:
                return brushTool;
            case MODE_FILL:
                return fillTool;
            case MODE_ERASE:
                return eraseTool;
        }
        return null;
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

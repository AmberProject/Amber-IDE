package amber.gui.editor.map;

import amber.data.map.Layer;
import amber.data.map.LevelMap;
import amber.data.map.Tile;
import amber.data.res.Tileset;
import amber.data.sparse.SparseMatrix;
import amber.data.sparse.SparseVector;
import amber.gl.FrameTimer;
import amber.gui.editor.map.tool._2d.Brush2D;
import amber.gui.editor.map.tool._2d.Eraser2D;
import amber.gui.editor.map.tool._2d.Fill2D;
import amber.gui.editor.map.tool._2d.Tool2D;
import amber.input.awt.AWTInputMap;
import amber.swing.MenuBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static amber.gui.editor.map.MapContext.MODE_BRUSH;
import static amber.gui.editor.map.MapContext.MODE_ERASE;
import static amber.gui.editor.map.MapContext.MODE_FILL;

/**
 * @author xiaomao
 */
public class J2DMapComponent2D extends JComponent implements IMapComponent {

    protected final MapContext context = new MapContext();
    protected Point cursorPos = new Point();
    protected JScrollPane display = new JScrollPane(this);

    public J2DMapComponent2D() {
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        display.getVerticalScrollBar().setUnitIncrement(16);
        display.getHorizontalScrollBar().setUnitIncrement(16);

        setBackground(Color.WHITE);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPress(e);
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                onMouseMove(mouseEvent);
                onMouseDown(mouseEvent);
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                onMouseMove(mouseEvent);
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                //onMouseMove(mouseEvent);
                onMouseDown(mouseEvent);
                repaint();
            }
        });
    }

    public J2DMapComponent2D(LevelMap map) {
        this();
        context.map = map;

        setPreferredSize(new Dimension(map.getWidth() * 32 + 1, map.getLength() * 32 + 1));
    }

    @Override
    public MapContext getMapContext() {
        return context;
    }

    @Override
    public Component getComponent() {
        return display;
    }

    protected boolean info = true, grid = true;
    @Override
    public JMenu[] getContextMenus() {
        return new JMenu[] {
            new MenuBuilder("View").addCheckbox("Info", true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    info ^= true;
                    repaint();
                }
            }).addCheckbox("Grid", true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    grid ^= true;
                    repaint();
                }
            }).create()
        };
    }

    @Override
    public void paintComponent(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;
        g.setBackground(Color.WHITE);
        List<Layer> layers = context.map.getLayers();
        for (int i = 0; i != layers.size(); i++) {
            drawLayer(g, layers.get(i));
        }
        drawGrid(g);

        if (info)
            g.drawString(String.format("Cursor: (%d, %d)", cursorPos.x, cursorPos.y), 4, 4 + g.getFontMetrics().getHeight());
    }

    protected void drawLayer(Graphics2D g, Layer layer) {
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
                        BufferedImage texture = sheet.getImage();

                        Point start = sprite.getStart();
                        Dimension size = sprite.getSize();

                        int dx = x * 32;
                        int dy = (getHeight() - y * 32) - 32;

                        g.drawImage(texture, dx, dy, dx + 32, dy + 32, start.x, start.y, start.x + size.width, start.y + size.height, null);
                    }
                }
            }
        }
    }
    static final Stroke stroke2 = new BasicStroke(2);
    static final Stroke stroke3 = new BasicStroke(3);

    protected void drawGrid(Graphics2D g) {
        Color oldColor = g.getColor();
        g.setColor(Color.GRAY);

        Stroke oldStroke = g.getStroke();

        g.translate(0, getHeight() - context.map.getLength() * 32);

        if (grid) {
            for (int x = 0; x <= context.map.getWidth(); x++) {
                g.drawLine(x * 32, 0, x * 32, context.map.getLength() * 32);
            }
            for (int y = 0; y <= context.map.getLength(); y++) {
                g.drawLine(0, y * 32, context.map.getWidth() * 32, y * 32);
            }
        }

        g.setColor(Color.BLACK);
        g.setStroke(stroke3);
        g.drawLine(0, 0, context.map.getWidth() * 32, 0);
        g.drawLine(0, 0, 0, context.map.getLength() * 32);
        g.drawLine(context.map.getWidth() * 32, 0, context.map.getWidth() * 32, context.map.getLength() * 32);
        g.drawLine(0, context.map.getLength() * 32, context.map.getWidth() * 32, context.map.getWidth() * 32);

        g.setStroke(stroke2);
        if (cursorPos != null) {
            Dimension size = currentTool().getDrawRectangleSize();
            if (size.height > 0 && size.width > 0)
                g.drawRect(cursorPos.x * 32, getHeight() + 32 - cursorPos.y * 32 - size.height * 32, size.width * 32, size.height * 32);
        }

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
    protected FrameTimer timer = new FrameTimer();

    protected void onKeyPress(KeyEvent e) {
        int delta = timer.getDelta() / 5;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                display.getVerticalScrollBar().setValue(display.getVerticalScrollBar().getValue() - delta);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                display.getVerticalScrollBar().setValue(display.getVerticalScrollBar().getValue() + delta);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                display.getHorizontalScrollBar().setValue(display.getHorizontalScrollBar().getValue() - delta);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                display.getHorizontalScrollBar().setValue(display.getHorizontalScrollBar().getValue() + delta);
                break;
            case KeyEvent.VK_Z:
                if (e.isControlDown() && !context.undoStack.empty()) {
                    context.redoStack.push(context.map.clone());
                    context.map = context.undoStack.pop();
                }
                break;
            case KeyEvent.VK_Y:
                if (e.isControlDown() && !context.redoStack.empty()) {
                    context.undoStack.push(context.map.clone());
                    context.map = context.redoStack.pop();
                }
                break;
            default:
                currentTool().doKey(AWTInputMap.map(e));
                break;
        }
    }

    protected void onMouseDown(MouseEvent e) {
        requestFocusInWindow();
        if (e.getButton() == MouseEvent.BUTTON1 || e.getID() == MouseEvent.MOUSE_DRAGGED) {
            LevelMap pre = context.map.clone();
            Tool2D tool = currentTool();

            if (tool != null && tool.apply(cursorPos.x, cursorPos.y - 1)) {
                context.undoStack.push(pre);
            }
        }
    }

    protected void onMouseMove(MouseEvent e) {
        cursorPos.setLocation(e.getX() / 32, context.map.getLength() - e.getY() / 32);
    }
    protected Tool2D brushTool = new Brush2D(context);
    protected Tool2D eraseTool = new Eraser2D(context);
    protected Tool2D fillTool = new Fill2D(context);

    private Tool2D currentTool() {
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
}

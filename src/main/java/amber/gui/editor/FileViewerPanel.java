package amber.gui.editor;

import amber.data.io.FileIO;
import java.io.File;
import java.util.HashMap;
import javax.swing.JMenu;
import javax.swing.JPanel;

/**
 *
 * @author Tudor
 */
public abstract class FileViewerPanel extends JPanel {

    protected File file;
    private static HashMap<String, Class<? extends FileViewerPanel>> panels = new HashMap<String, Class<? extends FileViewerPanel>>();

    public FileViewerPanel(File file) {
        this.file = file;
    }

    public abstract JMenu[] getContextMenus();

    public static void registerPanel(Class<? extends FileViewerPanel> panel, String extension) {
        registerPanel(panel, new String[]{extension});
    }

    public static void registerPanel(Class<? extends FileViewerPanel> panel, String... extensions) {
        for (String ext : extensions) {
            panels.put(ext, panel);
        }
    }

    public static void setDefaultPanel(Class<? extends FileViewerPanel> panel) {
        panels.put(null, panel);
    }

    public static FileViewerPanel fileViewerPanelFor(File file) {
        System.out.println("!!!!!!!!!!!!!" + FileIO.getFileExtension(file));
        Class<? extends FileViewerPanel> clazz = panels.get(FileIO.getFileExtension(file));
        if (clazz == null) {
            clazz = panels.get(null);
        }
        System.out.println("Fileviewer for " + file + " is " + clazz);
        if (clazz != null) {
            try {
                return clazz.getConstructor(File.class).newInstance(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}

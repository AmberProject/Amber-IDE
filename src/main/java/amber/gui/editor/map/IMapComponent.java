package amber.gui.editor.map;

import java.awt.Component;
import javax.swing.JMenu;

/**
 *
 * @author Tudor
 */
public interface IMapComponent {

    MapContext getMapContext();

    Component getComponent();

    JMenu[] getContextMenus();
}

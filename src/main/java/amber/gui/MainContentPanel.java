package amber.gui;

import amber.Amber;
import amber.data.Workspace;
import amber.data.state.LazyState;
import amber.data.state.Scope;
import amber.data.state.node.IState;
import amber.gui.editor.FileViewerPanel;
import amber.gui.editor.tool.ToolPanel;
import amber.gui.misc.FileTreeExplorer;
import amber.swing.UIUtil;
import amber.swing.tabs.CloseableTabbedPane;
import amber.swing.tabs.CloseableTabbedPane.CloseableTabComponent;
import amber.swing.tabs.TabCloseListener;
import amber.swing.tree.SmartExpander;
import amber.swing.tree.Trees;
import amber.tool.ToolDefinition;
import amber.tool.ToolManifest;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.*;

/**
 *
 * @author Tudor
 */
public class MainContentPanel extends javax.swing.JPanel {

    protected HashMap<Component, String> activeFiles = new HashMap<Component, String>();
    protected CloseableTabbedPane activeFilesTabbedPane;
    protected JLabel openFileLabel = new JLabel("Double-click a file to open it.", JLabel.CENTER);

    @LazyState(scope = Scope.PROJECT, name = "ProjectTreeExpansion")
    protected String saveTreeExpansion() {
        return Trees.getExpansionState(treeView, 0);
    }

    /**
     * Creates new form MainIDEPanel
     */
    public MainContentPanel(Workspace workspace) {
        initComponents();
        Amber.getStateManager().unregisterStateOwner(this);
        treeView.addFileTreeAdapter(new FileTreeExplorer(treeView));
        treeView.setRoot(workspace.getRootDirectory());
        IState treeState = Amber.getStateManager().getState(Scope.PROJECT, "ProjectTreeExpansion");

        if (treeState != null) {
            Trees.restoreExpanstionState(treeView, 0, (String) treeState.get());
        }
        Amber.getStateManager().registerStateOwner(this);
        SmartExpander.installOn(treeView);
        treeView.setRootVisible(true);
        projectDivider.setRightComponent(openFileLabel);
    }

    public CloseableTabbedPane getFilesTabbedPane() {
        if (activeFilesTabbedPane == null) {
            activeFilesTabbedPane = new CloseableTabbedPane();
            activeFilesTabbedPane.addTabCloseListener(new TabCloseListener() {
                public boolean tabClosed(String title, Component comp, CloseableTabbedPane pane) {
                    System.out.println("Tab closing...");
                    if (comp instanceof FileViewerPanel && ((FileViewerPanel) comp).modified()) {
                        switch (JOptionPane.showConfirmDialog(MainContentPanel.this,
                                "Do you want to save the following file:\n" + title,
                                "Confirm Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                            case JOptionPane.YES_OPTION:
                                ((FileViewerPanel) comp).save();
                                break;
                            case JOptionPane.NO_OPTION:
                                break;
                            case JOptionPane.CANCEL_OPTION:
                                return false;
                        }
                    }
                    if (activeFiles.containsKey(comp)) {
                        Amber.getWorkspace().getOpenedFiles().remove(activeFiles.remove(comp));
                    }
                    if (activeFilesTabbedPane.getTabCount() == 1) {
                        projectDivider.setRightComponent(openFileLabel);
                        activeFilesTabbedPane = null;
                    }
                    return true;
                }
            });
            int location = projectDivider.getDividerLocation();
            projectDivider.setRightComponent(activeFilesTabbedPane);
            projectDivider.setDividerLocation(location);
        }
        return activeFilesTabbedPane;
    }

    public void addTab(String id, Component tab) {
        activeFilesTabbedPane.add(id, tab);
    }

    public void addFileTab(FileViewerPanel file) {
        try {
            getFilesTabbedPane().add(file.getFile().getName(), file);
        } catch (RuntimeException ex) {
            getFilesTabbedPane().remove(file);
            throw ex; // Propagate to the error handler in FileTreeExplorer
        }
        activeFiles.put(file, file.getFile().getAbsolutePath());
        int i = getFilesTabbedPane().getTabCount() - 1;
        getFilesTabbedPane().setSelectedIndex(i);
        ((CloseableTabComponent) getFilesTabbedPane().getTabComponentAt(i)).getTitleLabel().setIcon(file.getTabIcon());
    }

    public void addToolTab(ToolDefinition tool) {
        ToolPanel toolPanel = new ToolPanel(tool);
        ToolManifest mf = tool.getManifest();
        try {
            getFilesTabbedPane().add(mf.name(), toolPanel);
        } catch (RuntimeException ex) {
            getFilesTabbedPane().remove(toolPanel);
            throw ex; // Propagate to the error handler in FileTreeExplorer
        }
        getFilesTabbedPane().setToolTipTextAt(getFilesTabbedPane().getTabCount() - 1,
                String.format("<html>"
                + "<b>%s</b> v%s by %s"
                + "<br/>"
                + "&nbsp;&nbsp;&nbsp;%s"
                + "</html>", mf.name(), mf.version(), Arrays.toString(mf.authors()), mf.description()));
        getFilesTabbedPane().setSelectedIndex(getFilesTabbedPane().getTabCount() - 1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectDivider = new amber.swing.misc.ThinSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        treeView = new amber.swing.tree.filesystem.FileSystemTree();

        setLayout(new java.awt.BorderLayout());

        projectDivider.setDividerLocation(150);
        projectDivider.setDividerSize(0);
        projectDivider.setMinimumSize(new java.awt.Dimension(0, 0));

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setViewportView(treeView);

        projectDivider.setLeftComponent(jScrollPane1);

        add(projectDivider, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private amber.swing.misc.ThinSplitPane projectDivider;
    private amber.swing.tree.filesystem.FileSystemTree treeView;
    // End of variables declaration//GEN-END:variables
}

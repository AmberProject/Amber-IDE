package amber;

import amber.data.Workspace;
import amber.gui.MainContentPanel;
import amber.gui.dialogs.AboutDialog;
import amber.gui.dialogs.JFontChooser;
import amber.gui.dialogs.NewProjectDialog;
import amber.gui.dialogs.ResourceDialog;
import amber.gui.dialogs.SettingsDialog;
import amber.gui.editor.FileViewerPanel;
import amber.gui.misc.StartPagePanel;
import amber.swing.Dialogs;
import amber.swing.UIUtil;
import amber.tool.ToolDefinition;

import java.awt.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Tudor
 */
public class IDE extends javax.swing.JFrame {

    private MainContentPanel content;

    /**
     * Creates new form Applet
     */
    public IDE() {
        initComponents();
        contentPane.add(BorderLayout.CENTER, new StartPagePanel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Component c : content.getFilesTabbedPane().getComponents()) {
                    if (c instanceof FileViewerPanel && ((FileViewerPanel) c).modified()) {
                        content.getFilesTabbedPane().setSelectedComponent(c);
                        switch (JOptionPane.showConfirmDialog(IDE.this,
                                "Do you want to save the following file:\n" + ((FileViewerPanel) c).getFile().getName(),
                                "Confirm Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                            case JOptionPane.YES_OPTION:
                                ((FileViewerPanel) c).save();
                                break;
                            case JOptionPane.NO_OPTION:
                                break;
                            case JOptionPane.CANCEL_OPTION:
                                return;
                        }
                    }
                }
                setVisible(false);
                System.exit(0);
            }
        });
    }

    public void loadProject(Workspace space) {
        contentPane.removeAll();
        contentPane.add(BorderLayout.CENTER, content = new MainContentPanel(space));
    }

    void addToolTab(final ToolDefinition tool) {
        content.addToolTab(tool);
    }

    void openFile(final File file) {
        FileViewerPanel editor;
        try {
            editor = FileViewerPanel.fileViewerPanelFor(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof IOException) {
                Dialogs.errorDialog()
                        .setTitle("Exception while loading file.")
                        .setMessage("Failed to read file: " + ex)
                        .show();
            } else {
                Dialogs.errorDialog()
                        .setTitle("Failed to create editor display.")
                        .setMessage("An error occured: " + ex)
                        .show();
            }
            return;
        }
        content.addFileTab(editor);
    }

    public JMenuBar getMenu() {
        return menuBar;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JSeparator();
        list1 = new java.awt.List();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToolBar1 = new javax.swing.JToolBar();
        newFileButton = new javax.swing.JButton();
        newProjectButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        resourceButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        headerSeparator = new javax.swing.JSeparator();
        footerSeparator = new javax.swing.JSeparator();
        memoryMonitorProgressBar1 = new amber.gui.misc.MemoryMonitorProgressBar();
        contentPane = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newProjectItem = new javax.swing.JMenuItem();
        newFileItem = new javax.swing.JMenuItem();
        openItem = new javax.swing.JMenuItem();
        saveItem = new javax.swing.JMenuItem();
        saveAsItem = new javax.swing.JMenuItem();
        settingsItem = new javax.swing.JMenuItem();
        synchItem = new javax.swing.JMenuItem();
        resourcesItem = new javax.swing.JMenu();
        manageItem = new javax.swing.JMenuItem();
        newResourceItem = new javax.swing.JMenu();
        newTilesetItem = new javax.swing.JMenuItem();
        newAudioItem = new javax.swing.JMenuItem();
        newModelItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutItem = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("amber/Bundle"); // NOI18N
        jMenu3.setText(bundle.getString("IDE.jMenu3.text")); // NOI18N
        jMenuBar2.add(jMenu3);

        jMenu4.setText(bundle.getString("IDE.jMenu4.text")); // NOI18N
        jMenuBar2.add(jMenu4);

        jToggleButton1.setText(bundle.getString("IDE.jToggleButton1.text")); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("IDE.title")); // NOI18N
        setFocusTraversalPolicyProvider(true);
        setIconImage(new javax.swing.ImageIcon(ClassLoader.getSystemResource("icon/Logo.png")).getImage());

        jToolBar1.setFloatable(false);

        newFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.NewFile.Large.png"))); // NOI18N
        newFileButton.setToolTipText(bundle.getString("IDE.newFileButton.toolTipText")); // NOI18N
        newFileButton.setFocusable(false);
        newFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newFileButton.setMargin(new java.awt.Insets(14, 14, 14, 14));
        newFileButton.setMaximumSize(new java.awt.Dimension(28, 32));
        newFileButton.setMinimumSize(new java.awt.Dimension(28, 32));
        newFileButton.setPreferredSize(new java.awt.Dimension(28, 32));
        newFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFileButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(newFileButton);

        newProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.NewProject.Large.png"))); // NOI18N
        newProjectButton.setToolTipText(bundle.getString("IDE.newProjectButton.toolTipText")); // NOI18N
        newProjectButton.setFocusable(false);
        newProjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newProjectButton.setMargin(new java.awt.Insets(14, 14, 14, 14));
        newProjectButton.setMaximumSize(new java.awt.Dimension(28, 32));
        newProjectButton.setMinimumSize(new java.awt.Dimension(28, 32));
        newProjectButton.setPreferredSize(new java.awt.Dimension(28, 32));
        newProjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(newProjectButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.OpenProject.Large.png"))); // NOI18N
        openButton.setToolTipText(bundle.getString("IDE.openButton.toolTipText")); // NOI18N
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setMargin(new java.awt.Insets(14, 14, 14, 14));
        openButton.setMaximumSize(new java.awt.Dimension(28, 32));
        openButton.setMinimumSize(new java.awt.Dimension(28, 32));
        openButton.setPreferredSize(new java.awt.Dimension(28, 32));
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.Save.Large.png"))); // NOI18N
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setMargin(new java.awt.Insets(14, 14, 14, 14));
        saveButton.setMaximumSize(new java.awt.Dimension(28, 32));
        saveButton.setMinimumSize(new java.awt.Dimension(28, 32));
        saveButton.setPreferredSize(new java.awt.Dimension(28, 32));
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(saveButton);
        jToolBar1.add(jSeparator4);

        resourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.Resources.Large.png"))); // NOI18N
        resourceButton.setFocusable(false);
        resourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resourceButton.setMargin(null);
        resourceButton.setMaximumSize(new java.awt.Dimension(28, 32));
        resourceButton.setMinimumSize(new java.awt.Dimension(28, 32));
        resourceButton.setPreferredSize(new java.awt.Dimension(28, 32));
        resourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resourceButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(resourceButton);
        jToolBar1.add(jSeparator2);

        contentPane.setLayout(new java.awt.BorderLayout());

        fileMenu.setText(bundle.getString("IDE.fileMenu.text")); // NOI18N

        newProjectItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        newProjectItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.NewProject.Small.png"))); // NOI18N
        newProjectItem.setText(bundle.getString("IDE.newProjectItem.text")); // NOI18N
        newProjectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProjectItem);

        newFileItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newFileItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.NewFile.Small.png"))); // NOI18N
        newFileItem.setText(bundle.getString("IDE.newFileItem.text")); // NOI18N
        newFileItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFileItemActionPerformed(evt);
            }
        });
        fileMenu.add(newFileItem);

        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/IDE.OpenProject.Small.png"))); // NOI18N
        openItem.setText(bundle.getString("IDE.openItem.text")); // NOI18N
        openItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openItemActionPerformed(evt);
            }
        });
        fileMenu.add(openItem);

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setText(bundle.getString("IDE.saveItem.text")); // NOI18N
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        saveAsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsItem.setText(bundle.getString("IDE.saveAsItem.text")); // NOI18N
        fileMenu.add(saveAsItem);

        settingsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, java.awt.event.InputEvent.ALT_MASK));
        settingsItem.setText(bundle.getString("IDE.settingsItem.text")); // NOI18N
        settingsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsItemActionPerformed(evt);
            }
        });
        fileMenu.add(settingsItem);

        synchItem.setText(bundle.getString("IDE.synchItem.text")); // NOI18N
        synchItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synchItemActionPerformed(evt);
            }
        });
        fileMenu.add(synchItem);

        resourcesItem.setText(bundle.getString("IDE.resourcesItem.text")); // NOI18N

        manageItem.setText(bundle.getString("IDE.manageItem.text")); // NOI18N
        resourcesItem.add(manageItem);

        newResourceItem.setText(bundle.getString("IDE.newResourceItem.text")); // NOI18N

        newTilesetItem.setText(bundle.getString("IDE.newTilesetItem.text")); // NOI18N
        newResourceItem.add(newTilesetItem);

        newAudioItem.setText(bundle.getString("IDE.newAudioItem.text")); // NOI18N
        newResourceItem.add(newAudioItem);

        newModelItem.setText(bundle.getString("IDE.newModelItem.text")); // NOI18N
        newResourceItem.add(newModelItem);

        resourcesItem.add(newResourceItem);

        fileMenu.add(resourcesItem);

        menuBar.add(fileMenu);

        helpMenu.setText(bundle.getString("IDE.helpMenu.text")); // NOI18N

        aboutItem.setText(bundle.getString("IDE.aboutItem.text")); // NOI18N
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(footerSeparator)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(memoryMonitorProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
                .addGap(14, 14, 14))
            .addComponent(contentPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(headerSeparator)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(footerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(memoryMonitorProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        JFileChooser browser = new JFileChooser("Choose project location...");
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browser.setApproveButtonText("Choose directory");
        browser.setVisible(true);
        if (browser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Amber.initializeProject(browser.getSelectedFile());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        Component selected = content.getFilesTabbedPane().getSelectedComponent();
        if (selected instanceof FileViewerPanel)
            ((FileViewerPanel) selected).save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void editorTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editorTabbedPaneStateChanged
    }//GEN-LAST:event_editorTabbedPaneStateChanged

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        new AboutDialog(this).setVisible(true);
    }//GEN-LAST:event_aboutItemActionPerformed

    private void resourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resourceButtonActionPerformed
        new ResourceDialog(this).setVisible(true);
    }//GEN-LAST:event_resourceButtonActionPerformed

    private void newFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFileButtonActionPerformed
    }//GEN-LAST:event_newFileButtonActionPerformed

    private void newFileItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFileItemActionPerformed
        newFileButtonActionPerformed(evt);
    }//GEN-LAST:event_newFileItemActionPerformed

    private void newProjectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectItemActionPerformed
        newProjectButtonActionPerformed(evt);
    }//GEN-LAST:event_newProjectItemActionPerformed

    private void newProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectButtonActionPerformed
        new NewProjectDialog(this).setVisible(true);
    }//GEN-LAST:event_newProjectButtonActionPerformed

    private void synchItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_synchItemActionPerformed
        //treeView.refresh();
    }//GEN-LAST:event_synchItemActionPerformed

    private void openItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openItemActionPerformed
        openButtonActionPerformed(evt);
    }//GEN-LAST:event_openItemActionPerformed

    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed
        saveButtonActionPerformed(evt);
    }//GEN-LAST:event_saveItemActionPerformed

    private void settingsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsItemActionPerformed
        new SettingsDialog(this).setVisible(true);
    }//GEN-LAST:event_settingsItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JPanel contentPane;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JSeparator footerSeparator;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToolBar jToolBar1;
    private java.awt.List list1;
    private javax.swing.JMenuItem manageItem;
    private amber.gui.misc.MemoryMonitorProgressBar memoryMonitorProgressBar1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newAudioItem;
    private javax.swing.JButton newFileButton;
    private javax.swing.JMenuItem newFileItem;
    private javax.swing.JMenuItem newModelItem;
    private javax.swing.JButton newProjectButton;
    private javax.swing.JMenuItem newProjectItem;
    private javax.swing.JMenu newResourceItem;
    private javax.swing.JMenuItem newTilesetItem;
    private javax.swing.JButton openButton;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JButton resourceButton;
    private javax.swing.JMenu resourcesItem;
    private javax.swing.JMenuItem saveAsItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JMenuItem settingsItem;
    private javax.swing.JMenuItem synchItem;
    // End of variables declaration//GEN-END:variables
}

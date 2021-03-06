package tk.amberide.ide.gui.misc;

import tk.amberide.Amber;
import tk.amberide.Storage;
import tk.amberide.ide.gui.dialogs.NewProjectDialog;
import java.awt.Container;
import java.io.File;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Tudor
 */
public class StartPagePanel extends javax.swing.JPanel {

    /**
     * Creates new form StartPane
     */
    public StartPagePanel() {
        initComponents();       
            DefaultTableModel model = (DefaultTableModel) recentProjectsTable.getModel();
            for (Map.Entry<String, String> entry : Storage.recentProjects.entrySet()) {
                model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
            if (recentProjectsTable.getModel().getRowCount() > 0) {
                recentProjectsTable.setRowSelectionInterval(0, 0);
            }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        recentProjectsTable = new javax.swing.JTable();
        openProjectButton = new javax.swing.JButton();
        newProjectButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(246, 246, 246));

        jPanel1.setBackground(new java.awt.Color(102, 153, 204));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(246, 246, 246));
        jPanel3.setMaximumSize(new java.awt.Dimension(0, 0));
        jPanel3.setName("Start Page"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Open an Existing Project");

        recentProjectsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Path", "Modified"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        recentProjectsTable.setFillsViewportHeight(true);
        recentProjectsTable.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        recentProjectsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        recentProjectsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(recentProjectsTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 491, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
        );

        openProjectButton.setText("Open Project");
        openProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectButtonActionPerformed(evt);
            }
        });

        newProjectButton.setText("New Project");
        newProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newProjectButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openProjectButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openProjectButton)
                    .addComponent(newProjectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void newProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectButtonActionPerformed
        new NewProjectDialog(Amber.getUI()).setVisible(true);
        Container parent = this.getParent();
        if (parent != null) {
            parent.remove(this);
        }
    }//GEN-LAST:event_newProjectButtonActionPerformed

    private void openProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectButtonActionPerformed
        if (recentProjectsTable.getModel().getRowCount() == 0) {
            // There are no projects, so create one
            JFileChooser browser = new JFileChooser("Choose project location...");
            browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            browser.setApproveButtonText("Choose directory");
            browser.setVisible(true);
            if (browser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Amber.initializeProject(browser.getSelectedFile());
            }
        } else {
            File project = new File((String) recentProjectsTable.getModel().getValueAt(recentProjectsTable.getSelectedRow(), 0));
            if (project.exists()) {
                Amber.initializeProject(project);
            }
        }
    }//GEN-LAST:event_openProjectButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton newProjectButton;
    private javax.swing.JButton openProjectButton;
    private javax.swing.JTable recentProjectsTable;
    // End of variables declaration//GEN-END:variables
}

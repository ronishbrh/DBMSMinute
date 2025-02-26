/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nitro
 */
public class DiscussionFrontend extends javax.swing.JFrame {
private int meetingID;
    /**
     * Creates new form DiscussionFrontend
     * @param meetingID
     */
    public DiscussionFrontend(int meetingID) {
        this.meetingID= meetingID;
        initComponents();
        
        showLatestDiscussionID();
        
        loadAgendaTopics(meetingID);
        
        loadDfTable(meetingID);
        
        setupListeners();
    }
    
    
    //==========================START OF DISCUSSION==============================================================
    
    
    private void setupListeners(){
        dfTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    int selectedRow = dfTable.getSelectedRow();

                    if (selectedRow != -1) { 
                        // Safely retrieve values from the table, handling null values
                        Object objDiscussionId = dfTable.getValueAt(selectedRow, 0);
                        Object objAgenda = dfTable.getValueAt(selectedRow, 1);
                        Object objDescription = dfTable.getValueAt(selectedRow, 2);
                        Object objFollowUp = dfTable.getValueAt(selectedRow, 3);
                        Object objState = dfTable.getValueAt(selectedRow, 4);
                        Object objNotes = dfTable.getValueAt(selectedRow, 5);


                        String discussionID = (objDiscussionId!= null) ? objDiscussionId.toString() : "";
                        String agenda = (objAgenda!= null) ? objAgenda.toString() : "";
                        String description = (objDescription != null) ? objDescription.toString() : "";
                        String followUp = (objFollowUp != null) ? objFollowUp.toString() : "";
                        String status = (objState != null) ? objState.toString() : "";
                        String notes = (objNotes != null) ? objNotes.toString() : "";

                        
                        discussionIDLabel.setText(String.valueOf(discussionID));
                        followUpForDiscussionLabel.setText(String.valueOf(discussionID));
                        descriptionTextArea.setText(description);
                        agendaComboBox.setSelectedItem(agenda);
                        fuDescriptionTextArea.setText(followUp);
                        fuStatusComboBox.setSelectedItem(status);
                        notesTextArea.setText(notes);
                    }
                }
            }     
        });
    }
    private void showLatestDiscussionID() {
        String getDiscussionIDquery = "SELECT discussion_id FROM discussion ORDER BY discussion_id DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getDiscussionIDquery);
             ResultSet DiscussionIDrs = pstmt.executeQuery()) {

            if (DiscussionIDrs.next()) {
                int discussionID = DiscussionIDrs.getInt("discussion_id"); 
                discussionID++;
                discussionIDLabel.setText(String.valueOf(discussionID)); 
                followUpForDiscussionLabel.setText(String.valueOf(discussionID)); 
            } else {
                discussionIDLabel.setText("1"); 
                followUpForDiscussionLabel.setText("1");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadAgendaTopics(int meetingID) {
        agendaComboBox.removeAllItems(); // Clear previous items

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic FROM agenda WHERE meeting_id = ?")) {

            pstmt.setInt(1, meetingID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String topic = rs.getString("topic");
                agendaComboBox.addItem(topic); 
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load agenda topics.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void loadDfTable(int meetingID) {
        String query = "SELECT d.discussion_id, a.topic AS agenda, d.description AS discussion_description, " +
                       "f.description AS follow_up_description, f.status, f.notes " +
                       "FROM discussion d " +
                       "JOIN agenda a ON d.agenda_id = a.agenda_id " +
                       "LEFT JOIN follow_up f ON d.discussion_id = f.discussion_id " +
                       "WHERE a.meeting_id = ?";

        DefaultTableModel model = (DefaultTableModel) dfTable.getModel();
        model.setRowCount(0); // Clear existing rows

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, meetingID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int discussionID = rs.getInt("discussion_id");
                String agenda = rs.getString("agenda");
                String discussionDesc = rs.getString("discussion_description");
                String followUpDesc = rs.getString("follow_up_description");
                String status = rs.getString("status");
                String notes = rs.getString("notes");

                model.addRow(new Object[]{discussionID, agenda, discussionDesc, followUpDesc, status, notes});
            }

            dfTable.setModel(model);  // Set data to the JTable
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load discussion and follow-up data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    
    
    
    //=================================================END OF DISCUSSION==============================

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        agendaComboBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        discussionIDLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        jLabel10 = new javax.swing.JLabel();
        followUpForDiscussionLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fuDescriptionTextArea = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        fuStatusComboBox = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        notesTextArea = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        dfTable = new javax.swing.JTable();
        deleteButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 0));
        jLabel1.setText("DISCUSSIONS ON AGENDA");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Agenda:");

        agendaComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Discussion ID:");

        discussionIDLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Description:");

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane1.setViewportView(descriptionTextArea);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Follow Up for Discussion ID:");

        followUpForDiscussionLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Description:");

        fuDescriptionTextArea.setColumns(20);
        fuDescriptionTextArea.setRows(5);
        jScrollPane2.setViewportView(fuDescriptionTextArea);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Follow Up Status");

        fuStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Scheduled", "Pending", "Completed", "Cancelled", "Re-scheduled" }));
        fuStatusComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fuStatusComboBoxActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Additional Notes:");

        notesTextArea.setColumns(20);
        notesTextArea.setRows(5);
        jScrollPane3.setViewportView(notesTextArea);

        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        dfTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Discussion ID", "Agenda", "Description", "Follow Up", "Status", "Notes"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(dfTable);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(184, 184, 184)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 833, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel8)
                                            .addComponent(jLabel7))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(discussionIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(agendaComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(11, 11, 11)
                                                .addComponent(updateButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(deleteButton)))))
                                .addGap(110, 110, 110)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(42, 42, 42)
                                        .addComponent(jLabel13))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(followUpForDiscussionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(fuStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(followUpForDiscussionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(discussionIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(agendaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 189, Short.MAX_VALUE))
                                    .addComponent(jScrollPane1)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fuStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deleteButton)
                            .addComponent(updateButton)
                            .addComponent(addButton))))
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void fuStatusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fuStatusComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fuStatusComboBoxActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
       
        int selectedRow = dfTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        String updateDiscussionSQL = "UPDATE discussion SET description = ? WHERE discussion_id = ?";
        String updateFollowUpSQL = "UPDATE follow_up SET description = ?, status = ?, notes = ? WHERE discussion_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); 

            // Update Discussion Table
            try (PreparedStatement pstmtDiscussion = conn.prepareStatement(updateDiscussionSQL)) {
                pstmtDiscussion.setString(1, descriptionTextArea.getText());
                pstmtDiscussion.setInt(2, Integer.parseInt(discussionIDLabel.getText()));
                pstmtDiscussion.executeUpdate();
            }

            // Update Follow-up Table
            try (PreparedStatement pstmtFollowUp = conn.prepareStatement(updateFollowUpSQL)) {
                pstmtFollowUp.setString(1, fuDescriptionTextArea.getText());
                pstmtFollowUp.setString(2, fuStatusComboBox.getSelectedItem().toString());
                pstmtFollowUp.setString(3, notesTextArea.getText());
                pstmtFollowUp.setInt(4, Integer.parseInt(discussionIDLabel.getText()));
                pstmtFollowUp.executeUpdate();
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(this, "Discussion and follow-up updated successfully.");
            loadDfTable(meetingID);
             showLatestDiscussionID();
                            followUpForDiscussionLabel.setText(discussionIDLabel.getText());
                            descriptionTextArea.setText("");
                            fuDescriptionTextArea.setText("");
                            fuStatusComboBox.setSelectedIndex(0);
                            notesTextArea.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update discussion/follow up.", "Error", JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_updateButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
                                        
        String getAgendaIDSQL = "SELECT agenda_id FROM agenda WHERE topic = ?";
        String checkDuplicateSQL = "SELECT COUNT(*) FROM discussion WHERE agenda_id = ? AND description = ?";
        String insertDiscussionSQL = "INSERT INTO discussion (agenda_id, description) VALUES (?, ?)";
        String getLastDiscussionIDSQL = "SELECT discussion_id FROM discussion WHERE agenda_id = ? ORDER BY discussion_id DESC LIMIT 1";
        String insertFollowUpSQL = "INSERT INTO follow_up (discussion_id, description, status, notes) VALUES (?, ?, ?, ?)";

        if (descriptionTextArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Discussion description is a required field!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement getAgendaIDStmt = conn.prepareStatement(getAgendaIDSQL)) {

            getAgendaIDStmt.setString(1, agendaComboBox.getSelectedItem().toString());
            ResultSet rs = getAgendaIDStmt.executeQuery();

            if (rs.next()) {
                int agendaID = rs.getInt("agenda_id"); // Retrieve the agenda_id

                // Check for duplicate discussion
                try (PreparedStatement checkDuplicateStmt = conn.prepareStatement(checkDuplicateSQL)) {
                    checkDuplicateStmt.setInt(1, agendaID);
                    checkDuplicateStmt.setString(2, descriptionTextArea.getText());
                    ResultSet duplicateRs = checkDuplicateStmt.executeQuery();

                    if (duplicateRs.next() && duplicateRs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "A discussion with the same description already exists for this agenda!", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                        return; // Prevent insertion
                    }
                }

                // Insert discussion
                try (PreparedStatement insertDiscussionStmt = conn.prepareStatement(insertDiscussionSQL)) {
                    insertDiscussionStmt.setInt(1, agendaID);
                    insertDiscussionStmt.setString(2, descriptionTextArea.getText());
                    insertDiscussionStmt.executeUpdate();
                }

                // Retrieve the last inserted discussion_id
                try (PreparedStatement getDiscussionIDStmt = conn.prepareStatement(getLastDiscussionIDSQL)) {
                    getDiscussionIDStmt.setInt(1, agendaID);
                    ResultSet discussionRs = getDiscussionIDStmt.executeQuery();

                    if (discussionRs.next()) {
                        int discussionID = discussionRs.getInt("discussion_id");

                        // Insert follow-up
                        try (PreparedStatement insertFollowUpStmt = conn.prepareStatement(insertFollowUpSQL)) {
                            insertFollowUpStmt.setInt(1, discussionID);
                            insertFollowUpStmt.setString(2, fuDescriptionTextArea.getText());
                            insertFollowUpStmt.setString(3, fuStatusComboBox.getSelectedItem().toString());  
                            insertFollowUpStmt.setString(4, notesTextArea.getText());  
                            insertFollowUpStmt.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Discussion and follow-up added successfully.");
                            loadDfTable(meetingID);
                            showLatestDiscussionID();
                            followUpForDiscussionLabel.setText(discussionIDLabel.getText());
                            descriptionTextArea.setText("");
                            fuDescriptionTextArea.setText("");
                            fuStatusComboBox.setSelectedIndex(0);
                            notesTextArea.setText("");
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No agenda found for the selected topic.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to insert discussion and follow-up.", "Error", JOptionPane.ERROR_MESSAGE);
        }


    

    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
      
        int selectedRow = dfTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int discussionID = (int) dfTable.getValueAt(selectedRow, 0);  

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this discussion and its follow-up?", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // Exit if user cancels deletion
        }

        String deleteFollowUpSQL = "DELETE FROM follow_up WHERE discussion_id = ?";
        String deleteDiscussionSQL = "DELETE FROM discussion WHERE discussion_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Delete Follow-up Record
            try (PreparedStatement pstmtFollowUp = conn.prepareStatement(deleteFollowUpSQL)) {
                pstmtFollowUp.setInt(1, discussionID);
                pstmtFollowUp.executeUpdate();
            }

            // Delete Discussion Record
            try (PreparedStatement pstmtDiscussion = conn.prepareStatement(deleteDiscussionSQL)) {
                pstmtDiscussion.setInt(1, discussionID);
                pstmtDiscussion.executeUpdate();
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(this, "Discussion and follow-up deleted successfully.");
            loadDfTable(meetingID);
             showLatestDiscussionID();
                            followUpForDiscussionLabel.setText(discussionIDLabel.getText());
                            descriptionTextArea.setText("");
                            fuDescriptionTextArea.setText("");
                            fuStatusComboBox.setSelectedIndex(0);
                            notesTextArea.setText("");

            // Remove row from table model
            DefaultTableModel model = (DefaultTableModel) dfTable.getModel();
            model.removeRow(selectedRow);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete discussion.", "Error", JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_deleteButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DiscussionFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DiscussionFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DiscussionFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DiscussionFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DiscussionFrontend(0).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox<String> agendaComboBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JTable dfTable;
    private javax.swing.JLabel discussionIDLabel;
    private javax.swing.JLabel followUpForDiscussionLabel;
    private javax.swing.JTextArea fuDescriptionTextArea;
    private javax.swing.JComboBox<String> fuStatusComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea notesTextArea;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}

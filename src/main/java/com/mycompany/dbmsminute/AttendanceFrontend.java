/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 *
 * @author Nitro
 */
public class AttendanceFrontend extends javax.swing.JFrame {
    private int meetingID;
    private int currentMeetingID;
    private String committeeName;
    /**
     * Creates new form AttendanceFrontend
     * @param meetingID
     */
    public AttendanceFrontend(int meetingID, String committeeName) {
        this.meetingID = meetingID;
        this.committeeName = committeeName;
        initComponents();
        
        loadMeetingMembers(meetingID, committeeName);
        attendanceList.setModel(new DefaultListModel<>());

        loadSavedAttendance();
        setupListeners();
    }


    private void loadMeetingMembers(int meetingID, String committeeName) {
        currentMeetingID = meetingID;
        DefaultListModel<String> memberModel = new DefaultListModel<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT m.member_id, m.name, m.role " +
                "FROM member m " +
                "JOIN attends a ON m.member_id = a.member_id " +
                "JOIN belongs_to b ON m.member_id = b.member_id " +
                "JOIN meeting mt ON mt.meeting_id = a.meeting_id " +
                "WHERE a.meeting_id = ? AND mt.committee_name = ?")) {

            pstmt.setInt(1, meetingID);
            pstmt.setString(2, committeeName);  // Use committee name directly as a string

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int memberID = rs.getInt("member_id");
                String memberName = rs.getString("name");
                String memberRole = rs.getString("role");
                memberModel.addElement(memberID + " - " + memberName + " (" + memberRole + ")");
            }
            memberList.setModel(memberModel);
        } catch (SQLException e) {
        } catch (NumberFormatException e) {
            System.err.println("Invalid committee ID: ");
        }
    }

    
    private void setupListeners(){
        memberList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Detect double click
                    transferMember();
                }
            }
        });
    }

    private void transferMember() {
        DefaultListModel<String> memberModel = (DefaultListModel<String>) memberList.getModel();
        DefaultListModel<String> attendanceModel = (DefaultListModel<String>) attendanceList.getModel();

        int selectedIndex = memberList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedMember = memberModel.getElementAt(selectedIndex);

            // Prevent duplicate addition
            if (!attendanceModel.contains(selectedMember)) {
                attendanceModel.addElement(selectedMember); // Move to attendance list
                memberModel.remove(selectedIndex); // Remove from member list
            } else {
                JOptionPane.showMessageDialog(this, "Member is already in the attendance list!", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
            }
        }
    }




    private void saveAttendance() {
        DefaultListModel<String> attendanceModel = (DefaultListModel<String>) attendanceList.getModel();

        // If attendanceList is empty, confirm with the user
        if (attendanceModel.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                null,
                "Attendance list is empty. Are you sure you want to proceed? Click 'Yes' if you want to update blank.",
                "Confirm Attendance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return; // Cancel the operation if the user selects "No"
            }
        }

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            String checkDuplicateSQL = "SELECT COUNT(*) FROM present_attendance WHERE meeting_id = ? AND member_id = ?";
            String insertSQL = "INSERT INTO present_attendance (meeting_id, member_id) VALUES (?, ?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSQL);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

                for (int i = 0; i < attendanceModel.getSize(); i++) {
                    int memberID = Integer.parseInt(attendanceModel.getElementAt(i).split(" - ")[0]);

                    // Check for duplicate entry
                    checkStmt.setInt(1, currentMeetingID);
                    checkStmt.setInt(2, memberID);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Member ID " + memberID + " is already marked present!", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                        continue; // Skip duplicate entry
                    }

                    // Insert new attendance record
                    insertStmt.setInt(1, currentMeetingID);
                    insertStmt.setInt(2, memberID);
                    insertStmt.addBatch();
                }

                insertStmt.executeBatch(); // Execute batch insert
                conn.commit(); // Commit transaction

                // Reload attendance list after saving
                loadSavedAttendance();

            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save attendance.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadSavedAttendance() {
        DefaultListModel<String> attendanceModel = new DefaultListModel<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT m.member_id, m.name, m.role FROM present_attendance p " +
                 "JOIN member m ON p.member_id = m.member_id " +
                 "WHERE p.meeting_id = ?")) {

            stmt.setInt(1, currentMeetingID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int memberID = rs.getInt("member_id");
                String memberName = rs.getString("name");
                String memberRole = rs.getString("role");
                attendanceModel.addElement(memberID + " - " + memberName + " (" + memberRole + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        attendanceList.setModel(attendanceModel); // Set the attendance list model
    }



    private void clearAttendance() {
        // Confirm before clearing attendance
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear all attendance records for this meeting?",
            "Confirm Clear Attendance",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return; // Cancel operation if user selects "No"
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM present_attendance WHERE meeting_id = ?")) {

            stmt.setInt(1, currentMeetingID);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Attendance cleared successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No attendance records found to clear.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to clear attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear the UI attendance list
        ((DefaultListModel<String>) attendanceList.getModel()).clear();
    }




    
    
 

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
        memberScroll = new javax.swing.JScrollPane();
        memberList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        attendanceScroll = new javax.swing.JScrollPane();
        attendanceList = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();
        clearAttendanceButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 2, 18)); // NOI18N
        jLabel1.setText("Select Present Members");

        memberScroll.setViewportView(memberList);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel2.setText("Member List");

        attendanceScroll.setViewportView(attendanceList);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel3.setText("Attendance List");

        doneButton.setBackground(new java.awt.Color(204, 255, 204));
        doneButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        clearAttendanceButton.setText("Clear");
        clearAttendanceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAttendanceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(memberScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(100, 100, 100)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(clearAttendanceButton))
                                    .addComponent(attendanceScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(360, 360, 360)
                        .addComponent(doneButton)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(clearAttendanceButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(attendanceScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .addComponent(memberScroll))
                .addGap(16, 16, 16)
                .addComponent(doneButton)
                .addContainerGap(10, Short.MAX_VALUE))
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

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        saveAttendance();
        this.dispose();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void clearAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAttendanceButtonActionPerformed
        clearAttendance();
    }//GEN-LAST:event_clearAttendanceButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AttendanceFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AttendanceFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AttendanceFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AttendanceFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AttendanceFrontend(0,"").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> attendanceList;
    private javax.swing.JScrollPane attendanceScroll;
    private javax.swing.JButton clearAttendanceButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JList<String> memberList;
    private javax.swing.JScrollPane memberScroll;
    // End of variables declaration//GEN-END:variables
}

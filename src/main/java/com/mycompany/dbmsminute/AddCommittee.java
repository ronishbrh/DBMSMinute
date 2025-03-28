/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import java.awt.Color;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author Nitro
 */
public class AddCommittee extends javax.swing.JFrame {
    private String email;
    private GUIMain guiMain;
    /**
     * Creates new form SignUp
     * @param email
     * @throws java.sql.SQLException
     */
    public AddCommittee(GUIMain guiMain, String email) throws SQLException {
        this.email = email;
        this.guiMain = guiMain;
        initComponents();
        
        emailTextLabel.setText(email);
        
       
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
        addCommitteeButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        committeeNameTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        jLabel27 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        emailTextLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        addCommitteeButton.setBackground(new java.awt.Color(0, 153, 153));
        addCommitteeButton.setForeground(new java.awt.Color(255, 255, 255));
        addCommitteeButton.setText("ADD");
        addCommitteeButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addCommitteeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCommitteeButtonActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI Emoji", 0, 18)); // NOI18N
        jLabel9.setText("Committee Name");

        committeeNameTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Segoe UI Emoji", 0, 18)); // NOI18N
        jLabel10.setText("Your Email:");

        jScrollPane6.setMaximumSize(new java.awt.Dimension(1000, 800));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane6.setViewportView(descriptionTextArea);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel27.setText("Description");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel26.setText("Date of formation");

        emailTextLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        emailTextLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(153, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(committeeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emailTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel26)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addContainerGap(42, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(342, 342, 342)
                .addComponent(addCommitteeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel27)
                    .addComponent(emailTextLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(committeeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(97, 97, 97)
                .addComponent(addCommitteeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
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

    private void addCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCommitteeButtonActionPerformed
        // TODO add your handling code here:
        if (committeeNameTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Committee name is a required field!");
            
        }
       
        else{
        
            String committeeSQL = "INSERT INTO committee (name, description, date_of_creation) VALUES (?,?,?)";
            String getMemberIDSQL = "SELECT member_ID FROM member WHERE email = ?";
            String belongsToSQL = "INSERT INTO belongs_to (committee_id, member_id) VALUES (?,?)";
            
            Connection conn = null;
        
            try {
                conn = DBUtil.getConnection();
                conn.setAutoCommit(false);
                
                PreparedStatement pstmt = conn.prepareStatement(committeeSQL, Statement.RETURN_GENERATED_KEYS);
  
              
                pstmt.setString(1, committeeNameTextField.getText());
                pstmt.setString(2, descriptionTextArea.getText());
                java.util.Date utilDate = jDateChooser2.getDate();
                if (utilDate != null) {
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                    pstmt.setDate(3, sqlDate);
                } else {
                    pstmt.setNull(3, java.sql.Types.DATE);
                }

                int rowsInserted = pstmt.executeUpdate();

                // generated committee_id
                int committeeID = -1;
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    committeeID = generatedKeys.getInt(1);
                }

                if (rowsInserted > 0 && committeeID != -1) {
                    // member_ID from member table using email
                    PreparedStatement pstmt2 = conn.prepareStatement(getMemberIDSQL);
                    pstmt2.setString(1, email);
                    ResultSet rsMember = pstmt2.executeQuery();

                    int memberID = -1;
                    if (rsMember.next()) {
                        memberID = rsMember.getInt("member_ID"); 
                    }

                    if (memberID != -1) {
                        // Insert into belongs_to table
                        PreparedStatement pstmt3 = conn.prepareStatement(belongsToSQL);
                        pstmt3.setInt(1, committeeID);
                        pstmt3.setInt(2, memberID);
                        pstmt3.executeUpdate();
                    }

                    conn.commit(); 
                    JOptionPane.showMessageDialog(null, "Committee Added Successfully!");
                        
                    if (guiMain != null) {
                        guiMain.loadCommittees(email);
                    }
                        committeeNameTextField.setText("");
                        descriptionTextArea.setText("");
                        jDateChooser2.setDate(null);
                        
                }else {
                    JOptionPane.showMessageDialog(null, "Failed to add Committee!");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
               
            }
        }
    }//GEN-LAST:event_addCommitteeButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AddCommittee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddCommittee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddCommittee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddCommittee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new AddCommittee(null, "").setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(AddCommittee.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCommitteeButton;
    private javax.swing.JTextField committeeNameTextField;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel emailTextLabel;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane6;
    // End of variables declaration//GEN-END:variables
}

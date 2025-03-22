/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import com.google.protobuf.TextFormat.ParseException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nitro
 */
public class MemberFrontend extends javax.swing.JFrame {
    private String committeeName;
    /**
     * Creates new form MemberFrontend
     * @param committeeName
     */
    public MemberFrontend(String committeeName) {
        this.committeeName= committeeName;
        initComponents();
        committeeNameLabel.setText(committeeName);
        showLatestMemberID();
        loadMembersTable();
        searchListeners();
    }

    private void showLatestMemberID() {
        String getMemberIDquery = "SELECT member_ID FROM member ORDER BY member_ID DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getMemberIDquery);
             ResultSet MemberIDrs = pstmt.executeQuery()) {

            if (MemberIDrs.next()) {
                int memberID = MemberIDrs.getInt("member_ID"); 
                memberID++;
                memberIDLabel.setText(String.valueOf(memberID)); 
            } else {
                memberIDLabel.setText("1"); 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMembersTable() {
       

        if (committeeName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Committee name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String getCommitteeIDSQL = "SELECT committee_ID FROM committee WHERE name = ?";
        String memberTableFetchSQL = "SELECT m.member_ID, m.name, m.role, m.address, m.email, m.phone_no, m.date_of_join " +
                          "FROM member m " +
                          "JOIN belongs_to b ON m.member_ID = b.member_ID " +
                          "WHERE b.committee_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement getCommitteeIDStmt = conn.prepareStatement(getCommitteeIDSQL);
             PreparedStatement memberStmt = conn.prepareStatement(memberTableFetchSQL)) {

            // Get committee ID from committee name
            getCommitteeIDStmt.setString(1, committeeName);
            ResultSet rs1 = getCommitteeIDStmt.executeQuery();

            if (!rs1.next()) {
                JOptionPane.showMessageDialog(null, "Invalid committee name!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int committeeID = rs1.getInt("committee_ID");

            // Fetch members based on committee ID
            memberStmt.setInt(1, committeeID);
            ResultSet rs2 = memberStmt.executeQuery();

            DefaultTableModel model = (DefaultTableModel) memberTable.getModel();
            model.setRowCount(0); // Clear existing rows

            while (rs2.next()) {
                int memberID = rs2.getInt("member_ID");
                String name = rs2.getString("name");
                String role = rs2.getString("role");
                String address = rs2.getString("address");
                String email = rs2.getString("email");
                String phoneNo = rs2.getString("phone_no");
                java.sql.Date dateJoined = rs2.getDate("date_of_join");

                // Add row to table model
                model.addRow(new Object[]{memberID, name, role, address, email, phoneNo, dateJoined});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
 

        
        
        //-------------------------------------------------------------------------------------------------------------
        
        memberTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = memberTable.getSelectedRow();

                if (selectedRow != -1) { 
                    // Safely retrieve values from the table, handling null values
                    Object objMemberId = memberTable.getValueAt(selectedRow, 0);
                    Object objName = memberTable.getValueAt(selectedRow, 1);
                    Object objRole = memberTable.getValueAt(selectedRow, 2);
                    Object objAddress = memberTable.getValueAt(selectedRow, 3);
                    Object objEmail = memberTable.getValueAt(selectedRow, 4);
                    Object objPhoneNumber = memberTable.getValueAt(selectedRow, 5);
                    Object objJoinedDateStr = memberTable.getValueAt(selectedRow, 6);

                    String memberID = (objMemberId != null) ? objMemberId.toString() : "";
                    String name = (objName != null) ? objName.toString() : "";
                    String role = (objRole != null) ? objRole.toString() : "";
                    String address = (objAddress != null) ? objAddress.toString() : "";
                    String email = (objEmail != null) ? objEmail.toString() : "";
                    String phoneNumber = (objPhoneNumber != null) ? objPhoneNumber.toString() : "";
                    String joinedDateStr = (objJoinedDateStr != null) ? objJoinedDateStr.toString() : "";

                    memberIDLabel.setText(String.valueOf(memberID));
                    nameTextField.setText(name);
                    roleComboBox.setSelectedItem(role);
                    addressTextField.setText(address);
                    emailTextField.setText(email);
                    phoneNumberTextField.setText(phoneNumber);

                    // Convert string to Date and set in JDateChooser
                    if (!joinedDateStr.isEmpty()) { 
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date joinedDate = dateFormat.parse(joinedDateStr);
                            joinedDateChooser.setDate(joinedDate);
                        } catch (java.text.ParseException ex) {
                            Logger.getLogger(MemberFrontend.class.getName()).log(Level.SEVERE, null, ex);
                            joinedDateChooser.setDate(null); // Ensure it clears in case of parsing error
                        }
                    } else {
                        joinedDateChooser.setDate(null); // clear the date if no joinedDate is available
                    }
                }   
            }
        }
    });

    }
    
    private void searchMember() {
        String searchText = memberSearchTextField.getText().trim();
        String committeeID = committeeNameLabel.getText().trim(); 

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a search term.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        

        String searchSQL = "SELECT m.* FROM member m " +
                           "JOIN belongs_to b ON m.member_ID = b.member_ID " +
                           "WHERE b.committee_ID = ? " + 
                           "AND (m.name LIKE ? OR m.member_ID LIKE ? OR m.role LIKE ? OR m.address LIKE ? OR m.email LIKE ? OR m.phone_no LIKE ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String searchPattern = "%" + searchText + "%"; 

            pstmt.setString(1, committeeID); 
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            pstmt.setString(6, searchPattern);
            pstmt.setString(7, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel model = (DefaultTableModel) memberTable.getModel();
            model.setRowCount(0); 

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("member_ID"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("phone_no"),
                    rs.getDate("date_of_join")
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void searchListeners() {
        memberSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchMember();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadMembersTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchMember();
            }
        });
        
        memberSearchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                memberTable.clearSelection(); // Deselects any selected row
                memberTable.getColumnModel().getSelectionModel().clearSelection();
            }
        });

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
        jLabel2 = new javax.swing.JLabel();
        committeeNameLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        roleComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        addressTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        phoneNumberTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        joinedDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        memberTable = new javax.swing.JTable();
        memberSearchTextField = new javax.swing.JTextField();
        searchMemberButton = new javax.swing.JButton();
        addMemberButton = new javax.swing.JButton();
        updateMemberButton = new javax.swing.JButton();
        deleteMemberButton = new javax.swing.JButton();
        memberIDLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 153, 0));
        jLabel1.setText("MANAGE MEMBERS");

        jLabel2.setText("Members for Commitee ID:");

        committeeNameLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Name");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Role");

        roleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chairperson", "Vice-Chairperson", "Secretary", "Treasurer", "Advisor", "General Member", "Volunteer", "+Add Role" }));
        roleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roleComboBoxActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Address");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Email Address");

        phoneNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberTextFieldActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Phone Number");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Joined Date");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Member ID:");

        memberTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Member ID", "Name", "Role", "Address", "Email Address", "Phone Number", "Joined Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(memberTable);

        searchMemberButton.setText("Search");
        searchMemberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMemberButtonActionPerformed(evt);
            }
        });

        addMemberButton.setText("Add");
        addMemberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMemberButtonActionPerformed(evt);
            }
        });

        updateMemberButton.setText("Update");
        updateMemberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMemberButtonActionPerformed(evt);
            }
        });

        deleteMemberButton.setText("Delete");
        deleteMemberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMemberButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(addMemberButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(updateMemberButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(deleteMemberButton))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(memberIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(nameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(roleComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(emailTextField)
                            .addComponent(phoneNumberTextField)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7)
                                    .addComponent(joinedDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(memberSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                                .addComponent(searchMemberButton))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(committeeNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(committeeNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memberIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(emailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(roleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(phoneNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(joinedDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memberSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchMemberButton)
                    .addComponent(addMemberButton)
                    .addComponent(updateMemberButton)
                    .addComponent(deleteMemberButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void roleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roleComboBoxActionPerformed
        String selectedItem = (String) roleComboBox.getSelectedItem();
        if (selectedItem != null && selectedItem.equals("+Add Role")) {
        String newItem = JOptionPane.showInputDialog(this, "Enter new role:");
       
        if (newItem != null && !newItem.isEmpty()) {
            roleComboBox.addItem(newItem);
        }
        roleComboBox.removeItem("+Add Role");
        roleComboBox.addItem("+Add Role");
        
        roleComboBox.setSelectedItem(newItem);
    }
    }//GEN-LAST:event_roleComboBoxActionPerformed

    private void phoneNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phoneNumberTextFieldActionPerformed

    private void addMemberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMemberButtonActionPerformed
                                                                                    
        String getCommitteeIDSQL = "SELECT committee_ID FROM committee WHERE name = ?";
        String insertMemberSQL = "INSERT INTO member (name, role, address, email, phone_no, date_of_join) VALUES (?, ?, ?, ?, ?, ?)";
        String insertIntoBelongsToSQL = "INSERT INTO belongs_to (committee_id, member_id) VALUES (?,?)";
        String insertIntoAttendsSQL = "INSERT IGNORE INTO attends (meeting_id, member_id) " +
                                      "SELECT meeting_id, ? FROM meeting WHERE committee_name = ?";

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 

            String name = nameTextField.getText();
            String role = (String) roleComboBox.getSelectedItem(); 
            String address = addressTextField.getText();
            String email = emailTextField.getText();
            String phoneNo = phoneNumberTextField.getText();


            java.util.Date utilDate = joinedDateChooser.getDate();
            java.sql.Date sqlDate = utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;

            if (name.isEmpty() || email.isEmpty() || committeeName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Member's name, email, and committee name are required!");
                return;
            }

            int committeeID = -1;

            // Fetch committee_ID using committeeName
            try (PreparedStatement stmt = conn.prepareStatement(getCommitteeIDSQL)) {
                stmt.setString(1, committeeName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        committeeID = rs.getInt("committee_ID");
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid committee name!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // Insert into member table
            try (PreparedStatement pstmt = conn.prepareStatement(insertMemberSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, role);
                pstmt.setString(3, address);
                pstmt.setString(4, email);
                pstmt.setString(5, phoneNo);
                pstmt.setDate(6, sqlDate);

                int rowsInserted = pstmt.executeUpdate();

                if (rowsInserted > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int generatedMemberID = generatedKeys.getInt(1);

                            // Insert into belongs_to
                            try (PreparedStatement pstmt2 = conn.prepareStatement(insertIntoBelongsToSQL)) {
                                pstmt2.setInt(1, committeeID);
                                pstmt2.setInt(2, generatedMemberID);
                                pstmt2.executeUpdate();
                            }

                            // Insert into attends
                            try (PreparedStatement pstmt3 = conn.prepareStatement(insertIntoAttendsSQL)) {
                                pstmt3.setInt(1, generatedMemberID);
                                pstmt3.setString(2, committeeName);
                                pstmt3.executeUpdate();
                            }

                            conn.commit();

                            JOptionPane.showMessageDialog(null, "Member added successfully and assigned to all meetings!");

                            nameTextField.setText("");
                            roleComboBox.setSelectedIndex(0);
                            addressTextField.setText("");
                            emailTextField.setText("");
                            phoneNumberTextField.setText("");
                            joinedDateChooser.setDate(null);

                            showLatestMemberID();
                            loadMembersTable();
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to retrieve Member ID!", "Error", JOptionPane.ERROR_MESSAGE);
                            conn.rollback();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to add member!", "Error", JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }




    }//GEN-LAST:event_addMemberButtonActionPerformed

    private void updateMemberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMemberButtonActionPerformed
        
            try {
            int currentMemberID = Integer.parseInt(memberIDLabel.getText());  
            String memberName = nameTextField.getText();
            String role = (String) roleComboBox.getSelectedItem();
            String address = addressTextField.getText();
            String email = emailTextField.getText();
            String phone_no = phoneNumberTextField.getText();

            java.util.Date utilDate = joinedDateChooser.getDate();
            java.sql.Date sqlDate = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;

            if (memberName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Important fields are required for update!");
                return;
            }

            String updateMemberDetailsSQL = "UPDATE member SET name=?, role=?, address=?, email=?, phone_no=?, date_of_join=? WHERE member_ID=?";

            try (Connection conn = DBUtil.getConnection(); 
                 PreparedStatement pstmt = conn.prepareStatement(updateMemberDetailsSQL)) {

                pstmt.setString(1, memberName);
                pstmt.setString(2, role);
                pstmt.setString(3, address);
                pstmt.setString(4, email);
                pstmt.setString(5, phone_no);
                pstmt.setDate(6, sqlDate);
                pstmt.setInt(7, currentMemberID); 

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Member (ID: " + currentMemberID + ") details updated successfully!");
                    showLatestMemberID();
                    loadMembersTable();
                } else {
                    JOptionPane.showMessageDialog(null, "No changes were made.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Member ID. Please select a valid Member.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_updateMemberButtonActionPerformed

    private void deleteMemberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMemberButtonActionPerformed
        try {
            int currentMemberID = Integer.parseInt(memberIDLabel.getText());

            int confirm = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete Member ID: " + currentMemberID + "?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Check if member exists
                String checkMemberSQL = "SELECT COUNT(*) FROM member WHERE member_ID=?";
                String deleteMemberSQL = "DELETE FROM member WHERE member_ID=?";

                try (Connection conn = DBUtil.getConnection();
                     PreparedStatement checkStmt = conn.prepareStatement(checkMemberSQL);
                     PreparedStatement deleteStmt = conn.prepareStatement(deleteMemberSQL)) {

                    checkStmt.setInt(1, currentMemberID);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {  // member exists
                        deleteStmt.setInt(1, currentMemberID);
                        int rowsDeleted = deleteStmt.executeUpdate();

                        if (rowsDeleted > 0) {
                            JOptionPane.showMessageDialog(null, "Member (ID: " + currentMemberID + ") deleted!");
                            nameTextField.setText("");
                            roleComboBox.setSelectedIndex(0);
                            addressTextField.setText("");
                            emailTextField.setText("");
                            phoneNumberTextField.setText("");
                            joinedDateChooser.setDate(null);

                            showLatestMemberID();
                            loadMembersTable();
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Deletion failed. Please try again.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Member ID " + currentMemberID + " does not exist!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, 
                "Invalid Member ID. Please select a valid Member.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteMemberButtonActionPerformed

    private void searchMemberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMemberButtonActionPerformed
        searchMember();
    }//GEN-LAST:event_searchMemberButtonActionPerformed

    
    
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
            java.util.logging.Logger.getLogger(MemberFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MemberFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MemberFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MemberFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MemberFrontend("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMemberButton;
    private javax.swing.JTextField addressTextField;
    private javax.swing.JLabel committeeNameLabel;
    private javax.swing.JButton deleteMemberButton;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser joinedDateChooser;
    private javax.swing.JLabel memberIDLabel;
    private javax.swing.JTextField memberSearchTextField;
    private javax.swing.JTable memberTable;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField phoneNumberTextField;
    private javax.swing.JComboBox<String> roleComboBox;
    private javax.swing.JButton searchMemberButton;
    private javax.swing.JButton updateMemberButton;
    // End of variables declaration//GEN-END:variables
}

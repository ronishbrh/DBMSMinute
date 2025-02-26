/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import java.awt.Desktop;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nitro
 */
public class AgendaFrontend extends javax.swing.JFrame {
    private String committeeName;
    /**
     * Creates new form AgendaFrontend
     * @param committeeName
     */
    public AgendaFrontend(String committeeName) {
        this.committeeName = committeeName;
        initComponents();
        loadMeetings(committeeName); 
        syncMeetingSelection();
        loadAgendaForMeetingLabel();
        showLatestAgendaID();
        loadAgendasTable();
        
        setupAgendaListeners();
        
        
        
        setupDocumentListeners();
    }
    
    private void loadAgendaForMeetingLabel(){
        String meetingSpecified = (String)meetingTitleComboBox.getSelectedItem();
        agendaForMeetingLabel.setText(meetingSpecified);
    }
    
    
    
    private void showLatestAgendaID() {
        String getAgendaIDquery = "SELECT agenda_id FROM agenda ORDER BY agenda_id DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getAgendaIDquery);
             ResultSet AgendaIDrs = pstmt.executeQuery()) {

            if (AgendaIDrs.next()) {
                int agendaID = AgendaIDrs.getInt("agenda_ID"); 
                agendaID++;
                agendaIDLabel.setText(String.valueOf(agendaID)); 
            } else {
                agendaIDLabel.setText("1"); 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupAgendaListeners(){
        agendasTable.getSelectionModel().addListSelectionListener(event -> {
        if (!event.getValueIsAdjusting()) { 
            int selectedRow = agendasTable.getSelectedRow();
            if (selectedRow != -1) { 
                        Object objAgendaId = agendasTable.getValueAt(selectedRow, 0);
                        Object objAgendaTopic = agendasTable.getValueAt(selectedRow, 1);
                        Object objAgendaTimeSlot = agendasTable.getValueAt(selectedRow, 2);
                        Object objAgendaPresenter = agendasTable.getValueAt(selectedRow, 3);

                        String agendaID = (objAgendaId!= null) ? objAgendaId.toString() : "";
                        String agendaTopic = (objAgendaTopic != null) ? objAgendaTopic.toString() : "";
                        String agendaTimeSlot = (objAgendaTimeSlot != null) ? objAgendaTimeSlot.toString() : "";
                        String agendaPresenter = (objAgendaPresenter!= null) ? objAgendaPresenter.toString() : "";
                      

                        agendaIDLabel.setText(String.valueOf(agendaID));
                        agendaTopicTextField.setText(agendaTopic);
                        timeSlotTextField.setText(agendaTimeSlot);
                        presenterTextField.setText(agendaPresenter);
                        
                        loadDocumentsTable();
                        
                addDocumentButton.setEnabled(true); // Enable button
                
                
            } else {
                addDocumentButton.setEnabled(false); // Disable if no row is selected
            }
        }
    });
        

    }
    
    private void loadAgendasTable() {
        int meetingID =Integer.parseInt(meetingIDComboBox.getSelectedItem().toString()) ;

        String agendaTableFetchSQL = "SELECT agenda_id, topic, time_slot, presenter " +
                                      "FROM agenda WHERE meeting_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(agendaTableFetchSQL)) {

            pstmt.setInt(1, meetingID);

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) agendasTable.getModel();
                model.setRowCount(0); // Clear existing rows

                while (rs.next()) {
                    int agendaID = rs.getInt("agenda_id");
                    String agendaTopic = rs.getString("topic"); // Now fetched correctly
                    String timeSlot = rs.getString("time_slot");
                    String agendaPresenter = rs.getString("presenter");

                    model.addRow(new Object[]{agendaID, agendaTopic, timeSlot, agendaPresenter});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void syncMeetingSelection() {
        meetingIDComboBox.addActionListener(e -> {
            if (meetingIDComboBox.getSelectedItem() != null) {
                
                String selectedMeetingID = meetingIDComboBox.getSelectedItem().toString();
                HashMap<Integer, String> meetingsMap = (HashMap<Integer, String>) meetingIDComboBox.getClientProperty("meetingsMap");

                if (meetingsMap != null) {
                    meetingTitleComboBox.setSelectedItem(meetingsMap.get(Integer.valueOf(selectedMeetingID)));
                    loadAgendaForMeetingLabel();
                    loadAgendasTable();
                }
            }
        });

        meetingTitleComboBox.addActionListener(e -> {
            if (meetingTitleComboBox.getSelectedItem() != null) {
                String selectedMeetingTitle = meetingTitleComboBox.getSelectedItem().toString();
                HashMap<Integer, String> meetingsMap = (HashMap<Integer, String>) meetingTitleComboBox.getClientProperty("meetingsMap");

                if (meetingsMap != null) {
                    for (Map.Entry<Integer, String> entry : meetingsMap.entrySet()) {
                        if (entry.getValue().equals(selectedMeetingTitle)) {
                            meetingIDComboBox.setSelectedItem(String.valueOf(entry.getKey()));
                            loadAgendaForMeetingLabel();
                            loadAgendasTable();
                            break;
                        }
                    }
                }
            }
        });
    }


    
    private void loadMeetings(String committeeName) {
        meetingIDComboBox.removeAllItems();  // Clear previous items
        meetingTitleComboBox.removeAllItems();


        if (committeeName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a valid committee!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fetchMeetingsSQL = "SELECT meeting_id, title FROM meeting WHERE committee_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(fetchMeetingsSQL)) {

            pstmt.setString(1, committeeName);
            ResultSet rs = pstmt.executeQuery();

            HashMap<Integer, String> meetingsMap = new HashMap<>(); //store ID-Title mapping

            while (rs.next()) {
                int meetingID = rs.getInt("meeting_id");
                String meetingTitle = rs.getString("title");

                meetingsMap.put(meetingID, meetingTitle);
                meetingIDComboBox.addItem(String.valueOf(meetingID)); 
                meetingTitleComboBox.addItem(meetingTitle); 
            }

            // Store the mapping in the combo box for easy access
            meetingIDComboBox.putClientProperty("meetingsMap", meetingsMap);
            meetingTitleComboBox.putClientProperty("meetingsMap", meetingsMap);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    //=========================for DOCUMENT========================================================================

    
    private boolean storeFileInDatabase(int agendaID, String fileName, byte[] fileData) {
        String insertSQL = "INSERT INTO document (agenda_id, file_name, file_data) VALUES (?, ?, ?)"; // No 'type'

            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

                pstmt.setInt(1, agendaID);
                pstmt.setString(2, fileName);
                pstmt.setBytes(3, fileData);

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected>0;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
    }

    
    private void loadDocumentsTable() {
        int agendaID = Integer.parseInt(agendaIDLabel.getText());

        String documentTableFetchSQL = "SELECT document_id, file_name FROM document WHERE agenda_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(documentTableFetchSQL)) {

            pstmt.setInt(1, agendaID);

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) documentsTable.getModel();
                model.setRowCount(0); // Clear existing rows

                while (rs.next()) {
                    int documentID = rs.getInt("document_id");
                    String fileName = rs.getString("file_name"); // Get file name instead of BLOB

                    model.addRow(new Object[]{documentID, fileName});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openDocument(int documentID) {
        String query = "SELECT file_name, file_data FROM document WHERE document_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, documentID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("file_name");
                    byte[] fileData = rs.getBytes("file_data");

                    // Save the file to a temporary location
                    File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileData);
                    }

                    // Open the file using the default application
                    Desktop.getDesktop().open(tempFile);
                } else {
                    JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(null, "Error opening document: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void setupDocumentListeners() {
        // Listen for Enter (open document) and Delete (delete document)
        documentsTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedRow = documentsTable.getSelectedRow();

                if (selectedRow != -1) {  // Ensure a row is selected
                    Object value = documentsTable.getValueAt(selectedRow, 0);

                    if (value != null) {
                        int documentID = (int) value;

                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            System.out.println("Opening Document with ID: " + documentID);
                            openDocument(documentID);
                        } 
                        else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                            int confirm = JOptionPane.showConfirmDialog(null, 
                                "Are you sure you want to delete this document?", 
                                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                            if (confirm == JOptionPane.YES_OPTION) {
                                deleteDocument(documentID);
                            }
                        }
                    } else {
                        System.out.println("No valid document ID found in selected row.");
                    }
                } else {
                    System.out.println("No row selected!");
                }
            }
        });
    }

    
    private void deleteDocument(int documentID) {
        String deleteSQL = "DELETE FROM document WHERE document_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, documentID);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "Document deleted successfully.");
                loadDocumentsTable();  // Refresh table after deletion
                GUIMain.getInstance().loadOuterAgendasTable();
            } else {
                JOptionPane.showMessageDialog(null, "Error: Document not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error deleting document: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    
//=====================================EXIT OF DOCUMENT===========================================================

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
        jLabel3 = new javax.swing.JLabel();
        meetingIDComboBox = new javax.swing.JComboBox<>();
        meetingTitleComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        agendaTopicTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        timeSlotTextField = new javax.swing.JTextField();
        addDocumentButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        insertAgendaButton = new javax.swing.JButton();
        updateAgendaButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        agendasTable = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        agendaForMeetingLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        documentsTable = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        presenterTextField = new javax.swing.JTextField();
        agendaIDLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 153));
        jLabel1.setText("MANAGE AGENDA");

        jLabel2.setText("Meeting Title:");

        jLabel3.setText("Meeting ID:");

        meetingIDComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        meetingTitleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Agenda ID");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Agenda Topic");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Time slot");

        timeSlotTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeSlotTextFieldActionPerformed(evt);
            }
        });

        addDocumentButton.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        addDocumentButton.setText("'+'");
        addDocumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDocumentButtonActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Add Related Documents:");

        insertAgendaButton.setBackground(new java.awt.Color(0, 255, 204));
        insertAgendaButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        insertAgendaButton.setText("INSERT");
        insertAgendaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertAgendaButtonActionPerformed(evt);
            }
        });

        updateAgendaButton.setBackground(new java.awt.Color(51, 153, 255));
        updateAgendaButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateAgendaButton.setText("UPDATE");
        updateAgendaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAgendaButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 0, 153)));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        agendasTable.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 0, 153)));
        agendasTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Agenda ID", "Agenda Topic", "Time slot", "Presenter"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(agendasTable);

        jLabel8.setText("Agendas for the meeting:");

        agendaForMeetingLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 0, 153)));

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        documentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DocumentID", "Document"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(documentsTable);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Presenter:");

        agendaIDLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(meetingIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(meetingTitleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(agendaTopicTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                        .addComponent(agendaIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(70, 70, 70)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                        .addComponent(timeSlotTextField))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addDocumentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(insertAgendaButton)
                                .addGap(18, 18, 18)
                                .addComponent(updateAgendaButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(agendaForMeetingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(presenterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(159, 159, 159)
                                .addComponent(jLabel1)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(presenterTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(meetingIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(agendaForMeetingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(meetingTitleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(timeSlotTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                            .addComponent(agendaIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(agendaTopicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addDocumentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertAgendaButton)
                    .addComponent(updateAgendaButton))
                .addContainerGap(13, Short.MAX_VALUE))
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

    private void timeSlotTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeSlotTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_timeSlotTextFieldActionPerformed

    private void addDocumentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDocumentButtonActionPerformed
        if (agendasTable.getSelectedRow() == -1) { 
            JOptionPane.showMessageDialog(this, "Please select an agenda from the Agenda Table first!", "Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        int agendaID = (int) agendasTable.getValueAt(agendasTable.getSelectedRow(), 0); 


        JFileChooser fileChooser = new JFileChooser(); 
        int returnValue = fileChooser.showOpenDialog(null); 

        if (returnValue == JFileChooser.APPROVE_OPTION) { 
            File selectedFile = fileChooser.getSelectedFile();

            // Read file content
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] fileData = new byte[(int) selectedFile.length()];
                fis.read(fileData);

                if(storeFileInDatabase(agendaID, selectedFile.getName(), fileData)){
                    JOptionPane.showMessageDialog(this, "File uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadDocumentsTable();
                    GUIMain.getInstance().loadOuterAgendasTable();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        
    }//GEN-LAST:event_addDocumentButtonActionPerformed

    private void insertAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertAgendaButtonActionPerformed
        String insertAgendaSQL = "INSERT INTO agenda (topic,meeting_id, time_slot, presenter) VALUES (?, ?, ?, ?)";


        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmtAgenda = conn.prepareStatement(insertAgendaSQL, Statement.RETURN_GENERATED_KEYS)) {

        int meetingID = Integer.parseInt(meetingIDComboBox.getSelectedItem().toString());

            String topic = agendaTopicTextField.getText();
            String timeSlot = timeSlotTextField.getText();
            String presenter = presenterTextField.getText();
            
            pstmtAgenda.setString(1, topic);
            pstmtAgenda.setInt(2, meetingID);
            pstmtAgenda.setString(3, timeSlot);
            pstmtAgenda.setString(4, presenter);
 

            int rowsInserted = pstmtAgenda.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmtAgenda.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int agendaID = generatedKeys.getInt(1);

                        JOptionPane.showMessageDialog(null, "Agenda (ID: " + agendaID + ") inserted successfully!");
                        GUIMain.getInstance().loadMeetingAgendaList();
               
                        agendaTopicTextField.setText("");
                        timeSlotTextField.setText("");
                        presenterTextField.setText("");
                        
                        showLatestAgendaID();
                        loadAgendasTable();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to retrieve Agenda ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add agenda details!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_insertAgendaButtonActionPerformed

    private void updateAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAgendaButtonActionPerformed
       try {
            int agendaID = Integer.parseInt(agendaIDLabel.getText());  
            String agendaTopic = agendaTopicTextField.getText();
            String agendaTimeSlot = timeSlotTextField.getText();
            String presenter = presenterTextField.getText();



            if (agendaTopic.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Atleast Agenda Topic is a required field!");
                return;
            }

            String updateAgendaDetailsSQL = "UPDATE agenda SET topic=?, time_slot=?, presenter=? WHERE agenda_id=?";

            try (Connection conn = DBUtil.getConnection(); 
                 PreparedStatement pstmt = conn.prepareStatement(updateAgendaDetailsSQL)) {

                pstmt.setString(1, agendaTopic);
                pstmt.setString(2, agendaTimeSlot);
                pstmt.setString(3, presenter);
                pstmt.setInt(4, agendaID);
               

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Agenda (ID: " + agendaID + ") details updated successfully!");
                    showLatestAgendaID();
                    loadAgendasTable();
                    GUIMain.getInstance().loadMeetingAgendaList();
                } else {
                    JOptionPane.showMessageDialog(null, "No changes were made.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Agenda ID. Please select a valid Agenda.");
        } catch (SQLException e){
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_updateAgendaButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AgendaFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AgendaFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AgendaFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AgendaFrontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AgendaFrontend("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDocumentButton;
    private javax.swing.JLabel agendaForMeetingLabel;
    private javax.swing.JLabel agendaIDLabel;
    private javax.swing.JTextField agendaTopicTextField;
    private javax.swing.JTable agendasTable;
    private javax.swing.JTable documentsTable;
    private javax.swing.JButton insertAgendaButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox<String> meetingIDComboBox;
    private javax.swing.JComboBox<String> meetingTitleComboBox;
    private javax.swing.JTextField presenterTextField;
    private javax.swing.JTextField timeSlotTextField;
    private javax.swing.JButton updateAgendaButton;
    // End of variables declaration//GEN-END:variables
}

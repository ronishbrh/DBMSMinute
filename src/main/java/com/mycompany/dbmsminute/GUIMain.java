/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.dbmsminute;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nitro
 */
public class GUIMain extends javax.swing.JFrame {
    private String email;
    private static GUIMain instance;
    /**
     * Creates new form GUIMain
     * @param email
     */
    public GUIMain(String email) {
        this.email = email;
        
        initComponents();
        
        showLatestMeetingID();
        

        loadCommittees(email);
        
        committeeComboBoxListeners();
        showCommitteeName();
        
        loadMeetingsTable();
       
        
        agendaTableSearchListeners();
        setupListeners();
        searchListeners();
        
        
        loadMeetings();
        setupMeetingTitleComboBoxListener();
        setupMeetingIDComboBoxListener();

        
        loadMeetingAgendaList();
        
        MeetingComboBoxListeners();
        
     
        minuteTableSearchListeners();
        
        
        setupMinutesListeners();
        
        instance = this;
       this.setExtendedState(JFrame.MAXIMIZED_BOTH);
       
       this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            confirmLogoutAndClose();
        }
    });


       
        
    }
    
    

    
    public static GUIMain getInstance(){
        if(instance == null)
            throw new IllegalStateException("GUIMain has not been initialized!");
        return instance;
    }
    
    private void confirmLogoutAndClose() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Closing the application will log you out.\nDo you want to proceed?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Close the application
            this.dispose();
            System.exit(0); // Ensures the program terminates completely
        }
    }

    
    
    
    //===================================START OF MINUTE MEETINGS=====================================================
    
    private void setupMeetingTitleComboBoxListener() {
       meetingTitleComboBox.addActionListener(e -> {
           // Get the selected title from the combo box
           Object selectedItem = meetingTitleComboBox.getSelectedItem();
           if (selectedItem == null) {
               JOptionPane.showMessageDialog(this, "No meeting title selected!", "Error", JOptionPane.ERROR_MESSAGE);
               return; //get out of listener if no item is selected
           }

           String selectedTitle = selectedItem.toString();

           // Retrieve the meetingsMap from the combo box
           HashMap<Integer, String> meetingsMap = (HashMap<Integer, String>) meetingTitleComboBox.getClientProperty("meetingsMap");
           if (meetingsMap == null) {
               JOptionPane.showMessageDialog(this, "Meetings map is not available!", "Error", JOptionPane.ERROR_MESSAGE);
               return; // out istener if meetingsMap is null
           }

           // Find the meeting ID corresponding to the selected title
           for (Map.Entry<Integer, String> entry : meetingsMap.entrySet()) {
               if (entry.getValue().equals(selectedTitle)) {
                   // Update the meetingIDComboBox with the corresponding meeting ID
                   meetingIDComboBox.setSelectedItem(String.valueOf(entry.getKey()));
                   return; //exitloop once the matching entry is found
               }
           }


           JOptionPane.showMessageDialog(this, "No matching meeting ID found for the selected title.", "Error", JOptionPane.ERROR_MESSAGE);
       });
       
      
   }
    
    private void setupMeetingIDComboBoxListener(){
       meetingIDComboBox.addActionListener(e -> {
           Object selectedItem = meetingIDComboBox.getSelectedItem();
           if (selectedItem == null) {
               JOptionPane.showMessageDialog(this, "No meeting ID selected!", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }

           String selectedID = selectedItem.toString();

           HashMap<Integer, String> meetingsMap = (HashMap<Integer, String>) meetingIDComboBox.getClientProperty("meetingsMap");
           if (meetingsMap == null) {
               JOptionPane.showMessageDialog(this, "Meetings map is not available!", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }

           try {
               int meetingID = Integer.parseInt(selectedID); 
               String meetingTitle = meetingsMap.get(meetingID);  

               if (meetingTitle != null) {
                   meetingTitleComboBox.setSelectedItem(meetingTitle);  
               } else {
                   JOptionPane.showMessageDialog(this, "No matching meeting title found.", "Error", JOptionPane.ERROR_MESSAGE);
               }
           } catch (NumberFormatException ex) {
               JOptionPane.showMessageDialog(this, "Invalid meeting ID format.", "Error", JOptionPane.ERROR_MESSAGE);
           }
       });
   }

    
    private void setupMinutesListeners() {
        ListSelectionModel selectionModel = minutesTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        selectionModel.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) { // Prevents multiple triggers
                int selectedRow = minutesTable.getSelectedRow();

                if (selectedRow == -1) {
                    clearMinuteDetails(); // Clears fields if nothing is selected
                    return;
                }

                // Get data from the selected row
                Object objMeetingTitle = minutesTable.getValueAt(selectedRow, 1);
                Object objContent = minutesTable.getValueAt(selectedRow, 2);
                Object objCreatedBy = minutesTable.getValueAt(selectedRow, 3);
                Object objCreationTime = minutesTable.getValueAt(selectedRow, 4);
                Object objApprovedBy = minutesTable.getValueAt(selectedRow, 6);

                // Convert data to strings (handle null values)
                String meetingTitle = (objMeetingTitle != null) ? objMeetingTitle.toString() : "";
                String content = (objContent != null) ? objContent.toString() : "";
                String createdBy = (objCreatedBy != null) ? objCreatedBy.toString() : "";
                String approvedBy = (objApprovedBy != null) ? objApprovedBy.toString() : "";

                // Convert creationTime to java.util.Date
                java.util.Date creationDate = null;
                if (objCreationTime != null) {
                    try {
                        java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(objCreationTime.toString());
                        creationDate = new java.util.Date(timestamp.getTime());
                    } catch (IllegalArgumentException e) {
                        creationDate = new java.util.Date(); // Default to current date if parsing fails
                    }
                } else {
                    creationDate = new java.util.Date();
                }

                meetingTitleComboBox.setSelectedItem(meetingTitle);
                contentTextArea.setText(content);
                createdByTextField.setText(createdBy);
                createdTimeSpinner.setValue(creationDate); // Set date & time in spinner
                approvedByTextField.setText(approvedBy);
            }
        });
    }

    
    private void MeetingComboBoxListeners() {
        meetingIDComboBox.addActionListener(e -> {
            if (meetingIDComboBox.getSelectedItem() != null) {
                loadMeetingAgendaList();
            }
        });
    }

    private void loadMeetings() {
        String committeeName = committeeNameLabel.getText();

    
        if (committeeName == null || committeeName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a valid committee!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        meetingIDComboBox.removeAllItems();
        meetingTitleComboBox.removeAllItems();

        String fetchMeetingsSQL = "SELECT meeting_id, title FROM meeting WHERE committee_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(fetchMeetingsSQL)) {

            pstmt.setString(1, committeeName);
            ResultSet rs = pstmt.executeQuery();

            HashMap<Integer, String> meetingsMap = new HashMap<>(); // Store ID-Title mapping

   
            while (rs.next()) {
                int meetingID = rs.getInt("meeting_id");
                String meetingTitle = rs.getString("title");

                meetingsMap.put(meetingID, meetingTitle);
                meetingIDComboBox.addItem(String.valueOf(meetingID));
                meetingTitleComboBox.addItem(meetingTitle);
            }

            // Debugging: Print combo box state
            System.out.println("Combo box item count: " + meetingIDComboBox.getItemCount());
            System.out.println("Selected item: " + meetingIDComboBox.getSelectedItem());

        
            if (meetingIDComboBox.getItemCount() > 0) {
                meetingIDComboBox.setSelectedIndex(0);  
                loadMinuteTable(Integer.parseInt(meetingIDComboBox.getSelectedItem().toString()));  
            } else {
                JOptionPane.showMessageDialog(null, "No meetings found for this committee.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        
            meetingIDComboBox.putClientProperty("meetingsMap", meetingsMap);
            meetingTitleComboBox.putClientProperty("meetingsMap", meetingsMap);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    


    
    
 //========================================END OF MINUTE MEETING==============================================
    



 //=========================================START OF MINUTE================================================
    
    
    
    
    private void minuteTableSearchListeners() {
        minuteSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchMinutes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                meetingIDComboBox.setSelectedIndex(0);
                loadMinuteTable(Integer.parseInt(meetingIDComboBox.getSelectedItem().toString()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchMinutes();
            }
        });
        
        minuteSearchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                minutesTable.clearSelection(); // Deselects any selected row
                minutesTable.getColumnModel().getSelectionModel().clearSelection();
                meetingIDComboBox.setSelectedIndex(0);
            }
        });

    }

    
    private void clearMinuteDetails() {
        meetingIDComboBox.setSelectedIndex(-1);
        contentTextArea.setText("");
        createdByTextField.setText("");
        createdTimeSpinner.setValue(new Time(System.currentTimeMillis())); // Reset to current time
        approvedByTextField.setText("");
        
    }
    
    private void searchMinutes() {
        String searchText = minuteSearchTextField.getText().trim();

        String searchSQL = "SELECT m.minute_id, mt.title AS meeting_title, m.content, m.created_by, " +
                           "m.creation_time, m.updated_time, m.approved_by " +
                           "FROM minute m " +
                           "JOIN meeting mt ON m.meeting_id = mt.meeting_id " +
                           "WHERE m.content LIKE ? OR m.created_by LIKE ? OR m.approved_by LIKE ? " +
                           "OR mt.title LIKE ? OR m.creation_time LIKE ? OR m.updated_time LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, searchPattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) minutesTable.getModel();
                model.setRowCount(0); // Clear table before inserting new rows

                while (rs.next()) {
                    int minuteID = rs.getInt("minute_id");
                    String meetingTitle = rs.getString("meeting_title");
                    String content = rs.getString("content");
                    String createdBy = rs.getString("created_by");
                    Time creationTime = rs.getTime("creation_time");
                    Time updatedTime = rs.getTime("updated_time");
                    String approvedBy = rs.getString("approved_by");

                    model.addRow(new Object[]{minuteID, meetingTitle, content, createdBy, creationTime, updatedTime, approvedBy});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    
    public void loadMeetingAgendaList() {
        Object selectedItem = meetingIDComboBox.getSelectedItem();
        if (selectedItem == null) {
            return;  // Avoid error when the combo box is empty or being reset
        }

        int meetingID = Integer.parseInt(selectedItem.toString());
        DefaultListModel<String> agendaModel = new DefaultListModel<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT topic FROM agenda WHERE meeting_id = ?")) {

            pstmt.setInt(1, meetingID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String agendaTopic = rs.getString("topic");
                agendaModel.addElement(agendaTopic);
            }

            meetingAgendaList.setModel(agendaModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMinuteTable(int meetingID) {
        String query = "SELECT m.minute_id, mt.title AS meeting_title, m.content, m.created_by, " +
                       "m.creation_time, m.updated_time, m.approved_by " +
                       "FROM minute m " +
                       "JOIN meeting mt ON m.meeting_id = mt.meeting_id " +
                       "WHERE m.meeting_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, meetingID);
            ResultSet rs = pstmt.executeQuery();

            // Get table model and clear existing data
            DefaultTableModel model = (DefaultTableModel) minutesTable.getModel();
            model.setRowCount(0); // Clear table before inserting new rows

            // Populate table with data from result set
            while (rs.next()) {
                int minuteID = rs.getInt("minute_id");
                String meetingTitle = rs.getString("meeting_title");
                String content = rs.getString("content");
                String createdBy = rs.getString("created_by");

                // Use Timestamp for DATETIME fields
                Timestamp creationTime = rs.getTimestamp("creation_time");
                Timestamp updatedTime = rs.getTimestamp("updated_time");
                String approvedBy = rs.getString("approved_by");

                // Convert to String and handle null values
                String creationTimeStr = (creationTime != null) ? creationTime.toString() : "";
                String updatedTimeStr = (updatedTime != null) ? updatedTime.toString() : "";

                model.addRow(new Object[]{minuteID, meetingTitle, content, createdBy, creationTimeStr, updatedTimeStr, approvedBy});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading minutes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    
//==================================END OF MINUTES=========================================================
   
    
    
//======================================PRINT MINUTE================================================================
    private void printMinute(int minuteId) {
       StringBuilder minuteDetails = new StringBuilder();
       int meetingID = -1;  // Initialize meetingID

       try (Connection conn = DBUtil.getConnection()) {

           // Retrieve Minute Details
           String minuteQuery = "SELECT m.content, m.creation_time, m.created_by, m.approved_by, " +
                                "meet.meeting_id, meet.title, meet.meeting_date, meet.meeting_time, meet.location, meet.committee_name " +
                                "FROM minute m " +
                                "JOIN meeting meet ON m.meeting_id = meet.meeting_id " +
                                "WHERE m.minute_id = ?";
           try (PreparedStatement pstmt = conn.prepareStatement(minuteQuery)) {
               pstmt.setInt(1, minuteId);
               ResultSet rs = pstmt.executeQuery();

               if (rs.next()) {
                   meetingID = rs.getInt("meeting_id");  // Set correct meeting ID

                   minuteDetails.append("** ").append(rs.getString("committee_name")).append(" **\n\n")
                                .append("Minute Creation Time: ").append(rs.getTimestamp("creation_time")).append("\n\n")
                                .append("Meeting Name: ").append(rs.getString("title")).append("\n")
                                .append("Meeting Date: ").append(rs.getDate("meeting_date")).append("\n")
                                .append("Meeting Time: ").append(rs.getTime("meeting_time")).append("\n")
                                .append("Meeting Location: ").append(rs.getString("location")).append("\n\n")
                                .append("Content:\n").append(rs.getString("content")).append("\n\n");
               }
           }

           if (meetingID == -1) {
               JOptionPane.showMessageDialog(null, "Invalid minute ID. No meeting found.");
               return;
           }

           // Retrieve Attendance List
           minuteDetails.append("Attendance List:\n");
           String attendanceQuery = "SELECT m.name, m.role FROM present_attendance p " +
                                    "JOIN member m ON p.member_id = m.member_id " +
                                    "WHERE p.meeting_id = ?";
           try (PreparedStatement pstmt = conn.prepareStatement(attendanceQuery)) {
               pstmt.setInt(1, meetingID);
               ResultSet rs = pstmt.executeQuery();
               boolean hasAttendance = false;
               while (rs.next()) {
                   hasAttendance = true;
                   minuteDetails.append("- ").append(rs.getString("name"))
                                .append(" (").append(rs.getString("role")).append(")\n");
               }
               if (!hasAttendance) {
                   minuteDetails.append("- No attendees recorded.\n");
               }
           }

           // Retrieve Absent List
           minuteDetails.append("\nAbsent List:\n");
           String absentQuery = "SELECT m.name FROM attends a " +
                                "JOIN member m ON a.member_id = m.member_id " +
                                "LEFT JOIN present_attendance p ON a.member_id = p.member_id AND a.meeting_id = p.meeting_id " +
                                "WHERE a.meeting_id = ? AND p.member_id IS NULL";
           try (PreparedStatement pstmt = conn.prepareStatement(absentQuery)) {
               pstmt.setInt(1, meetingID);
               ResultSet rs = pstmt.executeQuery();
               boolean hasAbsent = false;
               while (rs.next()) {
                   hasAbsent = true;
                   minuteDetails.append("- ").append(rs.getString("name")).append("\n");
               }
               if (!hasAbsent) {
                   minuteDetails.append("- No absentees recorded.\n");
               }
           }

           // Retrieve Agenda, Discussions, and Follow-ups
           minuteDetails.append("\nAgenda Items:\n");
           String agendaQuery = "SELECT a.agenda_id, a.topic, " +
                                "COALESCE(d.description, 'N/A') AS discussion, " +
                                "COALESCE(f.description, 'N/A') AS follow_up, " +
                                "COALESCE(f.status, 'Pending') AS status " +
                                "FROM agenda a " +
                                "LEFT JOIN discussion d ON a.agenda_id = d.agenda_id " +
                                "LEFT JOIN follow_up f ON d.discussion_id = f.discussion_id " +
                                "WHERE a.meeting_id = ?";
           try (PreparedStatement pstmt = conn.prepareStatement(agendaQuery)) {
               pstmt.setInt(1, meetingID);
               ResultSet rs = pstmt.executeQuery();

               int count = 1;
               boolean hasAgenda = false;
               while (rs.next()) {
                   hasAgenda = true;
                   minuteDetails.append("\n").append(count).append(". ").append(rs.getString("topic")).append("\n")
                                .append("   Discussion: ").append(rs.getString("discussion")).append("\n")
                                .append("   Follow-up: ").append(rs.getString("follow_up"))
                                .append(" (").append(rs.getString("status")).append(")\n");
                   count++;
               }
               if (!hasAgenda) {
                   minuteDetails.append("- No agenda items recorded.\n");
               }
           }

           // Append Created By & Approved By
           minuteDetails.append("\nCreated By: ").append(getCreatedBy(minuteId, conn)).append("\n");
           minuteDetails.append("Approved By: ").append(getApprovedBy(minuteId, conn)).append("\n");

       } catch (SQLException ex) {
           ex.printStackTrace();
           JOptionPane.showMessageDialog(null, "Error retrieving minute details.");
       }

       // Print Preview
       showPrintPreview(minuteDetails.toString());
   }

    
    private String getCreatedBy(int minuteId, Connection conn) throws SQLException {
        String query = "SELECT created_by FROM minute WHERE minute_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, minuteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("created_by");
        }
        return "Unknown";
    }

    private String getApprovedBy(int minuteId, Connection conn) throws SQLException {
        String query = "SELECT approved_by FROM minute WHERE minute_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, minuteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("approved_by");
        }
        return "Unknown";
    }
    
    private void showPrintPreview(String content) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/plain");  // Ensure plain text
        textPane.setText(content);
        textPane.setEditable(false);

        // Set proper font and ensure word wrap
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Print logic
        try {
            boolean complete = textPane.print();
            if (complete) {
                JOptionPane.showMessageDialog(null, "Printing successful!");
            } else {
                JOptionPane.showMessageDialog(null, "Printing cancelled.");
            }
        } catch (PrinterException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Printing failed: " + e.getMessage());
        }
    }

    


//============================================END OF PRINT==============================================================================
    
   
    
    //===============================================START OF AGENDA TABLE======================================
    
    private void searchAgendas() {
        String searchText = searchAgendaTextField.getText().trim();

        String searchSQL = "SELECT agenda_id, topic, time_slot, presenter FROM agenda " +
                           "WHERE topic LIKE ? OR presenter LIKE ? OR time_slot LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String searchPattern = "%" + searchText + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) agendasTable.getModel();
                model.setRowCount(0); // Clear table before inserting new rows

                while (rs.next()) {
                    int agendaID = rs.getInt("agenda_id");
                    String agendaTopic = rs.getString("topic");
                    String timeSlot = rs.getString("time_slot");
                    String agendaPresenter = rs.getString("presenter");

                    model.addRow(new Object[]{agendaID, agendaTopic, timeSlot, agendaPresenter});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    
    private void agendaTableSearchListeners() {
        searchAgendaTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchAgendas();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadOuterAgendasTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchAgendas();
            }
        });
        
        searchAgendaTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                agendasTable.clearSelection(); // Deselects any selected row
                agendasTable.getColumnModel().getSelectionModel().clearSelection();
            }
        });

    }
    
    public void loadOuterAgendasTable() {
        int selectedRow = meetingsTable.getSelectedRow();


        int meetingID = (int) meetingsTable.getValueAt(selectedRow, 0);

        String agendaTableFetchSQL = 
            "SELECT a.agenda_id, a.topic, a.time_slot, a.presenter, " +
            "(SELECT GROUP_CONCAT(d.file_name SEPARATOR ', ') FROM document d WHERE d.agenda_id = a.agenda_id) AS documents " +
            "FROM agenda a WHERE a.meeting_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(agendaTableFetchSQL)) {

            pstmt.setInt(1, meetingID);

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) agendasTable.getModel();
                model.setRowCount(0); // Clear existing rows

                while (rs.next()) {
                    int agendaID = rs.getInt("agenda_id");
                    String agendaTopic = rs.getString("topic");
                    String timeSlot = rs.getString("time_slot");
                    String agendaPresenter = rs.getString("presenter");
                    String documents = rs.getString("documents"); 

                    if (documents == null) {
                        documents = "No Documents";
                    }

                    model.addRow(new Object[]{agendaID, agendaTopic, timeSlot, agendaPresenter, documents});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //==========================END OF AGENDA OUTER==============================================================

    
    //=======================START OF MEETING===============================================================
    
        private void searchListeners(){
            meetingSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    searchMeeting();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    loadMeetingsTable();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    searchMeeting();
                }
            });
            
            meetingsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    // Ensure that the event is not fired multiple times (prevents double execution)
                    if (!event.getValueIsAdjusting()) {
                        int selectedRow = meetingsTable.getSelectedRow();

                        // Ignore event if no row is selected
                        if (selectedRow == -1) {
                            System.out.println("Selection cleared. Skipping loadOuterAgendasTable.");
                            return;
                        }

                        loadOuterAgendasTable();
                    }
                }
            });

            meetingSearchTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    meetingsTable.clearSelection(); // Deselects any selected row
                    meetingsTable.getColumnModel().getSelectionModel().clearSelection();
                }
            });


    }
        
    private void searchMeeting() {
        String searchText = meetingSearchTextField.getText().trim();
        String committeeName = committeeNameLabel.getText().trim(); 

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a search term.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        

        String searchSQL = "SELECT * FROM meeting m " +
                           "WHERE committee_name = ? " + 
                           "AND (title LIKE ? OR meeting_id LIKE ? OR meeting_date LIKE ? OR meeting_time LIKE ? OR location LIKE ? OR meeting_type LIKE ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String searchPattern = "%" + searchText + "%"; 

            pstmt.setString(1, committeeName); 
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            pstmt.setString(6, searchPattern);
            pstmt.setString(7, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel model = (DefaultTableModel) meetingsTable.getModel();
            model.setRowCount(0); 

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("meeting_ID"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getDate("meeting_date"),
                    rs.getTime("meeting_time"),
                    rs.getString("meeting_type"),
                    
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupListeners(){
        committeeNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadMeetingsTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                //loadMeetingsTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadMeetingsTable();
            }
        });
        
        meetingsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    
                        showLatestMeetingID();
                        meetingTitleTextField.setText("");
                        meetingLocationTextField.setText("");
                        meetingTypeTextField.setText("");
                    int selectedRow = meetingsTable.getSelectedRow();

                    if (selectedRow != -1) { 
                        // Safely retrieve values from the table, handling null values
                        Object objMeetingId = meetingsTable.getValueAt(selectedRow, 0);
                        Object objMeetingTitle = meetingsTable.getValueAt(selectedRow, 1);
                        Object objMeetingLocation = meetingsTable.getValueAt(selectedRow, 2);
                        Object objMeetingDateStr = meetingsTable.getValueAt(selectedRow, 3);
                        Object objMeetingTimeStr = meetingsTable.getValueAt(selectedRow, 4);
                        Object objMeetingType = meetingsTable.getValueAt(selectedRow, 5);

                        String meetingID = (objMeetingId != null) ? objMeetingId.toString() : "";
                        String meetingTitle = (objMeetingTitle != null) ? objMeetingTitle.toString() : "";
                        String meetingLocation = (objMeetingLocation != null) ? objMeetingLocation.toString() : "";
                        String meetingDateStr = (objMeetingDateStr != null) ? objMeetingDateStr.toString() : "";
                        String meetingTimeStr = (objMeetingTimeStr != null) ? objMeetingTimeStr.toString() : "";
                        String meetingType = (objMeetingType != null) ? objMeetingType.toString() : "";

                        meetingIDLabel.setText(String.valueOf(meetingID));
                        meetingTitleTextField.setText(meetingTitle);
                        meetingLocationTextField.setText(meetingLocation);
                        meetingTypeTextField.setText(meetingType);

                        // Convert string to Date and set in JDateChooser
                        if (!meetingDateStr.isEmpty()) { 
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date joinedDate = dateFormat.parse(meetingDateStr);
                                meetingDateChooser.setDate(joinedDate);
                            } catch (java.text.ParseException ex) {
                                Logger.getLogger(MemberFrontend.class.getName()).log(Level.SEVERE, null, ex);
                                meetingDateChooser.setDate(null); // Ensure it clears in case of parsing error
                            }
                        } else {
                            meetingDateChooser.setDate(null); // clear the date if no joinedDate is available
                        }
                        
                        if (!meetingTimeStr.isEmpty()) {
                            try {
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); // Adjust format based on DB storage
                                Date meetingTime = timeFormat.parse(meetingTimeStr);
                                meetingTimeSpinner.setValue(meetingTime);
                            } catch (java.text.ParseException ex) {
                                Logger.getLogger(MemberFrontend.class.getName()).log(Level.SEVERE, null, ex);
                                meetingTimeSpinner.setValue(new Date()); // Reset to current time in case of error
                            }
                        } else {
                            meetingTimeSpinner.setValue(new Date()); // Default to current time if empty
                        }
                        addMeetingAgendaButton.setEnabled(true);
                        updateMeetingAgendaButton.setEnabled(true);
                        deleteMeetingAgendaButton.setEnabled(true);
                    }   
                }
            }
        });
    }
    
    private void loadMeetingsTable() {
        String committeeName = committeeNameTextField.getText().trim(); // Trim extra spaces

        if (committeeName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a committee name.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String meetingTableFetchSQL = "SELECT meeting_id, committee_name, title, meeting_date, meeting_time, location, meeting_type " +
                                      "FROM meeting WHERE committee_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(meetingTableFetchSQL)) {

            pstmt.setString(1, committeeName);

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) meetingsTable.getModel();
                model.setRowCount(0); // Clear existing rows

                while (rs.next()) {
                    int meetingID = rs.getInt("meeting_id");
                    String committee = rs.getString("committee_name"); // Now fetched correctly
                    String meetingTitle = rs.getString("title");
                    java.sql.Date meetingDate = rs.getDate("meeting_date");
                    Time meetingTime = rs.getTime("meeting_time");
                    String meetingLocation = rs.getString("location");
                    String meetingType = rs.getString("meeting_type");

                    model.addRow(new Object[]{meetingID, meetingTitle, meetingLocation, meetingDate, meetingTime,  meetingType});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void showLatestMeetingID() {
        String getMeetingIDquery = "SELECT meeting_id FROM meeting ORDER BY meeting_id DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getMeetingIDquery);
             ResultSet MeetingIDrs = pstmt.executeQuery()) {

            if (MeetingIDrs.next()) {
                int meetingID = MeetingIDrs.getInt("meeting_id"); 
                meetingID++;
                meetingIDLabel.setText(String.valueOf(meetingID)); 
            } else {
                meetingIDLabel.setText("1"); 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCommitteeName(){
        committeeNameLabel.setText(committeeNameTextField.getText());
    }
    //==========================END OF MEETING===============================================================================
    
    
    
    //========================================EDIT COMMITTEE DETAILS==============================================================
    

    private void committeeComboBoxListeners(){
        committeeIDComboBox.addActionListener(e -> {

                String selectedCommittee = (String) committeeIDComboBox.getSelectedItem();
                
                if (selectedCommittee != null && !selectedCommittee.isEmpty()) {
                  
                    loadMeetings();
                }
            
        });
    }
    private void loadCommitteeDetails(int committeeID){
        
        committeeNameTextField.setText("");
        descriptionTextArea.setText("");
        dateOfCreationChooser.setDate(null);
        String getCommitteeDetailsSQL = "SELECT name, description, date_of_creation FROM committee WHERE committee_ID=?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt3 = conn.prepareStatement(getCommitteeDetailsSQL)){
           
                pstmt3.setInt(1, committeeID);
                ResultSet committeeDetailsRS = pstmt3.executeQuery();
                if(committeeDetailsRS.next()){
                   
                    String name = committeeDetailsRS.getString("name");
                    String description = committeeDetailsRS.getString("description");
                    java.sql.Date sqlDate = committeeDetailsRS.getDate("date_of_creation");

                    committeeNameTextField.setText(name);
                    descriptionTextArea.setText(description);

                    if (sqlDate != null) {
                        java.util.Date utilDate = new java.util.Date(sqlDate.getTime()); 
                        dateOfCreationChooser.setDate(utilDate); 
                    }

                }
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error loading committees: "+e.getMessage());
        }
    }
    
    public final void loadCommittees(String email){
        String getMemberIDSQL = "SELECT member_ID FROM member WHERE email = ?";
        String getCommitteeIDsSQL = "SELECT committee_ID FROM belongs_to WHERE member_ID = ?";
        
        
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt1 = conn.prepareStatement(getMemberIDSQL);
            PreparedStatement pstmt2 = conn.prepareStatement(getCommitteeIDsSQL)){
            
            pstmt1.setString(1, email);
            ResultSet memberIDrs = pstmt1.executeQuery();
            
            if(memberIDrs.next()){
                int memberID = memberIDrs.getInt("member_ID");
                pstmt2.setInt(1, memberID);
                ResultSet committeeIDrs = pstmt2.executeQuery();
                
                committeeIDComboBox.removeAllItems();
                //committeeIDComboBox.addItem("Select committee ID");
                while(committeeIDrs.next()){
                    int committeeID = committeeIDrs.getInt("committee_ID");
                    committeeIDComboBox.addItem(String.valueOf(committeeID));
                }
                
                
            }else{
                    JOptionPane.showMessageDialog(null, "Member or Committee Error!");
            }
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error loading committees: "+e.getMessage());
        }
    }
    //=============================================== END OF EDIT COMMITTEE DETAILS============================================

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
        logoutLabel = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        meetingTitleTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        meetingDateChooser = new com.toedter.calendar.JDateChooser();
        jScrollPane2 = new javax.swing.JScrollPane();
        meetingsTable = new javax.swing.JTable();
        meetingSearchTextField = new javax.swing.JTextField();
        meetingSearchButton = new javax.swing.JButton();
        deleteMeetingButton = new javax.swing.JButton();
        updateMeetingButton = new javax.swing.JButton();
        insertMeetingButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        meetingLocationTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        meetingTypeTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        agendasTable = new javax.swing.JTable();
        addMeetingAgendaButton = new javax.swing.JButton();
        updateMeetingAgendaButton = new javax.swing.JButton();
        deleteMeetingAgendaButton = new javax.swing.JButton();
        searchAgendaTextField = new javax.swing.JTextField();
        agendaSearchButton = new javax.swing.JButton();
        Date date1 = new Date();
        SpinnerDateModel sdm1 = new SpinnerDateModel(date1, null,null, Calendar.HOUR_OF_DAY);
        meetingTimeSpinner = new javax.swing.JSpinner(sdm1);
        committeeNameLabel = new javax.swing.JLabel();
        meetingIDLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        minutesTable = new javax.swing.JTable();
        minuteSearchTextField = new javax.swing.JTextField();
        searchMinuteButton = new javax.swing.JButton();
        deleteMinuteButton = new javax.swing.JButton();
        updateMinuteButton = new javax.swing.JButton();
        insertMinuteButton = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        approvedByTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        meetingIDComboBox = new javax.swing.JComboBox<>();
        meetingTitleComboBox = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();
        jLabel21 = new javax.swing.JLabel();
        createdByTextField = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        Date date2 = new Date();
        SpinnerDateModel sdm2 = new SpinnerDateModel(date2, null, null, Calendar.HOUR_OF_DAY);
        createdTimeSpinner = new javax.swing.JSpinner(sdm2);
        jScrollPane5 = new javax.swing.JScrollPane();
        meetingAgendaList = new javax.swing.JList<>();
        discussionButton = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        printMinuteButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        committeeNameTextField = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        dateOfCreationChooser = new com.toedter.calendar.JDateChooser();
        updateCommitteeButton = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        memberButton = new javax.swing.JButton();
        committeeIDComboBox = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        addCommitteeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(null);

        jPanel1.setBackground(new java.awt.Color(0, 153, 153));
        jPanel1.setMaximumSize(new java.awt.Dimension(1920, 1080));

        jLabel1.setFont(new java.awt.Font("Myanmar Text", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Edit Committee Details");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Myanmar Text", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Meeting");
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Myanmar Text", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Minute");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        logoutLabel.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        logoutLabel.setForeground(new java.awt.Color(255, 255, 255));
        logoutLabel.setText("Logout");
        logoutLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(logoutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addComponent(jLabel2)
                .addGap(6, 6, 6)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(462, 462, 462)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(logoutLabel)
                .addGap(6, 6, 6)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(1000, 1080));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1000, 1080));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMaximumSize(new java.awt.Dimension(1000, 1080));
        jPanel2.setPreferredSize(new java.awt.Dimension(1000, 1080));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 153, 153));
        jLabel5.setText("MEETING");

        meetingTitleTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Meeting ID");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Committee Name");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Meeting Date");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Meeting Location");

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setViewportBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jScrollPane2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(180, 180));

        meetingsTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        meetingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Meeting ID", "Meeting Title", "Meeting Location", "Meeting Date", "Meeting Time", "Meeting Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(meetingsTable);

        meetingSearchTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        meetingSearchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meetingSearchTextFieldActionPerformed(evt);
            }
        });

        meetingSearchButton.setBackground(new java.awt.Color(255, 204, 204));
        meetingSearchButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        meetingSearchButton.setText("Search");
        meetingSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meetingSearchButtonActionPerformed(evt);
            }
        });

        deleteMeetingButton.setBackground(new java.awt.Color(255, 102, 0));
        deleteMeetingButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        deleteMeetingButton.setText("DELETE");
        deleteMeetingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMeetingButtonActionPerformed(evt);
            }
        });

        updateMeetingButton.setBackground(new java.awt.Color(51, 153, 255));
        updateMeetingButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateMeetingButton.setText("UPDATE");
        updateMeetingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMeetingButtonActionPerformed(evt);
            }
        });

        insertMeetingButton.setBackground(new java.awt.Color(0, 255, 204));
        insertMeetingButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        insertMeetingButton.setText("INSERT");
        insertMeetingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertMeetingButtonActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Meeting Title");

        meetingLocationTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Meeting Time");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Meeting Type");

        meetingTypeTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Meeting Agenda:");

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 153)));
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        agendasTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 153), 3));
        agendasTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Agenda ID", "Topic", "Time", "Presenter", "Document"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(agendasTable);

        addMeetingAgendaButton.setText("Add");
        addMeetingAgendaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMeetingAgendaButtonActionPerformed(evt);
            }
        });

        updateMeetingAgendaButton.setText("Update");
        updateMeetingAgendaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMeetingAgendaButtonActionPerformed(evt);
            }
        });

        deleteMeetingAgendaButton.setText("Delete");
        deleteMeetingAgendaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMeetingAgendaButtonActionPerformed(evt);
            }
        });

        agendaSearchButton.setText("Search");
        agendaSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agendaSearchButtonActionPerformed(evt);
            }
        });

        JSpinner.DateEditor de1 = new JSpinner.DateEditor(meetingTimeSpinner, "HH:mm:ss");
        meetingTimeSpinner.setEditor(de1);
        meetingTimeSpinner.setFont(new java.awt.Font("Segoe UI", 0, 14));

        committeeNameLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        committeeNameLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 102), 2, true));

        meetingIDLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel12)
                        .addComponent(meetingTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5)
                                .addComponent(jLabel6)
                                .addComponent(meetingIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(58, 58, 58)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7)
                                .addComponent(committeeNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(42, 42, 42)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel10)
                                .addComponent(meetingTitleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(insertMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(deleteMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(updateMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(388, 388, 388)
                            .addComponent(meetingSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(meetingSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(meetingDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8)
                                    .addComponent(meetingLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 488, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(188, 188, 188))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(meetingTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(506, 506, 506)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addMeetingAgendaButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(updateMeetingAgendaButton)
                                .addGap(12, 12, 12)
                                .addComponent(deleteMeetingAgendaButton)
                                .addGap(18, 18, 18)
                                .addComponent(searchAgendaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(agendaSearchButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10))
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(committeeNameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(meetingTitleTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(meetingIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(63, 63, 63)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel13)
                    .addComponent(addMeetingAgendaButton)
                    .addComponent(deleteMeetingAgendaButton)
                    .addComponent(searchAgendaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateMeetingAgendaButton)
                    .addComponent(agendaSearchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(meetingLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(meetingDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(meetingTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(meetingTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertMeetingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(meetingSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(meetingSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(79, 79, 79))
        );

        jTabbedPane1.addTab("Meeting", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setMaximumSize(new java.awt.Dimension(1920, 1080));
        jPanel3.setPreferredSize(new java.awt.Dimension(1920, 1080));

        jLabel14.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 153, 153));
        jLabel14.setText("MINUTE");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Meeting ID:");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("Content");

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setViewportBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jScrollPane3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(180, 180));

        minutesTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        minutesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Minute ID", "Meeting Title", "Content", "Created By", "Creation Time", "Updated Time", "Approved By"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(minutesTable);

        minuteSearchTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        searchMinuteButton.setBackground(new java.awt.Color(255, 204, 204));
        searchMinuteButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        searchMinuteButton.setText("Search");
        searchMinuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMinuteButtonActionPerformed(evt);
            }
        });

        deleteMinuteButton.setBackground(new java.awt.Color(255, 102, 0));
        deleteMinuteButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        deleteMinuteButton.setText("DELETE");
        deleteMinuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMinuteButtonActionPerformed(evt);
            }
        });

        updateMinuteButton.setBackground(new java.awt.Color(51, 153, 255));
        updateMinuteButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateMinuteButton.setText("UPDATE");
        updateMinuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMinuteButtonActionPerformed(evt);
            }
        });

        insertMinuteButton.setBackground(new java.awt.Color(0, 255, 204));
        insertMinuteButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        insertMinuteButton.setText("INSERT");
        insertMinuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertMinuteButtonActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Created By");

        approvedByTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Meeting Agenda List:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Meeting Title:");

        meetingIDComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        meetingTitleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(0, 102, 102));
        jLabel20.setText("Create Minute For:");

        contentTextArea.setColumns(20);
        contentTextArea.setRows(5);
        jScrollPane4.setViewportView(contentTextArea);

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel21.setText("Approved By");

        createdByTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel22.setText("Creation Time");

        JSpinner.DateEditor de2 = new JSpinner.DateEditor(createdTimeSpinner,"yyyy-MM-dd HH:mm:ss");
        createdTimeSpinner.setEditor(de2);
        createdTimeSpinner.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        meetingAgendaList.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(204, 0, 0)));
        jScrollPane5.setViewportView(meetingAgendaList);

        discussionButton.setText("Discussions");
        discussionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discussionButtonActionPerformed(evt);
            }
        });

        jButton8.setText("Manage Attendance");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        printMinuteButton.setText("Print");
        printMinuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMinuteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1275, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(meetingIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(meetingTitleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel16)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(discussionButton)
                                .addGap(15, 15, 15)
                                .addComponent(jButton8))
                            .addComponent(jLabel14)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(insertMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(createdByTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel17))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel21)
                                                .addGap(121, 121, 121)
                                                .addComponent(jLabel22))
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(approvedByTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(createdTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel18)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(minuteSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(searchMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane5)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(printMinuteButton)
                                .addGap(12, 12, 12)))))
                .addContainerGap(111, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printMinuteButton))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(meetingIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(meetingTitleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(approvedByTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdByTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(updateMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(insertMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minuteSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchMinuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(discussionButton)
                            .addComponent(jButton8))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Minute", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setMaximumSize(new java.awt.Dimension(1920, 1080));
        jPanel4.setPreferredSize(new java.awt.Dimension(1920, 1080));

        jLabel23.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(0, 153, 153));
        jLabel23.setText("COMMITTEE");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Committee ID");

        committeeNameTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setText("Committee Name");

        jScrollPane6.setMaximumSize(new java.awt.Dimension(1000, 800));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane6.setViewportView(descriptionTextArea);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setText("Date of formation");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel27.setText("Description");

        updateCommitteeButton.setBackground(new java.awt.Color(51, 153, 255));
        updateCommitteeButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateCommitteeButton.setText("UPDATE");
        updateCommitteeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCommitteeButtonActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel28.setText("Members:");

        memberButton.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        memberButton.setText("...");
        memberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memberButtonActionPerformed(evt);
            }
        });

        committeeIDComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        committeeIDComboBox.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(0, 204, 51)));
        committeeIDComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                committeeIDComboBoxActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel30.setText("Add Committee:");

        addCommitteeButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        addCommitteeButton.setText("+");
        addCommitteeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCommitteeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(354, 354, 354)
                        .addComponent(updateCommitteeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(memberButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 784, Short.MAX_VALUE)
                                .addComponent(jLabel30))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel23)
                                        .addComponent(jLabel24)
                                        .addComponent(committeeIDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(46, 46, 46)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel25)
                                        .addComponent(committeeNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)))
                                .addComponent(jLabel26)
                                .addComponent(dateOfCreationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addCommitteeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(325, 325, 325))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(committeeNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(committeeIDComboBox))
                .addGap(56, 56, 56)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dateOfCreationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(memberButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(98, 98, 98)
                .addComponent(updateCommitteeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(93, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Edit Committee", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1442, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        jTabbedPane1.setSelectedIndex(0);
        
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jLabel3MouseClicked

    private void meetingSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meetingSearchButtonActionPerformed
        searchMeeting();
    }//GEN-LAST:event_meetingSearchButtonActionPerformed

    private void addMeetingAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMeetingAgendaButtonActionPerformed
        if (meetingsTable.getRowCount() == 0) { 
            JOptionPane.showMessageDialog(this, "Please insert the meeting details first!", "Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }
        
        AgendaFrontend agenda= new AgendaFrontend(committeeNameLabel.getText());
        agenda.setVisible(true);
    }//GEN-LAST:event_addMeetingAgendaButtonActionPerformed

    private void updateMeetingAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMeetingAgendaButtonActionPerformed
        if (meetingsTable.getRowCount() == 0) { 
            JOptionPane.showMessageDialog(this, "Please insert the meeting details first!", "Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }
        AgendaFrontend agenda= new AgendaFrontend(committeeNameLabel.getText());
        agenda.setVisible(true);
    }//GEN-LAST:event_updateMeetingAgendaButtonActionPerformed

    private void searchMinuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMinuteButtonActionPerformed
        searchMinutes();
    }//GEN-LAST:event_searchMinuteButtonActionPerformed

    private void insertMinuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertMinuteButtonActionPerformed
                                                       
        int response = 1;

        if (meetingIDComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a meeting ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int meetingID = Integer.parseInt(meetingIDComboBox.getSelectedItem().toString());
        String content = contentTextArea.getText().trim();
        String createdBy = createdByTextField.getText().trim();
        String approvedBy = approvedByTextField.getText().trim();

        // Get current date and time from Spinner
        java.util.Date selectedDateTime = (java.util.Date) createdTimeSpinner.getValue();
        java.sql.Timestamp creationTime = new java.sql.Timestamp(selectedDateTime.getTime());

        // Truncate milliseconds to avoid MySQL error
        creationTime.setNanos(0);

        if (content.isEmpty() || createdBy.isEmpty() || approvedBy.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertSQL = "INSERT INTO minute (meeting_id, content, created_by, creation_time, approved_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, meetingID);
            pstmt.setString(2, content);
            pstmt.setString(3, createdBy);
            pstmt.setTimestamp(4, creationTime);  // Use TIMESTAMP instead of TIME
            pstmt.setString(5, approvedBy);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Minute record added successfully.");
                loadMinuteTable(meetingID); 

               
            contentTextArea.setText("");  // Clear content
            createdByTextField.setText("");  // Clear createdBy
            approvedByTextField.setText("");  // Clear approvedBy
            createdTimeSpinner.setValue(new java.util.Date()); // Reset spinner to current date/time
           
                response = JOptionPane.showConfirmDialog(this, 
                    "Please add discussions for the agendas?", 
                    "Confirmation", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
                
                
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add minute record.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (response == JOptionPane.YES_OPTION) {
            DiscussionFrontend discussion = new DiscussionFrontend(meetingID);
            discussion.setVisible(true);
        }




    }//GEN-LAST:event_insertMinuteButtonActionPerformed

    private void discussionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discussionButtonActionPerformed
        DiscussionFrontend discussion = new DiscussionFrontend(Integer.parseInt((String) meetingIDComboBox.getSelectedItem()));
        discussion.setVisible(true);
    }//GEN-LAST:event_discussionButtonActionPerformed

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jLabel1MouseClicked

    private void memberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberButtonActionPerformed
        MemberFrontend mf = new MemberFrontend((String)committeeIDComboBox.getSelectedItem());
        mf.setVisible(true);
    }//GEN-LAST:event_memberButtonActionPerformed

    private void agendaSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agendaSearchButtonActionPerformed
        searchAgendas();
    }//GEN-LAST:event_agendaSearchButtonActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        AttendanceFrontend attendance = new AttendanceFrontend(Integer.parseInt((String) meetingIDComboBox.getSelectedItem()),
        Integer.parseInt((String) committeeIDComboBox.getSelectedItem()));
        attendance.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void printMinuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMinuteButtonActionPerformed
    
                int selectedRow = minutesTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a minute to print.");
                    return;
                }

                int minuteId = (int) minutesTable.getValueAt(selectedRow, 0); 
                printMinute(minuteId);
      

    }//GEN-LAST:event_printMinuteButtonActionPerformed

    private void committeeIDComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_committeeIDComboBoxActionPerformed
        String selectedCommittee = (String) committeeIDComboBox.getSelectedItem(); 
      

        if (selectedCommittee != null && !selectedCommittee.isEmpty()) {
        try {
            int committeeID = Integer.parseInt(selectedCommittee); // Convert to int
            loadCommitteeDetails(committeeID); // Pass ID to method
            showCommitteeName();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid selection. Please select a valid Committee ID.");
        }
    }
        
    }//GEN-LAST:event_committeeIDComboBoxActionPerformed

    private void addCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCommitteeButtonActionPerformed
        try {
            AddCommittee committee  = new AddCommittee(this, email);
            committee.setVisible(true);
            
        } catch (SQLException ex) {
            Logger.getLogger(GUIMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_addCommitteeButtonActionPerformed

    private void updateCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCommitteeButtonActionPerformed
        String selectedCommittee = (String) committeeIDComboBox.getSelectedItem(); 
      

        if (selectedCommittee != null && !selectedCommittee.isEmpty()) {
         

        

            try {
                int committeeID = Integer.parseInt(selectedCommittee.trim()); 
                String committeeName = committeeNameTextField.getText();
                String description = descriptionTextArea.getText();
                java.util.Date utilDate = dateOfCreationChooser.getDate();

                if (committeeName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Atleast Committee Name field must be filled.");
                    return;
                }

                // Convert java.util.Date to java.sql.Date
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

    
                String updateCommitteeSQL = "UPDATE committee SET name=?, description=?, date_of_creation=? WHERE committee_ID=?";

                try (Connection conn = DBUtil.getConnection(); 
                     PreparedStatement pstmt = conn.prepareStatement(updateCommitteeSQL)) {

                    pstmt.setString(1, committeeName);
                    pstmt.setString(2, description);
                    pstmt.setDate(3, sqlDate);
                    pstmt.setInt(4, committeeID);

                    int rowsUpdated = pstmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(null, "Committee details updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "No changes were made.");
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid Committee ID. Please select a valid Committee.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_updateCommitteeButtonActionPerformed

    private void insertMeetingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertMeetingButtonActionPerformed
                                                   
        String insertMeetingSQL = "INSERT INTO meeting (committee_name, title, meeting_date, meeting_time, location, meeting_type) VALUES (?, ?, ?, ?, ?, ?)";
        String getCommitteeIDSQL = "SELECT committee_ID FROM committee WHERE name = ?";
        String getMemberIDsSQL = "SELECT member_id FROM belongs_to WHERE committee_id = ?";
        String insertAttendsSQL = "INSERT INTO attends (meeting_id, member_id) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmtMeeting = conn.prepareStatement(insertMeetingSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtGetCommitteeID = conn.prepareStatement(getCommitteeIDSQL);
             PreparedStatement pstmtGetMembers = conn.prepareStatement(getMemberIDsSQL);
             PreparedStatement pstmtInsertAttends = conn.prepareStatement(insertAttendsSQL)) {

            // retrieve committee id
            String committeeName = committeeNameLabel.getText();
            pstmtGetCommitteeID.setString(1, committeeName);
            ResultSet rsCommittee = pstmtGetCommitteeID.executeQuery();

            int committeeID = -1;
            if (rsCommittee.next()) {
                committeeID = rsCommittee.getInt("committee_id");
            } else {
                JOptionPane.showMessageDialog(null, "Committee not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // insert meeting
            String title = meetingTitleTextField.getText();
            String location = meetingLocationTextField.getText();
            String type = meetingTypeTextField.getText();
            java.util.Date utilDate = meetingDateChooser.getDate();
            java.sql.Date sqlDate = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;

            java.util.Date selectedTime = (Date) meetingTimeSpinner.getValue();
            Time sqlTime = new Time(selectedTime.getTime());

            if (title.isEmpty() || sqlDate == null) {
                JOptionPane.showMessageDialog(null, "Meeting title and date are required!");
                return;
            }

            pstmtMeeting.setString(1, committeeName);
            pstmtMeeting.setString(2, title);
            pstmtMeeting.setDate(3, sqlDate);
            pstmtMeeting.setTime(4, sqlTime);
            pstmtMeeting.setString(5, location);
            pstmtMeeting.setString(6, type);

            int rowsInserted = pstmtMeeting.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmtMeeting.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int meetingID = generatedKeys.getInt(1);

                        //  Members of Committee
                        pstmtGetMembers.setInt(1, committeeID);
                        ResultSet rsMembers = pstmtGetMembers.executeQuery();

                        while (rsMembers.next()) {
                            int memberID = rsMembers.getInt("member_id");

                            //insert into Attends Table
                            pstmtInsertAttends.setInt(1, meetingID);
                            pstmtInsertAttends.setInt(2, memberID);
                            pstmtInsertAttends.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(null, "Meeting (ID: " + meetingID + ") inserted successfully with attendees!");

               
                        meetingTitleTextField.setText("");
                        meetingLocationTextField.setText("");
                        meetingTypeTextField.setText("");
                        meetingTimeSpinner.setValue(new Date());
                        meetingDateChooser.setDate(null);

                        showLatestMeetingID();
                        loadMeetingsTable();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to retrieve Meeting ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add meeting details!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_insertMeetingButtonActionPerformed

    private void updateMeetingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMeetingButtonActionPerformed
        try {
            int currentMeetingID = Integer.parseInt(meetingIDLabel.getText());  
            String meetingTitle = meetingTitleTextField.getText();
            String meetingLocation = meetingLocationTextField.getText();
            String meetingType = meetingTypeTextField.getText();

            java.util.Date utilDate = meetingDateChooser.getDate();
            java.sql.Date sqlDate = (utilDate != null) ? new java.sql.Date(utilDate.getTime()) : null;
            
            java.util.Date selectedTime = (Date) meetingTimeSpinner.getValue();
            Time sqlTime = new Time(selectedTime.getTime()); // Directly get SQL Time


            if (meetingTitle.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Important fields are required for update!");
                return;
            }

            String updateMeetingDetailsSQL = "UPDATE meeting SET title=?, meeting_date=?, meeting_time=?, location=?, meeting_type=? WHERE meeting_ID=?";

            try (Connection conn = DBUtil.getConnection(); 
                 PreparedStatement pstmt = conn.prepareStatement(updateMeetingDetailsSQL)) {

                pstmt.setString(1, meetingTitle);
                pstmt.setDate(2, sqlDate);
                pstmt.setTime(3, sqlTime);
                pstmt.setString(4, meetingLocation);
                pstmt.setString(5, meetingType);
                
                pstmt.setInt(6, currentMeetingID); 

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Meeting (ID: " + currentMeetingID + ") details updated successfully!");
                    
                        meetingTitleTextField.setText("");
                        meetingLocationTextField.setText("");
                        meetingTypeTextField.setText("");
                        meetingTimeSpinner.setValue(new Date());
                        meetingDateChooser.setDate(null);
                    showLatestMeetingID();
                    loadMeetingsTable();
                } else {
                    JOptionPane.showMessageDialog(null, "No changes were made.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Meeting ID. Please select a valid Meeting.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_updateMeetingButtonActionPerformed

    private void deleteMeetingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMeetingButtonActionPerformed
        try {
            int currentMeetingID = Integer.parseInt(meetingIDLabel.getText());

            int confirm = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete Meeting ID: " + currentMeetingID + "?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Check if member exists
                String checkMeetingSQL = "SELECT COUNT(*) FROM meeting WHERE meeting_id=?";
                String deleteMeetingSQL = "DELETE FROM meeting WHERE meeting_id=?";

                try (Connection conn = DBUtil.getConnection();
                     PreparedStatement checkStmt = conn.prepareStatement(checkMeetingSQL);
                     PreparedStatement deleteStmt = conn.prepareStatement(deleteMeetingSQL)) {

                    checkStmt.setInt(1, currentMeetingID);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {  // member exists
                        deleteStmt.setInt(1, currentMeetingID);
                        int rowsDeleted = deleteStmt.executeUpdate();

                        if (rowsDeleted > 0) {
                            JOptionPane.showMessageDialog(null, "Meeting (ID: " + currentMeetingID + ") deleted!");
                            meetingTitleTextField.setText("");
                            meetingLocationTextField.setText("");
                            meetingTypeTextField.setText("");
                            meetingTimeSpinner.setValue(new Date());
                            meetingDateChooser.setDate(null);

                            showLatestMeetingID();
                            loadMeetingsTable();
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Deletion failed. Please try again.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Meeting ID " + currentMeetingID + " does not exist!", 
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
    }//GEN-LAST:event_deleteMeetingButtonActionPerformed

    private void meetingSearchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meetingSearchTextFieldActionPerformed

    }//GEN-LAST:event_meetingSearchTextFieldActionPerformed

    private void deleteMeetingAgendaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMeetingAgendaButtonActionPerformed
                                                           
        int selectedRow = agendasTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an agenda to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int agendaID = (int) agendasTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this agenda?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String deleteSQL = "DELETE FROM agenda WHERE agenda_id = ?";

            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

                pstmt.setInt(1, agendaID);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Agenda deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadOuterAgendasTable(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete agenda!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }


    }//GEN-LAST:event_deleteMeetingAgendaButtonActionPerformed

    private void updateMinuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMinuteButtonActionPerformed
       
                                                
        int selectedRow = minutesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a minute entry to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int minuteID = Integer.parseInt(minutesTable.getValueAt(selectedRow, 0).toString()); 
        int meetingID = Integer.parseInt(meetingIDComboBox.getSelectedItem().toString());
        String content = contentTextArea.getText().trim();
        String createdBy = createdByTextField.getText().trim();
        String approvedBy = approvedByTextField.getText().trim();

        // Get the current date and time
        java.sql.Timestamp updatedTime = new java.sql.Timestamp(System.currentTimeMillis());

        if (content.isEmpty() || createdBy.isEmpty() || approvedBy.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All required fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String updateSQL = "UPDATE minute SET content = ?, created_by = ?, approved_by = ?, updated_time = ? WHERE minute_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, content);
            pstmt.setString(2, createdBy);
            pstmt.setString(3, approvedBy);
            pstmt.setTimestamp(4, updatedTime); // ✅ Corrected: Store full date and time
            pstmt.setInt(5, minuteID);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Minute entry updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                
            contentTextArea.setText("");  // Clear content
            createdByTextField.setText("");  // Clear createdBy
            approvedByTextField.setText("");  // Clear approvedBy
            createdTimeSpinner.setValue(new java.util.Date()); // Reset spinner to current date/time
            loadMinuteTable(Integer.parseInt(meetingIDComboBox.getSelectedItem().toString()));
            meetingIDComboBox.setSelectedIndex(0); 
          
            } else {
                JOptionPane.showMessageDialog(null, "Update failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }


    

    }//GEN-LAST:event_updateMinuteButtonActionPerformed

    private void deleteMinuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMinuteButtonActionPerformed
        
        int selectedRow = minutesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int minuteID = (int) minutesTable.getValueAt(selectedRow, 0); // Get Minute ID from selected row

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String query = "DELETE FROM minute WHERE minute_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, minuteID);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                

               
                    contentTextArea.setText("");
                    createdByTextField.setText("");
                    approvedByTextField.setText("");
                    createdTimeSpinner.setValue(new java.util.Date());
                    loadMinuteTable(Integer.parseInt(meetingIDComboBox.getSelectedItem().toString()));
                    meetingIDComboBox.setSelectedIndex(0);                
            } else {
                JOptionPane.showMessageDialog(this, "Deletion failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid Meeting ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }




    }//GEN-LAST:event_deleteMinuteButtonActionPerformed

    private void logoutLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutLabelMouseClicked
                                           
        int choice = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to log out?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Close current window
            this.dispose();

            // Open Login window
            new Login().setVisible(true);
        }


    }//GEN-LAST:event_logoutLabelMouseClicked

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
            java.util.logging.Logger.getLogger(GUIMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUIMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUIMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUIMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUIMain("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCommitteeButton;
    private javax.swing.JButton addMeetingAgendaButton;
    private javax.swing.JButton agendaSearchButton;
    private javax.swing.JTable agendasTable;
    private javax.swing.JTextField approvedByTextField;
    private javax.swing.JComboBox<String> committeeIDComboBox;
    private javax.swing.JLabel committeeNameLabel;
    private javax.swing.JTextField committeeNameTextField;
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JTextField createdByTextField;
    private javax.swing.JSpinner createdTimeSpinner;
    private com.toedter.calendar.JDateChooser dateOfCreationChooser;
    private javax.swing.JButton deleteMeetingAgendaButton;
    private javax.swing.JButton deleteMeetingButton;
    private javax.swing.JButton deleteMinuteButton;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JButton discussionButton;
    private javax.swing.JButton insertMeetingButton;
    private javax.swing.JButton insertMinuteButton;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel logoutLabel;
    private javax.swing.JList<String> meetingAgendaList;
    private com.toedter.calendar.JDateChooser meetingDateChooser;
    private javax.swing.JComboBox<String> meetingIDComboBox;
    private javax.swing.JLabel meetingIDLabel;
    private javax.swing.JTextField meetingLocationTextField;
    private javax.swing.JButton meetingSearchButton;
    private javax.swing.JTextField meetingSearchTextField;
    private javax.swing.JSpinner meetingTimeSpinner;
    private javax.swing.JComboBox<String> meetingTitleComboBox;
    private javax.swing.JTextField meetingTitleTextField;
    private javax.swing.JTextField meetingTypeTextField;
    private javax.swing.JTable meetingsTable;
    private javax.swing.JButton memberButton;
    private javax.swing.JTextField minuteSearchTextField;
    private javax.swing.JTable minutesTable;
    private javax.swing.JButton printMinuteButton;
    private javax.swing.JTextField searchAgendaTextField;
    private javax.swing.JButton searchMinuteButton;
    private javax.swing.JButton updateCommitteeButton;
    private javax.swing.JButton updateMeetingAgendaButton;
    private javax.swing.JButton updateMeetingButton;
    private javax.swing.JButton updateMinuteButton;
    // End of variables declaration//GEN-END:variables
}

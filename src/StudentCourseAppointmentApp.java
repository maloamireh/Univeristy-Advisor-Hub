import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentCourseAppointmentApp extends JFrame {

    private Connection connection;
    private JPanel mainPanel;
    private JPanel addStudentPanel;
    private JPanel addCoursePanel;
    private JPanel addAppointmentPanel;
    private JPanel appointmentPanel;

    private JTable appointmentTable;
    private int appointmentIdColumnIndex;
    private int timeColumnIndex;
    private int durationColumnIndex;

    private JPanel mainGrid;

    public StudentCourseAppointmentApp() throws SQLException {
        // Initialize the database connection
        connection = DatabaseUtil.getConnection();

        setTitle("Student, Course, and Appointment Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        addStudentPanel = createAddStudentPanel();
        addCoursePanel = createAddCoursePanel();
        addAppointmentPanel = createAddAppointmentPanel();
        appointmentPanel = viewAppointmentsPanel();

        JButton addStudentMenuItem = new JButton("Add Student");
        JButton addCourseMenuItem = new JButton("Add Course");
        JButton addAppointmentMenuItem = new JButton("Add Appointment");
        JButton viewAppointmentsMenuItem = new JButton("View Appointments");

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.add(addStudentMenuItem);
        menuContainer.add(addCourseMenuItem);
        menuContainer.add(addAppointmentMenuItem);
        menuContainer.add(viewAppointmentsMenuItem);

        mainGrid = new JPanel();

        mainPanel.add(menuContainer);
        mainPanel.add(mainGrid);

        add(mainPanel);

        addStudentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addStudentPanel);
            }
        });

        addCourseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addCoursePanel);
            }
        });

        addAppointmentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addAppointmentPanel);
            }
        });

        viewAppointmentsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(appointmentPanel);
            }
        });

        setVisible(true);
    }

    private void switchPanel(JPanel panel) {
        mainGrid.removeAll();
        mainGrid.add(panel);
        mainGrid.revalidate();
        mainGrid.repaint();
    }

    private JPanel viewAppointmentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create and configure a JTable for appointments
        appointmentTable = new JTable();
        // Add appointmentTable to a JScrollPane if needed

        JButton editAppointmentButton = new JButton("Edit Appointment");
        JButton deleteAppointmentButton = new JButton("Delete Appointment");

        appointmentIdColumnIndex = 0; // Index of the appointment_id column
        timeColumnIndex = 3; // Index of the appointment_time column
        durationColumnIndex = 4;

        // Add components to the panel

        // Handle edit and delete actions
        editAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEditAppointmentDialog(appointmentTable.getSelectedRow());
            }
        });

        // Inside the viewAppointmentsPanel method
        deleteAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = appointmentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int appointmentId = (int) appointmentTable.getValueAt(selectedRow, appointmentIdColumnIndex);

                    int confirmDialogResult = JOptionPane.showConfirmDialog(mainPanel, "Are you sure you want to delete this appointment?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                    if (confirmDialogResult == JOptionPane.YES_OPTION) {
                        try {
                            // Implement the logic to delete the appointment from the database
                            String deleteQuery = "DELETE FROM Appointments WHERE appointment_id = ?";
                            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                            deleteStatement.setInt(1, appointmentId);
                            int rowsDeleted = deleteStatement.executeUpdate();

                            if (rowsDeleted > 0) {
                                // Remove the appointment from the JTable
                                DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
                                model.removeRow(selectedRow);

                                showAlert("Appointment deleted successfully.");
                            } else {
                                showAlert("Error deleting appointment.");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            showAlert("Error deleting appointment: " + ex.getMessage());
                        }
                    }
                } else {
                    showAlert("Please select an appointment to delete.");
                }
            }
        });


        panel.add(appointmentTable);
        panel.add(editAppointmentButton);
        panel.add(deleteAppointmentButton);

        return panel;
    }

    private JPanel createAddStudentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel firstNameLabel = new JLabel("First Name:");
        JTextField firstNameField = new JTextField(20);
        JLabel lastNameLabel = new JLabel("Last Name:");
        JTextField lastNameField = new JTextField(20);
        JLabel regNumberLabel = new JLabel("Registration Number:");
        JTextField regNumberField = new JTextField(20);
        JButton addStudentButton = new JButton("Add Student");

        // Add components to the panel
        panel.add(firstNameLabel);
        panel.add(firstNameField);
        panel.add(lastNameLabel);
        panel.add(lastNameField);
        panel.add(regNumberLabel);
        panel.add(regNumberField);
        panel.add(addStudentButton);

        // Add action for the "Add Student" button
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String regNumber = regNumberField.getText();

                    // Insert student into the database
                    String insertSQL = "INSERT INTO Students (first_name, last_name, registration_number) VALUES (?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setString(1, firstName);
                    preparedStatement.setString(2, lastName);
                    preparedStatement.setString(3, regNumber);
                    preparedStatement.executeUpdate();

                    // Clear the fields after adding
                    firstNameField.setText("");
                    lastNameField.setText("");
                    regNumberField.setText("");

                    // Optionally, provide a success message
                    showAlert("Student added successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error adding student: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    // Similar methods for createAddCoursePanel and createAddAppointmentPanel

    private void showAlert(String message) {
        JOptionPane.showMessageDialog(this, message);
    }


    private void updateAppointmentDetails(int selectedRow, String updatedTime, int updatedDuration) throws SQLException {
        // Get the appointment ID from the JTable (assuming the JTable model is set up correctly)
        int appointmentId = (int) appointmentTable.getModel().getValueAt(selectedRow, appointmentIdColumnIndex);

        // Implement the logic to update the appointment's time and duration in the database
        String updateQuery = "UPDATE Appointments SET appointment_time = ?, duration_minutes = ? WHERE appointment_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
        preparedStatement.setString(1, updatedTime);
        preparedStatement.setInt(2, updatedDuration);
        preparedStatement.setInt(3, appointmentId);
        preparedStatement.executeUpdate();
    }

    private JPanel createAddCoursePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel courseCodeLabel = new JLabel("Course Code:");
        JTextField courseCodeField = new JTextField(20);
        JLabel courseNameLabel = new JLabel("Course Name:");
        JTextField courseNameField = new JTextField(20);
        JButton addCourseButton = new JButton("Add Course");

        // Add components to the panel
        panel.add(courseCodeLabel);
        panel.add(courseCodeField);
        panel.add(courseNameLabel);
        panel.add(courseNameField);
        panel.add(addCourseButton);

        // Add action for the "Add Course" button
        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String courseCode = courseCodeField.getText();
                    String courseName = courseNameField.getText();

                    // Insert course into the database
                    String insertSQL = "INSERT INTO Courses (course_code, course_name) VALUES (?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setString(1, courseCode);
                    preparedStatement.setString(2, courseName);
                    preparedStatement.executeUpdate();

                    // Clear the fields after adding
                    courseCodeField.setText("");
                    courseNameField.setText("");

                    // Optionally, provide a success message
                    showAlert("Course added successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error adding course: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private JPanel createAddAppointmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel studentNameLabel = new JLabel("Student Name:");
        JTextField studentNameField = new JTextField(20);
        JLabel courseLabel = new JLabel("Course:");
        JComboBox<String> courseComboBox = new JComboBox<>();
        JLabel timeLabel = new JLabel("Time:");
        JTextField timeField = new JTextField(20);
        JLabel durationLabel = new JLabel("Duration (minutes):");
        JTextField durationField = new JTextField(5);
        JButton addAppointmentButton = new JButton("Add Appointment");

        // Populate the courseComboBox with course names from the database
        populateCourseComboBox(courseComboBox);

        // Add components to the panel
        panel.add(studentNameLabel);
        panel.add(studentNameField);
        panel.add(courseLabel);
        panel.add(courseComboBox);
        panel.add(timeLabel);
        panel.add(timeField);
        panel.add(durationLabel);
        panel.add(durationField);
        panel.add(addAppointmentButton);

        // Add action for the "Add Appointment" button
        addAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String studentName = studentNameField.getText();
                    String selectedCourse = (String) courseComboBox.getSelectedItem();
                    String time = timeField.getText();
                    int duration = Integer.parseInt(durationField.getText());

                    // Retrieve student_id and course_id based on names
                    int studentId = getStudentIdByName(studentName);
                    int courseId = getCourseIdByName(selectedCourse);

                    // Insert appointment into the database
                    String insertSQL = "INSERT INTO Appointments (student_id, course_id, appointment_time, duration_minutes) VALUES (?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setInt(1, studentId);
                    preparedStatement.setInt(2, courseId);
                    preparedStatement.setString(3, time);
                    preparedStatement.setInt(4, duration);
                    preparedStatement.executeUpdate();

                    // Clear the fields after adding
                    studentNameField.setText("");
                    courseComboBox.setSelectedIndex(0);
                    timeField.setText("");
                    durationField.setText("");

                    // Optionally, provide a success message
                    showAlert("Appointment added successfully!");
                } catch (SQLException | NumberFormatException ex) {
                    ex.printStackTrace();
                    showAlert("Error adding appointment: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private void populateCourseComboBox(JComboBox<String> courseComboBox) {
        try {
            // Retrieve course names from the database and add them to the JComboBox
            String query = "SELECT course_name FROM Courses";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                courseComboBox.addItem(resultSet.getString("course_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Error populating course names: " + ex.getMessage());
        }
    }

    private void openEditAppointmentDialog(int selectedRow) {
        if (selectedRow >= 0) {
            // Get the appointment details from the JTable
            String appointmentTime = (String) appointmentTable.getValueAt(selectedRow, timeColumnIndex);
            int durationMinutes = (int) appointmentTable.getValueAt(selectedRow, durationColumnIndex);

            // Create a JDialog for editing the appointment
            JDialog editDialog = new JDialog(this, "Edit Appointment", true);
            editDialog.setLayout(new FlowLayout());

            JTextField appointmentTimeField = new JTextField(appointmentTime, 20);
            JTextField durationField = new JTextField(Integer.toString(durationMinutes), 5);
            JButton saveButton = new JButton("Save");

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String updatedTime = appointmentTimeField.getText();
                        int updatedDuration = Integer.parseInt(durationField.getText());

                        // Update the appointment's time and duration in the database
                        updateAppointmentDetails(selectedRow, updatedTime, updatedDuration);

                        // Update the JTable with the new values
                        appointmentTable.setValueAt(updatedTime, selectedRow, timeColumnIndex);
                        appointmentTable.setValueAt(updatedDuration, selectedRow, durationColumnIndex);

                        editDialog.dispose();

                        showAlert("Appointment updated successfully!");
                    } catch (NumberFormatException ex) {
                        showAlert("Invalid duration value. Please enter a valid number.");
                    } catch (SQLException ex) {
                        showAlert("Error updating appointment: " + ex.getMessage());
                    }
                }
            });

            editDialog.add(new JLabel("Appointment Time:"));
            editDialog.add(appointmentTimeField);
            editDialog.add(new JLabel("Duration (minutes):"));
            editDialog.add(durationField);
            editDialog.add(saveButton);

            editDialog.pack();
            editDialog.setLocationRelativeTo(this);
            editDialog.setVisible(true);
        } else {
            showAlert("Please select an appointment to edit.");
        }
    }


    private int getStudentIdByName(String studentName) throws SQLException {
        String[] nameParts = studentName.split(" ");
        if (nameParts.length != 2) {
            return -1; // Invalid input
        }

        String firstName = nameParts[0];
        String lastName = nameParts[1];

        String query = "SELECT student_id FROM Students WHERE first_name = ? AND last_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("student_id");
            }
        }
        return -1; // Not found
    }

    private int getCourseIdByName(String courseName) throws SQLException {
        String query = "SELECT course_id FROM Courses WHERE course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, courseName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("course_id");
            }
        }
        return -1; // Not found
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new StudentCourseAppointmentApp();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

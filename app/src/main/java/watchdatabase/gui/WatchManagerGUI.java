package watchdatabase.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Comparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.table.TableRowSorter;

import watchdatabase.dbhelpers.WatchDatabaseHelper;
import watchdatabase.models.Watch;

public class WatchManagerGUI {
    private JFrame frame;
    private JTable watchTable;
    private DefaultTableModel tableModel;
    private final WatchDatabaseHelper databaseHelper;
    private JLabel imageLabel;
    private final String user;
    private final Font font = new Font("SansSerif", Font.BOLD, 14);

    public WatchManagerGUI(WatchDatabaseHelper databaseHelper, String user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
        initialize();
    }

    private void initialize() {      
        frame = new JFrame("Watch Manager");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0); // Exit if user confirms
                }
            }
        });

        // Set up table model and table
        createTable();        
        loadWatchesIntoTable();

        JScrollPane scrollPane = new JScrollPane(watchTable);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        imageLabel = new JLabel("No image selected", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200));
        frame.getContentPane().add(imageLabel, BorderLayout.EAST);

        watchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // Ensure that the user actually selected a row
                if (!e.getValueIsAdjusting() && watchTable.getSelectedRow() != -1) {
                    onWatchSelection();  // Call the method to display the image
                }
            }
        });
    }

    private void editWatch() {
        int selectedRow = watchTable.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String brand = (String) tableModel.getValueAt(selectedRow, 1);
            String model = (String) tableModel.getValueAt(selectedRow, 2);
            double price = (double) tableModel.getValueAt(selectedRow, 3);
            String currentImagePath = databaseHelper.getWatchById(id).imagePath();

            // Input fields
            JTextField brandField = new JTextField(brand);
            JTextField modelField = new JTextField(model);
            JTextField priceField = new JTextField(String.valueOf(price));
            JTextField imageField = new JTextField(currentImagePath);
            JButton selectImageButton = new JButton("Select Image");

            // File chooser for selecting an image
            selectImageButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    imageField.setText(file.getAbsolutePath());
                }
            });

            // Layout
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Brand:"));
            panel.add(brandField);
            panel.add(new JLabel("Model:"));
            panel.add(modelField);
            panel.add(new JLabel("Price:"));
            panel.add(priceField);
            panel.add(new JLabel("Image Path:"));
            panel.add(imageField);
            panel.add(selectImageButton);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Edit Watch", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                // Retrieve updated values
                String newBrand = brandField.getText();
                String newModel = modelField.getText();
                double newPrice;

                try {
                    newPrice = Double.parseDouble(priceField.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String newImagePath = imageField.getText();

                // Update the watch in the database
                boolean success = databaseHelper.updateWatch(id, newBrand, newModel, newPrice, newImagePath, user);
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Watch updated successfully.");
                    loadWatchesIntoTable(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to update watch.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a watch to edit.");
        }
    }

    /**
     * Loads all watches for the current user into the table.
     */
    private void loadWatchesIntoTable() {
        List<Watch> watches = databaseHelper.getAllWatchesForUser(this.user);
        tableModel.setRowCount(0);  // Clear any existing rows

        for (Watch watch : watches) {
            tableModel.addRow(new Object[]{watch.id(), watch.brand(), watch.model(), watch.price()});
        }
    }

    // Show a dialog to add a new watch
    private void addWatch() {
        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField priceField = new JTextField();
        JLabel imageLabel = new JLabel("No image selected");

        // Button to allow the user to select an image
        JButton btnSelectImage = new JButton("Select Image");
        btnSelectImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File imageFile = ImageSelection.selectImageFile();  // Open file chooser
                if (imageFile != null) {
                    imageLabel.setText(imageFile.getAbsolutePath());  // Show selected image file name
                }
            }
        });

        Object[] message = {
            "Brand:", brandField,
            "Model:", modelField,
            "Price:", priceField,
            btnSelectImage,
            imageLabel
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add New Watch", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String brand = brandField.getText();
            String model = modelField.getText();
            double price = Double.parseDouble(priceField.getText());

            String imagePath = "";
            if (!imageLabel.getText().equals("No image selected")) {
                imagePath = imageLabel.getText();  // Get the selected image path
            }

            // Insert the new watch into the database with or without an image
            if (imagePath != null) {
                databaseHelper.insertWatch(brand, model, price, imagePath, user);
            }
            // Refresh the table to reflect the new data
            loadWatchesIntoTable();
        }
    }

    private void removeWatch() {
        int selectedRow = watchTable.getSelectedRow();
        if (selectedRow != -1) {
            // Get the ID of the selected watch (assumes ID is in the first column)
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            // Confirm deletion with the user
            int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this watch?", 
                                                            "Delete Watch", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                // Remove the watch from the database
                boolean success = databaseHelper.deleteWatchById(id);
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Watch deleted successfully.");
                    // Refresh the table to reflect the changes
                    loadWatchesIntoTable();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to delete watch.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a watch to delete.");
        }
    }

    private void displayImage(String imagePath) {
        try {
            // Load the image from the file path
            ImageIcon icon = new ImageIcon(imagePath);
            Image image = icon.getImage().getScaledInstance(250, 400, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
            
            // Set the image to the JLabel
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.WHITE);
            imageLabel.setIcon(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWatchSelection() {
        int selectedRow = watchTable.getSelectedRow();
        if (selectedRow != -1) {
            // Get the image path from the selected row (assuming it is stored in the 4th column)
            Integer selectedId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String imagePath = databaseHelper.getWatchById(selectedId).imagePath();
            
            if (imagePath != null && !imagePath.isEmpty()) {
                displayImage(imagePath);  // Display the image in the JLabel
            } else {
                imageLabel.setText("No image available");  // Default message if no image
                imageLabel.setIcon(null);  // Remove the image
            }
        }
    }

    public static void main(String[] args) {
        
    }

    public void run() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createTable() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Brand", "Model", "Price"}, 0);
        watchTable = new JTable(tableModel);
        watchTable.setRowHeight(30);
        watchTable.getTableHeader().setFont(font);
        watchTable.setShowGrid(false);
        watchTable.setIntercellSpacing(new Dimension(0,0));
        watchTable.setSelectionBackground(new Color(173, 216, 230));
        watchTable.setSelectionForeground(Color.BLACK);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(3, Comparator.comparingDouble(o -> Double.parseDouble(o.toString())));
        watchTable.setAutoCreateRowSorter(true);
        watchTable.setRowSorter(sorter);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add a button to insert a new watch
        JButton btnAddWatch = new JButton("Add Watch");
        btnAddWatch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addWatch();
            }
        });
        btnAddWatch.setFont(font);
        btnAddWatch.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnAddWatch.setFocusPainted(false);
        buttonPanel.add(btnAddWatch);

        JButton btnEditWatch = new JButton("Edit Watch");
        btnEditWatch.addActionListener(e -> editWatch());
        btnEditWatch.setFont(font);
        btnEditWatch.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnEditWatch.setFocusPainted(false);
        buttonPanel.add(btnEditWatch);

        JButton btnRemoveWatch = new JButton("Remove Watch");
        btnRemoveWatch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeWatch();
            }
        });
        btnRemoveWatch.setFont(font);
        btnRemoveWatch.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRemoveWatch.setFocusPainted(false);
        buttonPanel.add(btnRemoveWatch);
        return buttonPanel;
    }
}
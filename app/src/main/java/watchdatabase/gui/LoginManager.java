package watchdatabase.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import watchdatabase.dbhelpers.UserDatabaseHelper;
import watchdatabase.models.LoginResult;

public class LoginManager extends JDialog {

    private UserDatabaseHelper userDatabaseHelper;
    private JLabel statusLabel;
    private JTextField userText;
    private JPasswordField passText;
    private LoginResult loginResult = new LoginResult(null, false);
    private Font font = new Font("SansSerif", Font.BOLD, 16);

    public LoginManager (String dbUrl) {
        super((JFrame) null, "Watch Manager - Login", true);
        initializeDb(dbUrl);
        initializeGUI();
    }
    
    private void initializeDb(String dbUrl) {
        userDatabaseHelper = new UserDatabaseHelper(dbUrl);
    }

    private void initializeGUI() {
        //setSize(300, 300);
        setLayout(new GridLayout(3, 2));

        JPanel inputPanel = new JPanel();
        //inputPanel.setPreferredSize(new Dimension(200, 150));
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel userLabel = new JLabel("Username:");
        userText = new JTextField();
        userText.setColumns(15);
        userText.setFont(font);
        inputPanel.add(userLabel);
        inputPanel.add(userText);
        
        JLabel passLabel = new JLabel("Password:");
        passText = new JPasswordField();
        passText.setFont(font);
        passText.setColumns(15);
        inputPanel.add(passLabel);
        inputPanel.add(passText);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton loginButton = new JButton("Login");
        
        loginButton.addActionListener(createLoginButtonActionListener());

        JButton createUserButton = new JButton("Create User");
        createUserButton.addActionListener(createCreateUserButtonActionListener());
        
        statusLabel  = new JLabel();
        statusLabel.setPreferredSize(new Dimension(300, 15));

        buttonPanel.add(loginButton);
        buttonPanel.add(createUserButton);
        buttonPanel.add(statusLabel);

        add(inputPanel);
        add(buttonPanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean verifyUserPassword(String user, String password) {
        try {
            return userDatabaseHelper.verifyUserPassword(user, password);
        } catch (Exception e) {
            statusLabel.setText("Error when logging in: " + e.getMessage());
            return false;
        }
    }

    public LoginResult getLoginResult() {
        return loginResult;
    }

    private ActionListener createCreateUserButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUsername = JOptionPane.showInputDialog(LoginManager.this, "Enter new username:");
                if (newUsername != null && !newUsername.trim().isEmpty()) {
                    String newPassword = JOptionPane.showInputDialog(LoginManager.this, "Enter new password:");
                    boolean passWordOK = false;

                    if (newPassword == null) { // If user cancels
                        return;
                    }

                    while (!passWordOK) {
                        String newPasswordAgain = JOptionPane.showInputDialog(LoginManager.this, "Repeat new password:");
                        
                        if (newPasswordAgain == null) { // If user cancels
                            return;
                        }
                       
                        if (newPasswordAgain.equals(newPassword)) {
                            passWordOK = true;
                        } else {
                            JOptionPane.showMessageDialog(LoginManager.this, "Password repetition failed! Try again.");
                        }
                    }
                    
                    if (!newPassword.trim().isEmpty()) {
                        if (userDatabaseHelper.addUser(newUsername, newPassword)) {
                            JOptionPane.showMessageDialog(LoginManager.this, "User added successfully!");
                        } else {
                            JOptionPane.showMessageDialog(LoginManager.this, "Adding new user failed!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(LoginManager.this, "Password cannot be empty.");
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginManager.this, "Username cannot be empty.");
                }
            }
        };
    }

    private ActionListener createLoginButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passText.getPassword());

                if (verifyUserPassword(username, password)) {
                    statusLabel.setText("Login successful!");
                    JOptionPane.showMessageDialog(LoginManager.this, "Login successful!");
                    loginResult = new LoginResult(username, true);
                    dispose();
                } else {
                    statusLabel.setText("Invalid credentials. Try again!");
                }
            }
        };
    }
}
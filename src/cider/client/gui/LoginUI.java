package cider.client.gui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class LoginUI
{
	public String currentDir = "src\\cider\\client\\gui\\"; //this is from MainWindow.java

    private JFrame login;
    private JFrame connecting;
    
    JCheckBox chkRemember ;

    void displayLogin()
    {

        // Setup JFrame
        login = new JFrame();
        login.setDefaultCloseOperation(login.EXIT_ON_CLOSE);
        login.setTitle("CIDEr - Login");
        login.setResizable(false);
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }

        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        Box box = Box.createVerticalBox();
        
        
        ActionListener aL = newAction();

        // CIDEr Image
        URL u = this.getClass().getResource("loginimage.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        box.add(lblImage);

        // Username, Password, Address, Port Labels/Textboxes
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        GroupLayout infoLayout = new GroupLayout(infoPanel);
        infoPanel.setLayout(infoLayout);
        infoLayout.setAutoCreateGaps(true);
        infoLayout.setAutoCreateContainerGaps(true);

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        JLabel lblHost = new JLabel("Host Address:  ");
        JLabel lblPort = new JLabel("Port Number:");
        JTextField txtUsername = new JTextField(13);
        JPasswordField txtPassword = new JPasswordField(13);
        JTextField txtHost = new JTextField(13);
        JTextField txtPort = new JTextField(13);

        GroupLayout.SequentialGroup leftToRight = infoLayout
                .createSequentialGroup();
        GroupLayout.ParallelGroup leftColumn = infoLayout.createParallelGroup();
        leftColumn.addComponent(lblUser);
        leftColumn.addComponent(lblPass);
        leftColumn.addComponent(lblHost);
        leftColumn.addComponent(lblPort);
        GroupLayout.ParallelGroup rightColumn = infoLayout
                .createParallelGroup();
        rightColumn.addComponent(txtUsername);
        rightColumn.addComponent(txtPassword);
        rightColumn.addComponent(txtHost);
        rightColumn.addComponent(txtPort);
        leftToRight.addGroup(leftColumn);
        leftToRight.addGroup(rightColumn);

        GroupLayout.SequentialGroup topToBottom = infoLayout
                .createSequentialGroup();
        GroupLayout.ParallelGroup row1 = infoLayout.createParallelGroup();
        row1.addComponent(lblUser);
        row1.addComponent(txtUsername);
        GroupLayout.ParallelGroup row2 = infoLayout.createParallelGroup();
        row2.addComponent(lblPass);
        row2.addComponent(txtPassword);
        GroupLayout.ParallelGroup row3 = infoLayout.createParallelGroup();
        row3.addComponent(lblHost);
        row3.addComponent(txtHost);
        GroupLayout.ParallelGroup row4 = infoLayout.createParallelGroup();
        row4.addComponent(lblPort);
        row4.addComponent(txtPort);
        topToBottom.addGroup(row1);
        topToBottom.addGroup(row2);
        topToBottom.addGroup(row3);
        topToBottom.addGroup(row4);

        infoLayout.setHorizontalGroup(leftToRight);
        infoLayout.setVerticalGroup(topToBottom);
        box.add(infoPanel);

        // Remember Server Checkbox
        chkRemember = new JCheckBox("Remember Server Details");
        chkRemember.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        chkRemember.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(chkRemember);

        // Submit Button
        JButton btnSubmit = new JButton("Submit");
        btnSubmit.addActionListener(aL);
        btnSubmit.setMargin(new Insets(7, 30, 7, 30));
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(btnSubmit);

        // Finalise JFrame
        main.add(box);
        login.add(main);
        login.pack();
        login.setLocationByPlatform(true);
        login.setVisible(true);
    }
    
    public ActionListener newAction()
    {
    	ActionListener AL = new ActionListener() 
    	{
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			String action = e.getActionCommand();
    			if (chkRemember.isSelected() == true)
    			{
    				saveLoginDetails("string", "2", "blah", "troll");
        			//saveLoginDetails(txtUsername, txtPassword, txtHost, txtPort)
    			}
    			else
    			{
    				System.out.println("Don't save");
    			}
    			showConnectBox();
    		}
    	};
    	return AL;
    } 

    void saveLoginDetails(String txtUsername, String txtPassword, String txtHost, String txtPort)
    {
    	// TODO password encryption / encrypt everything
    	System.out.println(txtUsername);
    	System.out.println(currentDir);
    	
    	try
    	{
    		FileWriter fstream = new FileWriter(currentDir + "login.txt");
    		BufferedWriter out = new BufferedWriter(fstream);
    		out.write(txtUsername + txtPassword + txtHost + txtPort);
    		out.close();
    	}
    	catch (IOException e)
    	{
    		System.err.println("Error: " + e.getMessage());
    		System.exit(0);
    	}
    }
    
    void showConnectBox()
    {
    	login.setVisible(false);
    	
    	// Create New JFrame
    	connecting = new JFrame();
        connecting.setDefaultCloseOperation(login.EXIT_ON_CLOSE);
        connecting.setTitle("CIDEr - Connecting");
        connecting.setResizable(false);
        
        JPanel panel = new JPanel();
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }
        
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        Box box = Box.createHorizontalBox();
        
        // Connecting Image
        URL u = this.getClass().getResource("connectingimage.gif");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        box.add(lblImage);
        
        // Status Text
        JLabel lblStatus = new JLabel("Connecting to the server...");
        box.add(lblStatus);
        
        // Finalise JFrame
        panel.add(box);
        connecting.add(panel);
        connecting.pack();
        int x = (int) (login.getX() + login.getWidth()/2);
        int y = (int) (login.getY() + login.getHeight()/2);
        connecting.setLocation(x - connecting.getWidth()/2, y - connecting.getHeight()/3);
        connecting.setVisible(true);
        
        connect();
    }

    void connect()
    {
    	// TODO Connection Code
    	// On connect, close login and connect JFrames, run MainWindow
    }
    
    public static void main(String[] args)
    {

        LoginUI ui = new LoginUI();
        ui.displayLogin();

    }
}

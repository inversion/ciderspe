/**
 *  CIDER Admin Tools - Allows user to create config and profile files
 *  @author  Miles
 *
 */

package cider.client.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class AdminTools
{
    public String currentDir = "src\\cider\\client\\gui\\";
    static JFrame admintool; 


    // Admin Tools fields
    JTextField txtServiceName;
    JTextField txtHost;
    JTextField txtPort;
    JTextField txtChatName;
    JTextField txtBotName;
    JPasswordField txtBotPass;
    JTextField txtCheckName;
    JPasswordField txtCheckPass;
    JTextField txtSourceDir;
    JTextField txtProfileDir;
    JTextField txtChatHistDir;
    JTextField txtName;
    JPasswordField txtPass;
    
    MainWindow program;

    String errmsg;

    public void displayAdTools()
    {
        admintool = new JFrame();
        admintool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        admintool.setTitle("CIDEr - Admin Tools");
        admintool.setResizable(false);
        try
        {
            UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
        }
        catch (Exception e)
        {
            System.err
                    .println("Note: Can't use noire look and feel, add JTattoo.jar to your build path.");
            e.printStackTrace();
        }

        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        Box box = Box.createVerticalBox();

        ActionListener aL = newAction();

        // CIDEr Image
        URL u = this.getClass().getResource("adtoolimage.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        box.add(lblImage);

        
        // JFrame icon
        URL x = this.getClass().getResource("icon.png");
        ImageIcon image2 = new ImageIcon(x);
        Image test = image2.getImage();
        admintool.setIconImage(test);

        // Labels for all input info
        JPanel ConfigPanel = new JPanel();
        ConfigPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        GroupLayout ConfigLayout = new GroupLayout(ConfigPanel);
        ConfigPanel.setLayout(ConfigLayout);
        ConfigLayout.setAutoCreateGaps(true);
        ConfigLayout.setAutoCreateContainerGaps(true);
        JLabel lblServiceName = new JLabel("Service Name: ");
        JLabel lblHost = new JLabel("Host Address:  ");
        JLabel lblPort = new JLabel("Port Number:");
        JLabel lblChatName = new JLabel("Chatroom Name: ");
        JLabel lblBotName = new JLabel("Bot Username: ");
        JLabel lblBotPass = new JLabel("Bot Password: ");
        JLabel lblCheckName = new JLabel("Checker Name: ");
        JLabel lblCheckPass = new JLabel("Checker Password: ");
        JLabel lblSourceDir = new JLabel("Source Directory: ");
        JLabel lblProfileDir = new JLabel("Profile Directory: ");
        JLabel lblChatHistDir = new JLabel("Chat History Directory: ");
        txtServiceName = new JTextField(13);
        txtHost = new JTextField(13);
        txtPort = new JTextField(13);
        txtChatName = new JTextField(13);
        txtBotName = new JTextField(13);
        txtBotPass = new JPasswordField(13);
        txtCheckName = new JTextField(13);
        txtCheckPass = new JPasswordField(13);
        txtSourceDir = new JTextField(13);
        txtProfileDir = new JTextField(13);
        txtChatHistDir = new JTextField(13);
              

        GroupLayout.SequentialGroup leftToRight = ConfigLayout
                .createSequentialGroup();
        GroupLayout.ParallelGroup leftColumn = ConfigLayout.createParallelGroup();
        leftColumn.addComponent(lblHost);
        leftColumn.addComponent(lblServiceName);
        leftColumn.addComponent(lblPort);
        leftColumn.addComponent(lblChatName);
        leftColumn.addComponent(lblBotName);
        leftColumn.addComponent(lblBotPass);
        leftColumn.addComponent(lblCheckName);
        leftColumn.addComponent(lblCheckPass);
        leftColumn.addComponent(lblSourceDir);
        leftColumn.addComponent(lblProfileDir);
        leftColumn.addComponent(lblChatHistDir);
        GroupLayout.ParallelGroup rightColumn = ConfigLayout
                .createParallelGroup();
        rightColumn.addComponent(txtHost);
        rightColumn.addComponent(txtServiceName);
        rightColumn.addComponent(txtPort);
        rightColumn.addComponent(txtChatName);
        rightColumn.addComponent(txtBotName);
        rightColumn.addComponent(txtBotPass);
        rightColumn.addComponent(txtCheckName);
        rightColumn.addComponent(txtCheckPass);
        rightColumn.addComponent(txtSourceDir);
        rightColumn.addComponent(txtProfileDir);
        rightColumn.addComponent(txtChatHistDir);
        leftToRight.addGroup(leftColumn);
        leftToRight.addGroup(rightColumn);

        GroupLayout.SequentialGroup topToBottom = ConfigLayout
                .createSequentialGroup();
        GroupLayout.ParallelGroup row1 = ConfigLayout.createParallelGroup();
        row1.addComponent(lblHost);
        row1.addComponent(txtHost);
        GroupLayout.ParallelGroup row2 = ConfigLayout.createParallelGroup();
        row2.addComponent(lblServiceName);
        row2.addComponent(txtServiceName);
        GroupLayout.ParallelGroup row3 = ConfigLayout.createParallelGroup();
        row3.addComponent(lblPort);
        row3.addComponent(txtPort);
        GroupLayout.ParallelGroup row4 = ConfigLayout.createParallelGroup();
        row4.addComponent(lblChatName);
        row4.addComponent(txtChatName);
        GroupLayout.ParallelGroup row5 = ConfigLayout.createParallelGroup();
        row5.addComponent(lblBotName);
        row5.addComponent(txtBotName);
        GroupLayout.ParallelGroup row6 = ConfigLayout.createParallelGroup();
        row6.addComponent(lblBotPass);
        row6.addComponent(txtBotPass);
        GroupLayout.ParallelGroup row7 = ConfigLayout.createParallelGroup();
        row7.addComponent(lblCheckName);
        row7.addComponent(txtCheckName);
        GroupLayout.ParallelGroup row8 = ConfigLayout.createParallelGroup();
        row8.addComponent(lblCheckPass);
        row8.addComponent(txtCheckPass);
        GroupLayout.ParallelGroup row9 = ConfigLayout.createParallelGroup();
        row9.addComponent(lblSourceDir);
        row9.addComponent(txtSourceDir);
        GroupLayout.ParallelGroup row10 = ConfigLayout.createParallelGroup();
        row10.addComponent(lblProfileDir);
        row10.addComponent(txtProfileDir);
        GroupLayout.ParallelGroup row11 = ConfigLayout.createParallelGroup();
        row11.addComponent(lblChatHistDir);
        row11.addComponent(txtChatHistDir);
        topToBottom.addGroup(row1);
        topToBottom.addGroup(row2);
        topToBottom.addGroup(row3);
        topToBottom.addGroup(row4);
        topToBottom.addGroup(row5);
        topToBottom.addGroup(row6);
        topToBottom.addGroup(row7);
        topToBottom.addGroup(row8);
        topToBottom.addGroup(row9);
        topToBottom.addGroup(row10);
        topToBottom.addGroup(row11);
        
        ConfigLayout.setHorizontalGroup(leftToRight);
        ConfigLayout.setVerticalGroup(topToBottom);
        box.add(ConfigPanel);


        // Create Config File Button
        JButton btnCreateConfig = new JButton("Create Config Files");
        btnCreateConfig.setMargin(new Insets(7, 30, 7, 30));
        btnCreateConfig.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreateConfig.addActionListener(aL);
        btnCreateConfig.setActionCommand("Config");
        box.add(btnCreateConfig);
        
        JPanel ProfilePanel = new JPanel();
        ProfilePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        GroupLayout ProfileLayout = new GroupLayout(ProfilePanel);
        ProfilePanel.setLayout(ProfileLayout);
        ProfileLayout.setAutoCreateGaps(true);
        ProfileLayout.setAutoCreateContainerGaps(true);
        
        JLabel lblName = new JLabel("Name: ");
        JLabel lblPass = new JLabel("Password: ");
        
        txtName = new JTextField(13);
        txtPass = new JPasswordField(13);
        
        GroupLayout.SequentialGroup lToRight = ProfileLayout
        		.createSequentialGroup();
        GroupLayout.ParallelGroup lColumn = ProfileLayout.createParallelGroup();
        
        lColumn.addComponent(lblName);
        lColumn.addComponent(lblPass);
        
        GroupLayout.ParallelGroup rColumn = ProfileLayout
                .createParallelGroup();
        
        rColumn.addComponent(txtName);
        rColumn.addComponent(txtPass);
        lToRight.addGroup(lColumn);
        lToRight.addGroup(rColumn);
        
        GroupLayout.SequentialGroup tToBottom = ProfileLayout
		        .createSequentialGroup();
		GroupLayout.ParallelGroup Prow1 = ProfileLayout.createParallelGroup();
		Prow1.addComponent(lblName);
		Prow1.addComponent(txtName);
		GroupLayout.ParallelGroup Prow2 = ProfileLayout.createParallelGroup();
		Prow2.addComponent(lblPass);
		Prow2.addComponent(txtPass);

        tToBottom.addGroup(Prow1);
        tToBottom.addGroup(Prow2);
        
        ProfileLayout.setHorizontalGroup(lToRight);
        ProfileLayout.setVerticalGroup(tToBottom);
        
        box.add(ProfilePanel);
        
        // Create Profile Button
        JButton btnCreateProfile = new JButton("Create Profile");
        btnCreateProfile.setMargin(new Insets(7, 30, 7, 30));
        btnCreateProfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreateProfile.addActionListener(aL);
        btnCreateProfile.setActionCommand("Profile");
        box.add(btnCreateProfile);
        

        // Finalise JFrame
        main.add(box);
        admintool.add(main);
        admintool.pack();
        admintool.setLocationByPlatform(true);
        admintool.setVisible(true);
        
    }
    
    public ActionListener newAction()
    {
        ActionListener AL = new ActionListener()
        {            
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	String action = e.getActionCommand();
            	if (action.equals("Profile")) {
                    checkProfFields();
            	}
                else if (action.equals("Config"))
                {
                	checkConfigFields();
                }
            }
        };
        return AL;
    }
    
    void checkProfFields()
    {
    	CreateProfile(txtName.getText(), new String(txtPass.getPassword()));
    }
    
    void CreateProfile(String txtName, String txtPass)
    {
    	try
    	{
    		FileWriter fstream3 = new FileWriter(currentDir + "Profile.txt");
    		BufferedWriter out3 = new BufferedWriter(fstream3);
    		out3.write(txtName + " chars: 0 timespent: 0  idletime: 0 lastonline: Never! colour: 150 150 150 fontSize: 14");
    		out3.close();
    		FileWriter fstream4 = new FileWriter(currentDir + "LoginProfile.txt");
    		BufferedWriter out4 = new BufferedWriter(fstream4);
    		out4.write("NAME=" + txtName);
    		out4.newLine();
    		out4.write("PASSWORD=" + txtPass);
    		out4.close();
    	}
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }
    	
    void checkConfigFields()
    {
    	CreateConfig(txtHost.getText(),
                txtServiceName.getText(), txtPort.getText(),
                txtChatName.getText(), txtBotName.getText(),
                new String(txtBotPass.getPassword()),
                txtCheckName.getText(), new String(txtCheckPass.getPassword()),
                txtSourceDir.getText(), txtProfileDir.getText(),
                txtChatHistDir.getText());
    }
    
    void CreateConfig(String txtHost, String txtServiceName,
            String txtPort, String txtChatName, String txtBotName,
            String txtBotPass, String txtCheckName, String txtCheckPass,
            String txtSourceDir, String txtProfileDir, String txtChatHistDir)
    {
        try
        {
            FileWriter fstream = new FileWriter(currentDir + "BotTEST.conf");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("HOST=" + txtHost);
            out.newLine();
            out.write("SERVICE_NAME=" + txtServiceName);
            out.newLine();
            out.write("PORT=" + txtPort);
            out.newLine();
            out.newLine();
            out.write("CHATROOM_NAME=" + txtChatName);
            out.newLine();
            out.newLine();
            out.write("BOT_USERNAME=" + txtBotName);
            out.newLine();
            out.write("BOT_PASSWORD=" + txtBotPass);
            out.newLine();
            out.newLine();
            out.write("CHECKER_USERNAME=" + txtCheckName);
            out.newLine();
            out.write("CHECKER_PASSWORD=" + txtCheckPass);
            out.newLine();
            out.newLine();
            out.write("SOURCE_DIR=" + txtSourceDir);
            out.newLine();
            out.newLine();
            out.write("PROFILE_DIR=" + txtProfileDir);
            out.newLine();
            out.newLine();
            out.write("CHAT_HISTORY_DIR=" + txtChatHistDir);
            out.close();
            FileWriter fstream2 = new FileWriter(currentDir + "ClientTEST.conf");
            BufferedWriter out2 = new BufferedWriter(fstream2);
            out2.write("BOT_USERNAME=" + txtBotName);
            out2.newLine();
            out2.newLine();
         	out2.write("CHATROOM_NAME=" + txtChatName);
         	out2.newLine();
         	out2.write("CHECKER_USERNAME=" + txtCheckName);
            out2.close();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }
    

    public static void main(String[] args)
    {
        AdminTools ui = new AdminTools();

        try
        {
            ui.displayAdTools();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

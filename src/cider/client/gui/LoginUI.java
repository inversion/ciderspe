/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cider.client.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

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
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.jivesoftware.smack.XMPPException;

import cider.common.network.client.Client;
import cider.common.processes.Profile;
import cider.common.processes.SiHistoryFiles;
import cider.shared.ClientSharedComponents;


public class LoginUI
{
	private int Retrieved = 0;
	private int NotPort;
    public String currentDir = "src\\cider\\client\\gui\\"; // this is from
                                                            // MainWindow.java

    static JFrame login; // TODO changed from private to static?
    private JWindow connecting;

    // Default values for login box
    public static final String DEFAULT_HOST = "xmpp.org.uk";
    // TODO: Should be numeric really
    public static final String DEFAULT_PORT = "5222";
    public static final String DEFAULT_SERVICE_NAME = "xmpp.org.uk";
    

    // Login box fields
    JTextField txtUsername;
    JPasswordField txtPassword;
    JTextField txtServiceName;
    JTextField txtHost;
    JTextField txtPort;

    MainWindow program;

    JCheckBox chkRemember;

    String errmsg;
    private Thread mainWindowThread;
    private Thread connectBoxThread;
    
    CiderApplication ciderApplication;

    public LoginUI(CiderApplication ciderApplication)
    {
        this.ciderApplication = ciderApplication;
    }
    
    public void displayLogin()
    {
        //splashScreen();
        login = new JFrame();
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setTitle("CIDEr - Login");
        login.setResizable(false);
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
        URL u = this.getClass().getResource("loginimage.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        box.add(lblImage);

        // JFrame icon
        URL x = this.getClass().getResource("icon.png");
        ImageIcon image2 = new ImageIcon(x);
        Image test = image2.getImage();
        login.setIconImage(test);

        // Username, Password, Address, Port Labels/Textboxes
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        GroupLayout infoLayout = new GroupLayout(infoPanel);
        infoPanel.setLayout(infoLayout);
        infoLayout.setAutoCreateGaps(true);
        infoLayout.setAutoCreateContainerGaps(true);

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        JLabel lblServiceName = new JLabel("Service Name: ");
        JLabel lblHost = new JLabel("Host Address:  ");
        JLabel lblPort = new JLabel("Port Number:");
        txtUsername = new JTextField(13);
        txtPassword = new JPasswordField(13);
        txtServiceName = new JTextField(13);
        txtServiceName.setText(DEFAULT_SERVICE_NAME);
        txtHost = new JTextField(13);
        txtHost.setText(DEFAULT_HOST);
        txtPort = new JTextField(13);
        txtPort.setText(DEFAULT_PORT);

        // Add enter key listeners for all fields.
        JTextField[] fields = {txtUsername, txtServiceName, txtHost, txtPort};
        
        for( JTextField field : fields )
            field.addKeyListener(new KeyAdapter()
            {
                public void keyPressed(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        checkLogin();
                    }
                }
            });

        txtPassword.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    checkLogin();
                }
            }
        });

        GroupLayout.SequentialGroup leftToRight = infoLayout
                .createSequentialGroup();
        GroupLayout.ParallelGroup leftColumn = infoLayout.createParallelGroup();
        leftColumn.addComponent(lblUser);
        leftColumn.addComponent(lblPass);
        leftColumn.addComponent(lblServiceName);
        leftColumn.addComponent(lblHost);
        leftColumn.addComponent(lblPort);
        GroupLayout.ParallelGroup rightColumn = infoLayout
                .createParallelGroup();
        rightColumn.addComponent(txtUsername);
        rightColumn.addComponent(txtPassword);
        rightColumn.addComponent(txtServiceName);
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
        row3.addComponent(lblServiceName);
        row3.addComponent(txtServiceName);
        GroupLayout.ParallelGroup row4 = infoLayout.createParallelGroup();
        row4.addComponent(lblHost);
        row4.addComponent(txtHost);
        GroupLayout.ParallelGroup row5 = infoLayout.createParallelGroup();
        row5.addComponent(lblPort);
        row5.addComponent(txtPort);
        topToBottom.addGroup(row1);
        topToBottom.addGroup(row2);
        topToBottom.addGroup(row3);
        topToBottom.addGroup(row4);
        topToBottom.addGroup(row5);

        infoLayout.setHorizontalGroup(leftToRight);
        infoLayout.setVerticalGroup(topToBottom);
        box.add(infoPanel);

        // Remember Server Checkbox
        chkRemember = new JCheckBox("Remember Server Details");
        chkRemember.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        chkRemember.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(chkRemember);

        fetchLogin();
        if (Retrieved == 0)
        {
        	GetLogin();
        }

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
        
        makeLocalHistoryFolder();
    }
    
    
    private static void makeLocalHistoryFolder()
    {
        File f;
        f = new File(SiHistoryFiles.localEventFolderPath);
        if (!f.exists())
            f.mkdirs(); 
    }
    

    private void splashScreen()
    {
        // TODO: Can't we just put a nice image into the connecting box and eliminate the need for this? (Andrew)
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame f = new JFrame();
        f.setBounds((dim.width - 800) / 2, (dim.height - 600) / 2, 800, 600);
        f.setUndecorated(true);
        f.setVisible(true);

        Container layout = f.getContentPane();
        layout.setLayout(new GridLayout(5, 1));

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            
            e.printStackTrace();
        }
        f.dispose();
    }

    /**
     * Checks for login.txt file and fills in the details if found
     * 
     * @author Alex
     */
    private void fetchLogin()
    {
        try
        {
            FileReader fstream = new FileReader(currentDir + "login.txt");
            BufferedReader in = new BufferedReader(fstream);

            String line;
            int i = 0;
            String[] text = new String[5];

            while ((line = in.readLine()) != null)
            {
                StringTokenizer token = new StringTokenizer(line, ",");
                while (token.hasMoreTokens())
                {
                    text[i] = passwordEncrypt.decrypt(token.nextToken());
                    i++;
                }
            }
            in.close();
            Retrieved = 1;
            txtUsername.setText(text[0]);
            txtPassword.setText(text[1]);
            txtServiceName.setText(text[2]);
            txtHost.setText(text[3]);
            txtPort.setText(text[4]);
            chkRemember.setSelected(true);
        }
        catch (FileNotFoundException e)
        {
            // System.out.println("File not found");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // System.out.println("File not found");
            e.printStackTrace();
        }
    }

    void GetLogin()
    {
    	try
        {
            FileReader fstream = new FileReader(currentDir + "BotTEST.conf");
            BufferedReader in = new BufferedReader(fstream);

            String line;
            int i = 0;
            String[] text = new String[6];

            while ((line = in.readLine()) != null)
            {
                StringTokenizer token = new StringTokenizer(line, "=");
                while ((token.hasMoreTokens()) && (i<6))
                {
                    text[i] = (token.nextToken());
                    i++;
                }
            }
            in.close();

            txtServiceName.setText(text[1]);
            txtHost.setText(text[3]);
            txtPort.setText(text[5]);
        }
        catch (FileNotFoundException e)
        {
            // System.out.println("File not found");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // System.out.println("File not found");
            e.printStackTrace();
        }
    }
    
    void checkLogin()
    {
        // TODO: Can we have some commenting on what methods actually do please
        // GUI people
        if (chkRemember.isSelected() == true)
        {
            saveLoginDetails(txtUsername.getText(),
                    new String(txtPassword.getPassword()),
                    txtServiceName.getText(), txtHost.getText(),
                    txtPort.getText());
        }
        else
        {
            String fileName = currentDir + "login.txt";
            File file = new File(fileName);

            try
            {
                file.delete();
            }
            catch (IllegalArgumentException err)
            {
                System.out.println("Deletion failed: " + fileName);
                err.printStackTrace();
            }
        }
        this.mainWindowThread = connectMainWindow();
        if (this.mainWindowThread != null)
        {
            this.connectBoxThread = connectBox();

            // Run connect box and main window thread in parallel
            this.connectBoxThread.start();
            this.mainWindowThread.start();
        }

        else 
        {
        	if (NotPort == 0)
        		displayLogin();
            // program.killWindow();
        }
    }

    public ActionListener newAction()
    {
        ActionListener AL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkLogin();
            }
        };
        return AL;
    }

    void saveLoginDetails(String txtUsername, String txtPassword,
            String txtServiceName, String txtHost, String txtPort)
    {
        // TODO password encryption / encrypt everything
        System.out.println("Saving login details for '" + txtUsername
                + "' in directory: " + currentDir);

        try
        {
            FileWriter fstream = new FileWriter(currentDir + "login.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(passwordEncrypt.encrypt(txtUsername) + ","
                    + passwordEncrypt.encrypt(txtPassword) + ","
                    + passwordEncrypt.encrypt(txtServiceName) + ","
                    + passwordEncrypt.encrypt(txtHost) + ","
                    + passwordEncrypt.encrypt(txtPort));
            out.close();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    void splashFrame(Graphics2D g)
    {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(100, 100, 100, 100);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString("Connecting...", 5, 50);
    }

    Thread connectBox()
    {

        Runnable runnable = new Runnable()
        {

            @Override
            public void run()
            {
                // Create New JFrame
                connecting = new JWindow();
                connecting.setLocationRelativeTo(LoginUI.login);
                //connecting.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //connecting.setTitle("CIDEr - Connecting");
                //connecting.setResizable(false);
                connecting.toFront();

                login.setVisible(false);

                JPanel panel = new JPanel();
                /*
                 * panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                Box box = Box.createHorizontalBox();
                
                // Status Text
                JLabel lblStatus = new JLabel("Connecting to the server...");
                box.add(lblStatus);
                */          
                                
                URL u = this.getClass().getResource("splash.gif");
                ImageIcon image = new ImageIcon(u);
                JLabel lblImage = new JLabel(image);
                
                // Connecting Image
                /*URL u1 = this.getClass().getResource("connectingimage.gif");
                ImageIcon image1 = new ImageIcon(u1);
                JLabel lblImage1 = new JLabel(image1);
                lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));*/
                //box.add(lblImage1);            

                // Finalise JFrame
                //panel.add(box);
                panel.add(lblImage);
               //panel.add(lblImage1);
                connecting.add(panel);
                connecting.pack();
                //int x = (int) (login.getX() + login.getWidth() / 2);
                //int y = (int) (login.getY() + login.getHeight() / 2);
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                connecting.setLocation((dim.width - lblImage.getWidth()) / 2, (dim.height - lblImage.getHeight()) / 2);
                //connecting.setLocation(x - connecting.getWidth() / 2, y - connecting.getHeight() / 3);
                connecting.setVisible(true);
                connecting.repaint();
                // connecting.setUndecorated(true);
            }

        };

        return new Thread(runnable);

    }

    Thread connectMainWindow()
    {
        // On connect, close login and connect JFrames, run MainWindow

        // System.out.println(passwordEncrypt.encrypt(new
        // String(txtPassword.getPassword())));
    	NotPort = 0;
        final Client client;
        try
        {

            final ClientSharedComponents sharedComponents = new ClientSharedComponents();
            sharedComponents.profile = new Profile( txtUsername.getText() );

            // TODO: Recommended to zero bytes of password after use
            // TODO: Check that fields aren't null/validation stuff
            client = new Client(txtUsername.getText(), new String(
                    txtPassword.getPassword()), txtHost.getText(),
                    Integer.parseInt(txtPort.getText()),
                    txtServiceName.getText(), this, sharedComponents);

            Runnable runner = new Runnable()
            {

                @Override
                public void run()
                {
                    try
                    {
                        if (!client.attemptConnection())
                        {
                            errmsg = "Bot is not online or connection to bot timed out.";
                            JOptionPane.showMessageDialog(connecting, errmsg);
                            connecting.setVisible(false);
                            connecting.dispose();
                            ciderApplication.restarted();
                            ciderApplication = null;
                        }
                        else
                        {
                            program = new MainWindow(txtUsername.getText(),
                                    new String(txtPassword.getPassword()),
                                    txtHost.getText(), Integer.parseInt(txtPort
                                            .getText()),
                                    txtServiceName.getText(), client,
                                    LoginUI.this, sharedComponents);

                            connecting.setVisible(false);
                            connecting.dispose();
                            program.startApplication(login, CiderApplication.debugApp);
                        }
                    }
                    catch (XMPPException e)
                    {
                        e.printStackTrace();
                        errmsg = "Incorrect Username or Password";
                        JOptionPane.showMessageDialog(connecting, errmsg);
                        connecting.setVisible(false);
                        connecting.dispose();
                        ciderApplication.restarted();
                        ciderApplication = null;
                    }
                }

            };

            return new Thread(runner);
        }
        catch (NumberFormatException e)
        {
            errmsg = "Invalid port number";
            JOptionPane.showMessageDialog(connecting, errmsg);
            e.printStackTrace();
            NotPort = 1;
        }
        return null;
    }

    public void logout()
    {
        Toolkit.getDefaultToolkit().removeAWTEventListener( this.program.activityListener );
        this.program.shared.idleTimer.stop();
        this.program.client.disconnect();
        this.program.killWindow();
        this.ciderApplication.restarted();
        ciderApplication = null;
    }

}

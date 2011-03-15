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
import javax.swing.UIManager;

import org.jivesoftware.smack.XMPPException;

import cider.common.network.client.Client;
import cider.shared.ClientSharedComponents;

public class LoginUI
{
    public String currentDir = "src\\cider\\client\\gui\\"; // this is from
                                                            // MainWindow.java

    static JFrame login; // TODO changed from private to static?
    private JFrame connecting;

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

    public void displayLogin()
    {
        // splashScreen();
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
        // TODO: Make this numeric?
        txtPort = new JTextField(13);
        txtPort.setText(DEFAULT_PORT);

        txtUsername.addKeyListener(new KeyAdapter()
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

        txtServiceName.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    checkLogin();
                }
            }
        });

        txtHost.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    checkLogin();
                }
            }
        });

        txtPort.addKeyListener(new KeyAdapter()
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

    private void splashScreen()
    {
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
            // TODO Auto-generated catch block
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
        Thread mainWindowThread = connectMainWindow();
        if (mainWindowThread != null)
        {
            Thread connectBoxThread = connectBox();

            // Run connect box and main window thread in parallel
            connectBoxThread.start();
            mainWindowThread.start();
        }

        else
        {
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
                connecting = new JFrame();
                connecting.setLocationRelativeTo(LoginUI.login);
                connecting.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                connecting.setTitle("CIDEr - Connecting");
                connecting.setResizable(false);
                connecting.toFront();

                login.setVisible(false);

                JPanel panel = new JPanel();
                /*
                 * try {
                 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName
                 * ()); } catch (Exception e) { }
                 */

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
                int x = (int) (login.getX() + login.getWidth() / 2);
                int y = (int) (login.getY() + login.getHeight() / 2);
                connecting.setLocation(x - connecting.getWidth() / 2, y
                        - connecting.getHeight() / 3);
                connecting.setVisible(true);
                connecting.repaint();
                // connecting.setUndecorated(true);
            }

        };

        return new Thread(runnable);

    }

    Thread connectMainWindow()
    {
        // TODO Connection Code
        // On connect, close login and connect JFrames, run MainWindow

        // System.out.println(passwordEncrypt.encrypt(new
        // String(txtPassword.getPassword())));
        final Client client;
        try
        {

            final ClientSharedComponents sharedComponents = new ClientSharedComponents();

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
                            errmsg = "Bot is not online";
                            JOptionPane.showMessageDialog(connecting, errmsg);
                            connecting.setVisible(false);
                            connecting.dispose();
                            System.exit(1);
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
                            program.startApplication(login);
                        }
                    }
                    catch (XMPPException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            };

            return new Thread(runner);
        }
        catch (NumberFormatException e)
        {
            errmsg = "Invalid port number";
            e.printStackTrace();
        }
        return null;
    }

    public void logout()
    {
        program.client.disconnect();
        program.killWindow();
        LoginUI ui = new LoginUI();
        ui.displayLogin();
    }

    public static void main(String[] args)
    {
        LoginUI ui = new LoginUI();

        try
        {

            ui.displayLogin();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

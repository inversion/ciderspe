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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Entry point to the application
 */
public class CiderApplication
{
    /**
     * Version of the CIDER application
     */
    public static final double VERSION = 1.1;
    
	/**
	 * whether in debug mode or not
	 */
    public static boolean debugApp;

    /**
     * Launch the application
     * @param args whether in debug mode or not
     */
    public static void main(String[] args)
    {
        debugApp = false;
        for (String arg : args)
            if (arg.equals("debugapp=true"))
                debugApp = true;

        CiderApplication app = new CiderApplication();
        app.ui = new LoginUI(app);
        app.startCider();
    }

    LoginUI ui;

    public CiderApplication()
    {

    }

    /**
     * Return to the login screen
     */
    public void restarted()
    {
        ui = new LoginUI(this);
        this.startCider();
    }

    /**
     * Display the login screen
     */
    public void startCider()
    {
        try
        {
            ui.displayLogin();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            versionCheck();
        }
        catch (IOException e)
        {
            System.out.println("Warning: Couldn't connect to check version.");
        }
    }
    
    /**
     * Check if CIDER is up to date.
     * 
     * @author Andrew
     * @throws IOException
     */
    public static void versionCheck() throws IOException
    {
        URL url;
        InputStream is;
        InputStreamReader isr;
        BufferedReader br;
        
        url = new URL("http://www.cs.bris.ac.uk/~as9330/cider/version.txt");
        is = url.openStream();
        isr = new InputStreamReader( is );
        br = new BufferedReader( isr );
        
        String s = br.readLine();
        Double version = Double.parseDouble( s );
        if( CiderApplication.VERSION < version )
            JOptionPane.showMessageDialog(new JPanel(), "Warning: Your version of CIDER is out of date.\nYou are using version " + CiderApplication.VERSION + ". The most current version is " + version + ".\nPlease update at http://www.ciderspe.com/");
        is.close();
    }
}

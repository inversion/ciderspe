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

public class CiderApplication
{
    public static boolean debugApp;

    public static void main(String[] args)
    {
        debugApp = true;
        for (String arg : args)
            if (arg.equals("debugapp=true"))
                debugApp = false;

        CiderApplication app = new CiderApplication();
        app.ui = new LoginUI(app);
        app.startCider();
    }

    LoginUI ui;

    public CiderApplication()
    {

    }

    public void restarted()
    {
        ui = new LoginUI(this);
        this.startCider();
    }

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
    }
}

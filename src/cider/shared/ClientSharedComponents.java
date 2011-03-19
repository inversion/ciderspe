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

package cider.shared;

import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTabbedPane;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.ETASourceEditorPanel;

/**
 * (Mostly) GUI Components that the Client needs to share with MainWindow.
 * 
 * @author Andrew
 * 
 */

public class ClientSharedComponents
{
    // The list and count of users online
    public DefaultListModel userListModel = new DefaultListModel();
    public JList userList = new JList(userListModel);
    public JLabel userCount = new JLabel();

    // Tabs for source editors and table to keep track of them
    public JTabbedPane tabbedPane = new JTabbedPane();
    public Hashtable<String, ETASourceEditorPanel> openTabs = new Hashtable<String, ETASourceEditorPanel>();

    // The directory tree
    public DirectoryViewComponent dirView = new DirectoryViewComponent();

    // Tabs for chat sessions
    public JTabbedPane receiveTabs = new JTabbedPane();
}

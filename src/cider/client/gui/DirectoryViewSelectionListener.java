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

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import cider.common.network.client.Client;

/**
 * Listens for when a tree node is selected and if one is it gets a file from
 * the server and opens it in a new tab.
 * 
 * TODO: Type checking, error checking TODO: Commenting to explain functionality
 * 
 * @author Andrew
 * 
 */

public class DirectoryViewSelectionListener implements TreeSelectionListener
{

    private JTree tree;
    private Client client;

    DirectoryViewSelectionListener(JTree tree, Client client)
    {
        this.tree = tree;
        this.client = client;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        // TODO Auto-generated method stub
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                .getLastSelectedPathComponent();

        // Get the file and let the client message listener make the new tab
        // If the path is null it's a dir
        if (node != null && node.isLeaf())
        {
            client.openTabFor(node.getUserObjectPath());
            // client.directKeyboardInputTo(node.getUserObjectPath());
        }
    }
}

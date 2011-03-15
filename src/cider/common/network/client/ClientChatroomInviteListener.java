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

package cider.common.network.client;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Listens for invitations to chatrooms.
 * 
 * (Currently there is only one room, created by the bot, for all clients).
 * 
 * @author Andrew
 * 
 */

public class ClientChatroomInviteListener implements InvitationListener
{

    private static final boolean DEBUG = true;

    private MultiUserChat chatroom;
    private String nickname;
    private Client parent;

    public ClientChatroomInviteListener(MultiUserChat source, String nickname,
            Client parent)
    {
        this.nickname = nickname;
        chatroom = source;
        this.parent = parent;
    }

    @Override
    public void invitationReceived(Connection conn, String room,
            String inviter, String reason, String password, Message message)
    {
        try
        {
            if (DEBUG)
                System.out.println("Invited to chatroom " + room + " by "
                        + inviter + "...");
            chatroom.join(this.nickname, password);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: " + e.getMessage());
            parent.getLogin().logout();
        }
    }
}

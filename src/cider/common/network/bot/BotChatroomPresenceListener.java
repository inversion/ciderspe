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

package cider.common.network.bot;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listens for people's presence changes in the chatroom. Used to mark when
 * people have disconnected (uncleanly) or gone idle/back.
 * 
 * @author Andrew
 * 
 */

public class BotChatroomPresenceListener implements PacketListener
{
    private Bot bot;

    public BotChatroomPresenceListener(Bot bot)
    {
        this.bot = bot;
    }

    @Override
    public void processPacket(Packet packet)
    {
        Presence pres = (Presence) packet;
        String user = StringUtils.parseResource(pres.getFrom());
        if (!user.equals(bot.BOT_USERNAME))
        {
            if (pres.getType() == Presence.Type.unavailable)
                bot.chatListener.endSession(user);
        }
    }

}

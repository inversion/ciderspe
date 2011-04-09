package cider.common.network.bot;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listens for people's presence changes in the chatroom.
 * Used to mark when people have disconnected (uncleanly) or gone idle/back.
 * 
 * @author Andrew
 *
 */

public class BotChatroomPresenceListener implements PacketListener
{
    private Bot bot;
    
    public BotChatroomPresenceListener( Bot bot )
    {
        this.bot = bot;
    }
    
    @Override
    public void processPacket(Packet packet)
    {
        Presence pres = (Presence) packet;
        String user = StringUtils.parseResource( pres.getFrom() );
        if( !user.equals( bot.BOT_USERNAME ) )
        {
            if( pres.getType() == Presence.Type.unavailable )
                bot.chatListener.endSession( user );  
        }
    }

}

package cider.common.network;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public class DebugPacketFilter implements PacketFilter {

	@Override
	public boolean accept(Packet packet) {
		// TODO Auto-generated method stub
		return true;
	}

}

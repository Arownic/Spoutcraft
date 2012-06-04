package org.spoutcraft.client.packet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.spoutcraftapi.io.SpoutInputStream;
import org.spoutcraft.spoutcraftapi.io.SpoutOutputStream;

public class PacketPermissionUpdate implements SpoutPacket {
private Map<String, Boolean> permissions;

	public PacketPermissionUpdate() {
		permissions = new HashMap<String, Boolean>();
	}

	public PacketPermissionUpdate(Map<String, Boolean> permissions) {
		this.permissions = permissions;
	}

	@Override
	public void writeData(SpoutOutputStream output) throws IOException {
		output.writeInt(permissions.size());
		for (Entry<String, Boolean> perm:permissions.entrySet()) {
			output.writeString(perm.getKey());
			output.writeBoolean(perm.getValue());
		}
	}

	@Override
	public void readData(SpoutInputStream input) throws IOException {
		int num = input.readInt();
		for (int i = 0; i < num; i++) {
			String perm = input.readString();
			boolean allowed = input.readBoolean();
			permissions.put(perm, allowed);
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.PacketPermissionUpdate;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public void run(int playerId) {
		for (Entry<String, Boolean> perm:permissions.entrySet()) {
			SpoutClient.getInstance().setPermission(perm.getKey(), perm.getValue());
		}
	}

	@Override
	public void failure(int playerId) {}

}

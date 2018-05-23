package org.fastcatsearch.cluster;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.settings.NodeListSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Node implements Streamable {

	private static final Logger logger = LoggerFactory.getLogger(Node.class);

	private String nodeId;
	private String name;
	private InetSocketAddress socketAddress;
	private InetSocketAddress dataSocketAddress;

	private transient boolean isEnabled;
	private transient boolean isActive;

	// servicePort 정보를 유지한다.
	private int servicePort;

	public Node() {
	}

	public Node(String nodeId, InetSocketAddress socketAddress) {
		this.nodeId = nodeId.intern();
		this.name = this.nodeId;
		this.socketAddress = socketAddress;
	}

	public Node(String nodeId, String name, String address, int port) {
		this(nodeId, name, address, port, false);
	}

	public Node(String nodeId, String name, String address, int port, boolean isEnabled) {
		this.nodeId = nodeId;
		this.name = name;
		this.isEnabled = isEnabled;
		socketAddress = new InetSocketAddress(address, port);
	}

	public Node(NodeListSettings.NodeSettings settings) {
		this.nodeId = settings.getId();
		this.name = settings.getName();
		this.isEnabled = settings.isEnabled();
		this.socketAddress = new InetSocketAddress(settings.getAddress(), settings.getPort());
        logger.debug("#####SOCKETADDRESS > {}:{}", settings.getAddress(), settings.getPort());
		if(settings.getDataAddress() != null && !"".equals(settings.getDataAddress())) {
			this.dataSocketAddress = new InetSocketAddress(settings.getDataAddress(), settings.getPort());
            logger.debug("#####DATA_SOCKETADDRESS > {}:{}", settings.getDataAddress(), settings.getPort());
		}
	}

	public String toString() {
		return name + " (" + nodeId + "/" + socketAddress.getAddress() + (dataSocketAddress != null? "," + dataSocketAddress.getAddress() : "") + "/" + socketAddress.getPort() + ")";
	}

	public String status() {
		if (isEnabled) {
			return "Enabled / " + (isActive ? "Active" : "Inactive");
		} else {
			return "Disabled";
		}
	}

	public void setDisabled() {
		logger.debug("node {} state change to DISABLED", this);
		isEnabled = false;
	}

	public void setEnabled() {
		logger.debug("node {} state change to ENABLED", this);
		isEnabled = true;
	}

	public void setActive() {
		logger.debug("node {} state change to Active", this);
		isActive = true;
	}

	public void setInactive() {
		logger.debug("node {} state change to Inactive", this);
		isActive = false;
		// for(StackTraceElement e : Thread.currentThread().getStackTrace()){
		// logger.debug("stack trace >> {}", e);
		//
		// }
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	// enabled해야 active하다.
	public boolean isActive() {
		return isActive && isEnabled;
	}

	public String id() {
		return nodeId;
	}

	public String name() {
		return name;
	}

	public InetSocketAddress address() {
		return socketAddress;
	}

	public InetSocketAddress dataAddress() {
		return dataSocketAddress;
	}

	public int port() {
		return socketAddress.getPort();
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}
	
	public int servicePort() {
		return servicePort;
	}
	
	public static Node readNode(DataInput in) throws IOException {
		Node node = new Node();
		node.readFrom(in);
		return node;
	}

	@Override
	public void readFrom(DataInput in) throws IOException {
		nodeId = in.readString().intern();
		name = in.readString();
		String hostAddress = in.readString().intern();
		int port = in.readInt();
		socketAddress = new InetSocketAddress(hostAddress, port);
		if(in.readBoolean()) {
			String dataHostAddress = in.readString();
			dataSocketAddress = new InetSocketAddress(dataHostAddress, port);
		}
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.writeString(nodeId);
		out.writeString(name);
		out.writeString(socketAddress.getAddress().getHostAddress());
		out.writeInt(socketAddress.getPort());
		if(dataSocketAddress == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeString(dataSocketAddress.getAddress().getHostAddress());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Node))
			return false;

		Node other = (Node) obj;
		return this.nodeId.equals(other.nodeId);
	}

	@Override
	public int hashCode() {
		return nodeId.hashCode();
	}

	

}

package org.fastcatsearch.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class Node implements Streamable{
	private String nodeId;
	private InetSocketAddress socketAddress;
	
	public enum NodeStatus { ENABLED, DISABLED, ACTIVE, INACTIVE };
	private transient NodeStatus nodeStatus;
	
	public Node(){
	}
	
	public Node(String nodeId, InetSocketAddress socketAddress) {
        this.nodeId = nodeId.intern();
        this.socketAddress = socketAddress;
    }
	
	public Node(String nodeId, String address, int port) {
		this.nodeId = nodeId;
		socketAddress = new InetSocketAddress(address, port);
	}
	
	public String toString(){
		return nodeId+"/"+socketAddress.getHostName()+"/"+socketAddress.getPort();
	}
	
	public String status(){
		return nodeStatus.name();
	}
	
	public void setDisabled(){
		nodeStatus = NodeStatus.DISABLED;
	}
	public void setEnabled(){
		nodeStatus = NodeStatus.ENABLED;
	}
	public void setActive(){
		nodeStatus = NodeStatus.ACTIVE;
	}
	public void setInactive(){
		nodeStatus = NodeStatus.INACTIVE;
	}
	public boolean isEnabled(){
		return nodeStatus == NodeStatus.ENABLED;
	}
	
	public String id() {
		return nodeId;
	}
	
	public InetSocketAddress address() {
		return socketAddress;
	}
	
	public int port(){
		return socketAddress.getPort();
	}
	
	public static Node readNode(DataInput in) throws IOException {
        Node node = new Node();
        node.readFrom(in);
        return node;
    }

    @Override
    public void readFrom(DataInput in) throws IOException {
        nodeId = in.readString().intern();
        String hostName = in.readString().intern();
        int port = in.readInt();
        socketAddress = new InetSocketAddress(hostName, port);
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.writeString(nodeId);
        out.writeString(socketAddress.getHostName());
        out.writeInt(socketAddress.getPort());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node))
            return false;

        Node other = (Node) obj;
        return this.nodeId.equals(other.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }

	
}

package org.fastcatsearch.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class Node implements Streamable{
	private String nodeId;
	private String name;
	private InetSocketAddress socketAddress;
	
	private transient boolean isEnabled;
	private transient boolean isActive;
	
	public Node(){
	}
	
	public Node(String nodeId, InetSocketAddress socketAddress) {
        this.nodeId = nodeId.intern();
        this.socketAddress = socketAddress;
    }
	
	public Node(String nodeId,  String name, String address, int port) {
		this(nodeId, name, address, port, false);
	}
	
	public Node(String nodeId, String name, String address, int port, boolean isEnabled) {
		this.nodeId = nodeId;
		this.name = name;
		this.isEnabled = isEnabled;
		socketAddress = new InetSocketAddress(address, port);
	}

	public String toString(){
		return nodeId+"/"+socketAddress.getHostName()+"/"+socketAddress.getPort();
	}
	
	public String status(){
		if(isEnabled){
			return "Enabled / " + (isActive ? "Active" : "Inactive");
		}else{
			return "Disabled";
		}
	}
	
	public void setDisabled(){
		isEnabled = false;
	}
	public void setEnabled(){
		isEnabled = true;
	}
	public void setActive(){
		isActive = true;
	}
	public void setInactive(){
		isActive = false;
	}
	public boolean isEnabled(){
		return isEnabled;
	}
	public boolean isActive(){
		return isActive;
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

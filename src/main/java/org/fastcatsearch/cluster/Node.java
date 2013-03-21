package org.fastcatsearch.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class Node implements Streamable{
	private String nodeId;
	private InetSocketAddress socketAddress;
	
	public Node(){
	}
	
	public Node(String nodeName, InetSocketAddress socketAddress) {
        if (nodeName == null) {
            this.nodeId = "".intern();
        } else {
            this.nodeId = nodeName.intern();
        }

        this.socketAddress = socketAddress;
    }
	
	public Node(String nodeId, String address, int port) {
		this.nodeId = nodeId;
		socketAddress = new InetSocketAddress(address, port);
	}
	
	public String toString(){
		return nodeId+"/"+socketAddress.getHostName()+"/"+socketAddress.getPort();
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
	
	public static Node readNode(StreamInput in) throws IOException {
        Node node = new Node();
        node.readFrom(in);
        return node;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        nodeId = in.readString().intern();
        String hostName = in.readString().intern();
        int port = in.readInt();
        socketAddress = new InetSocketAddress(hostName, port);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
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

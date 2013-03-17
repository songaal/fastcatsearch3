package org.fastcatsearch.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class Node implements Streamable{
	private String nodeName;
	private String nodeId;
	private InetSocketAddress address;
	
	public Node(){
	}
	
	public Node(String nodeName, String nodeId, InetSocketAddress address) {
        if (nodeName == null) {
            this.nodeName = "".intern();
        } else {
            this.nodeName = nodeName.intern();
        }

        this.nodeId = nodeId.intern();
        this.address = address;
    }
	
	public String toString(){
		return nodeName+"/"+nodeId+"/"+address;
	}
	public String id() {
		return nodeId;
	}
	
	public String name() {
		return nodeName;
	}
	
	public InetSocketAddress address() {
		return address;
	}
	
	public int port(){
		return address.getPort();
	}
	
	public static Node readNode(StreamInput in) throws IOException {
        Node node = new Node();
        node.readFrom(in);
        return node;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        nodeName = in.readString().intern();
        nodeId = in.readString().intern();
        String hostName = in.readString().intern();
        int port = in.readInt();
        address = new InetSocketAddress(hostName, port);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(nodeName);
        out.writeString(nodeId);
        out.writeString(address.getHostName());
        out.writeInt(address.getPort());
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

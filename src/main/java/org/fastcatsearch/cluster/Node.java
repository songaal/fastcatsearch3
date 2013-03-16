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
	private int port;
	
	public Node(){
	}
	
	public Node(String nodeName, String nodeId, InetSocketAddress address, int port) {
        if (nodeName == null) {
            this.nodeName = "".intern();
        } else {
            this.nodeName = nodeName.intern();
        }

        this.nodeId = nodeId.intern();
        this.address = address;
        this.port = port;
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
		return port;
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
//        address = TransportAddressSerializers.addressFromStream(in);
        port = in.readInt();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(nodeName);
        out.writeString(nodeId);
//        addressToStream(out, address);
        out.writeInt(port);
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

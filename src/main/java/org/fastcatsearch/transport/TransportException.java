package org.fastcatsearch.transport;

import org.fastcatsearch.cluster.Node;

public class TransportException extends Exception {
	private static final long serialVersionUID = -5270666606963168636L;

	public TransportException(String message){
		super(message);
	}
	public TransportException(String message, Throwable t){
		super(message, t);
	}
	public TransportException(Throwable t){
		super(t);
	}
	public TransportException(Node node, String message){
		super("[" + node.toString() + "]"+message);
	}
	public TransportException(Node node, String message, Throwable t){
		super("[" + node.toString() + "]"+message, t);
	}
}

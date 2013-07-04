package org.fastcatsearch.transport;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;


public class NodeChannels {

    private Channel low;
    private Channel high;

    public NodeChannels() {
    }

    public boolean hasChannel(Channel channel) {
        return channel.equals(low) || channel.equals(high);
    }

    private boolean hasChannel(Channel channel, Channel[] channels) {
        for (Channel channel1 : channels) {
            if (channel.equals(channel1)) {
                return true;
            }
        }
        return false;
    }

    public Channel getLowChannel() {
       return low;
    }
    
    public Channel getHighChannel() {
    	return low;
    }

    public synchronized void close() {
        List<ChannelFuture> futures = new ArrayList<ChannelFuture>();
        closeChannelsAndWait(low, futures);
        closeChannelsAndWait(high, futures);
        for (ChannelFuture future : futures) {
            future.awaitUninterruptibly();
        }
    }

    private void closeChannelsAndWait(Channel channel, List<ChannelFuture> futures) {
        try {
            if (channel != null && channel.isOpen()) {
                futures.add(channel.close());
            }
        } catch (Exception ignore) {
            //ignore
        }
    }

	public void setLowChannel(Channel channel) {
		this.low = channel;
	}

	public void setHighChannel(Channel channel) {
		this.high = channel;
	}
}

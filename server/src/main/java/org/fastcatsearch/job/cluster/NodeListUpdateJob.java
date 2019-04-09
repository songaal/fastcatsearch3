package org.fastcatsearch.job.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.settings.NodeListSettings.NodeSettings;

public class NodeListUpdateJob extends Job implements Streamable {

	private static final long serialVersionUID = 7761738276375209615L;

	public NodeListUpdateJob() {
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			logger.debug("run node update job..");
			NodeListSettings nodeListSettings = (NodeListSettings)args; 
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			nodeService.updateNode(nodeListSettings);
			
			
			return new JobResult(true);

		} catch (Exception e) {
			logger.error("", e);
			throw new FastcatSearchException("ERR-00525", e);
		}

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		List<NodeSettings>settings = new ArrayList<NodeSettings>();
		int size = input.readInt();
		for(int inx=0;inx<size;inx++) {
			NodeSettings setting = new NodeSettings();
			setting.setId(new String(input.readAString()));
			setting.setName(new String(input.readUString()));
			setting.setAddress(new String(input.readAString()));
			setting.setDataAddress(new String(input.readAString()));
			setting.setPort(input.readInt());
			setting.setEnabled(input.readBoolean());
			settings.add(setting);
		}
		NodeListSettings nodeListSettings = new NodeListSettings();
		nodeListSettings.setNodeList(settings);
		this.args = nodeListSettings;
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		if(args!=null && args instanceof NodeListSettings) {
			NodeListSettings nodeListSettings = (NodeListSettings)args;
			List<NodeSettings> settings = nodeListSettings.getNodeList();
			output.writeInt(settings.size());
			for(int inx=0;inx< settings.size(); inx++) {
				NodeSettings setting = settings.get(inx);
				String id = setting.getId();
				String name = setting.getName();
				String address = setting.getAddress();
				String dataAddress = setting.getDataAddress();
				int port = setting.getPort();
				boolean enabled = setting.isEnabled();
				output.writeAString(id.toCharArray(),0,id.length());
				output.writeUString(name.toCharArray(),0,name.length());
				output.writeAString(address.toCharArray(),0,address.length());
				output.writeAString(dataAddress);
				output.writeInt(port);
				output.writeBoolean(enabled);
			}
		}
	}
}

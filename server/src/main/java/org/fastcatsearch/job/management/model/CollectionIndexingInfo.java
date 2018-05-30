package org.fastcatsearch.job.management.model;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

import java.io.IOException;

public class CollectionIndexingInfo implements Streamable {
    private String collectionId;
    Boolean isActive;
    String name;
    int sequence;
    String indexNode;
    String dataNodeList;
    String searchNodeList;
    String diskSize;
    int documentSize;
    int segmentSize;

    //TODO



    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getIndexNode() {
        return indexNode;
    }

    public void setIndexNode(String indexNode) {
        this.indexNode = indexNode;
    }

    public String getDataNodeList() {
        return dataNodeList;
    }

    public void setDataNodeList(String dataNodeList) {
        this.dataNodeList = dataNodeList;
    }

    public String getSearchNodeList() {
        return searchNodeList;
    }

    public void setSearchNodeList(String searchNodeList) {
        this.searchNodeList = searchNodeList;
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();

        //TODO
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {

        output.writeString(collectionId);
        output.writeBoolean(isActive);
        output.writeString(name);

        //TODO
    }
}

package org.fastcatsearch.job.management.model;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

import java.io.IOException;

public class CollectionIndexingInfo implements Streamable {

    private String collectionId;
    private boolean isActive;
    private String name;
    private int sequence;
    private String revisionUUID;
    private String indexNode;
    private String dataNodeList;
    private String searchNodeList;

    private int documentSize;
    private int segmentSize;
    private String diskSize;
    private String dataPath;
    private String createTime;

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        isActive = input.readBoolean();
        name = input.readString();
        sequence = input.readInt();
        revisionUUID = input.readString();
        indexNode = input.readString();
        dataNodeList = input.readString();
        searchNodeList = input.readString();
        documentSize = input.readInt();
        segmentSize = input.readInt();
        diskSize = input.readString();
        dataPath = input.readString();
        createTime = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeBoolean(isActive);
        output.writeString(name);
        output.writeInt(sequence);
        output.writeString(revisionUUID);
        output.writeString(indexNode);
        output.writeString(dataNodeList);
        output.writeString(searchNodeList);
        output.writeInt(documentSize);
        output.writeInt(segmentSize);
        output.writeString(diskSize);
        output.writeString(dataPath);
        output.writeString(createTime);
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
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

    public String getRevisionUUID() {
        return revisionUUID;
    }

    public void setRevisionUUID(String revisionUUID) {
        this.revisionUUID = revisionUUID;
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

    public int getDocumentSize() {
        return documentSize;
    }

    public void setDocumentSize(int documentSize) {
        this.documentSize = documentSize;
    }

    public int getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(int segmentSize) {
        this.segmentSize = segmentSize;
    }

    public String getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(String diskSize) {
        this.diskSize = diskSize;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

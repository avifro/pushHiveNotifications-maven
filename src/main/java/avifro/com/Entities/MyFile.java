package avifro.com.Entities;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by avifro on 12/12/14.
 */
public class MyFile {

    @JsonProperty(value = "id")
    private long storageProviderId;

    private String title;

    private String dateCreated;

    private String extension;

    private long size;


    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStorageProviderId() {
        return storageProviderId;
    }

    public void setStorageProviderId(long storageProviderId) {
        this.storageProviderId = storageProviderId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}

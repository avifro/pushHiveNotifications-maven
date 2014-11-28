package avifro.com.Entities;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Created by avifro on 11/1/14.
 */
public class MyTransfer {

    private String filename;

    private String dateCreated;

    private String extension;

    private long size;

    private String status;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyTransfer)) {
            return false;
        }
        MyTransfer that = (MyTransfer) obj;
        return new EqualsBuilder().append(this.filename, that.filename)
                                  .append(this.extension, that.extension)
                                  .append(this.getSize(), that.getSize()).isEquals();
    }
}

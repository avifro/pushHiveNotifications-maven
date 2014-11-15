package avifro.com;

import avifro.com.Entities.MyTransfer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

/**
 * Created by avifro on 11/15/14.
 */
public class MyTransferDbHelper {

    public static final String DOWNLOADS_COLLECTION_DB = "downloads";

    private DB db;

    public void setMongoDB(DB db) {
        this.db = db;
    }

    public void createDownloadsCollection() {
        if (!db.collectionExists(DOWNLOADS_COLLECTION_DB)) {
            db.createCollection(DOWNLOADS_COLLECTION_DB, null);
        }
    }

    public WriteResult insertDoc(MyTransfer myTransfer) {
        DBCollection coll = db.getCollection(DOWNLOADS_COLLECTION_DB);
        BasicDBObject dbObject = new BasicDBObject("fileName", myTransfer.getFilename())
                .append("dateCreated", myTransfer.getDateCreated())
                .append("extension", myTransfer.getExtension())
                .append("size", myTransfer.getSize());
        return coll.insert(dbObject);
    }
}

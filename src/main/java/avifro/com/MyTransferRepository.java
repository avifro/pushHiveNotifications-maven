package avifro.com;

import avifro.com.Entities.MyTransfer;
import com.mongodb.*;

/**
 * Created by avifro on 11/15/14.
 */
public class MyTransferRepository {

    private DB db;

    public void setMongoDB(DB db) {
        this.db = db;
    }

    public void createCollection(String collectionName) {
        if (db!= null && !db.collectionExists(collectionName)) {
            db.createCollection(collectionName, null);
        }
    }

    public boolean exists(String collectionName, String fileName) {
        DBCollection coll = db.getCollection(collectionName);
        DBObject dbObject = new BasicDBObject("fileName", fileName);
        return coll.find(dbObject).count() > 0;

    }

    public WriteResult insertDoc(String collectionName, MyTransfer myTransfer) {
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbObject = new BasicDBObject("fileName", myTransfer.getFilename())
                .append("dateCreated", myTransfer.getDateCreated())
                .append("extension", myTransfer.getExtension())
                .append("size", myTransfer.getSize());
        return coll.insert(dbObject);
    }
}

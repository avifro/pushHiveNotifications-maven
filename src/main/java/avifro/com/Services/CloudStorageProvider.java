package avifro.com.Services;

import avifro.com.Entities.MyFile;
import avifro.com.Entities.MyTransfer;

import java.util.List;

/**
 * Created by avifro on 11/6/14.
 */
public interface CloudStorageProvider {

    String getMyToken(String userName, String password);
    List<MyFile> findFilesByFolderId(long transferFolderId, String token);
    List<MyTransfer> findMyTransfers(String token);
    long findFolderIdByType(String token, String type);
    void moveFolderContentToAnotherFolder(long sourceFolderId, long destinationFolderId, String token);

}

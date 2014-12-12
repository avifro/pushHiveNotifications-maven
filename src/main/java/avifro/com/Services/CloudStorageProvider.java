package avifro.com.Services;

import avifro.com.Entities.MyTransfer;

import java.util.List;

/**
 * Created by avifro on 11/6/14.
 */
public interface CloudStorageProvider {

    String getMyToken(String userName, String password);
    List<MyTransfer> findMyTransfers(String token);
    long findVideoFolderId(String token);
    void moveToVideoFolder(long videoId, long videoFolderId, String token);

}

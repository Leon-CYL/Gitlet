package gitlet;

import java.io.Serializable;
import static gitlet.Utils.*;
import static gitlet.Repository.BLOBS_DIR;


public class Blob implements Serializable {

    private final String contents;
    private final String blobId;

    public Blob(String contents) {
        this.contents = contents;
        this.blobId = calcId();
    }


    private String calcId() {
        return sha1(contents);
    }

    public String getId() {
        return blobId;
    }

    public void save() {
        writeContents(join(BLOBS_DIR, blobId), contents);
    }

    public static String getContents(String id) {
        return readContentsAsString(join(BLOBS_DIR, id));
    }
}

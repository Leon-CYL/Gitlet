package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import static gitlet.Utils.*;
import static gitlet.Repository.COMMIT_DIR;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author ChaoYuan Lin
 */
public class Commit implements Serializable {

    private final String message;
    private final Date date;
    private final String parentID1;
    private final String parentID2;
    private final String ID;
    private String otherBranch;
    private File file;

    private String branch;

    /**<String filename, blobID>*/
    private HashMap<String, String> blobs;

    public Commit(String m, Date d, String b, String id1, String id2) {
        message = m;
        parentID1 = id1;
        parentID2 = id2;
        date = d;
        branch = b;
        ID = createID();
        blobs = new HashMap<String, String>();
        otherBranch = null;
        file = generateFile();
    }

    public void saveCommit() {
        writeObject(file, this);
    }

    public static List<String> getCommitIds() {
        return plainFilenamesIn(COMMIT_DIR);
    }

    public static Commit findCommit(String commitID) {
        List<String> commitIDs = getCommitIds();
        for (String id : commitIDs) {
            if (id.substring(0, 8).equals(commitID.substring(0, 8))) {
                File f = join(COMMIT_DIR, id);
                if (!f.exists()) {
                    return null;
                } else {
                    return readObject(f, Commit.class);
                }
            }
        }
        return null;
    }

    private String generateTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    private File generateFile() {
        return join(COMMIT_DIR, this.getID());
    }

    private String createID() {
        return sha1(generateTimeStamp(), message, branch);
    }

    public String getMessage() {
        return message;
    }

    public void add(String name, String bId) {
        blobs.put(name, bId);
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public Set<String> getFiles() {
        return getBlobs().keySet();
    }

    public String getDate() {
        return generateTimeStamp();
    }

    public String getID() {
        return ID;
    }

    public String getParentID1() {
        return parentID1;
    }

    public String getParentID2() {
        return parentID2;
    }

    public String getOtherBranch() {
        return otherBranch;
    }

    public String getBranch() {
        return branch;
    }

    public void setOtherBranch(String b) {
        otherBranch = b;
    }
    
    public void setBlobs(HashMap<String, String> newBlobs) {
        blobs = newBlobs;
    }
}

package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Repository.BRANCH_DIR;

public class Branch implements Serializable {


    public static void saveCommit(String branchName, String commitID) {
        File b = join(BRANCH_DIR, branchName);
        writeContents(b, commitID);
    }

    public static String getCommitID(String branchName) {
        File b = join(BRANCH_DIR, branchName);
        if (!b.exists()) {
            return null;
        } else {
            return readContentsAsString(b);
        }
    }

    public static List<String> getBranchName() {
        return plainFilenamesIn(BRANCH_DIR);
    }
}

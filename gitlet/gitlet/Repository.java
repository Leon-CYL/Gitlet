package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author ChaoYuan Lin
 */
public class Repository {

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");
    public static final File STAGE_DIR = join(GITLET_DIR, "Stage");
    public static final File REMOVAL_DIR = join(GITLET_DIR, "Removal");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File HEAD_DIR = join(GITLET_DIR, "head");
    public static final File HEAD_BRANCH = join(HEAD_DIR, "HeadBranch");

    /**
     * Create a new gitlet Version Control System locally on your machine, generate
     * an initial commit and make
     *  -COMMIT_DIR
     *  -BRANCH_DIR
     *  -BLOBS_DIR
     *  -REMOVAL_DIR
     *  -STAGE-DIR
     *  -HEAD_DIR
     * under the GITLET_DIR.
     * */
    public void init() {
        if (GITLET_DIR.exists()) {
            error("A Gitlet version-control system already exists in the current directory.");
        } else {
            mkdir(GITLET_DIR);
            mkdir(COMMIT_DIR);
            mkdir(BRANCH_DIR);
            mkdir(BLOBS_DIR);
            mkdir(REMOVAL_DIR);
            mkdir(STAGE_DIR);
            mkdir(HEAD_DIR);
            Commit init = new Commit("initial commit", new Date(0), "master", null, null);
            init.saveCommit();
            Branch.saveCommit("master", init.getID());
            setBranchName("master");
        }
    }

    /**
     * This method will first look for the file with given filename from CWD and add the file
     * under the STAGE_DIR if the head commit do not contain this file with the same content.
     *
     * @param filename The name of the file that will be work in this method.
     * */
    public void add(String filename) {
        File f = join(CWD, filename);
        if (!f.exists()) {
            error("File does not exist.");
        }

        Commit head = loadHead(getBranchName());
        Map<String, String> exist = head.getBlobs();
        Blob b = new Blob(readContentsAsString(f));
        String blobId = b.getId();

        // Check if the file is already in the current commit//
        if (exist.containsKey(filename) && blobId.equals(exist.get(filename))) {
            // delete it from the stage area if it exists.
            Stage.removeFile(filename);
            Removal.removeFile(filename);
            return;
        }

        Stage.saveFile(filename, readContentsAsString(f));
    }

    public void commit(String message) {
        commitHelper(message, loadHead(getBranchName()).getID(), null);
    }

    /**
     * 1. Create a new Commit.
     * 2. Copy all the tracked files in the current head Commit to the new Commit.
     * 3. Add the tracked files from the adding Stage and overwrite files
     *    into the new Commit if it is already existed.
     * 4. Remove the files that are currently tracked in the remove Stage.
     * 5. This new Commit become the head Commit.
     *
     * @param message The message for the new Commit.
     * @param id1 Parent 1 ID
     * @param id2 Parent 2 ID
     * */
    private void commitHelper(String message, String id1, String id2) {
        if (message.equals("")) {
            error("Please enter a commit message.");
        }
        List<String> add = Stage.load();
        List<String> remove = Removal.getFile();
        if (add.isEmpty() && remove.isEmpty()) {
            error("No changes added to the commit.");
        }

        Commit parent = Commit.findCommit(id1);
        assert parent != null;
        Commit node = new Commit(message, new Date(), getBranchName(), parent.getID(), id2);

        // Add all the files from parent to the Commit
        node.setBlobs(parent.getBlobs());

        // Add all the files from addStage to the Commit
        for (String name : add) {
            File f = join(STAGE_DIR, name);
            Blob b = new Blob(readContentsAsString(f));
            b.save();
            if (node.getBlobs().containsKey(name)) {
                node.getBlobs().remove(name);
            }
            node.add(name, b.getId());
        }

        // Remove all the files from the new Commit if it is tracked in the removeStage
        for (String fileName : Removal.getFile()) {
            node.getBlobs().remove(fileName);
        }

        // Clear all the files from the stage area
        Stage.clear();
        Removal.clear();
        node.saveCommit();
        Branch.saveCommit(getBranchName(), node.getID());
    }

    /**
     * Case 1: Remove the file with given filename if it exists in the add Stage.
     * Case 2: Remove the file with given filename if it exists in the current head
     *         Commit, add this file to the remove Stage and delete this file from CWD.
     * @param filename The name of the file that will be work in this method.
     * */
    public void rm(String filename) {
        List<String> add = Stage.load();
        // remove the file from the addStage
        if (add.contains(filename)) {
            Stage.removeFile(filename);
            return;
        }

        // remove the file from the Current Commit
        Commit curr = loadHead(getBranchName());
        if (curr.getBlobs().containsKey(filename)) {
            HashMap<String, String> blobs = curr.getBlobs();
            String bId = blobs.get(filename);
            String content = Blob.getContents(bId);
            Removal.saveFile(filename, content);
            join(CWD, filename).delete();
            return;
        }

        System.out.println("No reason to remove the file.");

    }

    /**
     * Case 1: Checkout File(Head Commit)
     * Case 2: Checkout Commit(Given Commit ID)
     *
     * This method takes the version of the file as it exists in the Commit with given
     * ID and puts it in the working directory,overwriting the version of the file
     * that’s already there if there is one. The new version of the file is not staged.
     *
     * @param commitId The ID of the Commit that will be work in this method.
     * @param fileName The name of the file that will be work in this method.
     */
    public void checkoutFile(String commitId, String fileName) {
        Commit commit = Commit.findCommit(commitId);
        if (commit == null) {
            error("No commit with that id exists.");
        }
        String blobId = commit.getBlobs().get(fileName);
        if (blobId == null) {
            error("File does not exist in that commit.");
        }
        byte[] blobContents = readContents(join(BLOBS_DIR, blobId));
        writeContents(join(CWD, fileName), blobContents);
    }

    /**
     * Case 3: Checkout Branch
     *
     * Takes all files in the commit at the head of the given branch and puts them
     * in the working directory, overwriting the versions of the files that are
     * already there if they exist. The given branch will become the new current branch
     * Any files that are tracked in the current branch but are not present in the
     * checked-out branch are deleted and the staging area is cleared.
     *
     * @param branchName The name of the branch that will be work in this method.
     */
    public void checkoutBranch(String branchName) {
        List<String> bName = Branch.getBranchName();
        if (!bName.contains(branchName)) {
            error("No such branch exists.");
        }

        if (branchName.equals(getBranchName())) {
            error("No need to checkout the current branch.");
        }

        String commitID = Branch.getCommitID(branchName);
        Commit commit = Commit.findCommit(commitID);
        checkoutCommit(commit.getID());
        //Reset branch name
        setBranchName(branchName);
    }

    /**
     * This method will take version of the files that are tracked in the Commit with given ID
     * to the CWD and overwrite the files if they exist in the CDW and remove the files from the
     * CWD if the file is tracked head Commit but not the Commit with given ID, remove it from the
     * CWD and clear all the files in the adding stage area.
     *
     * @param commitID The ID of the Commit that will be work in this method.
     */
    public void checkoutCommit(String commitID) {
        Commit target = Commit.findCommit(commitID);
        Commit curr = loadHead(getBranchName());

        assert target != null;
        checkUntracked(target);
        Set<String> fileNames = target.getBlobs().keySet();
        if (target.getID().equals(curr.getID())) {
            return;
        }

        for (String fileName : fileNames) {
            String blobId = target.getBlobs().get(fileName);
            byte[] blobContents = readContents(join(BLOBS_DIR, blobId));
            writeContents(join(CWD, fileName), (Object) blobContents);
        }

        for (String fileName : curr.getBlobs().keySet()) {
            if (!fileNames.contains(fileName)) {
                join(CWD, fileName).delete();
            }
        }

        Stage.clear();
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * The method will not set the given branchName to the current branch.(Look at the Checkout
     * method above).
     *
     * @param branchName The name of the branch that will be work in this method.
     * */
    public void branch(String branchName) {
        List<String> branches = plainFilenamesIn(BRANCH_DIR);
        assert branches != null;
        if (branches.contains(branchName)) {
            error("A branch with that name already exists.");
        }
        Commit head = loadHead(getBranchName());
        head.setOtherBranch(branchName);
        Branch.saveCommit(branchName, head.getID());
    }

    /**
     * Starting at the current head commit, display information about each commit backwards
     * along the commit tree until the initial commit, following the first parent commit links,
     * ignoring any second parents found in merge commits.
     * */
    public void log() {
        Commit head = loadHead(getBranchName());
        while (head.getParentID1() != null) {
            helpLog(head);
            head = Commit.findCommit(head.getParentID1());
        }
        helpLog(head);
    }

    /**
     * This is a helper method that print out the information of the given Commit.
     *
     * @param c The Commit that will be work in this method.
     * */
    private void helpLog(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getID());
        if (c.getParentID1() != null && c.getParentID2() != null) {
            String merge = String.format("Merge: %s %s", c.getParentID1().substring(0, 7),
                    c.getParentID2().substring(0, 7));
            System.out.println(merge);
        }
        System.out.println("Date: " + c.getDate());
        System.out.println(c.getMessage() + "\n");
    }

    /**
     * This method will display the information of all the commit that store in the COMMIT_DIR.
     * */
    public void globalLog() {
        List<String> commits = Commit.getCommitIds();
        for (String id : commits) {
            helpLog(Commit.findCommit(id));
        }
    }

    /**
     * This method prints out the ids of all commits that have the given commit message,
     * one per line.
     *
     * @param message Looking for the Commits that contain this message.
     * */
    public void find(String message) {
        boolean exist = false;
        List<String> commits = Commit.getCommitIds();
        for (String id : commits) {
            Commit c = Commit.findCommit(id);
            assert c != null;
            if (c.getMessage().equals(message)) {
                exist = true;
                System.out.println(id);
            }
        }

        if (!exist) {
            error("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal. The last two
     * sections are for extra credit(not finish).
     * */
    public void status() {
        if (!GITLET_DIR.exists()) {
            error("Not in an initialized Gitlet directory.");
        }

        System.out.println("=== Branches ===");
        for (String name : Branch.getBranchName()) {
            if (name.equals(getBranchName())) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }

        System.out.println("\n=== Staged Files ===");
        for (String fName : Stage.load()) {
            System.out.println(fName);
        }

        System.out.println("\n=== Removed Files ===");
        for (String fName : Removal.getFile()) {
            System.out.println(fName);
        }

        //Extra Credit.
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    /**
     * Deletes the branch with the given name.
     *
     * @param branchName The name of the branch that will be deleted.
     * */
    public void rmBranch(String branchName) {
        if (getBranchName().equals(branchName)) {
            error("Cannot remove the current branch.");
        }
        if (!Branch.getBranchName().contains(branchName)) {
            error("A branch with that name does not exist.");
        }

        deleteFile(join(BRANCH_DIR, branchName));
    }

    /**
     * Checks out all the files tracked by the given commit. Removes tracked files that
     * are not present in that commit. Also moves the current branch’s head to that commit node.
     *
     * @param id The Commit ID that will be work in this method.
     * */
    public void reset(String id) {
        Commit temp = Commit.findCommit(id);
        if (temp == null) {
            error("No commit with that id exists.");
        }

        checkoutCommit(id);
        Branch.saveCommit(getBranchName(), id);
    }



    /**
     * Merges files from the given branch into the current branch.
     *
     *  Case 1: Any files that have been modified in the given branch since the split point,
     *          current branch since the split point should be changed to their versions in
     *          the given branch (checked out from the commit at the front of the given branch).
     *
     *  Case 2: Any files that have been modified in the current branch but not in the given
     *          branch since the split point should stay as they are.
     *
     *  Case 3: Any files that have been modified in both the current and given branch in the
     *          same way are left unchanged by the merge. If a file was removed from both the
     *          current and given branch, but a file of the same name is present in the working
     *          directory, it is left alone and continues to be absent in the merge.
     *
     *  Case 4: Any files that were not present at the split point and are present only
     *          in the current branch should remain as they are.
     *
     *  Case 5: Any files that were not present at the split point and are present only
     *          in the given branch should be checked out and staged.
     *
     *  Case 6: Any files present at the split point, unmodified in the current branch,
     *          and absent in the given branch should be removed.
     *
     *  Case 7: Any files present at the split point, unmodified in the given branch,
     *          and absent in the current branch should remain absent.
     *
     *  Case 8: Any files modified in different ways in the current and given branches are in
     *          conflict. “Modified in different ways” can mean that the contents of both are
     *          changed and different from other, or the contents of one are changed and the
     *          other file is deleted, or the file was absent at the split point and has different
     *          contents in the given and current branches. In this case, replace the contents
     *          of the conflicted file with the indicated file’s contents and stage the result.
     * */
    public void merge(String branchName) {
        List<String> stage = Stage.load();
        List<String> removal = Removal.getFile();
        if (!stage.isEmpty() || !removal.isEmpty()) {
            error("You have uncommitted changes.");
        }
        String mergedCommitId = Branch.getCommitID(branchName);
        if (mergedCommitId == null) {
            error("A branch with that name does not exist.");
        }
        Commit mergedCommit = Commit.findCommit(mergedCommitId);
        assert mergedCommit != null;
        if (getBranchName().equals(branchName)) {
            error("Cannot merge a branch with itself.");
        }
        String currentCommitId = Branch.getCommitID(getBranchName());
        assert currentCommitId != null;
        Commit currentCommit = Commit.findCommit(currentCommitId);
        assert currentCommit != null;
        List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
        assert cwdFileNames != null;
        List<String> untrackedFiles = getUntrackedFiles();
        if (!untrackedFiles.isEmpty()) {
            for (String untrackedFileName : untrackedFiles) {
                if (mergedCommit.getBlobs().get(untrackedFileName) != null) {
                    error("There is an untracked file in the way; delete it, or add and commit "
                                    + "it first.");
                }
            }
        }
        if (!getUntrackedFiles().isEmpty()) {
            error("There is an untracked file in the way; delete it, or add and commit it first"
                            + ".");
        }
        String splitPointID = findSplit(currentCommitId, mergedCommitId);
        assert splitPointID != null;
        // If the split point is the same commit as the given branch, then we do nothing;
        if (splitPointID.equals(mergedCommitId)) {
            error("Given branch is an ancestor of the current branch.");
        }
        //  If the split point is the current branch, then the effect is to check out the given
        //  branch
        if (splitPointID.equals(currentCommitId)) {
            checkoutCommit(mergedCommit.getID());
            Branch.saveCommit(getBranchName(), mergedCommitId);
            error("Current branch fast-forwarded.");
        }

        Commit splitPoint = Commit.findCommit(splitPointID);
        assert splitPoint != null;
        boolean conflict = processMerge(splitPoint, currentCommit,
                mergedCommit);

        commitHelper(String.format("Merged %s into %s.", branchName, getBranchName()),
                currentCommitId, mergedCommitId);

        if (conflict) {
            error("Encountered a merge conflict.");
        }
    }

    private boolean processMerge(Commit splitPointCommit, Commit currentCommit,
                                        Commit mergedCommit) {
        boolean conflict = false;
        HashMap<String, String> splitBlobs = splitPointCommit.getBlobs();
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        HashMap<String, String> mergedBlobs = mergedCommit.getBlobs();

        for (String fileName : mergedBlobs.keySet()) {
            // modified in the given branch since the split point
            String mergedBlobId = mergedBlobs.get(fileName);
            String splitBlobId = splitBlobs.get(fileName);
            String currentBlobId = currentBlobs.get(fileName);
            // case1: Any files that have been modified in the given branch since the split
            // point, but not modified in the current branch since the split point should be
            // changed to their versions in the given branch
            if (splitBlobId != null && !mergedBlobId.equals(splitBlobId)) {
                if (splitBlobId.equals(currentBlobId)) {
                    checkoutFile(mergedCommit.getID(), fileName);
                    Stage.saveFile(fileName, Blob.getContents(mergedBlobId));
                    continue;
                }
            }
            // case3: keep same

            // case5: Any files that were not present at the split point and are present only in
            // the given branch should be checked out and staged.
            if (splitBlobId == null && currentBlobId == null) {
                checkoutFile(mergedCommit.getID(), fileName);
                Stage.saveFile(fileName, Blob.getContents(mergedBlobId));
                continue;
            }
            // case7: keep same

            // case8: or the contents of one are changed and the other file is deleted,
            if (splitBlobId != null && !mergedBlobId.equals(splitBlobId) && currentBlobId == null) {
                conflict = true;
                processConflict(fileName, currentBlobId, mergedBlobId);
            }
        }

        for (String fileName : currentBlobs.keySet()) {
            String currentBlobId = currentBlobs.get(fileName);
            String splitBlobId = splitBlobs.get(fileName);
            String mergedBlobId = mergedBlobs.get(fileName);
            // case2: keep same
            // case4: keep same
            // case6: Any files present at the split point, unmodified in the current branch, and
            // absent in the given branch should be removed (and untracked).
            if (currentBlobId.equals(splitBlobId)) {
                if (mergedBlobId == null) {
                    Utils.join(CWD, fileName).delete();
                    Removal.clear();
                    continue;
                }
            }
            // case8: the contents of both are changed and different from other
            if (splitBlobId != null && mergedBlobId != null) {
                if (!currentBlobId.equals(splitBlobId) && !mergedBlobId.equals(splitBlobId)) {
                    if (!currentBlobId.equals(mergedBlobId)) {
                        conflict = true;
                        processConflict(fileName, currentBlobId, mergedBlobId);
                    }
                }
            }
            // case8: or the contents of one are changed and the other file is deleted,
            if (splitBlobId != null && !currentBlobId.equals(splitBlobId) && mergedBlobId == null) {
                conflict = true;
                processConflict(fileName, currentBlobId, mergedBlobId);
            }
            // case8: or the file was absent at the split point and has different contents in the
            // given and current branches.
            if (splitBlobId == null && mergedBlobId != null) {
                if (!currentBlobId.equals(mergedBlobId)) {
                    conflict = true;
                    processConflict(fileName, currentBlobId, mergedBlobId);
                }
            }
        }
        return conflict;
    }

    private static void processConflict(String fileName, String headBlobId, String otherBlobId) {
        String newContents = conflictFileContents(headBlobId, otherBlobId);
        Blob newBlob = new Blob(newContents);
        newBlob.save();
        File file = join(CWD, fileName);
        writeContents(file, newContents);
        Stage.saveFile(fileName, newContents);
    }


    private static String conflictFileContents(String headBlobId, String otherBlobId) {
        String headContents;
        String otherContents;
        if (headBlobId == null) {
            headContents = "";
        } else {
            headContents = readContentsAsString(join(BLOBS_DIR, headBlobId));
        }
        if (otherBlobId == null) {
            otherContents = "";
        } else {
            otherContents = readContentsAsString(join(BLOBS_DIR, otherBlobId));
        }
        return "<<<<<<< HEAD\n" + headContents + "=======\n" + otherContents + ">>>>>>>\n";
    }

    /**
     * Return the split point commit.
     */

    private String findSplit(String currentCommitId, String mergedCommitId) {
        Set<String> commitSet = new HashSet<String>();
        Queue<String> bfsQueue = new ArrayDeque<String>();
        bfsQueue.add(currentCommitId);
        while (!bfsQueue.isEmpty()) {
            String commitId = bfsQueue.remove();
            Commit commit = Commit.findCommit(commitId);
            commitSet.add(commitId);
            if (commit.getParentID1() != null) {
                bfsQueue.add(commit.getParentID1());
            }
            if (commit.getParentID2() != null) {
                bfsQueue.add(commit.getParentID2());
            }
        }

        bfsQueue.add(mergedCommitId);
        while (!bfsQueue.isEmpty()) {
            String commitId = bfsQueue.remove();
            Commit commit = Commit.findCommit(commitId);
            if (commitSet.contains(commitId)) {
                return commitId;
            }
            if (commit.getParentID1() != null) {
                bfsQueue.add(commit.getParentID1());
            }
            if (commit.getParentID2() != null) {
                bfsQueue.add(commit.getParentID2());
            }
        }

        return null;
    }

    /**
     * Return the head Commit of the given branch.
     */
    public Commit loadHead(String branch) {
        return Commit.findCommit(Branch.getCommitID(branch));
    }


    /**
     * Exit with given error message
     */
    private void error(String message) {
        System.out.println(message);
        System.exit(0);
    }

    /**
     * Check untracked file and throw error
     */

    private void checkUntracked(Commit commit) {
        List<String> untrackedFiles = getUntrackedFiles();
        Map<String, String> tracked = commit.getBlobs();
        if (!untrackedFiles.isEmpty()) {
            for (String fileName : untrackedFiles) {
                if (tracked.containsKey(fileName)) {
                    error("There is an untracked file in the way; delete it, "
                            + "or add and commit it first.");
                }
            }
        }
    }

    private List<String> getUntrackedFiles() {
        List<String> result = new ArrayList<String>();
        List<String> cwd = Utils.plainFilenamesIn(CWD);
        Commit curr = loadHead(getBranchName());
        List<String> stage = Stage.load();

        for (String fileName : cwd) {
            boolean tracked = curr.getBlobs().containsKey(fileName);
            boolean staged = stage.contains(fileName);
            // untracked files
            if (!staged && !tracked) {
                result.add(fileName);
            }
        }
        Collections.sort(result);
        return result;
    }


    /**
     * Return the Head Branch Name
     */
    public String getBranchName() {
        return readContentsAsString(HEAD_BRANCH);
    }

    /**
     * Update the Head Branch Name
     */
    private void setBranchName(String branchName) {
        writeContents(HEAD_BRANCH, branchName);
    }

    /**
     * Helper Function for Make Directory
     */
    private void mkdir(File dir) {
        if (!dir.mkdir()) {
            throw new IllegalArgumentException(String.format("mkdir: %s: Failed to create.",
                    dir.getPath()));
        }
    }

    /**
     * Helper Function for delete File
     */
    private boolean deleteFile(File file) {
        if (!file.isDirectory()) {
            if (file.exists()) {
                return file.delete();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}

package gitlet;

import java.io.File;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Repository.STAGE_DIR;

public class Stage {

    /** Save the filename and content to the stage area*/
    public static void saveFile(String fileName, String content) {
        File temp = join(STAGE_DIR, fileName);
        writeContents(temp, content);
    }

    /** Remove the file from the stage area*/
    public static void removeFile(String fileName) {
        if (join(STAGE_DIR, fileName).exists()) {
            join(STAGE_DIR, fileName).delete();
        }
    }

    /** Return a list of filenames at the stage area*/
    public static List<String> load() {
        return plainFilenamesIn(STAGE_DIR);
    }

    public static void clear() {
        List<String> name = load();
        for (String n : name) {
            removeFile(n);
        }
    }
}

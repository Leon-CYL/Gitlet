package gitlet;
import java.io.File;
import java.util.List;
import static gitlet.Utils.*;
import static gitlet.Repository.REMOVAL_DIR;


public class Removal {

    public static void saveFile(String fileName, String content) {
        File temp = join(REMOVAL_DIR, fileName);
        writeContents(temp, content);
    }

    public static List<String> getFile() {
        return plainFilenamesIn(REMOVAL_DIR);
    }

    public static void removeFile(String fileName) {
        if (join(REMOVAL_DIR, fileName).exists()) {
            join(REMOVAL_DIR, fileName).delete();
        }
    }

    public static void clear() {
        List<String> files = getFile();
        for (String file : files) {
            removeFile(file);
        }
    }
}

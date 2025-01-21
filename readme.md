# Gitlet Design Document

**Name**: ChaoYuan(Leon) Lin

## Description:

> This project is from UC Berkeley's CS 61B(Data Structures & Algorithms) Spring 2021 version and passed all the tests(Expect the tests for extra credits) from GradeScope.

> Gitlet is a local version control system that contain similar basic features of the popular system Git with smaller and simpler functionalities.

> Full Spec: [Gitlet](https://sp21.datastructur.es/materials/proj/proj2/proj2)

## Classes

### Blob.Java

#### Description: 
> This class store all the blobs in the BLOB_DIR with a unique blob ID and the contents of the blob.

### Branch.Java

#### Description:
> This class store all the branch names with the head commit ID of each branch in BRANCH_DIR.

### Commit.Java

#### Description:
> This class store all the commits with a message, a unique commit ID parent IDs, date and a hashmap that contain filename as key and 
> blob ID as value in the COMMIT_DIR.

### Removal.Java

#### Description:

> This class store all the removed files in the REMOVAL_DIR.

### Stage.Java

#### Description:

> This class store all the files that are ready to commit in the STAGE_DIR.


## Command

### init

#### Description:
Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message "initial commit".

> java gitlet.Main init

### add

#### Description:
Adds a copy of the file as it currently exists to the staging area. Staging an already-staged file overwrites the previous entry in the staging area with the new contents.

> java gitlet.Main add [file name]

### commit

#### Description:
Save all the tracked files from the parent commit and overwrite the contents of the tracked files from the adding stage.

> java gitlet.Main commit [message]

### rm

#### Description:
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory.

> java gitlet.Main rm [file name]

### log

#### Description:
Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit.

> java gitlet.Main log

### global-log

#### Description:
Like log, except displays information about all commits ever made. The order of the commits does not matter.

> java gitlet.Main global-log

### find

#### Description:
Prints out the ids of all commits that have the given commit message, one per line.

> java gitlet.Main find [commit message]

### status

#### Description:
Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal.

> java gitlet.Main status

### checkout

#### Description:
1. Takes the version of the file as it exists in the head commit and putsit in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch.

> java gitlet.Main checkout -- [file name]

> java gitlet.Main checkout [commit id] -- [file name]
 
> java gitlet.Main checkout [branch name]

### branch

#### Description:
Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference to a commit node.

> java gitlet.Main branch [branch name]


### rm-branch

#### Description:
Deletes the branch with the given name.

> java gitlet.Main rm-branch [branch name]

### reset

#### Description:
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node.

> java gitlet.Main reset [commit id]

### merge

#### Description:
Merges files from the given branch into the current branch.

> java gitlet.Main merge [branch name]

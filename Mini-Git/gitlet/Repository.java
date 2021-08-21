package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Varun Nair & Agrima Sharma
 */
public class Repository {
    private static String HEAD;
    private static HashMap<String, String> branches = new HashMap<>();
    private static ArrayList<String> commits = new ArrayList();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");

    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static void initializedCheck() {
        List<String> initFiles = Utils.plainFilenamesIn(GITLET_DIR);
        if (initFiles == null) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() {
        List<String> dirFiles = Utils.plainFilenamesIn(GITLET_DIR);
        if (dirFiles != null) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();

            Commit initCommit = new Commit("initial commit", new Date(0));
            byte[] initCommitByte = Utils.serialize(initCommit);
            String initCommitHash = Utils.sha1(initCommitByte, "commit");

            File initialCommitFile = Utils.join(GITLET_DIR, initCommitHash);
            Utils.writeObject(initialCommitFile, initCommit);

            String master = initCommitHash;
            commits.add(initCommitHash);
            branches.put("master", initCommitHash);
            HEAD = master;
            String currentBranchName = "master";

            File branchesFile = Utils.join(GITLET_DIR, "branches");
            Utils.writeObject(branchesFile, branches);
            File currentBranchFile = Utils.join(GITLET_DIR, "HEAD.txt");
            Utils.writeContents(currentBranchFile, HEAD);
            File currentBranchNameFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
            Utils.writeContents(currentBranchNameFile, currentBranchName);
            File commitsFile = Utils.join(GITLET_DIR, "commits");
            Utils.writeObject(commitsFile, commits);

            HashMap<String, String> stagedForAddition = new HashMap<>();
            HashMap<String,String> stagedForRemoval = new HashMap<>();

            File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
            File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");

            Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
        }
    }

    public static void add(String fileName) {
        initializedCheck();
        List<String> Files = Utils.plainFilenamesIn(CWD);
        if (!(Files.contains(fileName))) {
            System.out.print("File does not exist.");
            System.exit(0);
        } else {

            File fileToBeAdded = Utils.join(CWD, fileName);
            byte[] fileToBeAddedByte = Utils.readContents(fileToBeAdded);
            String fileToAddHash = Utils.sha1(fileToBeAddedByte, "blob");

            File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);
            File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
            String head = Utils.readContentsAsString(headFile);
            File currentHeadFile = Utils.join(GITLET_DIR, head);
            Commit currentCommit = Utils.readObject(currentHeadFile, Commit.class);

            if (currentCommit.getFileBlob().containsKey(fileName)) {
                if (currentCommit.getFileBlob().get(fileName).equals(fileToAddHash)) {
                    stagedForAddition.remove(fileName);
                } else {
                    File fileToAddBlob = Utils.join(GITLET_DIR, fileToAddHash);
                    Utils.writeContents(fileToAddBlob, fileToBeAddedByte);
                    stagedForAddition.put(fileName, fileToAddHash);
                }
            } else {
                File fileToBeAddedBlob = Utils.join(GITLET_DIR, fileToAddHash);
                Utils.writeContents(fileToBeAddedBlob, fileToBeAddedByte);
                stagedForAddition.put(fileName, fileToAddHash);
            }
            Utils.writeObject(stagedForAdditionFile, stagedForAddition);


            File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile, HashMap.class);
            if (stagedForRemoval.containsKey(fileName)) {
                stagedForRemoval.remove(fileName);
            }
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);

        }
    }

    public static void commit(String message) {
        initializedCheck();
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else {
            File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);
            File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile, HashMap.class);

            if (stagedForAddition.size() == 0 && stagedForRemoval.size() == 0) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }

            File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
            String head = Utils.readContentsAsString(headFile);
            File currentHeadFile = Utils.join(GITLET_DIR, head);
            Commit currentCommit = Utils.readObject(currentHeadFile, Commit.class);
            Commit newCommit = new Commit(message, new Date(), currentCommit.getFileBlob(), head);

            for (Object i: stagedForAddition.keySet()) {
                newCommit.getFileBlob().put(i, stagedForAddition.get(i));
            }

            for (Object i: stagedForRemoval.keySet()) {
                newCommit.getFileBlob().remove(i);
            }


            stagedForAddition.clear();
            stagedForRemoval.clear();

            Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);

            byte[] newCommitByte = Utils.serialize(newCommit);
            String newCommitHash = Utils.sha1(newCommitByte, "commit");
            File newCommitFile = Utils.join(GITLET_DIR, newCommitHash);
            Utils.writeObject(newCommitFile, newCommit);
            Utils.writeContents(headFile, newCommitHash);

            ArrayList<String> commitList = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
            commitList.add(newCommitHash);
            Utils.writeObject(Utils.join(GITLET_DIR, "commits"), commitList);

            File currentBranchNameFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
            String currentBranchName = Utils.readContentsAsString(currentBranchNameFile);
            File branchMapFile = Utils.join(GITLET_DIR, "branches");
            HashMap branchMap = Utils.readObject(branchMapFile, HashMap.class);
            branchMap.put(currentBranchName, newCommitHash);
            Utils.writeObject(branchMapFile, branchMap);
        }
    }

    public static void checkout(String fileName) {
        initializedCheck();
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        Commit currentCommit = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);

        if (!(currentCommit.getFileBlob().containsKey(fileName))) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            String recovered = (String) currentCommit.getFileBlob().get(fileName);
            byte[] fileRecovered = Utils.readContents(Utils.join(GITLET_DIR, recovered));
            File fileToAdd = Utils.join(CWD, fileName);
            Utils.writeContents(fileToAdd, fileRecovered);
        }
    }

    public static void checkoutWithCommitID(String commitID, String fileName) {
        initializedCheck();
        ArrayList previousCommits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> iter = previousCommits.iterator();
        int length = commitID.length();
        if (length < 40) {
            while (iter.hasNext()) {
                String nextFile = iter.next();
                if (nextFile.length() >= length && nextFile.substring(0, length).equals(commitID)) {
                    commitID = nextFile;
                }
            }
        }
        if (!(previousCommits.contains(commitID))) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit currentCommit = Utils.readObject(Utils.join(GITLET_DIR, commitID), Commit.class);

            if (!(currentCommit.getFileBlob().containsKey(fileName))) {
                System.out.println("File does not exist in that commit.");
            } else {
                String recovered = (String) currentCommit.getFileBlob().get(fileName);
                byte[] fileRecovered = Utils.readContents(Utils.join(GITLET_DIR, recovered));
                File fileToAdd = Utils.join(CWD, fileName);
                Utils.writeContents(fileToAdd, fileRecovered);
            }
        }
    }

    public static void checkoutBranch(String branchName) {
        List<String> filesInDirectory = Utils.plainFilenamesIn(CWD);
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        File currentHeadFile = Utils.join(GITLET_DIR, head);
        Commit currentCommit = Utils.readObject(currentHeadFile, Commit.class);

        File branchesDir = Utils.join(GITLET_DIR, "branches");
        HashMap branchesMap = Utils.readObject(branchesDir, HashMap.class);
        File currBranchNameFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
        String currBranchName = Utils.readContentsAsString(currBranchNameFile);

        if (!branchesMap.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(currBranchName)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit branchCommit = Utils.readObject(Utils.join(GITLET_DIR, (String) branchesMap.get(branchName)), Commit.class);
            Iterator<String> branchesIter = filesInDirectory.iterator();

            while (branchesIter.hasNext()) {
                String nextFileInBranch = branchesIter.next();
                if (!currentCommit.getFileBlob().containsKey(nextFileInBranch)
                        && branchCommit.getFileBlob().containsKey(nextFileInBranch)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                } else if (currentCommit.getFileBlob().containsKey(nextFileInBranch) && !branchCommit.getFileBlob().containsKey(nextFileInBranch)) {
                    Utils.restrictedDelete(Utils.join(CWD, nextFileInBranch));
                }
            }

            Iterator<String> branchBlobIter = branchCommit.getFileBlob().keySet().iterator();
            while (branchBlobIter.hasNext()) {
                String BranchBlob = branchBlobIter.next();
                File BranchFile = Utils.join(CWD, BranchBlob);
                Utils.writeContents(BranchFile, Utils.readContents(Utils.join(GITLET_DIR, (String) branchCommit.getFileBlob().get(BranchBlob))));
            }

            File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);
            File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile,HashMap.class);
            stagedForAddition.clear();
            stagedForRemoval.clear();

            Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
            Utils.writeContents(headFile, branchesMap.get(branchName));
            Utils.writeContents(currBranchNameFile, branchName);
        }

    }

    public static void log() {
        initializedCheck();
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        Commit currentCommit = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
        while (head != null) {
            currentCommit = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
            System.out.println("===");
            System.out.println("commit " + head);
            System.out.println("Date: " + dateFormat.format(currentCommit.getTimestamp()));
            System.out.println(currentCommit.getMessage());
            System.out.println();
            head = currentCommit.getParent();
        }
    }

    public static void rm(String fileName) {
        initializedCheck();

        File fileToBeRemoved = Utils.join(CWD, fileName);
        List<String> filesInDirectory = Utils.plainFilenamesIn(CWD);

        File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);

        File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile, HashMap.class);

        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        File currentHeadFile = Utils.join(GITLET_DIR, head);
        Commit currentCommit = Utils.readObject(currentHeadFile, Commit.class);

        if (stagedForAddition.containsKey(fileName) || currentCommit.getFileBlob().containsKey(fileName)) {
            if (stagedForAddition.containsKey(fileName)) {
                stagedForAddition.remove(fileName);
                Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            }
            if (currentCommit.getFileBlob().containsKey(fileName)) {
                if (filesInDirectory.contains(fileName)){
                    byte[] removedFileByte = Utils.readContents(fileToBeRemoved);
                    String removedFileHash = Utils.sha1(removedFileByte, "blob");
                    stagedForRemoval.put(fileName, removedFileHash);
                    Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
                    Utils.restrictedDelete(Utils.join(CWD, fileName));
                } else {
                    stagedForRemoval.put(fileName, "");
                    Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
                }
            }
        } else {
            System.out.println("No reason to remove the file.");
        }


    }

    public static void global_log() {
        ArrayList<String> commits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> commitsIter = commits.iterator();

        while (commitsIter.hasNext()) {
            String commitHash = commitsIter.next();
            File commitFile = Utils.join(GITLET_DIR, commitHash);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitHash);
            System.out.println("Date: " + dateFormat.format(commit.getTimestamp()));
            System.out.println(commit.getMessage());
            System.out.println("");
        }
    }

    public static void branch(String branchName) {
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);

        File branchesDir = Utils.join(GITLET_DIR, "branches");
        HashMap branchesMap = Utils.readObject(branchesDir, HashMap.class);

        if (branchesMap.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branchesMap.put(branchName, head);
            Utils.writeObject(branchesDir, branchesMap);
        }

    }


    public static void find(String Message) {
        ArrayList commits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> commitIter = commits.iterator();
        boolean messageMatches = false;
        while (commitIter.hasNext()) {
            String nextCommitHash = commitIter.next();
            File nextCommitFile = Utils.join(GITLET_DIR, nextCommitHash);
            Commit nextCommit = Utils.readObject(nextCommitFile, Commit.class);
            if (nextCommit.getMessage().equals(Message)) {
                System.out.println(nextCommitHash);
                messageMatches = true;
            }
        }
        if (messageMatches == false) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void rm_branch(String branchName) {
        File branchDir = Utils.join(GITLET_DIR, "branches");
        HashMap branchesMap = Utils.readObject(branchDir, HashMap.class);
        File currentBranchFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
        String currentBranchName = Utils.readContentsAsString(currentBranchFile);

        if (!branchesMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branchesMap.remove(branchName);
            Utils.writeObject(branchDir, branchesMap);
        }
    }

    public static void status() {
        File branchDir = Utils.join(GITLET_DIR, "branches");
        HashMap branchesMap = Utils.readObject(branchDir, HashMap.class);
        File currentBranchFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
        String currentBranchName = Utils.readContentsAsString(currentBranchFile);


        System.out.println("=== Branches ===");
        Iterator<String> branchesIter = branchesMap.keySet().iterator();
        ArrayList<String> branchesList = new ArrayList<>();
        while (branchesIter.hasNext()) {
            branchesList.add(branchesIter.next());
        }
        Collections.sort(branchesList);
        Iterator<String> branchesListIter = branchesList.iterator();
        while (branchesListIter.hasNext()) {
            String nextBranch = branchesListIter.next();
            if (nextBranch.equals(currentBranchName)) {
                System.out.println("*" + nextBranch);
            } else {
                System.out.println(nextBranch);
            }
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
        HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);
        Iterator<String> additionStagedIter = stagedForAddition.keySet().iterator();
        ArrayList<String> additionStagedList = new ArrayList<>();
        while (additionStagedIter.hasNext()) {
            additionStagedList.add(additionStagedIter.next());
        }
        Collections.sort(additionStagedList);
        Iterator<String> additionStagedListIter = additionStagedList.iterator();
        while (additionStagedListIter.hasNext()) {
            System.out.println(additionStagedListIter.next());
        }
        System.out.println("");


        System.out.println("=== Removed Files ===");
        File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
        HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile, HashMap.class);
        Iterator<String> removeIter = stagedForRemoval.keySet().iterator();
        ArrayList<String> removeList = new ArrayList<>();
        while (removeIter.hasNext()) {
            removeList.add(removeIter.next());
        }
        Collections.sort(removeList);
        Iterator<String> removalListIter = removeList.iterator();
        while (removalListIter.hasNext()) {
            System.out.println(removalListIter.next());
        }
        System.out.println("");
        
        

        System.out.println("=== Modifications Not Staged For Commit ===");
        File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
        String head = Utils.readContentsAsString(headFile);
        Commit currentCommit = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);
        List<String> filesInDirectory = Utils.plainFilenamesIn(CWD);
        ArrayList<String> modifiedList = new ArrayList<>();
        ArrayList<String> deletedList = new ArrayList<>();
        ArrayList<String> MandDList = new ArrayList<>();
        Iterator<String> currentCommitIter = currentCommit.getFileBlob().keySet().iterator();
        while (currentCommitIter.hasNext()) {
            String nextTrackedFile = currentCommitIter.next();
            if (filesInDirectory.contains(nextTrackedFile)) {
                File cwdTrackedFile = Utils.join(CWD, nextTrackedFile);
                byte[] cwdTracked = Utils.readContents(cwdTrackedFile);
                String cwdTrackedHash = Utils.sha1(cwdTracked, "blob");
                if (!cwdTrackedHash.equals(currentCommit.getFileBlob().get(nextTrackedFile)) && !stagedForAddition.containsKey(nextTrackedFile)) {
                    if (!MandDList.contains(nextTrackedFile)) {
                        MandDList.add(nextTrackedFile);
                        modifiedList.add(nextTrackedFile);
                    }
                }
            } else {
                if (!stagedForRemoval.containsKey(nextTrackedFile)) {
                    if (!MandDList.contains(nextTrackedFile)) {
                        MandDList.add(nextTrackedFile);
                        deletedList.add(nextTrackedFile);
                    }
                }
            }
        }

        Iterator<String> stagedAdditionIter = stagedForAddition.keySet().iterator();
        while (stagedAdditionIter.hasNext()) {
            String nextStagedAdditionFile = stagedAdditionIter.next();
            if (filesInDirectory.contains(nextStagedAdditionFile)) {
                File cwdAdditionFile = Utils.join(CWD, nextStagedAdditionFile);
                byte[] cwdAdded = Utils.readContents(cwdAdditionFile);
                String cwdAddedHash = Utils.sha1(cwdAdded, "blob");
                if (!cwdAddedHash.equals(stagedForAddition.get(nextStagedAdditionFile))) {
                    if (!MandDList.contains(nextStagedAdditionFile)) {
                        MandDList.add(nextStagedAdditionFile);
                        modifiedList.add(nextStagedAdditionFile);
                    }
                }
            } else {
                if (!MandDList.contains(nextStagedAdditionFile)) {
                    MandDList.add(nextStagedAdditionFile);
                    deletedList.add(nextStagedAdditionFile);
                }
            }
        }

        Collections.sort(MandDList);
        Iterator<String> bothIter = MandDList.iterator();
        while (bothIter.hasNext()) {
            String nextBothFile = bothIter.next();
            if (modifiedList.contains(nextBothFile)) {
                System.out.println(nextBothFile + " (modified)");
            } else if (deletedList.add(nextBothFile)) {
                System.out.println(nextBothFile + " (deleted)");
            }
        }
        System.out.println("");


        System.out.println("=== Untracked Files ===");
        Iterator<String> cwdIter = filesInDirectory.iterator();
        ArrayList<String> untrackedFilesList = new ArrayList<>();
        while (cwdIter.hasNext()) {
            String nextCwdFile = cwdIter.next();
            if (!stagedForAddition.containsKey(nextCwdFile) && !currentCommit.getFileBlob().containsKey(nextCwdFile)) {
                untrackedFilesList.add(nextCwdFile);
            }
        }
        Collections.sort(untrackedFilesList);
        Iterator<String> untrackedIter = untrackedFilesList.iterator();
        while (untrackedIter.hasNext()) {
            System.out.println(untrackedIter.next());
        }
        System.out.println("");

    }

    public static void reset(String commitID) {
        ArrayList allCommits = Utils.readObject(Utils.join(GITLET_DIR, "commits"), ArrayList.class);
        Iterator<String> commitsIter = allCommits.iterator();
        int length = commitID.length();
        if (length < 40) {
            while (commitsIter.hasNext()) {
                String nextFile = commitsIter.next();
                if (nextFile.length() >= length && nextFile.substring(0, length).equals(commitID)) {
                    commitID = nextFile;
                }
            }
        }
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            List<String> filesInDirectory = Utils.plainFilenamesIn(CWD);
            Commit resetCommit = Utils.readObject(Utils.join(GITLET_DIR, commitID), Commit.class);
            File headFile = Utils.join(GITLET_DIR, "HEAD.txt");
            String head = Utils.readContentsAsString(headFile);
            Commit currentCommit = Utils.readObject(Utils.join(GITLET_DIR, head), Commit.class);

            Iterator<String> cwdIter = filesInDirectory.iterator();
            while (cwdIter.hasNext()) {
                String nextCwdFile = cwdIter.next();
                if (!currentCommit.getFileBlob().containsKey(nextCwdFile)
                        && resetCommit.getFileBlob().containsKey(nextCwdFile)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                } else if (currentCommit.getFileBlob().containsKey(nextCwdFile) && !resetCommit.getFileBlob().containsKey(nextCwdFile)) {
                    Utils.restrictedDelete(Utils.join(CWD, nextCwdFile));
                }
            }
            Iterator<String> resetCommitIter = resetCommit.getFileBlob().keySet().iterator();
            while (resetCommitIter.hasNext()) {
                checkoutWithCommitID(commitID, resetCommitIter.next());
            }

            File branchesDir = Utils.join(GITLET_DIR, "branches");
            HashMap branchMap = Utils.readObject(branchesDir, HashMap.class);
            File currentBranchNameFile = Utils.join(GITLET_DIR, "currentBranchName.txt");
            String currentBranchName = Utils.readContentsAsString(currentBranchNameFile);
            branchMap.put(currentBranchName, commitID);
            Utils.writeObject(branchesDir, branchMap);
            Utils.writeContents(headFile, commitID);

            File stagedForAdditionFile = Utils.join(GITLET_DIR, "stagedForAddition");
            HashMap stagedForAddition = Utils.readObject(stagedForAdditionFile, HashMap.class);
            File stagedForRemovalFile = Utils.join(GITLET_DIR, "stagedForRemoval");
            HashMap stagedForRemoval = Utils.readObject(stagedForRemovalFile, HashMap.class);
            stagedForAddition.clear();
            stagedForRemoval.clear();
            Utils.writeObject(stagedForAdditionFile, stagedForAddition);
            Utils.writeObject(stagedForRemovalFile, stagedForRemoval);
        }
    }

    public static void merge(String branchName) {

    }


}



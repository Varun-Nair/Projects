package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Varun Nair and Agrima Sharma
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgs(args, 1);
                Repository.init();
                break;
            case "add":
                checkArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "checkout":
                Repository.initializedCheck();
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.checkout(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.checkoutWithCommitID(args[1], args[3]);
                } else {
                    checkArgs(args, 2);
                    Repository.checkoutBranch(args[1]);
                }
                break;
            case "log":
                checkArgs(args, 1);
                Repository.log();
                break;
            case "rm":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "global-log":
                Repository.initializedCheck();
                checkArgs(args, 1);
                Repository.global_log();
                break;
            case "branch":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.branch(args[1]);
                break;


            case "find":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                Repository.initializedCheck();
                checkArgs(args, 1);
                Repository.status();
                break;
            case "rm-branch":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.initializedCheck();
                checkArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }


    }

    public static void checkArgs(String args[], int n) {
        if (args.length != n) {
            System.out.println("Incorrect number of Arguments passed for the command");
            System.exit(0);
        }
    }
}

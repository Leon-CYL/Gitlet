package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author ChaoYuan Lin
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        int len = args.length;
        Repository gitlet = new Repository();

        switch(firstArg) {
            case "init":
                numCommands(1, len);
                gitlet.init();
                break;

            case "add":
                numCommands(2, len);
                gitlet.add(args[1]);
                break;

            case "commit":
                numCommands(2, len);
                String message = args[1];
                gitlet.commit(message);
                break;

            case "rm":
                numCommands(2, len);
                gitlet.rm(args[1]);
                break;

            case "log":
                numCommands(1, len);
                gitlet.log();
                break;

            case "branch":
                numCommands(2, len);
                gitlet.branch(args[1]);
                break;

            case "checkout":
                if (len == 2) {
                    gitlet.checkoutBranch(args[1]);
                } else if (len == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    gitlet.checkoutFile(gitlet.loadHead(gitlet.getBranchName()).getID() ,args[2]);
                } else if (len == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    gitlet.checkoutFile(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;

            case "global-log":
                numCommands(1, len);
                gitlet.globalLog();
                break;

            case "find":
                numCommands(2, len);
                gitlet.find(args[1]);
                break;

            case "status":
                numCommands(1, len);
                gitlet.status();
                break;

            case "rm-branch":
                numCommands(2, len);
                gitlet.rmBranch(args[1]);
                break;

            case "reset":
                numCommands(2, len);
                gitlet.reset(args[1]);
                break;

            case "merge":
                numCommands(2, len);
                gitlet.merge(args[1]);
                break;

            default :
                System.out.println("No command with that name exists.");
                break;
        }
    }

    public static void numCommands(int expect, int len) {
        if (len != expect) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}

import java.util.Scanner;
import java.io.File;

class Parser
{

    private String commandName;
    private String[] args;

    public boolean parse(String input)
    {

        //remove any trailing spaces
        input = input.trim();

        if(input.isEmpty()) return false;

        String[] parts = input.split(" ");

        //the command is always written first thats why we take parts first index
        commandName = parts[0];

        //check if there is arguments inserted
        if(parts.length > 1)
        {

            args = new String[parts.length - 1];

            for(int i = 1; i < parts.length; i++) args[i-1] = parts[i];

        }
        else args = new String[0]; //if no arguments make empty arguments array

        return true;

    }
    public String getCommandName() {return commandName;}
    public String[] getArgs() {return args;}

}
class Terminal
{

    private Parser parser;
    private String currentDirectory; //a variable to save the current directory to use in implementing cd

    Terminal()
    {

        parser = new Parser();
        currentDirectory = System.getProperty("user.dir"); //saves current path
        // into the variable when an instance of terminal is created

    }

    public String pwd() {return currentDirectory;}
    public void cd(String[] args)
    {

        //to handle directories with space in their name like "Operating Systems" as it should be read as 1 arg not multiple ones
        String path = String.join(" ", args);

        //if no args change dir to home dir
        if(args.length == 0) System.out.println(System.getProperty("user.home"));
        else if(args[0].equals("..")) //if arg = .. go to parent dir
        {

            //create file object from the path
            File current = new File(currentDirectory);
            String parent = current.getParent();

            if(parent != null)
            {

                currentDirectory = parent;

                System.out.println(currentDirectory);

            }
            else System.out.println("Error: Already at root directory");

        }
        else
        {

            File newPath = new File(path); //create file object from the path

            //if its short path (relative), combine it with the current path
            if(!newPath.isAbsolute()) newPath = new File(currentDirectory, args[0]);
            if(newPath.exists() && newPath.isDirectory()) //make sure this path exists and that its a directory
            {

                currentDirectory = newPath.getAbsolutePath();

                System.out.println(currentDirectory);

            }
            else System.out.println("Error: Directory does not exist");

        }

    }
    public void chooseCommandAction()
    {

        if(parser.getCommandName().equals("pwd")) System.out.println(pwd());
        else if(parser.getCommandName().equals("cd")) cd(parser.getArgs());

    }
    public static void main(String[] args)
    {

        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);

        while(true)
        {

            System.out.print("> ");
            System.out.flush();

            String input = scanner.nextLine();

            if(input.equals("exit")) break;

            if(terminal.parser.parse(input)) terminal.chooseCommandAction();
            else
            {

                System.out.println("error");

            }

        }

        scanner.close();

    }

}
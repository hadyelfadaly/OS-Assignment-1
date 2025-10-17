import java.util.Objects;
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

        if(input == null || input.isEmpty()) return false;

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
    public String getCommandName(){return commandName;}
    public String[] getArgs(){return args;}

}
class Terminal
{

    Parser parser;
    private String currentDirectory; //a variable to save the current directory to use in implementing cd

    Terminal()
    {

        parser = new Parser();
        currentDirectory = new String(System.getProperty("user.dir"));//saves current path into it

    }

    public String pwd() {return currentDirectory;}
    public void cd(String[] args)
    {

        //to handle directories with space in their name
        String path = String.join(" ", args);

        if(args.length == 0) System.out.println(System.getProperty("user.home"));
        else if(args[0].equals(".."))
        {

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
            //make sure this path exists and that its a directory
            if(newPath.exists() && newPath.isDirectory())
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
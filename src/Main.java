import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;

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
    public void mkdir(String[] args)
    {

        //to handle directories with space in their name like "Operating Systems" as it should be read as 1 arg not multiple ones
        String path = String.join(" ", args);
        File testPath = new File(path);

        if(args.length == 0)
        {

            System.out.println("mkdir: missing operand");

            return;

        }
        else if(args.length == 1)
        {

            //create file object of our arg
            File ourArg =  new File(args[0]);

            //if the argument is a path
            if(ourArg.isAbsolute())
            {

                //if this path doesnt exist create the new dir in the end of the path
                if(!ourArg.exists())
                {

                    ourArg.mkdir();

                    return;

                }
                else
                {

                    System.out.println("mkdir: cannot create directory " + args[0] + " File exists");

                    return;

                }

            }
            else //argument is just a directory name
            {

                ourArg = new File(currentDirectory, args[0]); //creates a new path for the intended file we want to create

                //if the dir of same name already exists
                if(ourArg.exists() && ourArg.isDirectory())
                {

                    System.out.println("mkdir: cannot create directory " + args[0] + " File exists");

                    return;

                }
                else if(!ourArg.exists()) //if dir doesnt exist create it in current path
                {

                    ourArg.mkdir();

                    return;

                }

            }

        }

        if(testPath.isAbsolute())
        {

            //if this path doesnt exist create the new dir in the end of the path
            if(!testPath.exists())
            {

                testPath.mkdir();

                return;

            }
            else
            {

                System.out.println("mkdir: cannot create directory " + path + " File exists");

                return;

            }

        }


        //array to store the dirs we gonna create (ArrayList for dynamic size);
        ArrayList<String> newDirs = new ArrayList<>();

        for(String s : args)
        {

            //create file object of the dir or path
            File file = new File(s);

            if(!file.isDirectory()) newDirs.add(s);
            else if(file.isDirectory() || file.isAbsolute())
            {

                //if(newDirs.size() == 1)

            }

        }

    }
    public void ls(String[] args)
    {

        if(args.length > 0)
        {

            System.out.println("this command takes no arguments"); // Check for arguments

            return;

        }

        File dir = new File(currentDirectory); // find current directory
        String[] contents = dir.list(); //  array of file names.

        if(contents != null)
        {

            List<String> list = Arrays.asList(contents); // sort alphabetically

            Collections.sort(list);

            for(String item : list) System.out.print(item + "  "); // print items

            System.out.print("\n"); //start from a new line when it finishes listing

        }
        else System.out.println("Error: Can't list directory contents");

    }
    public void cp(String[] args)
    {

        if(args.length != 2)
        {

            System.out.println("cp: must be 2 arguments"); //validate the arguments must be cp <source> <destination>

            return;

        }

        if(args[0].equals("-r"))
        {

            System.out.println("cp: missing arguments");

            return;

        }

        //make file objects of given arguments
        File source = new File(args[0]);
        File destination = new File(args[1]);

        //construct paths if not given fully
        if(!source.isAbsolute()) source = new File(currentDirectory, args[0]);
        if(!destination.isAbsolute()) destination = new File(currentDirectory, args[1]);

        if(!source.exists() || !source.isFile())  //make sure source file exists
        {

            System.out.println("cp: source file does not exist");

            return;
        }


        try
        {

            //using toPath() function to convert file object to path object
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING); //copy the file

        }
        catch(IOException error) //catches input or output or file operations errors
        {

            System.out.println("cp: error");

        }

    }
    public void cp_r(String[] args)
    {

        if(args.length != 3 || !args[0].equals("-r")) //validate arguments and format
        {

            System.out.println("cp -r: must be cp -r <sourceDir> <destinationDir>");

            return;

        }

        //make file objects of given arguments
        File sourceDir = new File(args[1]);
        File destDir = new File(args[2]);

        //construct paths if not given fully
        if(!sourceDir.isAbsolute()) sourceDir = new File(currentDirectory, args[1]);
        if(!destDir.isAbsolute()) destDir = new File(currentDirectory, args[2]);

        if(!sourceDir.exists() || !sourceDir.isDirectory()) //check if the source exists and it is a directory
        {

            System.out.println("cp -r: source is not a valid ");

            return;

        }

        try
        {

            //helper function to copy directories
            copyDirectoryRecursive(sourceDir, new File(destDir, sourceDir.getName()));

        }
        catch(IOException error) //catches input or output or file operations errors
        {

            System.out.println("cp -r: error copying directory");

        }

    }
    //helper function for recursive copy
    private void copyDirectoryRecursive(File source, File dest) throws IOException
    {

        //create dest dir if it does not exist
        if(!dest.exists()) dest.mkdir();

        //array of all contents in the source
        File[] files = source.listFiles();

        if(files != null)
        {

            //copy file by file
            for(File file : files)
            {

                File newDest = new File(dest, file.getName());

                //if the file is a directory call the function recursively
                if(file.isDirectory()) copyDirectoryRecursive(file, newDest);
                else Files.copy(file.toPath(), newDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            }

        }

    }

    public void chooseCommandAction()
    {

        if(parser.getCommandName().equals("pwd")) System.out.println(pwd());
        else if(parser.getCommandName().equals("cd")) cd(parser.getArgs());
        else if(parser.getCommandName().equals("mkdir")) mkdir(parser.getArgs());
        else if(parser.getCommandName().equals("ls")) ls(parser.getArgs());
        else if(parser.getCommandName().equals("cp"))
        {
            String[] arguments = parser.getArgs();

            if(arguments.length > 0 && arguments[0].equals("-r")) cp_r(arguments);
            else cp(arguments);

        }

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
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;
import java.io.FileInputStream;

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

        //the command is always written first that's why we take parts first index
        commandName = parts[0];

        //check if there is arguments inserted
        if(parts.length > 1)
        {

            args = new String[parts.length - 1];

            for(int i = 1; i < parts.length; i++)
            {

                if(parts[i].charAt(0) == '\'')
                {

                    //if argument name is between quotes
                    if(parts[i].charAt(parts[i].length()-1) == '\'')
                    {

                        parts[i] = parts[i].substring(1,  parts[i].length()-1); //argument without the quotes
                        args[i-1] = parts[i];

                    }
                    else
                    {

                        int j = i;
                        ArrayList<String> quotedArguments = new ArrayList<>(); //arraylist to hold the argument between quotes

                        //add first word without its starting '
                        quotedArguments.add(parts[j].substring(1));
                        j++;

                        //add in between words
                        while(parts[j].charAt(parts[j].length()-1) != '\'')
                        {

                            quotedArguments.add(parts[j]);
                            j++;

                        }

                        //add last word without last '
                        quotedArguments.add(parts[j].substring(0, parts[j].length()-1));

                        args[i-1] = String.join(" ", quotedArguments);

                        i = j;

                    }

                }
                else args[i-1] = parts[i];

            }

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
        //into the variable when an instance of terminal is created

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

            System.out.println("this command takes no arguments"); //check for arguments

            return;

        }

        File dir = new File(currentDirectory); //find current directory
        String[] contents = dir.list(); //array of file names.

        if(contents != null)
        {

            List<String> list = Arrays.asList(contents); //sort alphabetically

            Collections.sort(list);

            for(String item : list) System.out.print(item + "  ");

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

        //make file objects of given arguments
        File source = new File(args[0]);
        File destination = new File(args[1]);

        //construct paths if not given fully
        if(!source.isAbsolute()) source = new File(currentDirectory, args[0]);
        if(!destination.isAbsolute()) destination = new File(currentDirectory, args[1]);

        if(!source.exists() || !source.isFile()) //make sure source file exists
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
    public void touch(String[] args)
    {

        //if user didn't write anything
        if(args.length == 0)
        {

            System.out.println("touch: missing file ");

            return;

        }

         //join all args to handle file name with spaces
         String path = String.join(" ", args);
         File file = new File(path);

         //if user didn't write full path
         if(!file.isAbsolute()) file = new File(currentDirectory, path);

         try
         {
             //if file already exist
             if(file.exists())
             {

                 //update to modify file timestamp
                 boolean updated = file.setLastModified(System.currentTimeMillis());

                 if(!updated) System.out.println("touch: failed to update timestamp for " + file.getName());

             }
             else
             {
                 //create file
                 boolean created = file.createNewFile();

                 if(!created) System.out.println("touch: failed to create file " + file.getName());

             }

         }
         catch(IOException error)
         {
             // exception
             System.out.println("touch: error - " + error.getMessage());
         }

    }
    public void rmdir(String[] args)
    {
        //check if user wrote any argument
        if(args.length == 0)
        {

            System.out.println("rmdir: missing operand");

            return;

        }

        //case 1: if argument is "*"
        if(args.length == 1 && args[0].equals("*"))
        {

            //get files/dirs of the dir we are in
            File currentDir = new File(currentDirectory);
            File[] contents = currentDir.listFiles();

            if(contents != null)
            {

                for(File item : contents)
                {

                    //delete only empty directories
                    if(item.isDirectory())
                    {

                        File[] inside = item.listFiles();

                        if(inside != null && inside.length == 0)
                        {

                            boolean deleted = item.delete();

                            if(deleted) System.out.println("Deleted empty directory: " + item.getName());
                            else System.out.println("rmdir: failed to delete " + item.getName());

                        }

                    }

                }

            }
            else System.out.println("rmdir: cannot access current directory");

            return;

        }

        //case 2: deleting one specific directory
        String path = String.join(" ", args); //handle names with spaces
        File dir = new File(path);

        //if user didn't write full path, attach current directory to get full path
        if(!dir.isAbsolute()) dir = new File(currentDirectory, path);
        if(!dir.exists())
        {

            System.out.println("rmdir: directory does not exist");

            return;

        }
        if(!dir.isDirectory())
        {

            System.out.println("rmdir: not a directory");

            return;

        }

        File[] filesInside = dir.listFiles();

        if(filesInside != null && filesInside.length > 0)
        {

            System.out.println("rmdir: directory not empty");

            return;

        }

        //now delete
        boolean deleted = dir.delete();

        if(deleted) System.out.println("Directory deleted successfully: " + dir.getName());
        else System.out.println("rmdir: failed to delete " + dir.getName());

    }

public void rm(String[] args)
{
    //checking for an input file
    if(args.length==0)
    {

        System.out.println("ERROR , Enter filename. ");
        return;

    }

    //handling files with spaces in its name.
    String filename = String.join(" ",args);

    File file = new File(filename);

    //checking if not abs path then use current dir
    if(!file.isAbsolute())
    {
        file = new File (currentDirectory,filename);

    }

    if(!file.exists())
    {

        System.out.println("No such a file. ");

        return;

    }

    boolean deleted = file.delete();

    if(deleted) return;

    else System.out.println(filename + " : Cannot be deleted. ");

}


public void cat(String[] args) {

    if (args.length == 0) {

        System.out.println("Enter argument. ");

        return;

    }

    if (args.length > 2) {

        System.out.println("Maximum 1 or 2 arguments only. ");

    }

    //First case 1 argument

    if (args.length == 1) {

        String filename = args[0];
        File file = new File(filename);

        //checking if not abs path then use current dir
        if (!file.isAbsolute()) {
            file = new File(currentDirectory, filename);
        }

        //checking if the file exists
        if (!file.exists()) {
            System.out.println("No such file. ");

            return;
        }

        if (!file.isFile()) {
            System.out.println(filename + "is a directory");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("error reading file - " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                System.out.println("error closing file");
            }
        }
    } else if (args.length == 2) {
        String fileName1 = args[0];
        String fileName2 = args[1];

        File file1 = new File(fileName1);
        File file2 = new File(fileName2);

        // if not abs paths, use current dir
        if (!file1.isAbsolute())
            file1 = new File(currentDirectory, fileName1);
        if (!file2.isAbsolute())
            file2 = new File(currentDirectory, fileName2);

        // Checking both files existing
        if (!file1.exists()) {
            System.out.println(fileName1 + ": no such file");
            return;
        }
        if (!file2.exists()) {
            System.out.println(fileName2 + ": no such file");
            return;
        }

        // Checking if both are files
        if (!file1.isFile()) {
            System.out.println(fileName1 + ": is a directory");
            return;
        }
        if (!file2.isFile()) {
            System.out.println(fileName2 + ": is a directory");
            return;
        }

        BufferedReader reader1 = null;
        BufferedReader reader2 = null;
        try
        {
            // Reading first file
            reader1 = new BufferedReader(new FileReader(file1));
            String line;
            while((line = reader1.readLine()) != null)
            {
                System.out.println(line);
            }

            // Read second file
            reader2 = new BufferedReader(new FileReader(file2));
            while((line = reader2.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch(IOException e)
        {
            System.out.println("error reading files: " + e.getMessage());
        }
        finally
        {
            try
            {
                if(reader1 != null) reader1.close();
                if(reader2 != null) reader2.close();
            }
            catch(IOException e)
            {
                System.out.println("error closing files");
            }
        }
    }
}

public void wc(String [] args)
{
    if(args.length==0)
    {

        System.out.println("Enter an argument. ");

        return;

    }

    String filename = String.join(" ",args);

    File file = new File(filename);

    if(!file.isAbsolute())
    {
        file = new File(currentDirectory,filename);
    }

    if(!file.exists())
    {

        System.out.println(filename + " no such a file. ");
        return;

    }

    if(!file.isFile())
    {
        System.out.println(filename + " is a directory. ");
        return;
    }

    int lines=0;
    int words=0;
    int charCount=0;

    BufferedReader reader = null;

    try
    {
        reader = new BufferedReader(new FileReader(file));
        String line;
        while((line = reader.readLine()) != null)
        {
            lines++;
            charCount += line.length();

            // counting words split by space
            String trimmedLine = line.trim();
            if(!trimmedLine.isEmpty())
            {
                String[] word = trimmedLine.split("\\s+");
                words += word.length;
            }
        }


    }
    catch(IOException e)
    {
        System.out.println("error reading file - " + e.getMessage());
        return;
    }
    finally
    {
        try
        {
            if(reader != null) reader.close();
        }
        catch(IOException e)
        {
            System.out.println("error closing file");
        }
    }

    // Print output in format: lines words characters filename
    System.out.println(lines + " " + words + " " + charCount + " " + filename);

}
public void zip(String[] args)
{
    // Check if we have enough arguments
    if(args.length < 2)
    {
        System.out.println("zip: requires at least 2 arguments (archive name and file/directory)");
        return;
    }

    String zipFileName = args[0];
    File zipFile = new File(zipFileName);

    if(!zipFile.isAbsolute())
        {
            zipFile = new File(currentDirectory, zipFileName);
        }
    
    // Check if -r flag is present for recursive directory compression
    boolean recursive = false;
    int startIndex = 1;
    
    if(args[1].equals("-r"))
    {
        recursive = true;
        startIndex = 2;
        
        if(args.length < 3)
        {
            System.out.println("zip: -r requires directory path");
            return;
        }
    }
    
    try
    {
        // Create zip file
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        
        if(recursive)
        {
            // Compress directory recursively
            String directoryPath = args[2];
            File directory = new File(directoryPath);
            
            if(!directory.isAbsolute())
            {
                directory = new File(currentDirectory, directoryPath);
            }
            
            if(!directory.exists())
            {
                System.out.println("zip: directory does not exist: " + directoryPath);
                zos.close();
                fos.close();
                return;
            }
            
            if(!directory.isDirectory())
            {
                System.out.println("zip: path is not a directory: " + directoryPath);
                zos.close();
                fos.close();
                return;
            }
            
            // Zip the directory recursively
            zipDirectory(directory, directory.getName(), zos);
            
        }
        else
        {
            // Compress individual files
            for(int i = startIndex; i < args.length; i++)
            {
                File fileToZip = new File(args[i]);
                
                if(!fileToZip.isAbsolute())
                {
                    fileToZip = new File(currentDirectory, args[i]);
                }
                
                if(!fileToZip.exists())
                {
                    System.out.println("zip: file does not exist: " + args[i]);
                    continue;
                }
                
                if(fileToZip.isDirectory())
                {
                    System.out.println("zip: " + args[i] + " is a directory. Use -r flag for directories.");
                    continue;
                }
                
                // Add file to zip
                addFileToZip(fileToZip, fileToZip.getName(), zos);
            }
        }
        
        zos.close();
        fos.close();
        System.out.println("Successfully created: " + zipFile.getName());
        
    }
    catch(IOException e)
    {
        System.out.println("zip: error creating zip file: " + e.getMessage());
    }
}
// Helper method to add a single file to zip
private void addFileToZip(File file, String fileName, ZipOutputStream zos) throws IOException
{
    FileInputStream fis = new FileInputStream(file);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zos.putNextEntry(zipEntry);
    
    byte[] buffer = new byte[1024];
    int length;
    while((length = fis.read(buffer)) >= 0)
    {
        zos.write(buffer, 0, length);
    }
    
    zos.closeEntry();
    fis.close();
}

// Helper method to recursively zip a directory
private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException
{
    File[] files = folder.listFiles();
    
    if(files == null)
    {
        return;
    }
    
    for(File file : files)
    {
        if(file.isDirectory())
        {
            // Recursively zip subdirectory
            zipDirectory(file, parentFolder + "/" + file.getName(), zos);
        }
        else
        {
            // Add file to zip with its relative path
            addFileToZip(file, parentFolder + "/" + file.getName(), zos);
        }
    }
}
public void unzip(String[] args)
{
    // Check if we have the required arguments
    if(args.length == 0)
    {
        System.out.println("unzip: missing zip file argument");
        return;
    }
    
    // Parse arguments to handle -d flag and spaces in paths
    String zipFileName = null;
    File extractDir = new File(currentDirectory);
    
    // Check if -d flag is present
    boolean hasDFlag = false;
    int dFlagIndex = -1;
    
    for(int i = 0; i < args.length; i++)
    {
        if(args[i].equals("-d"))
        {
            hasDFlag = true;
            dFlagIndex = i;
            break;
        }
    }
    
    if(hasDFlag)
    {
        // Extract zip file name (all args before -d)
        if(dFlagIndex == 0)
        {
            System.out.println("unzip: missing zip file argument before -d");
            return;
        }
        
        String[] zipNameParts = Arrays.copyOfRange(args, 0, dFlagIndex);
        zipFileName = String.join(" ", zipNameParts);
        
        // Extract destination directory (all args after -d)
        if(dFlagIndex + 1 >= args.length)
        {
            System.out.println("unzip: missing destination directory after -d");
            return;
        }
        
        String[] destParts = Arrays.copyOfRange(args, dFlagIndex + 1, args.length);
        String destPath = String.join(" ", destParts);
        extractDir = new File(destPath);
        
        if(!extractDir.isAbsolute())
        {
            extractDir = new File(currentDirectory, destPath);
        }
    }
    else
    {
        // No -d flag, all args are the zip file name
        zipFileName = String.join(" ", args);
    }
    
    // Create File object for zip file
    File zipFile = new File(zipFileName);
    
    // Handle relative path for zip file
    if(!zipFile.isAbsolute())
    {
        zipFile = new File(currentDirectory, zipFileName);
    }
    
    // Check if zip file exists
    if(!zipFile.exists())
    {
        System.out.println("unzip: file does not exist: " + zipFileName);
        return;
    }
    
    if(!zipFile.isFile())
    {
        System.out.println("unzip: not a file: " + zipFileName);
        return;
    }
    
    // Create extraction directory if it doesn't exist
    if(!extractDir.exists())
    {
        boolean created = extractDir.mkdirs();
        if(!created)
        {
            System.out.println("unzip: failed to create extraction directory");
            return;
        }
    }
    
    // Extract the zip file
    FileInputStream fis = null;
    ZipInputStream zis = null;
    
    try
    {
        fis = new FileInputStream(zipFile);
        zis = new ZipInputStream(fis);
        ZipEntry entry;
        
        while((entry = zis.getNextEntry()) != null)
        {
            // Security check: prevent path traversal attacks
            String entryName = entry.getName();
            File newFile = new File(extractDir, entryName);
            String canonicalDestPath = extractDir.getCanonicalPath();
            String canonicalNewFilePath = newFile.getCanonicalPath();
            
            if(!canonicalNewFilePath.startsWith(canonicalDestPath + File.separator))
            {
                System.out.println("unzip: invalid entry path: " + entryName);
                zis.closeEntry();
                continue;
            }
            
            // Create parent directories if needed
            if(entry.isDirectory())
            {
                newFile.mkdirs();
            }
            else
            {
                // Create parent directories for file
                File parent = newFile.getParentFile();
                if(parent != null && !parent.exists())
                {
                    parent.mkdirs();
                }
                
                // Extract file
                FileOutputStream fos = new FileOutputStream(newFile);
                byte[] buffer = new byte[1024];
                int length;
                
                while((length = zis.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, length);
                }
                
                fos.close();
            }
            
            zis.closeEntry();
        }
        
        System.out.println("Successfully extracted: " + zipFile.getName());
    }
    catch(IOException e)
    {
        System.out.println("unzip: error extracting zip file: " + e.getMessage());
    }
    finally
    {
        try
        {
            if(zis != null) zis.close();
            if(fis != null) fis.close();
        }
        catch(IOException e)
        {
            System.out.println("unzip: error closing streams");
        }
    }
}

    public void chooseCommandAction()
{
    if(parser.getCommandName().equals("pwd")) System.out.println(pwd());
    else if(parser.getCommandName().equals("cd")) cd(parser.getArgs());
    else if(parser.getCommandName().equals("mkdir")) mkdir(parser.getArgs());
    else if(parser.getCommandName().equals("ls")) ls(parser.getArgs());
    else if(parser.getCommandName().equals("touch")) touch(parser.getArgs());
    else if(parser.getCommandName().equals("rmdir")) rmdir(parser.getArgs());
    else if(parser.getCommandName().equals("rm")) rm(parser.getArgs());
    else if(parser.getCommandName().equals("cat")) cat(parser.getArgs());
    else if(parser.getCommandName().equals("wc")) wc(parser.getArgs());
    else if(parser.getCommandName().equals("zip")) zip(parser.getArgs());  
    else if(parser.getCommandName().equals("unzip")) unzip(parser.getArgs());
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
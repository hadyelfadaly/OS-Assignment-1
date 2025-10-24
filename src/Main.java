import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Arrays;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

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
            int argIndex = 0;

            for(int i = 1; i < parts.length; i++)
            {

                if(parts[i].charAt(0) == '\'')
                {

                    //if argument name is between quotes
                    if(parts[i].charAt(parts[i].length()-1) == '\'')
                    {

                        parts[i] = parts[i].substring(1,  parts[i].length()-1); //argument without the quotes
                        args[argIndex] = parts[i];

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

                        args[argIndex] = String.join(" ", quotedArguments);

                        i = j;

                    }

                }
                else if(parts[i].charAt(0) == '"')
                {

                    //if argument name is between quotes
                    if(parts[i].charAt(parts[i].length()-1) == '"')
                    {

                        parts[i] = parts[i].substring(1,  parts[i].length()-1); //argument without the quotes
                        args[argIndex] = parts[i];

                    }
                    else
                    {

                        int j = i;
                        ArrayList<String> quotedArguments = new ArrayList<>(); //arraylist to hold the argument between quotes

                        //add first word without its starting '
                        quotedArguments.add(parts[j].substring(1));
                        j++;

                        //add in between words
                        while(parts[j].charAt(parts[j].length()-1) != '"')
                        {

                            quotedArguments.add(parts[j]);
                            j++;

                        }

                        //add last word without last '
                        quotedArguments.add(parts[j].substring(0, parts[j].length()-1));

                        args[argIndex] = String.join(" ", quotedArguments);

                        i = j;

                    }

                }
                else args[argIndex] = parts[i];

                argIndex++;

            }

            //if arguments after joining is smaller than arg og size then resize it
            if (argIndex < args.length) args = Arrays.copyOf(args, argIndex);

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

    public String pwd(String[] args)
    {

        if(args.length > 0) return "This Command Takes no Arguments";

        return currentDirectory;

    }
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

        if(args.length == 0)
        {

            System.out.println("mkdir: missing operand");

            return;

        }

        //iterate over args
        for(int i = 0; i < args.length; i++)
        {

            //create object file from the arg
            File file = new File(args[i]);

           if(!file.isAbsolute()) file = new File(currentDirectory, args[i]); //get full path if not given
           if(file.exists() && file.isDirectory()) //if file exists
            {

                System.out.println("mkdir: cannot create directory " + args[i] + " : File exists");

                continue;

            }

            try
            {

                file.mkdir();

            }
            catch(Exception error)
            {

                    System.out.println("mkdir: cannot create directory " + args[i] + " : " + error);

            }

        }

    }
    public String ls(String[] args)
    {

        if(args.length > 0)
        {return "this command takes no arguments";}

        File dir = new File(currentDirectory); //find current directory
        String[] contents = dir.list(); //array of file names.
        String result = "";

        if(contents != null)
        {

            List<String> list = Arrays.asList(contents); //sort alphabetically

            Collections.sort(list);

            for(String item : list) result += (item + "  ");

        }
        else return "Error: Can't list directory contents";

        return result;

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

                            if(!deleted)  System.out.println("rmdir: failed to delete " + item.getName());

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

        if(!deleted)  System.out.println("rmdir: failed to delete " + dir.getName());

    }
    public void rm(String[] args)
    {

        if(args.length == 0)
        {

            System.out.println("rm: missing operand");

            return;

        }

        //to handle files with space in their name
        String filename = String.join(" ", args);
        File file = new File(filename);

        //if not absolute get full path
        if(!file.isAbsolute()) file = new File(currentDirectory, filename);
        if(!file.exists())
        {

            System.out.println("No such a file. ");

            return;

        }

        boolean deleted = file.delete();

        if(!deleted) System.out.println(filename + " : Cannot be deleted. ");

    }
    public String cat(String[] args)
    {

        if(args.length == 0)
        {return "cat : invalid operand";}
        if(args.length > 2)
        {return "Invalid Input";}

        String result = "";

        if(args.length == 1)
        {

            //create object file from the argument
            String filename = args[0];
            File file = new File(filename);

            //if not full path create its fullpath
            if(!file.isAbsolute()) file = new File(currentDirectory, filename);
            if(!file.exists()) return "No such file.";
            if(!file.isFile()) return filename + " is a directory";

            //using bufferedReader class to read contents of files
            BufferedReader reader = null;

            try
            {

                reader = new BufferedReader(new FileReader(file));
                String line;

                while((line = reader.readLine()) != null) result += line + "\n";

                result = result.substring(0, result.length() - 1); //remove the last '\n'

            }
            catch(IOException error)
            {

                return "error reading file - " + error.getMessage();

            }
            finally
            {

                try
                {

                    //if reader created and read successfully then delete
                    if(reader != null) reader.close();

                }
                catch(IOException error)
                {

                    return "error closing file";

                }

            }

        }
        else if(args.length == 2)
        {

            String fileName1 = args[0], fileName2 = args[1];
            File file1 = new File(fileName1), file2 = new File(fileName2);

            //get full paths of the files
            if(!file1.isAbsolute()) file1 = new File(currentDirectory, fileName1);
            if(!file2.isAbsolute()) file2 = new File(currentDirectory, fileName2);
            if(!file1.exists()) return fileName1 + ": no such file";
            if(!file2.exists()) return fileName2 + ": no such file";
            if(!file1.isFile()) return fileName1 + ": is not a valid file";
            if(!file2.isFile()) return fileName2 + ": is not a valid file";

            BufferedReader reader1 = null, reader2 = null;

            try
            {

                reader1 = new BufferedReader(new FileReader(file1));
                String line;

                while((line = reader1.readLine()) != null) result += line + "\n";

                reader2 = new BufferedReader(new FileReader(file2));

                while((line = reader2.readLine()) != null) result += line + "\n";

                result = result.substring(0, result.length() - 1); //remove the last '\n'

            }
            catch(IOException error)
            {

                return "error reading files: " + error.getMessage();

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

                    return "error closing files";

                }

            }

        }

        return result;

    }
    public String wc(String[] args)
    {

        if(args.length == 0) return "wc: missing operand";

        //to handle file with spaces in their names
        String filename = String.join(" ", args);
        File file = new File(filename);

        //get full path if not given
        if(!file.isAbsolute()) file = new File(currentDirectory, filename);
        if(!file.exists()) return filename + " no such a file. ";
        if(!file.isFile()) return filename + " is an invalid file. ";

        //variables to store needed measures
        int lines = 0, words = 0, charCount = 0;
        BufferedReader reader = null;

        try
        {

            reader = new BufferedReader(new FileReader(file));
            String line;

            while((line = reader.readLine()) != null)
            {

                lines++;

                String trimmedLine = line.trim();

                if(!trimmedLine.isEmpty())
                {

                    String[] word = trimmedLine.split("\\s+");
                    words += word.length;

                    charCount += trimmedLine.length() - word.length + 1;

                }

            }

        }
        catch(IOException error)
        {

            return "error reading file - " + error.getMessage();

        }
        finally
        {

            try
            {

                //if reader created and read successfully then delete
                if(reader != null) reader.close();

            }
            catch(IOException e)
            {

                return "error closing file";

            }

        }

        return lines + " " + words + " " + charCount + " " + filename;

    }
    public void zip(String[] args)
    {

        if(args.length < 2)
        {

            System.out.println("zip: missing operand");

            return;

        }

        //creating object files for the zip file
        String zipFileName = args[0];
        File zipFile = new File(zipFileName);

        //get its full path if not given
        if(!zipFile.isAbsolute()) zipFile = new File(currentDirectory, zipFileName);

        boolean recursive = false;
        int startIndex = 1;

        //check if its directory recursive
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

            //both classes used to write data into a file and create new zip stream
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            //if directory recursive
            if(recursive)
            {

                //getting dir path
                String directoryPath = args[2];
                File directory = new File(directoryPath);

                //get full path if not written
                if(!directory.isAbsolute()) directory = new File(currentDirectory, directoryPath);
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

                //call helper function to zip directories
                zipDirectory(directory, directory.getName(), zos);

            }
            else
            {

                //compress individual files
                for(int i = startIndex; i < args.length; i++)
                {

                    //create file object of the file we gonna zip
                    File fileToZip = new File(args[i]);

                    //get its full path
                    if(!fileToZip.isAbsolute()) fileToZip = new File(currentDirectory, args[i]);
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

                    addFileToZip(fileToZip, fileToZip.getName(), zos);

                }

            }

            zos.close();
            fos.close();

            System.out.println("Successfully created: " + zipFile.getName());

        }
        catch(IOException error)
        {

            System.out.println("zip: error creating zip file: " + error.getMessage());

        }

    }
    private void addFileToZip(File file, String fileName, ZipOutputStream zos) throws IOException
    {

        //creates an input stream to read the raw data (bytes) from the file that is going to be zipped
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName); //creating entry of the file we will add to the zip
        //that holds its metadata

        zos.putNextEntry(zipEntry);

        //creates a temporary byte array to efficiently move data in chunks rather than byte by byte.
        byte[] buffer = new byte[1024];
        int length;

        //reading the data of the file into the buffer then writes and compresses it
        while((length = fis.read(buffer)) >= 0) zos.write(buffer, 0, length);

        zos.closeEntry();
        fis.close();

    }
    //helper function for recursive zipping
    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException
    {

        //listing files in the dir in an array
        File[] files = folder.listFiles();

        if(files == null) return;

        for(File file : files)
        {

            if(file.isDirectory()) zipDirectory(file, parentFolder + "/" + file.getName(), zos);
            else addFileToZip(file, parentFolder + "/" + file.getName(), zos);

        }

    }
    public void unzip(String[] args)
    {

        if(args.length == 0)
        {

            System.out.println("unzip: missing zip file argument");

            return;

        }

        String zipFileName = null;
        File extractDir = new File(currentDirectory);
        boolean hasDFlag = false;
        int dFlagIndex = -1;

        //check if the user wants to extract into different place
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

            //check if the zip file name is given
            if(dFlagIndex == 0)
            {

                System.out.println("unzip: missing zip file argument before -d");

                return;

            }

            //getting zip file name while handling if it has any spaces in its name
            String[] zipNameParts = Arrays.copyOfRange(args, 0, dFlagIndex);
            zipFileName = String.join(" ", zipNameParts);

            //check if destination is given
            if(dFlagIndex + 1 >= args.length)
            {

                System.out.println("unzip: missing destination directory after -d");

                return;

            }

            //getting destination while handling if there is any spaces in its name
            String[] destParts = Arrays.copyOfRange(args, dFlagIndex + 1, args.length);
            String destPath = String.join(" ", destParts);
            extractDir = new File(destPath);

            //getting its full path if not given
            if(!extractDir.isAbsolute()) extractDir = new File(currentDirectory, destPath);

        }
        else zipFileName = String.join(" ", args);

        //creating file object of the zip file name
        File zipFile = new File(zipFileName);

        //getting its full path
        if(!zipFile.isAbsolute()) zipFile = new File(currentDirectory, zipFileName);
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
        if(!extractDir.exists())
        {

            boolean created = extractDir.mkdirs();

            if(!created)
            {

                System.out.println("unzip: failed to create extraction directory");

                return;

            }

        }

        FileInputStream fis = null;
        ZipInputStream zis = null;

        try
        {

            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis); //reads zip data
            ZipEntry entry;

            while((entry = zis.getNextEntry()) != null)
            {

                String entryName = entry.getName();
                File newFile = new File(extractDir, entryName);
                String canonicalDestPath = extractDir.getCanonicalPath(), canonicalNewFilePath = newFile.getCanonicalPath();
                //using getCanonicalPath() function to get full right path

                if(!canonicalNewFilePath.startsWith(canonicalDestPath + File.separator))
                {

                    System.out.println("unzip: invalid entry path: " + entryName);

                    zis.closeEntry();

                    continue;

                }

                //if the zipped file is a dir
                if(entry.isDirectory()) newFile.mkdirs();
                else
                {

                    File parent = newFile.getParentFile();

                    //create parent dir if not existing
                    if(parent != null && !parent.exists()) parent.mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int length;

                    while((length = zis.read(buffer)) > 0) fos.write(buffer, 0, length);

                    fos.close();

                }

                zis.closeEntry();

            }

            System.out.println("Successfully extracted: " + zipFile.getName());

        }
        catch(IOException error)
        {

            System.out.println("unzip: error extracting zip file: " + error.getMessage());

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
    public void exit(String commandName)
    {

        if(commandName.equals("exit"))
        {System.exit(0);}

    }

    public void chooseCommandAction() throws IOException
    {

        String[] arguments = parser.getArgs();
        boolean override = false, append = false;
        int index = 0;

        //see the arguments have '>' or '>>' and get their index
        for(int i = 0; i < arguments.length; i++)
        {

            if(arguments[i].equals(">"))
            {

                override = true;
                index = i;

                break;

            }
            else if(arguments[i].equals(">>"))
            {

                append = true;
                index = i;

                break;

            }

        }

        if(parser.getCommandName().equals("pwd"))
        {

            if(arguments.length > 2)
            {

                System.out.println("Invalid Arguments");

                return;

            }
            if(override)
            {

                try
                {

                    FileWriter fw = new FileWriter(arguments[1], false); //false for override

                    fw.write(pwd(new String[]{})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else if(append)
            {

                try
                {

                    FileWriter fw = new FileWriter(arguments[1], true); //true for append

                    fw.write(pwd(new String[]{})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else System.out.println(pwd(arguments));

        }
        else if(parser.getCommandName().equals("cd")) cd(parser.getArgs());
        else if(parser.getCommandName().equals("mkdir")) mkdir(parser.getArgs());
        else if(parser.getCommandName().equals("ls"))
        {

            if(arguments.length > 2)
            {

                System.out.println("Invalid Arguments");

                return;

            }
            if(override)
            {

                try
                {

                    FileWriter fw = new FileWriter(arguments[1], false); //false for override

                    fw.write(ls(new String[]{})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else if(append)
            {

                try
                {

                    FileWriter fw = new FileWriter(arguments[1], true); //true for append

                    fw.write(ls(new String[]{})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else System.out.println(ls(arguments));
        }
        else if(parser.getCommandName().equals("touch")) touch(parser.getArgs());
        else if(parser.getCommandName().equals("rmdir")) rmdir(parser.getArgs());
        else if(parser.getCommandName().equals("rm")) rm(parser.getArgs());
        else if(parser.getCommandName().equals("cat"))
        {

            if(arguments.length > 5)
            {

                System.out.println("Invalid Arguments");

                return;

            }
            if(override)
            {

                try
                {

                    FileWriter fw = null;
                    String[] tempArgs =  new String[arguments.length];

                    if(index == 1)
                    {

                        fw = new FileWriter(arguments[2], false); //false for override

                        tempArgs = Arrays.copyOfRange(arguments, 0, 1);

                    }
                    else if(index == 2)
                    {

                        fw = new FileWriter(arguments[3], false);

                        tempArgs = Arrays.copyOfRange(arguments, 0, 2);

                    }

                    fw.write(cat(tempArgs)); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else if(append)
            {

                try
                {

                    FileWriter fw = null;
                    String[] tempArgs =  new String[arguments.length];

                    if(index == 1)
                    {

                        fw = new FileWriter(arguments[2], true ); //true for append

                        tempArgs = Arrays.copyOfRange(arguments, 0, 1);

                    }
                    else if(index == 2)
                    {

                        fw = new FileWriter(arguments[2], true ); //true for append

                        tempArgs = Arrays.copyOfRange(arguments, 0, 2);

                    }

                    fw.write(cat(tempArgs)); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else System.out.println(cat(parser.getArgs()));

        }
        else if(parser.getCommandName().equals("wc"))
        {

            if(arguments.length > 3)
            {

                System.out.println("Invalid Arguments");

                return;

            }
            if(override)
            {

                try
                {

                    FileWriter fw = fw = new FileWriter(arguments[2], false); //false for override

                    fw.write(wc(new String[]{arguments[0]})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else if(append)
            {

                try
                {

                    FileWriter fw = fw = new FileWriter(arguments[2], true); //true for append

                    fw.write(wc(new String[]{arguments[0]})); //writes output into file, creates it if not existing
                    fw.close();

                }
                catch(IOException error)
                {

                    System.out.println("Error writing to file: " + error.getMessage());

                }

            }
            else System.out.println(wc(parser.getArgs()));

        }
        else if(parser.getCommandName().equals("zip")) zip(parser.getArgs());
        else if(parser.getCommandName().equals("unzip")) unzip(parser.getArgs());
        else if(parser.getCommandName().equals("exit")) exit(parser.getCommandName());
        else if(parser.getCommandName().equals("cp"))
        {

            if(arguments.length > 0 && arguments[0].equals("-r")) cp_r(arguments);
            else cp(arguments);

        }
        else System.out.println(parser.getCommandName() + ": command not found");

    }
    
    public static void main(String[] args) throws IOException
    {

        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);

        while(true)
        {

            System.out.print("> ");

            String input = scanner.nextLine();

            if(terminal.parser.parse(input)) terminal.chooseCommandAction();
            else System.out.println("error");

        }

    }

}
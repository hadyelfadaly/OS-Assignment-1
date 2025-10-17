class Parser
{

    String commandName;
    String[] args;

    public boolean parse(String input){return true;}
    public String getCommandName(){return commandName;}
    public String[] getArgs(){return args;}

}
class Terminal
{

    Parser parser;

    public String pwd()
    {return System.getProperty("user.dir");}
    public void cd(String[] args){}

    public void chooseCommandAction(){}
    public static void main(String[] args){}

}
public class Main
{

    public static void main(String[] args)
    {
        //Terminal obj1 = null;

        
        //System.out.println(obj1.pwd());
    }

}


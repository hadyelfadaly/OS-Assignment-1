public class Parser
{

    String commandName;
    String[] args;

    public boolean parse(String input){...}
    public String getCommandName(){...}
    public String[] getArgs(){...}

}
public class Terminal
{

    Parser parser;

    public String pwd(){...}
    public void cd(String[] args){...}

    public void chooseCommandAction(){...}
    public static void main(String[] args){...}

}
public class Main
{

    public static void main(String[] args)
    {


    }

}


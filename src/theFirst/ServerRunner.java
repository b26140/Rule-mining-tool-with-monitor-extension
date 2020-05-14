package theFirst;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ServerRunner implements Runnable{
	private Thread t;
	private String threadName;
	private boolean conflict;
	private String monitorType;

	public ServerRunner(String name, boolean conflict, String monitorType){
		threadName = name;
		this.conflict = conflict;
		this.monitorType = monitorType;
		System.out.println("Creating ServerRunner Thread");
	}
	@Override
	public void run() {
		String cmd = null;;
		if (monitorType.equals("declare")) {
			cmd = "java -jar MPDeclareMonitoring.jar ";
			if (conflict)
				cmd = cmd + "-conflictCheck";			
		} else if (monitorType.equals("ltl")) {
			cmd = "java -jar MPMoBuConLTL.jar ";
		} else if (monitorType.equals("onlinedcl")) {
			cmd = "java -jar OnlineDeclareAnalyzerPlugin.jar ";
		}
		
        try {
        	ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));        	
    		Process p = pb.start();
    		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    		String line = null;
    		while ( (line = reader.readLine()) != null) {
    			System.out.println(line);
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}		
	}
	public void start () {
		System.out.println("Starting " +  threadName );
		if (t == null) {
			t = new Thread (this, threadName);
		    t.start ();
		    }
		}
}

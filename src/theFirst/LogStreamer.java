package theFirst;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.*;
import org.processmining.operationalsupport.xml.OSXMLConverter;
import org.deckfour.xes.model.XLog;

public class LogStreamer implements Runnable{
    private static int PORT = 4444;
    private static String HOST;
    private OSXMLConverter osxmlConverter = new OSXMLConverter();
    private Thread t;
    private String threadName; 
    
    private String model;
    private XLog log;
    private int times;
    
    static {
        try {
            HOST = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
    }
    
    public LogStreamer(String model, XLog log, int times) {
    	this.model = model;
    	this.log = log;
    	this.times = times;
    	this.threadName = "LogStreamer";
    	System.out.println("Creating LogStreamer thread");
    }
    public void start () {
        System.out.println("Starting LogStreamer" );
        if (t == null) {
           t = new Thread (this, threadName);
           t.start ();
        }
     }
	@Override
	public void run() {
		System.out.println("Running " + threadName);
	   	try {
	    	Socket socket = new Socket(HOST, PORT);
	    	socket.isConnected();
	        PrintWriter writeOnTheSocket = new PrintWriter(socket.getOutputStream(), true);
	        writeOnTheSocket.println(model);
	        writeOnTheSocket.println("</model>");
	        writeOnTheSocket.flush();
	        for (XTrace t : log) {
	           long traceTimeIncrement = 0;
	           int eventIndex = 0;


	           for (XEvent ev : t) {
	               XTraceImpl t1 = new XTraceImpl(t.getAttributes());
	               XAttributeTimestampImpl timestamp = (XAttributeTimestampImpl) ev.getAttributes().get("time:timestamp");
	               ((XAttributeTimestampImpl) ev.getAttributes().get("time:timestamp")).setValueMillis(timestamp.getValueMillis() + traceTimeIncrement + eventIndex);
	               // add trace attributes to event
	               String eventName = XConceptExtension.instance().extractName(ev);
	               ev.getAttributes().putAll(t.getAttributes());
	               XConceptExtension.instance().assignName(ev,eventName);
	               // Add event to trace
	               t1.add(ev);

	               System.out.println("Writing to socket Event : " + ev.getAttributes().get("concept:name"));
	               String packet = osxmlConverter.toXML(t1).replace('\n', ' ');
	               // Transmit trace

	               System.out.println(packet);
	               writeOnTheSocket.println(packet);
	               writeOnTheSocket.flush();
	               eventIndex++;
	               Thread.sleep(times);
	           }

	           XTraceImpl lastTrace = new XTraceImpl(t.getAttributes());
	           // Add last trace.
	           XFactory nxFactory = XFactoryRegistry.instance().currentDefault();
	           XEvent le = nxFactory.createEvent();
	           //le.setAttributes(t.get(t.size() - 1).getAttributes());
	           XConceptExtension nconcept = XConceptExtension.instance();
	           nconcept.assignName(le, "complete");
	           XLifecycleExtension nlc = XLifecycleExtension.instance();
	           nlc.assignTransition(le, "complete");
	           XTimeExtension ntimeExtension = XTimeExtension.instance();
	           ntimeExtension.assignTimestamp(le, ntimeExtension.extractTimestamp(t.get(t.size() - 1)).getTime() + 1);
	           lastTrace.add(le);
	           System.out.println("Writing to socket Event : " + le.getAttributes().get("concept:name"));
	           String packet = osxmlConverter.toXML(lastTrace).replace('\n', ' ');
	           // Transmit trace
	           writeOnTheSocket.println(packet);
	           System.out.println("Kirjutame:");
	           System.out.println(packet);
	           writeOnTheSocket.flush();
	       }
	       socket.shutdownOutput();
			return;
	    	}
	    	catch (Exception ex){ex.printStackTrace();}
		
	}
}
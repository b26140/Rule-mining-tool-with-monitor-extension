package theFirst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import javax.swing.JFrame;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.operationalsupport.xml.OSXMLConverter;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import exceptions.DeclareParserException;
import it.unibo.ai.rec.common.TimeGranularity;
import it.unibo.ai.rec.engine.FluentsConverter;
import it.unibo.ai.rec.model.RecTrace;
import it.unibo.ai.rec.model.FluentsModel;
import it.unibo.ai.rec.model.NoGroupingStrategy;
import it.unibo.ai.rec.visualization.BasicDateEventOutputter;
import it.unibo.ai.rec.visualization.FluentChartContainer;
import it.unibo.ai.rec.visualization.FluentChartFactory;
import it.unibo.ai.rec.visualization.FluentChartStandardPanel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import runner.MonitorRunner;

public class MoBuConClient extends GridPane implements MouseListener{
	
	@FXML
	private ListView<InstanceInfo> instancesList = new ListView<>();
	
	@FXML
	private AnchorPane runTimeMonitorView;
	
	private JPanel roundedPanelDiag;
	private JPanel roundedPanelHealth;
		
	JScrollPane scrollPane;
	boolean primo = true;

    private Color chartBackgroundColor = new Color(232, 232, 232);

    private boolean conflict = true; // Should get here somehow to set the parameter.
    
	private boolean showA;
	private JList diagnosticsList;
	private Hashtable violationsModels;
	private FluentChartFactory factory;
	private String someFluent;
    private FluentsConverter converter;
    FluentChartContainer chartPanel;
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss:S";
    private Vector instances;
    private Vector instancesAll; // this is the instances vector - this is where you get it on the board.
    private static int PORT = 4444;// to logstreamer
    public static void setPORT(int pORT) {
		PORT = pORT;
		System.out.println("Port: " + PORT );
	}

	private static String HOST;
    private Hashtable partialTraces;
    private Hashtable types;
    HashMap<String, XTrace> traceHashMap;
    private XTrace trace;
    OSXMLConverter osxmlConverter = new OSXMLConverter();
    private Vector instancesViol;
    private String shipType = "REF MODEL: no model selected";
    private Color backgroundColor = SlickerColors.COLOR_BG_4;
    private Hashtable healthGraphs;
    
    MonitorRunner mr;

    private String selected = "";
    private Hashtable monitorGraphs;
    
    static {
        try {
            HOST = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
    }
	
	public MoBuConClient() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MonitorView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
		try {
            factory = new FluentChartFactory(TimeGranularity.MILLIS, new BasicDateEventOutputter(
                    TimeGranularity.MILLIS, DATE_FORMAT), false, "stateColors.properties", "fluentColors.properties");
            fxmlLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

		types = new Hashtable();
		instancesAll = new Vector();
		showA = true;
		instancesViol = new Vector();
		monitorGraphs = new Hashtable();
		violationsModels = new Hashtable();
		diagnosticsList = new JList();
        final SwingNode swingNode1 = new SwingNode();
        healthGraphs = new Hashtable();

		createFirstSwingContent(swingNode1);
        runTimeMonitorView.getChildren().add(swingNode1);
    	AnchorPane.setBottomAnchor(swingNode1, 0.0);
		AnchorPane.setTopAnchor(swingNode1, 0.0);
		AnchorPane.setLeftAnchor(swingNode1, 0.0);
		AnchorPane.setRightAnchor(swingNode1, 0.0);
		instancesList.getSelectionModel().clearSelection();
        instancesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InstanceInfo>() {
			@Override
			public void changed(ObservableValue<? extends InstanceInfo> arg0, InstanceInfo arg1, InstanceInfo arg2) {
                if(arg2 == null) return;
                else {
                	instancesSelectionChanged();
                	System.out.println("New value: " + arg2.toString());
                }
			}
		});
        primo = true;
	}
	
	private void createSwingContent2(final SwingNode swingNode){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                	roundedPanelHealth.removeAll();
                	if ((FluentChartStandardPanel) healthGraphs.get(selected) != null) {
                        roundedPanelHealth.add((FluentChartStandardPanel) healthGraphs.get(selected));
                    }
                    if ((Vector) violationsModels.get(selected) != null) {
                        System.out.println("Here in 2nd if");
                        diagnosticsList.setModel(new ViolationListModel((Vector) violationsModels.get(selected)));
                    } else {
                        diagnosticsList.setModel(new ViolationListModel(new Vector()));
                    }
                    swingNode.setContent(roundedPanelHealth);
                }
            });
    }
	private void createFirstSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                roundedPanelHealth = SlickerFactory.instance().createRoundedPanel(50, chartBackgroundColor);
                roundedPanelHealth.setLayout((LayoutManager) new BoxLayout(roundedPanelHealth, BoxLayout.LINE_AXIS));
                swingNode.setContent(roundedPanelHealth);
            }
        });
    }
	
	
    private JScrollPane createSlickerScrollPane() {

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setOpaque(false);
        scrollpane.getViewport().setOpaque(false);
        scrollpane.setBorder(BorderFactory.createEmptyBorder());
        //scrollpane.setViewportBorder(BorderFactory.createLineBorder(new Color(
        //		10, 10, 10), 2));
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        SlickerDecorator.instance().decorate(scrollpane.getVerticalScrollBar(),
                new Color(0, 0, 0, 0), new Color(140, 140, 140),
                new Color(80, 80, 80));
        scrollpane.getVerticalScrollBar().setOpaque(false);

        SlickerDecorator.instance().decorate(scrollpane.getHorizontalScrollBar(),
                new Color(0, 0, 0, 0), new Color(140, 140, 140),
                new Color(80, 80, 80));
        scrollpane.getHorizontalScrollBar().setOpaque(false);
        return scrollpane;
    }
	
	private void sendRequest() {
		ProxyDaemon daemon;
		try {
			daemon = new ProxyDaemon(PORT,this);
			daemon.start();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runMoBuConClient() {
		sendRequest();
	}
	
    private void updateOutput(String piID, String eventName) {
        System.out.println("updateOutput piID: " + piID + " eventName: "+ eventName  );
        try {
            XTrace trace = (XTrace) partialTraces.get(piID);// Trace siit
            String fluent = someFluent;// String result from the matrix
            FluentsModel model = converter.toFluentsModel(fluent);
            Xes2RecTraceTranslator traceTranslator = new Xes2RecTraceTranslator(
                    it.unibo.ai.rec.common.TimeGranularity.MILLIS, Xes2RecTraceTranslator.TimestampStrategy.ABSOLUTE);
            RecTrace rtrace = traceTranslator.translate(trace);
			chartPanel = new FluentChartStandardPanel(factory);
		    chartPanel.update(rtrace, model);
		    chartPanel.getChartPanel().setOpaque(true);
		    chartPanel.getChartPanel().setBackground(Color.white);
		    if (primo) {
		    	roundedPanelHealth.add(chartPanel);
		        primo = false;
		        }
		    chartPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		    healthGraphs.put(piID, chartPanel);
            if (selected.equals(piID)) {
                SwingNode swingNode2 = new SwingNode();
                //createSwingContent(swingNode2);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	runTimeMonitorView.getChildren().add(swingNode2);
        				AnchorPane.setBottomAnchor(swingNode2, 0.0);
        	    		AnchorPane.setTopAnchor(swingNode2, 0.0);
        	    		AnchorPane.setLeftAnchor(swingNode2, 0.0);
        	    		AnchorPane.setRightAnchor(swingNode2, 0.0);
        	    		swingNode2.setContent(roundedPanelHealth);
                    }
                });
                
                //Thread.sleep(1000);
                
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	
	public class ProxySimulator extends Thread {
		
        private final Socket socket;
        private final OutputStream outStream;
        private final InputStream inStream;
        public ProxySimulator(final Socket socket, final OutputStream outStream, final InputStream inStream) {
            super("Proxy Simulator");
            setDaemon(true);
            this.outStream = outStream;
            this.inStream = inStream;
            this.socket = socket;
        }
        @Override
        public void run() {
            BufferedReader in = null;
            HashMap properties = new HashMap();
            String referenceDecl = null;
            PrintWriter out = null;
            String[] completeSetEvents = null;
            Hashtable handles = new Hashtable();
            partialTraces = new Hashtable();
            traceHashMap = new HashMap<>();
            in = new BufferedReader(new InputStreamReader(inStream));
            out = new PrintWriter(outStream, true);
            String letto = null;
            File message = null;
            mr = new MonitorRunner(conflict);
            try {
                referenceDecl = in.readLine() + "\n";
                while (!referenceDecl.contains("</model>")) {
                    referenceDecl = referenceDecl + in.readLine() + "\n";

                }
                Socket socket = null;
                ObjectOutputStream oos = null;
                //socket = new Socket(HOST, 9875);
                //oos = new ObjectOutputStream(socket.getOutputStream());
                //oos.writeObject("model");// Send model to myServer
                //oos = new ObjectOutputStream(socket.getOutputStream());
                //System.out.println("Sending request to Socket Server");
                //oos.writeObject(referenceDecl);
                System.out.println("Writing model");
                mr.setModel(referenceDecl); // Maybe some sort of error thingy here.
                completeSetEvents = new String[0];
                message = File.createTempFile("message", ".xml");
                message.deleteOnExit();
                PrintWriter printmessage = new PrintWriter(new FileWriter(message));
                letto = in.readLine();

                while (!letto.contains("</org.deckfour.xes.model.impl.XTraceImpl>")) {
                    letto = in.readLine();
                    printmessage.println(letto);
                }
                printmessage.println(letto);
                trace = (XTrace) osxmlConverter.fromXML(letto);// Letto should still be the original trace.
                traceHashMap.put(letto, trace);
                System.out.println("Letto: " + letto);
                System.out.println("Tracesize after letto: " + trace.size());

                printmessage.flush();
            } catch (IOException | DeclareParserException e1) {
                e1.printStackTrace();
                System.out.println("BAAAAAAD"); 
            }
            while (message != null && trace != null) {
            	try {
                    String modelID = "MyModel";//here could actually somehow get the name of the model from the model that has been inserted.
                    String processInstanceID = XConceptExtension.instance().extractName(trace);
                    types.put(processInstanceID, modelID);
            		
                    XTrace partialTrace;
                    String eventName = "";// Mine event name here
                    long timestamp = 0;
                    String piID = "";
                    long old = -1;
                    XEvent completeEvent = new XEventImpl();
                    for(XEvent e : trace) {
                        XAttributeMap eventAttributeMap = e.getAttributes();
                        completeEvent = e;
                        eventName = XConceptExtension.instance().extractName(e).replaceAll(" ", "_");
                        String ts = eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION).getAttributes().toString();
                        long current = XTimeExtension.instance().extractTimestamp(e).getTime();
                        if (current <= old) {
                            old = old + 1;
                        } else {
                            old = current;
                        }
                        timestamp = old;
                        piID = XConceptExtension.instance().extractName(trace);
                    }
                    if (partialTraces.containsKey(piID)) {
                        partialTrace = (XTrace) partialTraces.get(piID);
                    } else {
                        partialTrace = new XTraceImpl(new XAttributeMapImpl());
                        if (!instancesAll.contains(piID)) {
                            instancesAll.add(piID);
                        }
                    }
                    trace.get(0);
                    converter = new FluentsConverter(new NoGroupingStrategy());
                    
                    partialTrace.add(completeEvent);
                    partialTraces.put(piID, partialTrace);
                    Socket socket = null;
                    ObjectOutputStream oos = null;
                    //socket = new Socket(HOST, 9875);
                    //oos = new ObjectOutputStream(socket.getOutputStream());
                    //oos.writeObject("trace");// Send trace to myServer
                    //oos = new ObjectOutputStream(socket.getOutputStream());
                    //System.out.println("Sending request to Socket Server");
                    //oos.writeObject(letto);
                    //ObjectInputStream ois = null;
                    //ois = new ObjectInputStream(socket.getInputStream());
                    //someFluent = (String) ois.readObject();
                    try {
                    	someFluent  = mr.setTrace(letto);
                    	System.out.println("Somefluent: " + someFluent);
                    	if (someFluent.equals("nothing")) {
                    		throw new InterruptedException();
                    	}
                    }catch(InterruptedException error) {              
                    	Platform.runLater(new Runnable() {
                    		@Override public void run() {
                    			Alert alert = new Alert(AlertType.ERROR);
                    			alert.setTitle("Oops");
                    			alert.setContentText("Something went wrong, make sure that your model and log match");
                    			Optional<ButtonType> result = alert.showAndWait();
                    			}
                    		});
                    	
                    	//Platform.runLater(new Runnable() {
                    	//	@Override
                    	//	public void run() {
                    	//		Alert alert = new Alert(AlertType.ERROR);
                    	//		alert.setTitle("Oops!");
                    	//		alert.setContentText("Something went wrong, make sure your log and model match!");
		                 //   	}
                    	//	});
                    	Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
                    	    try {
                    	        socket.close();
                    	        System.out.println("The server is shut down!");
                    	    } catch (IOException e) { /* failed */ }
                    	}});
                        return;
                    }
                    
                    if (showA) {
                        System.out.println("in if showA");
                        instances = new Vector();
                        for (int i = 0; i < instancesAll.size(); i++) {
                            InstanceInfo ii = new InstanceInfo();
                            ii.setId((String) instancesAll.get(i));
                            System.out.println("ii" + ii);
                            if (instancesViol.contains(instancesAll.get(i))) {
                                ii.setViolated(true);
                            } else {
                                ii.setViolated(false);
                            }
                            instances.add(ii);
                        }
                    }
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            ObservableList<Integer> selectedIndices = instancesList.getSelectionModel().getSelectedIndices();
                            int index = -1;
                            if (selectedIndices.size() > 0) {
                                index = selectedIndices.get(0);
                            }
                            instancesList.getItems().clear();
                            instancesList.setItems(FXCollections.observableList(instances));
                            instancesList.getSelectionModel().select(index);
                        }
                    });
                    Thread.sleep(1000);
                    Vector vv = (Vector) violationsModels.get(piID);
                    String diags = "Observed   " + eventName;
                    int oldSize;
                    if (vv == null) {
                        oldSize = 0;
                    } else {
                        oldSize = vv.size();
                    }
                    if (!someFluent.equals("Nothing")) {
                        updateOutput(piID, eventName);// TODO We get to about here
                    }
                    vv = (Vector) violationsModels.get(piID);
                    int newSize;
                    if (vv == null) {
                        newSize = 0;
                    } else {
                        newSize = vv.size();
                    }
                    if (oldSize == newSize) {
                        out.println("");
                    } else {
                        String positive = "";// no need for this
                        if (!positive.isEmpty()) {
                            if (positive != null) {
                                String[] positivesForSize = positive.split(",");
                                if (positivesForSize.length == 1) {
                                    diags = diags + ",   while for this reference model expecting   "
                                            + positivesForSize[0];
                                } else {
                                    String posList = positive.replace(",", ", ");
                                    int indCo = posList.lastIndexOf(",");
                                    String sub = posList.substring(indCo);
                                    posList = posList
                                            .replaceFirst(sub, sub.replaceFirst(", ", ", or "));
                                    diags = diags + ",   while for this reference model expecting   "
                                            + posList;
                                }
                            }
                        }
                        String[] negativeSet = null;
                        String negative = "";
                        if (!negative.isEmpty()) {
                            if (negative != null) {
                                negativeSet = negative.split(",");
                            }
                        }
                        String[] positiveSet = positive.split(",");
                        Vector ve = new Vector();
                        for (int i = 0; i < positiveSet.length; i++) {
                            ve.add(positiveSet[i]);
                        }
                        if (negativeSet != null) {
                            if (negativeSet.length >= 1) {
                                Vector vecCom = new Vector();
                                for (int g = 0; g < completeSetEvents.length; g++) {
                                    vecCom.add(completeSetEvents[g]);
                                }
                                Vector vecNeg = new Vector();
                                for (int g = 0; g < negativeSet.length; g++) {
                                    vecNeg.add(negativeSet[g]);
                                }
                                for (int g = 0; g < completeSetEvents.length; g++) {
                                    if (vecNeg.contains(completeSetEvents[g])) {
                                        vecCom.remove(completeSetEvents[g]);
                                    }
                                }
                                diags = diags + "   (or otherwise everything different from   ";
                                String last = null;
                                if (vecNeg.size() == 1) {
                                    diags = diags + vecNeg.get(0) + ")";
                                } else {
                                    for (int g = 0; g < vecNeg.size(); g++) {
                                        if (!ve.contains(vecNeg.get(g))) {
                                            diags = diags + vecNeg.get(g) + ", ";
                                            last = (String) vecNeg.get(g);
                                        }
                                    }
                                    diags = diags.replace(", " + last + ", ", " and " + last + ")");
                                }
                            }
                        }
                        out.println(diags);
                    }
                    if (selected.equals(piID)) {
                        roundedPanelHealth.removeAll();
                        roundedPanelHealth.add((FluentChartStandardPanel) healthGraphs.get(selected));
                        diagnosticsList.setModel(new ViolationListModel((Vector) violationsModels.get(selected)));
                    }
            	}catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    message.delete();
                    message = File.createTempFile("message", ".xml");
                    message.deleteOnExit();
                    PrintWriter printmessage = new PrintWriter(new FileWriter(message));
                    letto = in.readLine();
                    System.out.println("letto: ");
                    System.out.println(letto);
                    if (letto == null || letto.isEmpty()) {
                        message = null;
                        trace = null;
                        System.out.println("Connection closed by log streamer");
                        Socket socket = null;
                        ObjectOutputStream oos = null;
                        //socket = new Socket(HOST, 9875);
                        //oos = new ObjectOutputStream(socket.getOutputStream());
                        //oos.writeObject("exit");// Send trace to myServer

                    } else {
                        while (!letto.contains("</org.deckfour.xes.model.impl.XTraceImpl>")) {
                            printmessage.println(letto);
                        }
                        printmessage.println(letto);
                        trace = (XTrace) osxmlConverter.fromXML(letto);
                        traceHashMap.put(letto, trace);
                        printmessage.flush();
                        printmessage.close();
                    }
                }catch (IOException e) {
                    message = null;
                    trace = null;
                    handles.clear();
                    e.printStackTrace();
                }
            }
            if (someFluent.equals("nothing")) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Oops!");
                alert.setContentText("Something went wrong, make sure your log and model match!");
            }
        }
	}
	protected void instancesSelectionChanged() {
        ObservableList<Integer> selectedIndices = instancesList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() > 0) {
        	selected =((InstanceInfo) instances.get(selectedIndices.get(0))).getId();
        	final SwingNode swingNode = new SwingNode();
        	createSwingContent2(swingNode);
        	runTimeMonitorView.getChildren().add(swingNode);
        	AnchorPane.setBottomAnchor(swingNode, 0.0);
    		AnchorPane.setTopAnchor(swingNode, 0.0);
    		AnchorPane.setLeftAnchor(swingNode, 0.0);
    		AnchorPane.setRightAnchor(swingNode, 0.0);
        }
    }


	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

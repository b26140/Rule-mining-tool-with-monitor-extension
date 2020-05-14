package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.openmbean.OpenDataException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.DataConformance.framework.ActivityMatchCost;
import org.processmining.plugins.DataConformance.framework.VariableMatchCost;
import org.processmining.plugins.DeclareConformance.ReplayableActivityDefinition;
import org.processmining.plugins.DeclareConformance.Replayer;
import org.processmining.plugins.DeclareConformance.ResultReplayDeclare;
import org.processmining.plugins.dataawaredeclarereplayer.Runner;
import org.processmining.plugins.dataawaredeclarereplayer.result.AlignmentAnalysisResult;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareanalyzer.AnalysisResult;
import org.processmining.plugins.declareanalyzer.Tester;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.util.XLogReader;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.io.params.OutputModelParameters;
import task.ActivityMappingTask;
import task.DataAwareDeclareReplayerTask;
import task.DeclareAnalyzerTask;
import task.DeclareReplayerTask;
import task.DiscoverMinerfulTask;
import task.DiscoverTask;
import task.LogGenTask;
import task.MinerfulResult;
import util.AlloyLogGeneratorTemplate;
import util.ConformanceCheckingTemplate;
import theFirst.LogStreamer;
import theFirst.MoBuConClient;
import theFirst.ServerRunner;
import util.GraphGenerator;
import util.MinerfulTemplate;
import util.ProcessModelGenerator;
import view.Browser;

public class TabbedMainViewController extends TabPane {
	
	@FXML
	private ChoiceBox<String> fileChoice;
	
	@FXML
	private ChoiceBox<String> discoveryChoice;
	
	@FXML
	private Label dmLabel;
	
	@FXML
	private AnchorPane actionPane;
	
	@FXML
	private StackPane buttonStack;
	
	@FXML
	private AnchorPane actionPane2;
	
	@FXML
	private AnchorPane actionPane3;
	
	@FXML
	private AnchorPane actionPane4;
	
	@FXML
	private Button discoverButton;
	
	@FXML
	private AnchorPane actionPane5;// for Monitoring
	
	@FXML
	private Button configureButton;
	
	@FXML
	private Button generateButton;
	
	@FXML
	private Button exportModelButton;
	
	@FXML
	private Button settingButton;
	
	@FXML
	private Button settingButton2;
	
	@FXML
	private Button checkButton;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private ChoiceBox<String> generatorChoice;
	
	@FXML
	private ChoiceBox<String> logFileChoice;
	
	@FXML
	private ChoiceBox<String> logMonitorChoice;
	
	@FXML
	private ChoiceBox<String> modelMonitorChoice;
	
	@FXML
	private ChoiceBox<String> declareModelChoice;
	
	@FXML
	private ChoiceBox<String> chooseMonitorMethod;
	
	@FXML
	private ChoiceBox<String> mpDeclareChoice;
	
	@FXML
	private Slider zoomSlider;
	
	@FXML
	private Label zoomValue;
	
	@FXML
	private Label zoomText;
	
	@FXML
	private ChoiceBox<String> methodChoice;
	
	@FXML
	private ChoiceBox<String> editedModels;
	
	private Stage stage;
	
	private Map<String,String> openedFiles;
	
	private Map<String,DiscoveryResultController> previousWorks = new HashMap<String,DiscoveryResultController>();
	
	private Node previousDiscoverySetting;
	
	private Node previousDiscoverySettingMinerFul;
	
	private Node previousReplayerWDataSetting;
	
	private Node previousReplayerWODataSetting;
	
	private Node previousCCresult;
	
	private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	@FXML
	private RadioButton declare;
	
	@FXML
	private RadioButton mobucon;
	
	@FXML
	private RadioButton onlinedclare;
	
	private String monitorModel = "";
	
	private ModelController mc;
	
	public MoBuConClient mob;
	
	private TabbedMonitorController tarc;

	private boolean useBuiltIn;
	
	private int port;
	
	@FXML
	private ToggleGroup toggleGroup2 = new ToggleGroup();
	
	@FXML
	private CheckBox conflictCheck;
	
	@FXML
	private RadioButton builtin;
	
	@FXML
	private RadioButton external;
	
	@FXML 
	private TextField portField;
	
	
	public TabbedMainViewController(Stage stage) {
		this.stage = stage;
		this.openedFiles = new HashMap<String,String>();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/TabbedMainView2.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            //TODO Listener, so it would get rid of everything.
            
            conflictCheck.setSelected(true);
            builtin.setToggleGroup(toggleGroup2);
        	external.setToggleGroup(toggleGroup2);
        	toggleGroup2.selectToggle(builtin);
        	useBuiltIn = true;
        	portField.setDisable(true);
            chooseMonitorMethod.getSelectionModel().selectFirst();
        	toggleGroup2.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
        		if (newVal == builtin) {
        	    	portField.setDisable(true);
        	    	useBuiltIn = true;
        		}
        		else{
        	    	portField.setDisable(false);
        	    	useBuiltIn = false;
        		}});
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void shutdown() {
		service.shutdown();
	}
	
	public String getDiscoveredLogName() {
		String log = this.fileChoice.getSelectionModel().getSelectedItem();
		int ld = log.lastIndexOf('.');
		return log.substring(0, ld);
	}
	
	@FXML
	public void openFile() {
		if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Log files",Arrays.asList("*.xes","*.mxml","*.xes.gz",".mxml.gz"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
		//String absPath = "";
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	
        	fileChoice.getItems().remove(file.getName());
        	fileChoice.getItems().add(file.getName());
        	fileChoice.getSelectionModel().select(file.getName());
        	
        	logFileChoice.getItems().remove(file.getName());
        	logFileChoice.getItems().add(file.getName());
        	
        	discoveryChoice.getSelectionModel().selectFirst();
        	//discover();
        }
	}
	
	@FXML
	public void openFile2() {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Log files",Arrays.asList("*.xes","*.mxml"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
		//String absPath = "";
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	
        	logFileChoice.getItems().remove(file.getName());
        	logFileChoice.getItems().add(file.getName());
        	logFileChoice.getSelectionModel().select(file.getName());
        	
        	fileChoice.getItems().remove(file.getName());
        	fileChoice.getItems().add(file.getName());
        	
        	if(!declareModelChoice.getSelectionModel().isEmpty()) {
        		methodChoice.getSelectionModel().selectFirst();
        	}
        }
	}
	
	public void addLogFile(File file) {
		openedFiles.put(file.getName(), file.getAbsolutePath());
    	
    	fileChoice.getItems().remove(file.getName());
    	fileChoice.getItems().add(file.getName());
    	
    	logFileChoice.getItems().remove(file.getName());
    	logFileChoice.getItems().add(file.getName());
	}
	
	public void addModelFile(File file) {
		openedFiles.put(file.getName(), file.getAbsolutePath());
    	
	    //String mpDeclare = mpDeclareChoice.getSelectionModel().getSelectedItem();
    	mpDeclareChoice.getItems().remove(file.getName());
    	mpDeclareChoice.getItems().add(file.getName());
    	/*if(mpDeclare != null && mpDeclare.equals(file.getName())) {
    		mpDeclareChoice.getSelectionModel().select(mpDeclare);
    	}*/
    	
    	//String declareModel = declareModelChoice.getSelectionModel().getSelectedItem();
    	declareModelChoice.getItems().remove(file.getName());
    	declareModelChoice.getItems().add(file.getName());
    	/*if(declareModel != null && declareModel.equals(file.getName())) {
    		declareModelChoice.getSelectionModel().select(declareModel);
    	}*/
    	
    	editedModels.getItems().remove(file.getName());
    	editedModels.getItems().add(file.getName());
    	//editedModels.getSelectionModel().select(file.getName());
	}
	
	public void removeModelFile(String fn) {
		openedFiles.remove(fn);
		declareModelChoice.getItems().remove(fn);
		mpDeclareChoice.getItems().remove(fn);
		editedModels.getItems().remove(fn);
	}
	
	public void addInMemory(String name, String data) {
		openedFiles.put(name, data);
		
		declareModelChoice.getItems().remove(name);
		declareModelChoice.getItems().add(name);
		
		mpDeclareChoice.getItems().remove(name);
		mpDeclareChoice.getItems().add(name);
		
		editedModels.getItems().remove(name);
		editedModels.getItems().add(name);
	}
	
	// Here we open Log. 
	@FXML
	public void openLog() {
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Log files",Arrays.asList("*.xes", "*.mxml"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	logMonitorChoice.getItems().add(file.getName());
        	logMonitorChoice.getSelectionModel().select(file.getName());
        }
	}
	
	public XLog convertToXlog(String inputLogFileName) {
		File logFile = new File(inputLogFileName);
		XLog xlog = null;
		
		if (inputLogFileName.toLowerCase().endsWith("mxml")){
			XMxmlParser parser = new XMxmlParser();
			if(parser.canParse(logFile)){
				try {
					xlog = parser.parse(logFile).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (inputLogFileName.toLowerCase().endsWith("xes")){
			XesXmlParser parser = new XesXmlParser();
			if(parser.canParse(logFile)){
				try {
					xlog = parser.parse(logFile).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return xlog;
	}
	
	@FXML
	public void openMonitorModel() {
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = null;
		//TODO get this fixed
		if (chooseMonitorMethod.getValue().toString().equals("MP-Declare w Alloy")) {
			filter = new ExtensionFilter("Log files",Arrays.asList("*.decl"));			
		}
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {

        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	modelMonitorChoice.getItems().add(file.getName());
        	modelMonitorChoice.getSelectionModel().select(file.getName());
        	monitorModel = readAllText(file);
        	System.out.println(monitorModel);
    		if (chooseMonitorMethod.getValue().toString().equals("MP-Declare w Alloy")) {
    			mc = new ModelController(monitorModel, true);			
    		} 
        	mob = new MoBuConClient();
    		tarc = new TabbedMonitorController(mc, mob, true);
    		actionPane5.getChildren().clear();
    		actionPane5.getChildren().add(tarc);
    		AnchorPane.setLeftAnchor(tarc, 0.0);
    		AnchorPane.setRightAnchor(tarc, 0.0);
    		AnchorPane.setTopAnchor(tarc, 0.0);
    		AnchorPane.setBottomAnchor(tarc, 0.0);
        }      
	}
	@FXML
	public void runMonitoring() throws Exception {
		String model = modelMonitorChoice.getSelectionModel().getSelectedItem();// failinimed.
		String logFile = logMonitorChoice.getSelectionModel().getSelectedItem();
		if ((model == null || logFile == null) && useBuiltIn) {
			showAlert("Model or log is missing!");
		}
		else {
			if (!useBuiltIn) {
	        	mob = new MoBuConClient();
	        	try {
	        		port = Integer.parseInt(portField.getText());
	        	} catch (NumberFormatException e) {
	        		showAlert("Port number must be an integer value");
	        		return;	
	        		}
				mob.setPORT(port);
				if (chooseMonitorMethod.getValue().toString().equals("MP-Declare w Alloy")) {
	        		mc = new ModelController(monitorModel, true);
	        	}
				tarc = new TabbedMonitorController(mc, mob, false);
				actionPane5.getChildren().clear();
	    		actionPane5.getChildren().add(tarc);
	    		AnchorPane.setLeftAnchor(tarc, 0.0);
	    		AnchorPane.setRightAnchor(tarc, 0.0);
	    		AnchorPane.setTopAnchor(tarc, 0.0);
	    		AnchorPane.setBottomAnchor(tarc, 0.0);
			}
			//TODO This place pick and do some stuff here.
			mob.setConflict(conflictCheck.isSelected());			
			//MoBuConClient mobu = new MoBuConClient();
			Thread.sleep(1000);
			mob.runMoBuConClient();
			if(useBuiltIn) {
				String inputLogFilePath = openedFiles.get(logFile);
				XLog log = convertToXlog(inputLogFilePath);
				
				LogStreamer ls = new LogStreamer(monitorModel, log, 5000);
				ls.start();
				tarc.getTabPane().getSelectionModel().select(tarc.getMonitorTab());
			}
		}
	}
    public static String readAllText(File file) {
        StringBuilder sb = new StringBuilder(2048);
        try {
            FileInputStream is = new FileInputStream(file);
            Reader r = new InputStreamReader(is, "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
	
	private String convertToXml(String filePath, boolean isSingleQuote) throws Exception {
		if(filePath.endsWith(".decl")) {
			Scanner sc = new Scanner(new File(filePath));
			List<String> activityList = new ArrayList<String>();
			List<String> constraintList = new ArrayList<String>();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.startsWith("activity")) {
					activityList.add(line.substring(9));
				}
				else if(line.indexOf('|') != -1) {
					constraintList.add(line);
				}
			}
			sc.close();
			return buildXml(activityList,constraintList,isSingleQuote);
		}
		else {
			Scanner sc = new Scanner(filePath);
			List<String> activityList = new ArrayList<String>();
			List<String> constraintList = new ArrayList<String>();
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.startsWith("activity")) {
					activityList.add(line.substring(9));
				}
				else if(line.indexOf('|') != -1) {
					constraintList.add(line);
				}
			}
			sc.close();
			return buildXml(activityList,constraintList,isSingleQuote);
		}
		
	}
	
	private String prepareActivityCondition(String s,boolean isSingleQuote) {
    	if(s.isEmpty()) return s;
    	String condition = s;
    	if(condition.contains(" = ")) condition = condition.replace("=", "==");
    	if(condition.contains(" is not ")) condition = condition.replace("is not", "!=");
    	if(condition.contains(" is ")) condition = condition.replace("is", "==");
    	if(condition.contains(" and ")) condition = condition.replace("and", "&&");
    	if(condition.contains(" or ")) condition = condition.replace("or", "||");
    	Matcher m = Pattern.compile("(.*) (<|>|<=|>=|==|!=) (.*)").matcher(condition);
    	m.find();
    	String data = m.group(3);
    	if(data.matches("^\\d+.\\d+$|^\\d+$")) {
    		return m.group(1)+" "+m.group(2)+" "+""+m.group(3)+"";
    	}
    	else if(isSingleQuote) {
    		return m.group(1)+" "+m.group(2)+" "+"'"+m.group(3)+"'";
    	}
    	else {
    		return m.group(1)+" "+m.group(2)+" "+"\""+m.group(3)+"\"";
    	}
    }
	
	private String prepareConstraintCondition(String s,boolean isSingleQuote) {
    	if(s.isEmpty()) return s;
    	System.out.println("Ccondition: "+s);
    	String condition = "T."+s.substring(2);
    	if(condition.contains(" = ")) condition = condition.replace("=", "==");
    	if(condition.contains(" is not ")) condition = condition.replace("is not", "!=");
    	if(condition.contains(" is ")) condition = condition.replace("is", "==");
    	if(condition.contains(" and ")) condition = condition.replace("and", "&&");
    	if(condition.contains(" or ")) condition = condition.replace("or", "||");
    	Matcher m = Pattern.compile("(.*) (<|>|<=|>=|==|!=) (.*)").matcher(condition);
    	m.find();
    	String data = m.group(3);
    	if(data.matches("^\\d+.\\d+$|^\\d+$")) {
    		return m.group(1)+" "+m.group(2)+" "+""+m.group(3)+"";
    	}
    	else if(isSingleQuote) {
    		return m.group(1)+" "+m.group(2)+" "+"'"+m.group(3)+"'";
    	}
    	else {
    		return m.group(1)+" "+m.group(2)+" "+"\""+m.group(3)+"\"";
    	}
    }
	
	private String prepareCondition(String s, boolean isSingleQuote) {
		if(s.isEmpty()) return s;
		Matcher mAnd = Pattern.compile("(.*) and (.*)").matcher(s);
		if(mAnd.find()) {
			String s2 = "(" + prepareCondition(mAnd.group(1),isSingleQuote) + ")" + " && " + "(" + prepareCondition(mAnd.group(2),isSingleQuote) + ")";
			return s2;
		}
		else {
			Matcher mOr = Pattern.compile("(.*) or (.*)").matcher(s);
			if(mOr.find()) {
				String s2 = "("+ prepareCondition(mOr.group(1),isSingleQuote) + ")" + " || " + "(" + prepareCondition(mOr.group(2),isSingleQuote) + ")";
				return s2;
			}
			else {
				String s2 = s;
				if(s2.contains(" = ")) s2 = s2.replace("=", "==");
		    	if(s2.contains(" is not ")) s2 = s2.replace("is not", "!=");
		    	if(s2.contains(" is ")) s2 = s2.replace("is", "==");
		    	Matcher mSame = Pattern.compile("same (.*)").matcher(s2);
		    	if(mSame.find()) {
		    		s2 = "A."+mSame.group(1)+" == "+"T."+mSame.group(1);
		    	}
		    	Matcher mDiff = Pattern.compile("different (.*)").matcher(s2);
		    	if(mDiff.find()) {
		    		s2 = "A."+mDiff.group(1)+" != "+"T."+mDiff.group(1);
		    	}
		    	Matcher mNotIn = Pattern.compile("(\\w\\.\\w+) not in \\((.*)\\)").matcher(s2);
		    	if(mNotIn.find()) {
		    		String lhs = mNotIn.group(1);
		    		String[] rha = mNotIn.group(2).split(",");
		    		String ss2 = "";
		    		for(String r: rha) {
		    			if(isSingleQuote) {
		    				ss2 += "("+lhs + " != '" + r.trim() + "') && ";
		    			}
		    			else {
		    				ss2 += "("+lhs + " != \"" + r.trim() + "\") && ";
		    			}
		    		}
		    		s2 = ss2.substring(0, ss2.length()-4);
		    		return s2;
		    	}
		    	Matcher mIn = Pattern.compile("(\\w\\.\\w+) in \\((.*)\\)").matcher(s2);
		    	if(mIn.find()) {
		    		String lhs = mIn.group(1);
		    		String[] rha = mIn.group(2).split(",");
		    		String ss2 = "";
		    		for(String r: rha) {
		    			if(isSingleQuote) {
		    				ss2 += "("+lhs + " == '" + r.trim() + "') || ";
		    			}
		    			else {
		    				ss2 += "("+lhs + " == \"" + r.trim() + "\") || ";
		    			}
		    		}
		    		s2 = ss2.substring(0, ss2.length()-4);
		    		return s2;
		    	}
		    	Matcher m = Pattern.compile("(.*) ?(<|>|<=|>=|==|!=) ?(.*)").matcher(s2);
		    	m.find();
		    	String data = m.group(3);
		    	if(data.matches("^\\d+.\\d+$|^\\d+$")) {
		    		String mg1 = m.group(1).trim();
		    		String mg2 = m.group(2).trim();
		    		String mg3 = m.group(3).trim();
		    		return mg1+" "+mg2+" "+""+mg3+"";
		    	}
		    	else if(data.matches("^\\D\\..*$")) {
		    		String mg1 = m.group(1).trim();
		    		String mg2 = m.group(2).trim();
		    		String mg3 = m.group(3).trim();
		    		return mg1+" "+mg2+" "+""+mg3+"";
		    	}
		    	else if(isSingleQuote) {
		    		String mg1 = m.group(1).trim();
		    		String mg2 = m.group(2).trim();
		    		String mg3 = m.group(3).trim();
		    		return mg1+" "+mg2+" "+"'"+mg3+"'";
		    	}
		    	else {
		    		String mg1 = m.group(1).trim();
		    		String mg2 = m.group(2).trim();
		    		String mg3 = m.group(3).trim();
		    		return mg1+" "+mg2+" "+"\""+mg3+"\"";
		    	}
			}
		}
	}
	
	private boolean isUnaryTemplate(String t) {
		return t.startsWith("Existence") || t.startsWith("Absence") || t.startsWith("Init") || t.startsWith("Exactly");
	}
	
	private String getCondition(String constraint,boolean isSingleQuote) {
    	Matcher mBinary = Pattern.compile("(.*)\\[.*\\] \\|(.*) \\|(.*) \\|(.*)").matcher(constraint);
    	Matcher mUnary = Pattern.compile("(.*)\\[.*\\] \\|(.*) \\|(.*)").matcher(constraint);
    	if(mBinary.find() && !isUnaryTemplate(mBinary.group(1)))
    		return "["+prepareCondition(mBinary.group(2),isSingleQuote)+"]"+"["+prepareCondition(mBinary.group(3),isSingleQuote)+"]"+"["+mBinary.group(4)+"]";
    	else if(mUnary.find() && isUnaryTemplate(mUnary.group(1))){
    		return "["+prepareCondition(mUnary.group(2),isSingleQuote)+"]"+"[]"+"["+mUnary.group(3)+"]";
    	}
    	else {
    		StringBuilder error = new StringBuilder();
    		error.append("\"").
    			append(constraint).
    			append("\"").append(" is invalid.\n");
    		if(isUnaryTemplate(constraint)) {
    			error.append("Unary constraint format: template[activity] |activation_condition |time_condition");
    			throw new IllegalArgumentException(error.toString());
    		}
    		else {
    			error.append("Binary constraint format: template[activity1, activity2] |activation_condition |correlation_condition |time_condition");
    			throw new IllegalArgumentException(error.toString());
    		}
    	}
	}
	
	private String buildXml(List<String> activityList, List<String> constraintList, boolean isSingleQuote) {
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+
						"<model><assignment language=\"ConDec\" name=\"new model\">"+
						"<activitydefinitions>";
		StringBuilder sb = new StringBuilder(header);
		for(int i=0; i<activityList.size(); i++) {
			sb.append("<activity id=\""+(i+1)+"\" name=\""+activityList.get(i)+"\"/>");
		}
		sb.append("</activitydefinitions>");
		sb.append("<constraintdefinitions>");
		for(int i=0; i<constraintList.size(); i++) {
			sb.append("<constraint id=\""+(11+i)+"\" mandatory=\"true\">");
			String constraint = constraintList.get(i);
			String cname = constraint.substring(0,constraint.indexOf('[')).toLowerCase();
			try {
				ConformanceCheckingTemplate.valueOf(cname.replace(" ", "_").replace("-", "$"));
			}
			catch(Exception e) {
				throw new IllegalArgumentException("\""+cname+"\" is not a valid declare template for conformance checking.");
			}
			sb.append("<condition>");
			String condition = getCondition(constraint,isSingleQuote).replace("&&","&amp;&amp;").replace("<", "&lt;");
			System.out.println(condition);
			sb.append(condition);
			sb.append("</condition>");
			sb.append("<name>"+cname+"</name>");
			sb.append("<template>");
			sb.append("<description>"+cname+"</description>");
			sb.append("<display>"+cname+"</display>");
			sb.append("<name>"+cname+"</name>");
			sb.append("<text>"+cname+"</text>");
			sb.append("<parameters>");
			Matcher mBinary = Pattern.compile(".*\\[(.*), (.*)\\] \\|.* \\|.* \\|.*").matcher(constraint);
			Matcher mUnary = Pattern.compile(".*\\[(.*)\\] \\|.* \\|.*").matcher(constraint);
			if(mBinary.find()) {
				
				String first = mBinary.group(1);
				String second = mBinary.group(2);
				
				sb.append("<parameter branchable=\"true\" id=\"1\" name=\"A\">");
				sb.append("<graphical>");
				sb.append("<style number=\"1\"/>");
				sb.append("<begin fill=\"true\" style=\"5\"/>");
				sb.append("<middle fill=\"false\" style=\"0\"/>");
				sb.append("<end fill=\"false\" style=\"0\"/>");
				sb.append("</graphical>");
				sb.append("</parameter>");
				
				sb.append("<parameter branchable=\"true\" id=\"2\" name=\"B\">");
				sb.append("<graphical>");
				sb.append("<style number=\"1\"/>");
				sb.append("<begin fill=\"true\" style=\"5\"/>");
				sb.append("<middle fill=\"false\" style=\"0\"/>");
				sb.append("<end fill=\"false\" style=\"0\"/>");
				sb.append("</graphical>");
				sb.append("</parameter>");
				sb.append("</parameters></template>");
				sb.append("<constraintparameters>");
				
				sb.append("<parameter templateparameter=\"1\">");
				sb.append("<branches>");
				sb.append("<branch name=\""+first+"\"/>");
				sb.append("</branches></parameter>");
				
				sb.append("<parameter templateparameter=\"2\">");
				sb.append("<branches>");
				sb.append("<branch name=\""+second+"\"/>");
				sb.append("</branches></parameter>");
				
				sb.append("</constraintparameters></constraint>");
			}
			else if(mUnary.find()) {
				
				String first = mUnary.group(1);
				
				sb.append("<parameter branchable=\"false\" id=\"1\" name=\"A\">");
				sb.append("<graphical>");
				sb.append("<style number=\"1\"/>");
				sb.append("<begin fill=\"true\" style=\"0\"/>");
				sb.append("<middle fill=\"false\" style=\"0\"/>");
				sb.append("<end fill=\"false\" style=\"0\"/>");
				sb.append("</graphical>");
				sb.append("</parameter>");
				sb.append("</parameters></template>");
				sb.append("<constraintparameters>");
				
				sb.append("<parameter templateparameter=\"1\">");
				sb.append("<branches>");
				sb.append("<branch name=\""+first+"\"/>");
				sb.append("</branches></parameter>");
				
				sb.append("</constraintparameters></constraint>");
			}
		}
		
		sb.append("</constraintdefinitions></assignment></model>");
		return writeToXml(sb.toString());
	}
	
	private String writeToXml(String content) {
		Path path = Paths.get("temp.xml");
		try (BufferedWriter writer = Files.newBufferedWriter(path))
		{
		    writer.write(content);
		    writer.close();
		    return path.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//See DeclareExecutionTrace for Declare Replayer
	@FXML
	public void check() throws Exception {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		String logFile = logFileChoice.getSelectionModel().getSelectedItem();
		String modelFile = declareModelChoice.getSelectionModel().getSelectedItem();
		String method = methodChoice.getSelectionModel().getSelectedItem();
		if(logFile == null) {
			showAlert("A log file must be selected!");
			return;
		}
		if(modelFile == null) {
			showAlert("A model file must be selected!");
			return;
		}
		if(method == null) {
			showAlert("A method must be selected!");
			return;
		}
		if(method.equals("Declare Analyzer")) {
			String convertedXmlFile = null;
			try {
				convertedXmlFile = convertToXml(openedFiles.get(modelFile),true);
			}
			catch(Exception e) {
				showAlert(e.getMessage());
				removeModelFile(modelFile);
				return;
			}
			DeclareAnalyzerTask task = new DeclareAnalyzerTask(openedFiles.get(logFile), convertedXmlFile);
			
			Node before = actionPane3.getChildren().size() == 0 ? null : actionPane3.getChildren().get(0);
			ProgressController pc = new ProgressController();
			pc.setOperationName("Checking...");
			pc.getCancelOperation().setOnAction(e -> {
				task.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane3.getChildren().remove(pc);
				if(before != null) actionPane3.getChildren().add(before);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(task.progressProperty());
			actionPane3.getChildren().add(pc);
			actionPane3.getChildren().remove(before);
			logFileChoice.setDisable(true);
			declareModelChoice.setDisable(true);
			methodChoice.setDisable(true);
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				DetailedAnalysisResultController darc = new DetailedAnalysisResultController(task.getValue());
				AnalysisResultController arc = new AnalysisResultController(task.getValue());
				TabbedAnalysisResultController tarc = new TabbedAnalysisResultController(arc, darc);
				this.previousCCresult = tarc;
				actionPane3.getChildren().clear();
				actionPane3.getChildren().add(tarc);
				AnchorPane.setLeftAnchor(tarc, 0.0);
				AnchorPane.setRightAnchor(tarc, 0.0);
				AnchorPane.setTopAnchor(tarc, 0.0);
				AnchorPane.setBottomAnchor(tarc, 0.0);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
				System.out.println("Conformance checking ends millis: "+System.currentTimeMillis());
			});
			service.execute(task);
			return;
		}
		if(method.equals("DataAware Declare Replayer")) {
			String convertedXmlFile = null;
			try {
				convertedXmlFile = convertToXml(openedFiles.get(modelFile),false);
			}
			catch(Exception e) {
				showAlert(e.getMessage());
				removeModelFile(modelFile);
				return;
			}
			DataAwareDeclareReplayerTask task = new DataAwareDeclareReplayerTask(openedFiles.get(logFile),convertedXmlFile,getMap(),getLamc(),getLvmc());
			
			Node before = actionPane3.getChildren().size() == 0 ? null : actionPane3.getChildren().get(0);
			ProgressController pc = new ProgressController();
			pc.setOperationName("Checking...");
			pc.getCancelOperation().setOnAction(e -> {
				task.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane3.getChildren().remove(pc);
				if(before != null) actionPane3.getChildren().add(before);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(task.progressProperty());
			actionPane3.getChildren().add(pc);
			actionPane3.getChildren().remove(before);
			logFileChoice.setDisable(true);
			declareModelChoice.setDisable(true);
			methodChoice.setDisable(true);
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				AlignmentAnalysisResult aar = task.getValue();
				XLog traces = null;
				try {
					traces = XLogReader.openLog(openedFiles.get(logFile));
				}catch(Exception exc) {
					exc.printStackTrace();
				}
				List<String> constraints = getListOfConstraintsFromFile(openedFiles.get(modelFile));
				StringBuilder sb = new StringBuilder();
				constraints.forEach(s -> {
					sb.append(s.replace('|', ' ').trim()+"\n");
				});
				AlignmentResultController arc = new AlignmentResultController(aar,traces);
				StatsController stats = new StatsController
						(String.format("%.2f",aar.getAverageFitness()),
				         String.format("%.2f",aar.getMedianFitness()),
				         aar.getTraceCount()+"",aar.getNumberOfConstraints()+"",aar.getComputationTime()+"");
				TabbedAnalysisResultController tarc = new TabbedAnalysisResultController("Statistics", "Trace/Alignment Details");
				tarc.setToFirstTab(stats);
				arc.insertConstraints(constraints);
				tarc.setToSecondTab(arc);
				this.previousCCresult = tarc;
				actionPane3.getChildren().clear();
				actionPane3.getChildren().add(tarc);
				AnchorPane.setLeftAnchor(tarc, 0.0);
				AnchorPane.setRightAnchor(tarc, 0.0);
				AnchorPane.setTopAnchor(tarc, 0.0);
				AnchorPane.setBottomAnchor(tarc, 0.0);
				checkButton.setVisible(true);
				settingButton.setVisible(true);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
				System.out.println("Conformance checking ends millis: "+System.currentTimeMillis());
			});
			service.execute(task);
			return;
		}
		if(method.equals("Declare Replayer")) {
			String convertedXmlFile = null;
			try {
				convertedXmlFile = convertToXml(openedFiles.get(modelFile),false);
			}
			catch(Exception e) {
				showAlert(e.getMessage());
				removeModelFile(modelFile);
				return;
			}
			DeclareReplayerTask task = new DeclareReplayerTask(openedFiles.get(logFile),convertedXmlFile, getMap(), getLamc());
			
			Node before = actionPane3.getChildren().size() == 0 ? null : actionPane3.getChildren().get(0);
			ProgressController pc = new ProgressController();
			pc.setOperationName("Checking...");
			pc.getCancelOperation().setOnAction(e -> {
				task.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane3.getChildren().remove(pc);
				if(before != null) actionPane3.getChildren().add(before);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(task.progressProperty());
			actionPane3.getChildren().add(pc);
			actionPane3.getChildren().remove(before);
			logFileChoice.setDisable(true);
			declareModelChoice.setDisable(true);
			methodChoice.setDisable(true);
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				ResultReplayDeclare rrd = task.getValue();
				XLog traces = null;
				try {
					traces = XLogReader.openLog(openedFiles.get(logFile));
				}catch(Exception exc) {
					exc.printStackTrace();
				}
				AlignmentResultController arc = new AlignmentResultController(rrd,traces);
				StatsController stats = new StatsController();
				stats.setHeader1("Average Fitness", String.format("%.2f",rrd.meanFitness));
				stats.setHeader3("Number of Traces", String.format("%d",rrd.getAlignedLog().size()));
				List<String> constraints = getListOfConstraintsFromFile(openedFiles.get(modelFile));
				StringBuilder sb = new StringBuilder();
				constraints.forEach(s -> {
					sb.append(s.replace('|', ' ').trim()+"\n");
				});
				stats.setHeader4("Number of Constraints", String.format("%d",constraints.size()));
				//stats.setHeader2("Constraints", sb.toString());
				//stats.hideHeader3();
				//stats.hideHeader4();
				//stats.hideHeader5();
				arc.insertConstraints(constraints);
				TabbedAnalysisResultController tarc = new TabbedAnalysisResultController("Statistics", "Trace/Alignment Details");
				tarc.setToSecondTab(arc);
				tarc.setToFirstTab(stats);
				this.previousCCresult = tarc;
				actionPane3.getChildren().clear();
				actionPane3.getChildren().add(tarc);
				AnchorPane.setLeftAnchor(tarc, 0.0);
				AnchorPane.setRightAnchor(tarc, 0.0);
				AnchorPane.setTopAnchor(tarc, 0.0);
				AnchorPane.setBottomAnchor(tarc, 0.0);
				checkButton.setVisible(true);
				settingButton.setVisible(true);
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
				System.out.println("Conformance checking ends millis: "+System.currentTimeMillis());
			});
			service.execute(task);
			return;
		}
	}
	
	@FXML
	public void openXmlFile() {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Declare model",Arrays.asList("*.decl"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
		//String absPath = "";
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	
        	declareModelChoice.getSelectionModel().clearSelection();
        	declareModelChoice.getItems().remove(file.getName());
        	declareModelChoice.getItems().add(file.getName());
        	declareModelChoice.getSelectionModel().select(file.getName());
        	
        	mpDeclareChoice.getItems().remove(file.getName());
        	mpDeclareChoice.getItems().add(file.getName());
        	
        	editedModels.getItems().remove(file.getName());
        	editedModels.getItems().add(file.getName());
        	
        	if(!logFileChoice.getSelectionModel().isEmpty()) {
        		methodChoice.getSelectionModel().selectFirst();
        	}
        }
	}
	
	@FXML
	public void openMPDeclare() {
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Declare model",Arrays.asList("*.decl"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
		//String absPath = "";
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	
        	mpDeclareChoice.getSelectionModel().clearSelection();
        	mpDeclareChoice.getItems().remove(file.getName());
        	mpDeclareChoice.getItems().add(file.getName());
        	mpDeclareChoice.getSelectionModel().select(file.getName());
        	
        	declareModelChoice.getItems().remove(file.getName());
        	declareModelChoice.getItems().add(file.getName());
        	
        	editedModels.getItems().remove(file.getName());
        	editedModels.getItems().add(file.getName());
        }
	}
	
	public void cancel() {
		String discovery = discoveryChoice.getSelectionModel().getSelectedItem();
		if(discovery != null && discovery.equals("Declare Miner")) {
			this.previousDiscoverySetting = actionPane.getChildren().get(0);
		}
		if(discovery != null && discovery.equals("Minerful")) {
			this.previousDiscoverySettingMinerFul = actionPane.getChildren().get(0);
		}
		actionPane.getChildren().clear();
		DiscoveryResultController drc = previousWorks.get(fileChoice.getSelectionModel().getSelectedItem());
		if(drc != null) {
			actionPane.getChildren().add(drc);
			zoomSlider.setVisible(true);
			zoomValue.setVisible(true);
			zoomText.setVisible(true);
			//settingButton2.setVisible(true);
			//discoverButton.setVisible(false);
		}
		else {
			discoveryChoice.setVisible(true);
			dmLabel.setVisible(true);
			settingButton2.setVisible(true);
			discoverButton.setVisible(true);
		}
		//viewLabel.setVisible(true);
		//viewChoice.setVisible(true);
		//discoveryChoice.getSelectionModel().select(previousDiscoveryChoice);
	}
	
	public void closeCostConfig() {
		saveCCSetting();
		actionPane3.getChildren().clear();
		if(this.previousCCresult != null) {
			actionPane3.getChildren().add(this.previousCCresult);
		}
	}
	
	public void saveCCSetting() {
		CostConfigController ccc = (CostConfigController) actionPane3.getChildren().get(0);
		if(ccc.isVariableCostApplied()) {
			this.previousReplayerWDataSetting = actionPane3.getChildren().get(0);
		}
		else {
			this.previousReplayerWODataSetting = actionPane3.getChildren().get(0);
		}
	}
	
	@FXML
	public void openConfiguration() {
		if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		//discoveryChoice.setVisible(true);
		//dmLabel.setVisible(true);
		//configureButton.setVisible(false);
		//if(previousWorks.get(fileChoice.getSelectionModel().getSelectedItem()) != null) undoButton.setVisible(true);
		//settingButton2.setVisible(false);
		//discoverButton.setVisible(true);
		zoomSlider.setVisible(false);
		zoomValue.setVisible(false);
		zoomText.setVisible(false);
		//viewLabel.setVisible(false);
		//viewChoice.setVisible(false);
		String discovery = discoveryChoice.getSelectionModel().getSelectedItem();
		if(discovery == null) {
			showAlert("A discovery method must be selected!");
			//actionPane.getChildren().clear();
			return;
		}
		if(discovery.equals("Declare Miner")) {
			if(this.previousDiscoverySetting != null) {
				actionPane.getChildren().clear();
				actionPane.getChildren().add(this.previousDiscoverySetting);
				return;
			}
			showDeclareMinerConfiguration();
		}
		else if(discovery.equals("Minerful")) {
			if(this.previousDiscoverySettingMinerFul != null) {
				actionPane.getChildren().clear();
				actionPane.getChildren().add(this.previousDiscoverySettingMinerFul);
				return;
			}
			showMinerfulConfiguration();
		}
		//discoverButton.setVisible(true);
		//cancelButton.setVisible(true);
	}
	
	private void openCostConfigFor(String method, String logFile, String modelFile) throws Exception {
		if(method.equals("DataAware Declare Replayer")) {
			//actionPane3.getChildren().clear();
			//String logFile = logFileChoice.getSelectionModel().getSelectedItem();
			//String modelFile = declareModelChoice.getSelectionModel().getSelectedItem();
			String convertedXmlFile = null;
			try {
				convertedXmlFile = convertToXml(openedFiles.get(modelFile),false);
			}
			catch(Exception e) {
				showAlert(e.getMessage());
				removeModelFile(modelFile);
				return;
			}
			ActivityMappingTask amt = new ActivityMappingTask(openedFiles.get(logFile), convertedXmlFile);
			Node before = !actionPane3.getChildren().isEmpty() ? actionPane3.getChildren().get(0) : null ;
			ProgressController pc = new ProgressController();
			pc.setOperationName("Opening cost model configuration...");
			pc.getCancelOperation().setOnAction(e -> {
				amt.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane3.getChildren().remove(pc);
				if(before != null) {
					actionPane3.getChildren().add(before);
				}
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(amt.progressProperty());
			actionPane3.getChildren().clear();
			actionPane3.getChildren().add(pc);
			logFileChoice.setDisable(true);
			declareModelChoice.setDisable(true);
			methodChoice.setDisable(true);
			amt.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				Map<ReplayableActivityDefinition,XEventClass> map = amt.getValue();
				actionPane3.getChildren().clear();
				if(map == null) {
					showAlert("Something went wrong!");
				}
				else {
					try {
						CostConfigController ccc = new CostConfigController(map,openedFiles.get(modelFile),true);
						ccc.setTmvc(this);
						actionPane3.getChildren().add(ccc);
						AnchorPane.setBottomAnchor(ccc, 0.0);
						AnchorPane.setTopAnchor(ccc, 0.0);
						AnchorPane.setLeftAnchor(ccc, 0.0);
						AnchorPane.setRightAnchor(ccc, 0.0);
						settingButton.setVisible(true);
						checkButton.setVisible(true);
						logFileChoice.setDisable(false);
						declareModelChoice.setDisable(false);
						methodChoice.setDisable(false);
					}catch(Exception _e){
						showAlert("Something went wrong!");
					}
				}
			});
			service.execute(amt);
			
		}
		else if(method.equals("Declare Replayer")) {
			//actionPane3.getChildren().clear();
			//String logFile = logFileChoice.getSelectionModel().getSelectedItem();
			//String modelFile = declareModelChoice.getSelectionModel().getSelectedItem();
			String convertedXmlFile = null;
			try {
				convertedXmlFile = convertToXml(openedFiles.get(modelFile),false);
			}
			catch(Exception e) {
				showAlert(e.getMessage());
				removeModelFile(modelFile);
				return;
			}
			ActivityMappingTask amt = new ActivityMappingTask(openedFiles.get(logFile), convertedXmlFile);
			Node before = !actionPane3.getChildren().isEmpty() ? actionPane3.getChildren().get(0) : null ;
			ProgressController pc = new ProgressController();
			pc.setOperationName("Opening cost model configuration...");
			pc.getCancelOperation().setOnAction(e -> {
				amt.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane3.getChildren().remove(pc);
				if(this.previousCCresult != null) {
					actionPane3.getChildren().add(before);
				}
				logFileChoice.setDisable(false);
				declareModelChoice.setDisable(false);
				methodChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(amt.progressProperty());
			actionPane3.getChildren().clear();
			actionPane3.getChildren().add(pc);
			logFileChoice.setDisable(true);
			declareModelChoice.setDisable(true);
			methodChoice.setDisable(true);
			amt.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				Map<ReplayableActivityDefinition, XEventClass> map = amt.getValue();
				actionPane3.getChildren().clear();
				if(map == null) {
					showAlert("Something went wrong!");
				}
				else {
					try {
						CostConfigController ccc = new CostConfigController(map,openedFiles.get(modelFile),false);
						ccc.setTmvc(this);
						actionPane3.getChildren().add(ccc);
						AnchorPane.setBottomAnchor(ccc, 0.0);
						AnchorPane.setTopAnchor(ccc, 0.0);
						AnchorPane.setLeftAnchor(ccc, 0.0);
						AnchorPane.setRightAnchor(ccc, 0.0);
						settingButton.setVisible(true);
						checkButton.setVisible(true);
						logFileChoice.setDisable(false);
						declareModelChoice.setDisable(false);
						methodChoice.setDisable(false);
					}catch(Exception _e) {
						showAlert("Something went wrong");
					}
				}
			});
			service.execute(amt);
		}
	}
	
	@FXML
	public void openSetting() throws Exception{
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof ProgressController) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		String method = methodChoice.getSelectionModel().getSelectedItem();
		String logFile = logFileChoice.getSelectionModel().getSelectedItem();
		String modelFile = declareModelChoice.getSelectionModel().getSelectedItem();
		if(method == null) {
			showAlert("A method must be selected!");
			return;
		}
		if(logFile == null) {
			showAlert("A log file must be selected!");
			return;
		}
		if(modelFile == null) {
			showAlert("A model file must be selected!");
			return;
		}
		if(method.equals("DataAware Declare Replayer") && this.previousReplayerWDataSetting != null) {
			actionPane3.getChildren().clear();
			actionPane3.getChildren().add(this.previousReplayerWDataSetting);
			return;
		}
		if(method.equals("Declare Replayer") && this.previousReplayerWODataSetting != null) {
			actionPane3.getChildren().clear();
			actionPane3.getChildren().add(this.previousReplayerWODataSetting);
			return;
		}
		openCostConfigFor(method,logFile,modelFile);
	}
	
	private Map<ReplayableActivityDefinition,XEventClass> getMap() {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof CostConfigController) {
			saveCCSetting();
			CostConfigController ccc = (CostConfigController) actionPane3.getChildren().get(0);
			return ccc.getMap();
		}
		if(methodChoice.getSelectionModel().getSelectedItem().equals("DataAware Declare Replayer")) {
			if(this.previousReplayerWDataSetting != null) {
				CostConfigController ccc = (CostConfigController) this.previousReplayerWDataSetting;
				return ccc.getMap();
			}
		}
		if(methodChoice.getSelectionModel().getSelectedItem().equals("Declare Replayer")) {
			if(this.previousReplayerWODataSetting != null) {
				CostConfigController ccc = (CostConfigController) this.previousReplayerWODataSetting;
				return ccc.getMap();
			}
		}
		return null;
	}
	
	private List<ActivityMatchCost> getLamc() {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof CostConfigController) {
			saveCCSetting();
			CostConfigController ccc = (CostConfigController) actionPane3.getChildren().get(0);
			return ccc.getLamc();
		}
		if(methodChoice.getSelectionModel().getSelectedItem().equals("DataAware Declare Replayer")) {
			if(this.previousReplayerWDataSetting != null) {
				CostConfigController ccc = (CostConfigController) this.previousReplayerWDataSetting;
				return ccc.getLamc();
			}
		}
		if(methodChoice.getSelectionModel().getSelectedItem().equals("Declare Replayer")) {
			if(this.previousReplayerWODataSetting != null) {
				CostConfigController ccc = (CostConfigController) this.previousReplayerWODataSetting;
				return ccc.getLamc();
			}
		}
		return CostConfigController.defaultLamc();
	}
	
	private List<VariableMatchCost> getLvmc() {
		if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof CostConfigController) {
			saveCCSetting();
			CostConfigController ccc = (CostConfigController) actionPane3.getChildren().get(0);
			return ccc.getLvmc();
		}
		if(methodChoice.getSelectionModel().getSelectedItem().equals("DataAware Declare Replayer")) {
			if(this.previousReplayerWDataSetting != null) {
				CostConfigController ccc = (CostConfigController) this.previousReplayerWDataSetting;
				return ccc.getLvmc();
			}
		}
		return CostConfigController.defaultLvmc();
	}
	
	@FXML
	public void discover() {
		System.out.println("Discover button clicked...");
		zoomSlider.setVisible(false);
		zoomText.setVisible(false);
		zoomValue.setVisible(false);
		String discovery = discoveryChoice.getSelectionModel().getSelectedItem();
		String chosen = fileChoice.getSelectionModel().getSelectedItem();
		if(discovery == null) {
			showAlert("A discovery method must be selected!");
			return;
		}
		if(chosen == null) {
			showAlert("A file must be selected!");
			return;
		}
		if(discovery.equals("Declare Miner")) {
			String absPath = openedFiles.get(chosen);
			
			ConfigurationController controller = new ConfigurationController();
			if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ConfigurationController) {
				this.previousDiscoverySetting = actionPane.getChildren().get(0);
				controller = (ConfigurationController)actionPane.getChildren().get(0);
			}
			else if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ProgressController) {
				showAlert("There is an ongoing process, please cancel it first.");
				return;
			}
			else if(this.previousDiscoverySetting != null) {
				controller = (ConfigurationController)this.previousDiscoverySetting;
			}
			
			controller.setFilePath(absPath);
			Node before = actionPane.getChildren().size() == 0 ? null : actionPane.getChildren().get(0);
			controller.setZoom(zoomSlider);
			//controller.setViewChoice(viewChoice);
			final ConfigurationController control = controller;
			DiscoverTask discover = new DiscoverTask(control);
			
			ProgressController pc = new ProgressController();
			pc.setOperationName("Discovering...");
			pc.getCancelOperation().setOnAction(e -> {
				discover.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane.getChildren().remove(pc);
				if(before != null) actionPane.getChildren().add(before);
				discoveryChoice.setVisible(true);
				dmLabel.setVisible(true);
				discoverButton.setVisible(true);
				settingButton2.setVisible(true);
				fileChoice.setDisable(false);
				discoveryChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(discover.progressProperty());
			actionPane.getChildren().add(pc);
			actionPane.getChildren().remove(before);
			discover.setOnCancelled(e -> {
				System.out.println("Discover with "+absPath+" cancelled!");
			});
			fileChoice.setDisable(true);
			discoveryChoice.setDisable(true);
			discover.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (event) -> {
				System.out.println("The task has succeeded...");
				DiscoveryResultController drc = obtainDiscoveryResultController(discover.getValue(),control);
				this.previousWorks.put(chosen, drc);
				//cancelButton.setVisible(false);
				zoomSlider.setVisible(true);
				zoomValue.setVisible(true);
				zoomText.setVisible(true);
				zoomSlider.setValue(100);
				//viewLabel.setVisible(true);
				//viewChoice.setVisible(true);
				//this.previousDiscoveryChoice = discovery;
				actionPane.getChildren().clear();
				actionPane.getChildren().add(drc);
				AnchorPane.setTopAnchor(drc, 0.0);
				AnchorPane.setBottomAnchor(drc, 0.0);
				AnchorPane.setLeftAnchor(drc, 0.0);
				AnchorPane.setRightAnchor(drc, 0.0);
				//configureButton.setVisible(true);
				//discoverButton.setVisible(false);
				settingButton2.setVisible(true);
				discoverButton.setVisible(true);
				discoveryChoice.setVisible(true);
				dmLabel.setVisible(true);
				fileChoice.setDisable(false);
				discoveryChoice.setDisable(false);
				System.out.println("Result has inserted...");
				System.out.println("Discovery ends millis: "+System.currentTimeMillis());
			});
			System.out.println("Discover task has started...");
			service.execute(discover);
		}
		else if(discovery.equals("Minerful")) {
			String absPath = openedFiles.get(fileChoice.getSelectionModel().getSelectedItem());
			
			ConfigurationMinerFulController controller = new ConfigurationMinerFulController();
			if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ConfigurationMinerFulController) {
				this.previousDiscoverySettingMinerFul = actionPane.getChildren().get(0);
				controller = (ConfigurationMinerFulController)actionPane.getChildren().get(0);
			}
			else if(this.previousDiscoverySettingMinerFul != null) {
				controller = (ConfigurationMinerFulController)this.previousDiscoverySettingMinerFul;
			}
						
			controller.setFilePath(absPath);
			//controller.setViewChoice(viewChoice);
			Node before = actionPane.getChildren().size() == 0 ? null : actionPane.getChildren().get(0);
			controller.setZoom(zoomSlider);
			final ConfigurationMinerFulController control = controller;
			DiscoverMinerfulTask discover = new DiscoverMinerfulTask(control);
			
			ProgressController pc = new ProgressController();
			pc.setOperationName("Discovering...");
			pc.getCancelOperation().setOnAction(e -> {
				discover.cancel(true);
				pc.getIndicator().progressProperty().unbind();
				actionPane.getChildren().remove(pc);
				if(before != null) actionPane.getChildren().add(before);
				discoveryChoice.setVisible(true);
				dmLabel.setVisible(true);
				discoverButton.setVisible(true);
				settingButton2.setVisible(true);
				fileChoice.setDisable(false);
				discoveryChoice.setDisable(false);
			});
			pc.getIndicator().progressProperty().unbind();
			pc.getIndicator().progressProperty().bind(discover.progressProperty());
			actionPane.getChildren().add(pc);
			actionPane.getChildren().remove(before);
			fileChoice.setDisable(true);
			discoveryChoice.setDisable(true);
			discover.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				DiscoveryResultController drc = obtainDiscoveryResultController(discover.getValue(), control);
				this.previousWorks.put(chosen, drc);
				//cancelButton.setVisible(false);
				zoomSlider.setVisible(true);
				zoomValue.setVisible(true);
				zoomText.setVisible(true);
				zoomSlider.setValue(100);
				//viewLabel.setVisible(true);
				//viewChoice.setVisible(true);
				//this.previousDiscoveryChoice = discovery;
				actionPane.getChildren().clear();
				actionPane.getChildren().add(drc);
				AnchorPane.setTopAnchor(drc, 0.0);
				AnchorPane.setBottomAnchor(drc, 0.0);
				AnchorPane.setLeftAnchor(drc, 0.0);
				AnchorPane.setRightAnchor(drc, 0.0);
				//configureButton.setVisible(true);
				//discoverButton.setVisible(false);
				//settingButton2.setVisible(true);
				//discoverButton.setVisible(false);
				settingButton2.setVisible(true);
				discoverButton.setVisible(true);
				discoveryChoice.setVisible(true);
				dmLabel.setVisible(true);
				fileChoice.setDisable(false);
				discoveryChoice.setDisable(false);
				System.out.println("Discovery ends millis: "+System.currentTimeMillis());
			});
			service.execute(discover);
		}
	}
	
	private DiscoveryResultController obtainDiscoveryResultController(MinerfulResult value,
			ConfigurationMinerFulController controller) {
		// TODO Auto-generated method stub
		DiscoveryResultController drc = new DiscoveryResultController();
		//drawOutputAlt(getConstraintParametersMap(processModel), getTemplatesMap(processModel));
		Browser b = controller.drawInOutputPane(value.getModel(),value.getActSuppMap(),"Declare");
		TaskCharArchive archive = new TaskCharArchive(value.getModel().getTaskCharArchive().getCopyOfTaskChars());
		ConstraintsBag bag = (ConstraintsBag) value.getModel().bag.clone();
		ProcessModel pm = new ProcessModel(archive,bag);
		FilterMinerFulController fmc = controller.addFilterValues(value.getModel(), pm, value.getParams(),drc.getOutputPane());
		fmc.setTmvc(this);
		drc.setBrowser(b);
		drc.setFmc(fmc);
		return drc;
	}

	private DiscoveryResultController obtainDiscoveryResultController(DeclareMinerOutput output, ConfigurationController controller) {
		// TODO Auto-generated method stub
		DiscoveryResultController drc = new DiscoveryResultController();
		FilterController fc = controller.addFilterValues(output,drc.getOutputPane());
		fc.setTmvc(this);
		List<Integer> finalKeys = new ArrayList<Integer>();
		output.getVisiblesupportRule().keySet().forEach(k -> {
			finalKeys.add(k);
		});
		List<Integer> actKeys = new ArrayList<Integer>();
		finalKeys.forEach(k -> {
			List<String> l = output.getConstraintParametersMap().get(k);
			if(l.size() == 1) {
				String s = l.get(0);
				output.getAllActivities().forEach((kk,v) -> {
					if(v.equals(s)) actKeys.add(kk);
				});
			}
			else {
				String s = l.get(0);
				String s2 = l.get(1);
				output.getAllActivities().forEach((kk,v) -> {
					if(v.equals(s)) actKeys.add(kk);
					if(v.equals(s2)) actKeys.add(kk);
				});
			}
		});
		Set<Integer> actKeysSet = new HashSet<Integer>(actKeys);
		//Browser b = drawGraph(output,new HashSet<Integer>(finalKeys));
		drc.setView("Declare");
		drc.setFc(fc);
		//drc.getFilterPane().getChildren().add(new Label("Hello World"));
		System.out.println("Preparing output representation...");
		drc.setBrowser(controller.getBrowserFrom(output,actKeysSet,finalKeys,"Declare"));
		System.out.println("Model ready: "+System.currentTimeMillis());
		//drc.setBrowser(b);
		return drc;
	}

	private void showDeclareMinerConfiguration() {
		actionPane.getChildren().clear();
		ConfigurationController cc = new ConfigurationController();
		cc.setTmvc(this);
		actionPane.getChildren().add(cc);
		AnchorPane.setBottomAnchor(cc, 0.0);
		AnchorPane.setTopAnchor(cc, 0.0);
		AnchorPane.setLeftAnchor(cc, 0.0);
		AnchorPane.setRightAnchor(cc, 0.0);
	}
	
	private void showMinerfulConfiguration() {
		actionPane.getChildren().clear();
		ConfigurationMinerFulController cmfc = new ConfigurationMinerFulController();
		cmfc.setTmvc(this);
		actionPane.getChildren().add(cmfc);
		AnchorPane.setBottomAnchor(cmfc, 0.0);
		AnchorPane.setTopAnchor(cmfc, 0.0);
		AnchorPane.setLeftAnchor(cmfc, 0.0);
		AnchorPane.setRightAnchor(cmfc, 0.0);
	}
	
	@FXML
	public void initialize() {
		discoveryChoice.getItems().add("Declare Miner");
		discoveryChoice.getItems().add("Minerful");
		//ettingButton2.setVisible(true);
		//discoverButton.setVisible(false);
		//discoveryChoice.getSelectionModel().selectFirst();
		discoveryChoice.setVisible(false);
		dmLabel.setVisible(false);
		discoverButton.setVisible(false);
		settingButton2.setVisible(false);
		//viewLabel.setVisible(false);
		//viewChoice.setVisible(false);
		//viewChoice.getItems().addAll("Declare","Textual","Automaton");
		//viewChoice.getSelectionModel().select("Declare");
		exportModelButton.setVisible(false);
		saveButton.setVisible(false);
		methodChoice.getItems().addAll("Declare Analyzer","DataAware Declare Replayer","Declare Replayer");
		chooseMonitorMethod.getItems().addAll("MP-Declare w Alloy");

		//generatorChoice.getItems().add("MinerFul Log Generator");
		//generatorChoice.getItems().add("AlloyLogGenerator");
		
		discoveryChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
		
			if(newV == null) return;
			String discovery = newV;
			List<Node> l = actionPane.getChildren();
			if(discovery.equals("Declare Miner") && !l.isEmpty() && l.get(0) instanceof ConfigurationMinerFulController) {
				this.previousDiscoverySettingMinerFul = l.get(0);
				zoomSlider.setVisible(false);
				zoomValue.setVisible(false);
				zoomText.setVisible(false);
				settingButton2.setVisible(true);
				discoverButton.setVisible(true);
				if(this.previousDiscoverySetting != null) {
					actionPane.getChildren().clear();
					actionPane.getChildren().add(this.previousDiscoverySetting);
					return;
				}
				showDeclareMinerConfiguration();
			}
			else if(discovery.equals("Minerful") && !l.isEmpty() && l.get(0) instanceof ConfigurationController) {
				this.previousDiscoverySetting = l.get(0);
				zoomSlider.setVisible(false);
				zoomValue.setVisible(false);
				zoomText.setVisible(false);
				settingButton2.setVisible(true);
				discoverButton.setVisible(true);
				if(this.previousDiscoverySettingMinerFul != null) {
					actionPane.getChildren().clear();
					actionPane.getChildren().add(this.previousDiscoverySettingMinerFul);
					return;
				}
				showMinerfulConfiguration();
			}
		});
		
		fileChoice.getSelectionModel().selectedItemProperty().addListener((oV,oldV,newV) -> {
			if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof ProgressController) {
				showAlert("There is an ongoing process, please cancel it first.");
				return;
			}
			if(newV == null) return;
			else {
				if(previousWorks.get(newV) == null) {
					discoveryChoice.getSelectionModel().select("Declare Miner");
					discover();
				}
				else {
					DiscoveryResultController drc = previousWorks.get(newV);
					zoomSlider.setVisible(true);
					zoomValue.setVisible(true);
					zoomText.setVisible(true);
					zoomSlider.setValue(100);
					//viewLabel.setVisible(true);
					//viewChoice.setVisible(true);
					//this.previousDiscoveryChoice = discovery;
					actionPane.getChildren().clear();
					actionPane.getChildren().add(drc);
					AnchorPane.setTopAnchor(drc, 0.0);
					AnchorPane.setBottomAnchor(drc, 0.0);
					AnchorPane.setLeftAnchor(drc, 0.0);
					AnchorPane.setRightAnchor(drc, 0.0);
					//configureButton.setVisible(true);
					//discoverButton.setVisible(false);
					settingButton2.setVisible(true);
					discoverButton.setVisible(true);
				}
			}
 		});
		
		/*viewChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(!oldV.equals(newV)) {
				changeOutputView();
			}
		});*/
		
		zoomValue.setText(String.format("%.1f%%", zoomSlider.getValue()));
		zoomSlider.setVisible(false);
		zoomValue.setVisible(false);
		zoomText.setVisible(false);
		zoomSlider.valueProperty().addListener((ov,oldV,newV) -> {
			if(!actionPane.getChildren().isEmpty() && actionPane.getChildren().get(0) instanceof DiscoveryResultController) {
				DiscoveryResultController drc = (DiscoveryResultController) actionPane.getChildren().get(0);
				Pane p = (Pane) drc.getOutputPane().getChildren().get(0);
				WebView w = (WebView) p.getChildren().get(0);
				w.setZoom(newV.doubleValue()/100);
				zoomValue.setText(String.format("%.1f%%", newV.doubleValue()));
			}
		});
		
		zoomSlider.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
			System.out.println("Entered zoom slider!!!");
		});
		
		mpDeclareChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null)
			arrangeGeneratorChoices(openedFiles.get(newV));
		});
		
		declareModelChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null) {
				List<String> l = getListOfConstraintsFromFile(openedFiles.get(newV));
				for(String s: l) {
					String t = s.substring(0, s.indexOf('['));
					if(t.equals("Not Response")) {
						showAlert("Not Response is not supported!");
						declareModelChoice.getItems().remove(newV);
						return;
					}
					if(t.equals("Not Precedence")) {
						showAlert("Not Precedence is not supported!");
						declareModelChoice.getItems().remove(newV);
						return;
					}
					if(t.equals("Not Responded Existence")) {
						showAlert("Not Responded Existence is not supported!");
						declareModelChoice.getItems().remove(newV);
						return;
					}
					if(t.equals("End")) {
						showAlert("End is not supported!");
						declareModelChoice.getItems().remove(newV);
						return;
					}
					if(t.equals("Participation")) {
						showAlert("Participation is not supported! You can use Existence instead.");
						declareModelChoice.getItems().remove(newV);
						return;
					}
					if(t.equals("AtMostOne")) {
						showAlert("AtMostOne is not supported! Yon can use Absence2 instead.");
						declareModelChoice.getItems().remove(newV);
						return;
					}
				}
				arrangeCheckerChoices(l);
			}
		});
		
		editedModels.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null) {
				String absPath = openedFiles.get(newV);
				if(absPath.endsWith(".decl")) {
					File file = new File(absPath);
					TabbedConstructionController tcc = new TabbedConstructionController(file,generatorChoice,stage);
		    		actionPane2.getChildren().clear();
		    		actionPane2.getChildren().add(tcc);
		    		AnchorPane.setBottomAnchor(tcc, 0.0);
		    		AnchorPane.setTopAnchor(tcc, 0.0);
		    		AnchorPane.setLeftAnchor(tcc, 0.0);
		    		AnchorPane.setRightAnchor(tcc, 0.0);
		    		exportModelButton.setVisible(true);
		    		saveButton.setVisible(true);
				}
				else {
					TabbedConstructionController tcc = new TabbedConstructionController(absPath,generatorChoice,stage);
		    		actionPane2.getChildren().clear();
		    		actionPane2.getChildren().add(tcc);
		    		AnchorPane.setBottomAnchor(tcc, 0.0);
		    		AnchorPane.setTopAnchor(tcc, 0.0);
		    		AnchorPane.setLeftAnchor(tcc, 0.0);
		    		AnchorPane.setRightAnchor(tcc, 0.0);
		    		exportModelButton.setVisible(true);
		    		saveButton.setVisible(true);
				}
			}
		});
		
		generatorChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null) {
				if(newV.equals("AlloyLogGenerator")) {
					LogGenController lgc = new LogGenController(true,stage);
					if(!actionPane4.getChildren().isEmpty() && actionPane4.getChildren().get(0) instanceof LogGenController) {
						LogGenController prev = (LogGenController) actionPane4.getChildren().get(0);
						lgc.setFields(prev.getFields());
					}
					actionPane4.getChildren().clear();
					String absPath = openedFiles.get(mpDeclareChoice.getSelectionModel().getSelectedItem());
					List<String> cL = getListOfConstraintsFromFile(absPath);
					lgc.setConstraintList(cL);
					actionPane4.getChildren().add(lgc);
					AnchorPane.setTopAnchor(lgc, 0.0);
					AnchorPane.setBottomAnchor(lgc, 0.0);
					AnchorPane.setLeftAnchor(lgc, 0.0);
					AnchorPane.setRightAnchor(lgc, 0.0);
				}
				else {
					LogGenController lgc = new LogGenController(false,stage);
					if(!actionPane4.getChildren().isEmpty() && actionPane4.getChildren().get(0) instanceof LogGenController) {
						LogGenController prev = (LogGenController) actionPane4.getChildren().get(0);
						lgc.setFields(prev.getFields());
					}
					actionPane4.getChildren().clear();
					String absPath = openedFiles.get(mpDeclareChoice.getSelectionModel().getSelectedItem());
					List<String> cL = getListOfConstraintsFromFile(absPath);
					lgc.setConstraintList(cL);
					actionPane4.getChildren().add(lgc);
					AnchorPane.setTopAnchor(lgc, 0.0);
					AnchorPane.setBottomAnchor(lgc, 0.0);
					AnchorPane.setLeftAnchor(lgc, 0.0);
					AnchorPane.setRightAnchor(lgc, 0.0);
				}
			}
		});
		
		methodChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(actionPane3.getChildren().isEmpty()) {
				if(newV == null) return;
				else {
					if(newV.equals("Declare Analyzer")) {
						settingButton.setVisible(true);
						checkButton.setVisible(true);
					}
					else {
						settingButton.setVisible(true);
						checkButton.setVisible(true);
					}
				}
			}
			else if(actionPane3.getChildren().get(0) instanceof CostConfigController) {
				if(newV == null) return;
				else {
					if(newV.equals("Declare Analyzer")) {
						closeCostConfig();
						settingButton.setVisible(true);
						checkButton.setVisible(true);
					}
					else {
						try {
							saveCCSetting();
							openSetting();
							settingButton.setVisible(true);
							checkButton.setVisible(true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			else {
				if(newV == null) return;
				else {
					if(newV.equals("Declare Analyzer")) {
						settingButton.setVisible(true);
						checkButton.setVisible(true);
					}
					else {
						settingButton.setVisible(true);
						checkButton.setVisible(true);
					}
				}
			}
		});
		
		logFileChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null) {
				String decl = declareModelChoice.getSelectionModel().getSelectedItem();
				if(decl != null ) {
					this.previousReplayerWDataSetting = null;
					this.previousReplayerWODataSetting = null;
					if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof CostConfigController) {
						actionPane3.getChildren().clear();
					}
				}
			}
		});
		
		declareModelChoice.getSelectionModel().selectedItemProperty().addListener((ov,oldV,newV) -> {
			if(newV != null) {
				String decl = logFileChoice.getSelectionModel().getSelectedItem();
				if(decl != null ) {
					this.previousReplayerWDataSetting = null;
					this.previousReplayerWODataSetting = null;
					if(!actionPane3.getChildren().isEmpty() && actionPane3.getChildren().get(0) instanceof CostConfigController) {
						actionPane3.getChildren().clear();
					}
				}
			}
		});
	}
	
	private void arrangeCheckerChoices(List<String> list) {
		// TODO Auto-generated method stub
		List<String> cL = list;
		if(cL.stream().map(s -> containsData(s)).reduce(false, (a,b) -> a || b)) {
			methodChoice.getItems().remove("Declare Replayer");
			
		}
		else {
			if(!methodChoice.getItems().contains("Declare Replayer")) {
				methodChoice.getItems().add("Declare Replayer");
			}
		}
		methodChoice.getSelectionModel().select("Declare Analyzer");
	}

	private List<String> getListOfConstraintsFromFile(String absPath) {
		if(absPath.endsWith(".decl")) {
			File file = new File(absPath);
			List<String> cL = new ArrayList<String>();
			try {
				Scanner s = new Scanner(file);
				while(s.hasNextLine()) {
					String line = s.nextLine();
					takeAction(line,cL);
				}
				s.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cL;
		}
		else {
			List<String> cL = new ArrayList<String>();
			try {
				Scanner s = new Scanner(absPath);
				while(s.hasNextLine()) {
					String line = s.nextLine();
					takeAction(line,cL);
				}
				s.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cL;
		}
	}
	
	private void arrangeGeneratorChoices(String absPath) {
		List<String> cL = getListOfConstraintsFromFile(absPath);
		if(cL.stream().map(s -> containsData(s)).reduce(false, (a,b) -> a || b)) {
			generatorChoice.setItems(
					FXCollections.observableArrayList("AlloyLogGenerator"));
			
		}
		else {
			generatorChoice.setItems(
					FXCollections.observableArrayList("MinerFul Log Generator","AlloyLogGenerator"));
			
		}
		generatorChoice.getSelectionModel().select("AlloyLogGenerator");
	}
	
	private boolean containsData(String s) {
	   	 String[] arr = s.split("\\|");
	   	 arr[0] = "";
	   	 for(String str:arr) {
	   		 if(!str.trim().isEmpty()) return true;
	   	 }
	   	 return false;
	   }
	
	private void takeAction(String line, List<String> cL) {
		if(line.startsWith("activity")) {}
		else if(line.startsWith("bind")) {}
		else {
			Pattern p = Pattern.compile("\\w+(\\[.*\\]) \\|");
			Matcher m = p.matcher(line);
			if(m.find()) {
				cL.add(line);
			}
		}
	}
	
	@FXML
	public void generateLog() {
		System.out.println("Log generate button clicked...");
		String file = mpDeclareChoice.getSelectionModel().getSelectedItem();
		String gen = generatorChoice.getSelectionModel().getSelectedItem();
		if(file == null) {
			showAlert("A file must be selected!");
			return;
		}
		if(gen == null) {
			showAlert("A generator must be selected!");
			return;
		}
		if(!(actionPane4.getChildren().get(0) instanceof LogGenController)) {
			showAlert("There is an ongoing process, please cancel it first!");
			return;
		}
		LogGenController lgc = (LogGenController)actionPane4.getChildren().get(0);
		String abs = openedFiles.get(file);
		lgc.setMpFile(abs);
		List<String> constraintList = getListOfConstraintsFromFile(abs);
		StringBuilder temporalWarning = new StringBuilder();
		boolean isAlloyLog = gen.equals("AlloyLogGenerator");
		boolean isMinerful = gen.equals("MinerFul Log Generator");
		for(String s: constraintList) {
			String template = s.substring(0,s.indexOf('['));
			if(isAlloyLog) {
				try {
					AlloyLogGeneratorTemplate.valueOf(template.replace(" ", "_"));
				}catch(Exception e) {
					showAlert(template+" is not valid template for AlloyLogGenerator.");
					return;
				}
				if(isUnaryTemplate(template)) {
					int lp = s.lastIndexOf('|');
					if(lp == -1) {
						showAlert(template + " must be written as template[activity] |activation_condition |time_condition, where time condition will be ignored if exists.");
						return;
					}
					String cond = s.substring(lp+1);
					if(!cond.isEmpty()) {
						temporalWarning.append("Time condition in \"").append(s)
						.append("\" will be ignored.\n");
					}
				}
				else {
					int lp = s.lastIndexOf('|');
					if(lp == -1) {
						showAlert(template + " must be written as template[activity] |activation_condition |correlation_condition |time_condition, where time condition will be ignored if exists.");
						return;
					}
					String cond = s.substring(lp+1);
					if(!cond.isEmpty()) {
						temporalWarning.append("Time condition in \"").append(s)
						.append("\" will be ignored.\n");
					}
				}
			}
			if(isMinerful) {
				try {
					MinerfulTemplate.valueOf(template.replace(" ", "_").replace("-","$"));
				}catch(Exception e) {
					showAlert(template+" is not valid template for Minerful Log Generator.");
					return;
				}
			}
			
		}
		System.out.println("All templates are valid");
		String warning = temporalWarning.toString();
		if(!warning.isEmpty()) showWarning(warning);
		lgc.setConstraintList(constraintList);
		LogGenTask task = new LogGenTask(lgc,gen);
		
		ProgressController pc = new ProgressController();
		pc.setOperationName("Generating...");
		pc.getCancelOperation().setOnAction(e -> {
			task.cancel(true);
			pc.getIndicator().progressProperty().unbind();
			actionPane4.getChildren().remove(pc);
			actionPane4.getChildren().add(lgc);
			mpDeclareChoice.setDisable(false);
			generatorChoice.setDisable(false);
		});
		pc.getIndicator().progressProperty().unbind();
		pc.getIndicator().progressProperty().bind(task.progressProperty());
		actionPane4.getChildren().add(pc);
		actionPane4.getChildren().remove(lgc);
		mpDeclareChoice.setDisable(true);
		generatorChoice.setDisable(true);
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
			System.out.println("LogGen task has finished");
			actionPane4.getChildren().remove(pc);
			actionPane4.getChildren().add(lgc);
			String message = task.getValue();
			if(message.startsWith("Error")) {
				System.out.println("Log not generated");
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setContentText(message);
				alert.showAndWait();
			}
			else {
				File logFile = new File(message);
				addLogFile(logFile);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setContentText("Log is generated!");
				alert.showAndWait();
				System.out.println("Log generated!");
				System.out.println("Log gen ends millis: "+System.currentTimeMillis());
			}
			mpDeclareChoice.setDisable(false);
			generatorChoice.setDisable(false);
		});
		System.out.println("LogGen task started...");
		service.execute(task);
	}
	
	public Slider getZoomSlider() {
		return zoomSlider;
	}
	
	@FXML
	public void openNewModel() {
		TabbedConstructionController tcc = new TabbedConstructionController(stage,generatorChoice);
		actionPane2.getChildren().clear();
		actionPane2.getChildren().add(tcc);
		AnchorPane.setBottomAnchor(tcc, 0.0);
		AnchorPane.setTopAnchor(tcc, 0.0);
		AnchorPane.setLeftAnchor(tcc, 0.0);
		AnchorPane.setRightAnchor(tcc, 0.0);
		exportModelButton.setVisible(true);
		saveButton.setVisible(true);
		editedModels.getSelectionModel().clearSelection();
	}
	
	@FXML
	public void importModel() {
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Declare model",Arrays.asList("*.decl"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(stage);
		//String absPath = "";
        if (file != null) {
        	openedFiles.put(file.getName(), file.getAbsolutePath());
        	
        	declareModelChoice.getItems().remove(file.getName());
        	declareModelChoice.getItems().add(file.getName());
        	
        	mpDeclareChoice.getItems().remove(file.getName());
        	mpDeclareChoice.getItems().add(file.getName());
        	
        	editedModels.getSelectionModel().clearSelection();
        	editedModels.getItems().remove(file.getName());
        	editedModels.getItems().add(file.getName());
        	editedModels.getSelectionModel().select(file.getName());
        }
	}
	
	@FXML
	public void saveChangesForModel() {
		TabbedConstructionController tcc = (TabbedConstructionController)actionPane2.getChildren().get(0);
		String export = tcc.getExportData();
		String recommend = editedModels.getSelectionModel().getSelectedItem() != null ? editedModels.getSelectionModel().getSelectedItem() : "edited";
		if(!recommend.equals("edited")) recommend = recommend.substring(0, recommend.length()-5);
		TextInputDialog dialog = new TextInputDialog(recommend);
		dialog.setTitle("Edited model name");
		dialog.setHeaderText("Enter a name for edited model");
		dialog.setContentText("Model name:");
		
		Optional<String> res = dialog.showAndWait();
		if(res.isPresent()) {
			addInMemory(res.get()+".decl", export);
			showSuccess("Saved successfully");
			editedModels.getSelectionModel().select(res.get()+".decl");
		}
	}
	
	@FXML
	public void exportModel() {
		TabbedConstructionController tcc = (TabbedConstructionController)actionPane2.getChildren().get(0);
		String export = tcc.getExportData();
		FileChooser fileChooser = new FileChooser();
		ExtensionFilter filter = new ExtensionFilter("Declare model",Arrays.asList("*.decl"));
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showSaveDialog(stage);
		//String absPath = "";
        if (file != null) {
        	Path path = Paths.get(file.getAbsolutePath());
    		try (BufferedWriter writer = Files.newBufferedWriter(path))
    		{
    		    writer.write(export);
    		    writer.close();
    		    showSuccess("Model is exported successfully!");
    		    openedFiles.put(file.getName(), file.getAbsolutePath());
            	
    		    String mpDeclare = mpDeclareChoice.getSelectionModel().getSelectedItem();
            	mpDeclareChoice.getItems().remove(file.getName());
            	mpDeclareChoice.getItems().add(file.getName());
            	if(mpDeclare != null && mpDeclare.equals(file.getName())) {
            		mpDeclareChoice.getSelectionModel().select(mpDeclare);
            	}
            	
            	String declareModel = declareModelChoice.getSelectionModel().getSelectedItem();
            	declareModelChoice.getItems().remove(file.getName());
            	declareModelChoice.getItems().add(file.getName());
            	if(declareModel != null && declareModel.equals(file.getName())) {
            		declareModelChoice.getSelectionModel().select(declareModel);
            	}
            	
            	editedModels.getItems().remove(file.getName());
            	editedModels.getItems().add(file.getName());
            	editedModels.getSelectionModel().select(file.getName());
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
	}
	
	private void showAlert(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	private void showSuccess(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	private void showWarning(String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setContentText(message);
		alert.showAndWait();
	}

}

package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.DataConformance.Alignment;
import org.processmining.plugins.DataConformance.framework.ExecutionStep;
import org.processmining.plugins.DataConformance.visualization.DataAwareStepTypes;
import org.processmining.plugins.DeclareConformance.Alignstep;
import org.processmining.plugins.DeclareConformance.ViolationIdentifier;
import org.processmining.plugins.dataawaredeclarereplayer.gui.AnalysisSingleResult;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import controller.TraceViewController;
import graph.ArrowProperty;
import graph.Arrows;
import graph.Font;
import graph.Smooth;
import graph.VisEdge;
import graph.VisGraph;
import graph.VisNode;
import javafx.scene.control.Slider;
import javafx.stage.Screen;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.io.params.OutputModelParameters;
import view.Browser;

public class GraphGenerator {
	
	public static Browser browserify(List<XAttributeMap> activityListInTrace, Set<Integer> fulfillments, Set<Integer> violations) {
		VisGraph graph = new VisGraph();
		List<VisNode> nodes = new ArrayList<VisNode>();
		List<VisEdge> edges = new ArrayList<VisEdge>();
		int id = 0;
		for(XAttributeMap xmap : activityListInTrace) {
			VisNode node = new VisNode(id,"("+(id+1)+")  "+xmap.get("concept:name").toString(),null);
			StringBuilder sb = new StringBuilder();
			sb.append("Event attributes<br>");
			xmap.forEach((k,v) -> {
				sb.append(k+" = "+v.toString()+"<br>");
			});
			if(fulfillments.contains(id)) {
				node.setColor("#00ff00");
				node.setTitle("This activity is a fulfillment"+"<br>"+sb.toString());
			}
			else if(violations.contains(id)) {
				node.setColor("red");
				node.setTitle("This activity is a violation"+"<br>"+sb.toString());
			}
			else {
				node.setTitle(sb.toString());
			}
			nodes.add(node);
			id++;
		}
		for(int i = 0; i<nodes.size()-1; i++) {
			VisEdge edge = new VisEdge(nodes.get(i),nodes.get(i+1),null,null,null);
			edges.add(edge);
		}
		VisNode[] nodeArr = (VisNode[]) nodes.toArray(new VisNode[nodes.size()]);
		VisEdge[] edgeArr = (VisEdge[]) edges.toArray(new VisEdge[edges.size()]);
		graph.addEdges(edgeArr);
		graph.addNodes(nodeArr);
		return new Browser(graph,Screen.getPrimary().getVisualBounds().getHeight()*0.9,Screen.getPrimary().getVisualBounds().getWidth()*0.6,"baseGraph3.html");
	}
	
	public static TraceViewController getTraceView(List<XAttributeMap> activityListInTrace, Set<Integer> fulfillments, Set<Integer> violations) {
		List<TraceElement> list = new ArrayList<TraceElement>();
		int id = 0;
		for(XAttributeMap xmap : activityListInTrace) {
			TraceElement element = new TraceElement();
			element.setText(xmap.get("concept:name").toString());
			List<String> attributes = new ArrayList<String>();
			xmap.forEach((k,v) -> {
				attributes.add(k+" = "+v.toString()+"\n");
			});
			if(fulfillments.contains(id)) {
				attributes.add(0,"This activity is a fulfillment\n");
				attributes.add(1,"\nEvent Attributes\n");
				element.setColor("#00ff00");
				element.setAttributes(attributes);
				list.add(element);
			}
			else if(violations.contains(id)) {
				attributes.add(0,"This activity is a violation\n");
				attributes.add(1,"\nEvent Attributes\n");
				element.setColor("red");
				element.setAttributes(attributes);
				list.add(element);
			}
			else {
				attributes.add(0,"Event Attributes\n");
				element.setColor("white");
				element.setAttributes(attributes);
				list.add(element);
			}
			id++;
		}
		return new TraceViewController(list,Screen.getPrimary().getVisualBounds().getHeight()*0.9,Screen.getPrimary().getVisualBounds().getWidth()*0.56);
	}
	
	public static Browser browserify(AnalysisSingleResult asr) {
		VisGraph graph = new VisGraph();
		List<VisNode> nodes = new ArrayList<VisNode>();
		List<VisEdge> edges = new ArrayList<VisEdge>();
		Set<Integer> s1 = asr.getMovesInBoth();
		Set<Integer> s2 = asr.getMovesInBothDiffData();
		Set<Integer> s3 = asr.getMovesInLog();
		Set<Integer> s4 = asr.getMovesInModel();
		List<ExecutionStep> l = asr.getAlignment().getLogTrace();
		List<ExecutionStep> l2 = asr.getAlignment().getProcessTrace();
		for(int i=0; i<l.size(); i++) {
			ExecutionStep es = l.get(i);
			ExecutionStep es2 = l2.get(i);
			String name = (es.getActivity() != null) ? es.getActivity() : es2.getActivity();
			String color = "";
			String title = null;
			if(s1.contains(i)) {
				color = "#00d200";
				title = "This is a move in log and model";
			}
			if(s2.contains(i)) {
				color = "#ffffff";
				title = "This is a move in log and model with different data";
			}
			if(s3.contains(i)) {
				color = "#ffff00";
				title = "This is a move in log";
			}
			if(s4.contains(i)) {
				color = "#e0b0ff";
				title = "This is a move in model";
			}
			if(!es.isEmpty()) {
				title += "<p><b>Trace Attributes</b><br>";
				for(String s: es.keySet()) {
					String logValue = es.get(s).toString();
					String processValue = es2.get(s).toString();
					if(processValue.equals(logValue)) {
						title += s + " = " + logValue + "<br>";
					}
					else {
						title += s + " = " + processValue + " &#8800; " + logValue + "<br>";
					}
				}
				title += "</p>";
			}
			VisNode node = new VisNode(i,name,title);
			node.setColor(color);
			nodes.add(node);
		}
		for(int i=0; i<nodes.size()-1; i++) {
			VisEdge edge = new VisEdge(nodes.get(i),nodes.get(i+1),null,null,null);
			edges.add(edge);
		}
		VisNode[] nodeArr = (VisNode[]) nodes.toArray(new VisNode[nodes.size()]);
		VisEdge[] edgeArr = (VisEdge[]) edges.toArray(new VisEdge[edges.size()]);
		graph.addEdges(edgeArr);
		graph.addNodes(nodeArr);
		return new Browser(graph,Screen.getPrimary().getVisualBounds().getHeight()*0.6,Screen.getPrimary().getVisualBounds().getWidth()*0.73,"baseGraph3.html");
	}
	
	public static TraceViewController getTraceView(AnalysisSingleResult asr, XLog traces) {
		List<TraceElement> list = new ArrayList<TraceElement>();
		Set<Integer> s1 = asr.getMovesInBoth();
		Set<Integer> s2 = asr.getMovesInBothDiffData();
		Set<Integer> s3 = asr.getMovesInLog();
		Set<Integer> s4 = asr.getMovesInModel();
		List<ExecutionStep> l = asr.getAlignment().getLogTrace();
		List<ExecutionStep> l2 = asr.getAlignment().getProcessTrace();
		for(int i=0; i<l.size(); i++) {
			ExecutionStep es = l.get(i);
			ExecutionStep es2 = l2.get(i);
			String name = (es.getActivity() != null) ? es.getActivity() : es2.getActivity();
			String color = "";
			TraceElement te = new TraceElement();
			List<String> attributes = new ArrayList<String>();
			te.setText(name);
			if(s1.contains(i)) {
				te.setColor("#00ff00");
				attributes.add("This is a move in log and model");
			}
			if(s2.contains(i)) {
				te.setColor("#ffffff");
				attributes.add("This is a move in log and model with different data");
			}
			if(s3.contains(i)) {
				te.setColor("#ffff00");
				attributes.add("This is a move in log");
			}
			if(s4.contains(i)) {
				te.setColor("#a020f0");
				attributes.add("This is a move in model");
			}
			if(!es.isEmpty() && !es2.isEmpty()) {
				for(String s: es.keySet()) {
					String logValue = es.get(s).toString();
					String processValue = es2.get(s).toString();
					if(processValue.equals(logValue)) {
						attributes.add(s + " = " + logValue + "\n");
					}
					else {
						attributes.add(s + " = " + logValue + " replaced by " + processValue + "\n");
					}
				}
			}
			Optional<List<String>> opt = Optional.empty();
			if(es.getActivity() != null) {
				opt = EventFinder.getEventAttributes(traces, asr.getAlignment().getTraceName(), i);
			}
			if(opt.isPresent()) {
				List<String> ea = opt.get();
				if(!ea.isEmpty()) attributes.add("\nEvent Attributes:\n");
				for(String str: ea) {
					attributes.add(str);
				}
			}
			te.setAttributes(attributes);
			list.add(te);
		}
		return new TraceViewController(list,Screen.getPrimary().getVisualBounds().getHeight()*0.9,Screen.getPrimary().getVisualBounds().getWidth()*0.5);
	}
	
	public static Browser browserify(Alignment alignment,DeclareMap model) {
		VisGraph graph = new VisGraph();
		List<VisNode> nodes = new ArrayList<VisNode>();
		List<VisEdge> edges = new ArrayList<VisEdge>();
		List<DataAwareStepTypes> steps = alignment.getStepTypes();
		ViolationIdentifier vid=null;
		try {
			vid = new ViolationIdentifier(alignment.getLogTrace(),alignment.getProcessTrace(),model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0; i<steps.size(); i++) {
			String step = steps.get(i).toString();
			if(step.endsWith("Log")) {
				String contribution = "<p>"+new Alignstep(alignment.getLogTrace().get(i).getActivity(),vid,i,steps.get(i)).getToolTipText()+"</p>";
				VisNode node = new VisNode(i,alignment.getLogTrace().get(i).getActivity(),"This is a move in log"+contribution);
				node.setColor("#ffff00");
				nodes.add(node);
			}
			else if(step.endsWith("Model")) {
				String contribution = "<p>"+new Alignstep(alignment.getLogTrace().get(i).getActivity(),vid,i,steps.get(i)).getToolTipText()+"</p>";
				VisNode node = new VisNode(i,alignment.getProcessTrace().get(i).getActivity(),"This is a move in model"+contribution);
				node.setColor("#e0b0ff");
				nodes.add(node);
			}
			else if(step.endsWith("Both")) {
				VisNode node = new VisNode(i,alignment.getLogTrace().get(i).getActivity(),"This is a move in log and model");
				node.setColor("#00d200");
				nodes.add(node);
			}
		}
		for(int i=0; i<nodes.size()-1; i++) {
			VisEdge edge = new VisEdge(nodes.get(i),nodes.get(i+1),null,null,null);
			edges.add(edge);
		}
		VisNode[] nodeArr = (VisNode[]) nodes.toArray(new VisNode[nodes.size()]);
		VisEdge[] edgeArr = (VisEdge[]) edges.toArray(new VisEdge[edges.size()]);
		graph.addEdges(edgeArr);
		graph.addNodes(nodeArr);
		return new Browser(graph,Screen.getPrimary().getVisualBounds().getHeight()*0.6,Screen.getPrimary().getVisualBounds().getWidth()*0.73,"baseGraph3.html");
	}
	
	private static String[] getContributeArray(Alignstep alignstep) {
		String[] contributed=alignstep.getContributedToSolve();
		String[] solved=alignstep.getSolved();
		if (contributed.length+solved.length==0)
			return null;
		String[] contributed2=new String[contributed.length+solved.length];
		int j=0;
		for(String x : contributed)
			contributed2[j++]=x;
		for(String x : solved)
			contributed2[j++]=x;
		return contributed2;
	}
	
	public static TraceViewController getTraceView(Alignment alignment,DeclareMap model,XLog traces) {
		List<TraceElement> list = new ArrayList<TraceElement>();
		List<DataAwareStepTypes> steps = alignment.getStepTypes();
		ViolationIdentifier vid=null;
		try {
			vid = new ViolationIdentifier(alignment.getLogTrace(),alignment.getProcessTrace(),model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0; i<steps.size(); i++) {
			String step = steps.get(i).toString();
			TraceElement te = new TraceElement();
			if(step.endsWith("Log")) {
				Alignstep alignstep = new Alignstep(alignment.getLogTrace().get(i).getActivity(),vid,i,steps.get(i));
				String[] contributes = getContributeArray(alignstep);
				List<String> attributes = new ArrayList<String>();
				attributes.add("This is a move in log\n");
				if(contributes != null) {
					attributes.add("\nContributes to solve: \n");
					for(String s: contributes) {
						attributes.add(s+"\n");
					}
				}
				Optional<List<String>> opt = EventFinder.getEventAttributes(traces, alignment.getTraceName(), i);
				if(opt.isPresent()) {
					List<String> la = opt.get();
					if(!la.isEmpty()) {
						attributes.add("\nEvent Attributes:\n");
						for(String s: la) {
							attributes.add(s);
						}
					}
				}
				te.setAttributes(attributes);
				te.setText(alignment.getLogTrace().get(i).getActivity());
				te.setColor("#ffff00");
				list.add(te);
			}
			else if(step.endsWith("Model")) {
				Alignstep alignstep = new Alignstep(alignment.getLogTrace().get(i).getActivity(),vid,i,steps.get(i));
				String[] contributes = getContributeArray(alignstep);
				List<String> attributes = new ArrayList<String>();
				attributes.add("This is a move in model\n");
				if(contributes != null) {
					attributes.add("\nContributes to solve: \n");
					for(String s: contributes) {
						attributes.add(s+"\n");
					}
				}
				te.setAttributes(attributes);
				te.setText(alignment.getProcessTrace().get(i).getActivity());
				te.setColor("#a020f0");
				list.add(te);
			}
			else if(step.endsWith("Both")) {
				List<String> attributes = new ArrayList<String>();
				attributes.add("This is a move in log and model\n");
				Optional<List<String>> opt = EventFinder.getEventAttributes(traces, alignment.getTraceName(), i);
				if(opt.isPresent()) {
					List<String> la = opt.get();
					if(!la.isEmpty()) {
						attributes.add("\nEvent Attributes:\n");
						for(String s: la) {
							attributes.add(s);
						}
					}
				}
				te.setAttributes(attributes);
				te.setText(alignment.getLogTrace().get(i).getActivity());
				te.setColor("#00ff00");
				list.add(te);
			}
		}
		return new TraceViewController(list,Screen.getPrimary().getVisualBounds().getHeight()*0.9,Screen.getPrimary().getVisualBounds().getWidth()*0.56);
	}
	
	private static String getForTitle(String str1, String str2) {
		// TODO Auto-generated method stub
		Matcher m1 = Pattern.compile("\\{(.*)=(.*)\\}").matcher(str1);
		Matcher m2 = Pattern.compile("\\{(.*)=(.*)\\}").matcher(str2);
		if(m1.find() && m2.find()) {
			return m1.group(1)+" = "+m2.group(2)+" &#8800; "+m1.group(2);
		}
		return "";
	}

	public static Browser browserify(HashMap<Integer,String> actL, HashMap<Integer,List<String>> cspL, HashMap<Integer,String> tL, List<String> cL, Slider zoom) {
		Map<Integer,Boolean> isDrawnMap = new HashMap<Integer,Boolean>();
		Map<String,String> nodesMap = new HashMap<String,String>();
		cspL.keySet().forEach(k -> isDrawnMap.put(k, false));
		StringBuilder sb = new StringBuilder("digraph \"\" {");
		//sb.append("size = \"6\"");
		//sb.append("ratio = \"fill\"");
		//sb.append("rankdir = \"LR\"");
		sb.append("ranksep = \"1\"");
		sb.append("nodesep = \".5\"");
		List<String> nodes = new ArrayList<String>();
		List<String> edges = new ArrayList<String>();
		cspL.forEach((k,v) -> {
			if(!isDrawnMap.get(k) && v.size() == 2) {
				String a = v.get(0);
				String b = v.get(1);
				List<String> allUnaryForA = findAllUnaryFor(a,cspL,tL,cL,isDrawnMap);
				List<String> allUnaryForB = findAllUnaryFor(b,cspL,tL,cL,isDrawnMap);
				int ka = getKeyFor(a,actL);
				int kb = getKeyFor(b,actL);
				String nodeA = "node"+ka;
				String nodeB = "node"+kb;
				if(nodesMap.get(nodeA) == null) {
					nodesMap.put(nodeA, buildNodeString(nodeA,a,allUnaryForA,-1));
				}
				if(nodesMap.get(nodeB) == null) {
					nodesMap.put(nodeB, buildNodeString(nodeB,b,allUnaryForB,-1));
				}
				edges.add(buildEdgeString(nodeA,nodeB,tL.get(k),getLabelFromConstraint(cL.get(k))));
				isDrawnMap.put(k, true);
			}
			if(!isDrawnMap.get(k) && v.size() == 1) {
				String a = v.get(0);
				List<String> allUnaryForA = findAllUnaryFor(a,cspL,tL,cL,isDrawnMap);
				int ka = getKeyFor(a,actL);
				String nodeA = "node"+ka;
				if(nodesMap.get(nodeA) == null) {
					nodesMap.put(nodeA, buildNodeString(nodeA,a,allUnaryForA,-1));
				}
				//nodes.add(buildNodeString(nodeA,a,allUnaryForA));
				isDrawnMap.put(k, true);
			}
		});
		sb.append("node [style=\"filled\", shape=box, fontsize=\"8\", fontname=\"Helvetica\"]");
		sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".5\"]");
		for(String s: nodesMap.values()) {
			sb.append(s);
		}
		for(String s: edges) {
			sb.append(s);
		}
		sb.append("}");
		return new Browser(Screen.getPrimary().getVisualBounds().getHeight()*0.76,Screen.getPrimary().getVisualBounds().getWidth() * 1,sb.toString(),zoom);
	
	}
	
	private static String findAllExistenceConstraints(HashMap<Integer,List<String>> constraintParameters,HashMap<Integer,String> templates, String activity, Set<Integer> picked, HashMap<Integer,Boolean> isDrawn, List<String> cL) {
		List<String> list = Arrays.asList(activity);
		List<Integer> keys = new ArrayList<Integer>();
		constraintParameters.forEach((k,v) -> {
			if(v.equals(list) && picked.contains(k) && !isDrawn.get(k)) keys.add(k);
		});
		String existence = "";
		List<String> templateList = new ArrayList<String>();
		for(int k: keys) {
			if(!isDrawn.get(k)) {
				isDrawn.put(k,true);
				templateList.add(templates.get(k)+"\n"+getLabelFromConstraint(cL.get(k)));
				//existence += templates.get(k) + "\n";
			}
		}
		Set<String> templateSet = new HashSet<String>(templateList);
		for(String s: templateSet) {
			existence += s + "\n";
		}
		if(existence.equals("")) return existence;
		else return existence+"\n";
	}
	
	private static String getLabelFromConstraint(String c) {
		Matcher mBinary = Pattern.compile(".*\\[.*\\] \\|(.*) \\|(.*) \\|(.*)").matcher(c);
		Matcher mUnary = Pattern.compile(".*\\[.*\\] \\|(.*) \\|(.*)").matcher(c);
		if(mBinary.find()) {
			return "[" + mBinary.group(1) + "]" + "[" + mBinary.group(2) + "]" + "[" + mBinary.group(3) + "]";
		}
		if(mUnary.find()) {
			return "[" + mUnary.group(1) + "]" + "[" + mUnary.group(2) + "]";
		}
		return "";
	}
	
	private static VisEdge getCorrespondingEdge(VisNode start, VisNode end, DeclareTemplate template, String c) {
		int last = start.getLabel().lastIndexOf('\n');
		String startActivity = start.getLabel().substring(last+1);
		last = end.getLabel().lastIndexOf('\n');
		String endActivity = end.getLabel().substring(last+1);
		int diff = Math.abs(start.getId()-end.getId());
		String constraint = template.name() + "[" +startActivity+", "+endActivity+"]";
		String label = getLabelFromConstraint(c);
		if(template == DeclareTemplate.Responded_Existence) {
			Smooth smooth = new Smooth(true, "dynamic");
			VisEdge edge = new VisEdge(start,end,null,smooth,constraint);//,diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			return edge;
		}
		if(template == DeclareTemplate.Response) {
			ArrowProperty to = new ArrowProperty(true,"arrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			return edge;
		}
		if(template == DeclareTemplate.Alternate_Response) {
			ArrowProperty to = new ArrowProperty(true,"arrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"doubleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Chain_Response) {
			ArrowProperty to = new ArrowProperty(true,"arrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Not_Chain_Response) {
			ArrowProperty to = new ArrowProperty(true,"arrow");
			ArrowProperty middle = new ArrowProperty(true,"bar");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,middle,from);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Not_Response) {
			ArrowProperty to = new ArrowProperty(true,"arrow");
			ArrowProperty middle = new ArrowProperty(true,"bar");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,middle,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Precedence) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			Arrows arrows = new Arrows(to,null,null);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			return edge;
		}
		if(template == DeclareTemplate.Alternate_Precedence) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			Arrows arrows = new Arrows(to,null,null);
			
			Smooth smooth = new Smooth(true,"doubleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Chain_Precedence) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			Arrows arrows = new Arrows(to,null,null);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Not_Chain_Precedence) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty middle = new ArrowProperty(true, "bar");
			Arrows arrows = new Arrows(to,middle,null);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Not_Precedence) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty middle = new ArrowProperty(true, "bar");
			Arrows arrows = new Arrows(to,middle,null);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.CoExistence) {
			ArrowProperty to = new ArrowProperty(true,"circle");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			return edge;
		}
		if(template == DeclareTemplate.Not_CoExistence) {
			ArrowProperty to = new ArrowProperty(true,"circle");
			ArrowProperty middle = new ArrowProperty(true,"bar");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,middle,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Succession) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			return edge;
		}
		if(template == DeclareTemplate.Not_Succession) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty middle = new ArrowProperty(true,"bar");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,middle,from);
			
			Smooth smooth = new Smooth(true,"dynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Alternate_Succession) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"doubleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Chain_Succession) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,null,from);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-5);
			edge.setFont(f);
			return edge;
		}
		if(template == DeclareTemplate.Not_Chain_Succession) {
			ArrowProperty to = new ArrowProperty(true,"circlearrow");
			ArrowProperty middle = new ArrowProperty(true,"bar");
			ArrowProperty from = new ArrowProperty(true,"circle");
			Arrows arrows = new Arrows(to,middle,from);
			
			Smooth smooth = new Smooth(true,"tripleDynamic");
			VisEdge edge = new VisEdge(start,end,arrows,smooth,constraint);//diff*400*Math.pow(1.25, offset));
			edge.setLabel(label);
			Font f = new Font("#000000");
			f.setVadjust(-15);
			edge.setFont(f);
			return edge;
		}
		VisEdge edge = new VisEdge(start,end,null,null,constraint);//diff*400*Math.pow(1.25, offset));
		edge.setLabel(label);
		return edge;
	}
	
	private static List<String> findAllUnaryFor(String s, Map<Integer,List<String>> cpM, Map<Integer,String> tM, Map<Integer,Boolean> idM) {
		List<String> l = new ArrayList<String>();
		cpM.forEach((k,v) -> {
			if(!idM.get(k) && v.size() == 1 && v.get(0).equals(s)) {
				l.add(tM.get(k));
				idM.put(k, true);
			}
		});
		return l;
	}
	
	private static List<String> findAllUnaryFor(String s, Map<Integer,List<String>> cpM, Map<Integer,String> tL, List<String> cL, Map<Integer,Boolean> idM) {
		List<String> l = new ArrayList<String>();
		cpM.forEach((k,v) -> {
			if(!idM.get(k) && v.size() == 1 && v.get(0).equals(s)) {
				String constraint = cL.get(k);
				String insertIt = tL.get(k)+getLabelFromConstraint(constraint);
				l.add(insertIt.replace("<", "&lt;").replace(">","&gt;").replace("||","\\|\\|"));
				idM.put(k, true);
			}
		});
		return l;
	}
	
	private static int getKeyFor(String s, Map<Integer,String> aM) {
		for(int k: aM.keySet()) {
			if(aM.get(k).equals(s)) return k;
		}
		return -1;
	}
	
	private static String getHexValue(long value) {
		long b1 = value / 16;
		long b2 = value % 16;
		String s = "";
		if(b1 == 0) s = "0";
		if(b1 == 1) s = "1";
		if(b1 == 2) s = "2";
		if(b1 == 3) s = "3";
		if(b1 == 4) s = "4";
		if(b1 == 5) s = "5";
		if(b1 == 6) s = "6";
		if(b1 == 7) s = "7";
		if(b1 == 8) s = "8";
		if(b1 == 9) s = "9";
		if(b1 == 10) s = "a";
		if(b1 == 11) s = "b";
		if(b1 == 12) s = "c";
		if(b1 == 13) s = "d";
		if(b1 == 14) s = "e";
		if(b1 == 15) s = "f";
		
		if(b2 == 0) s += "0";
		if(b2 == 1) s += "1";
		if(b2 == 2) s += "2";
		if(b2 == 3) s += "3";
		if(b2 == 4) s += "4";
		if(b2 == 5) s += "5";
		if(b2 == 6) s += "6";
		if(b2 == 7) s += "7";
		if(b2 == 8) s += "8";
		if(b2 == 9) s += "9";
		if(b2 == 10) s += "a";
		if(b2 == 11) s += "b";
		if(b2 == 12) s += "c";
		if(b2 == 13) s += "d";
		if(b2 == 14) s += "e";
		if(b2 == 15) s += "f";
		
		return s;
	}
	
	private static String getColorFrom(double supp,int size) {
		double res = 51 + 26 * (1-supp) * 27.46;
		double portion = 1.5 / (size+1.5); 
		String color = "";
		if(res > 255) {
			long remaining = Math.round((res - 255) / 2);
			color = "#"+getHexValue(remaining)+getHexValue(remaining)+"ff";
		}
		else {
			color = "#0000"+getHexValue(Math.round(res));
		}
		String fc = "#e6e600";
		return "fillcolor=\""+color+";"+portion+":#808080\" gradientangle=90 fontcolor=\""+fc+"\"";
	}
	
	private static String buildNodeString(String n, String s, List<String> ls, double supp) {
		String color = "";
		String ss = "";
		if(supp != -1) {
			color = getColorFrom(supp,ls.size());
			ss = String.format("%.1f%%", supp*100);
		}
		if(supp == -1) {
			double portion = 1.0 / (ls.size()+1);
			color = "#0000ff";
			String fc= "#ffffff";
			color = "fillcolor=\""+color+";"+portion+":#000000\" gradientangle=90 fontcolor=\""+fc+"\"";
		}
		if(ls.isEmpty()) {
			return n + " [label=" + "\"" + s + "\\\\n" + ss +"\"" + color +" tooltip=\""+s+"\"]";
		}
		else {
			String unaryRep = "\"{";
			for(String u : ls) {
				unaryRep += u + "|";
			}
			unaryRep += s + "\\\\n" + ss + "}\"";
			//System.out.println(n + " [shape=\"record\" label="+ unaryRep +" "+color+"]");
			return n + " [shape=\"record\" label="+ unaryRep +" "+color+" tooltip=\""+s+"\"]"; 
		}
	}
	
	private static String getStyleForTemplate(String template, String label) {
		String supp = label;
		String penwidth = "";
		if(template.equals("Co-Existence")) {
			return "[dir=\"both\", edgetooltip=\"CoExistence\", labeltooltip=\"CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
		}
		if(template.equals("Not_Co-Existence")) {
			return "[dir=\"both\", edgetooltip=\"Not CoExistence\", labeltooltip=\"Not CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
		}
		DeclareTemplate dt = DeclareTemplate.valueOf(template);
		switch(dt) {
			case Responded_Existence:
				return "[dir=\"both\",edgetooltip=\"Responded Existence\",labeltooltip=\"Responded Existence\",arrowhead=\"none\",arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Response:
				return "[dir=\"both\", edgetooltip=\"Response\",labeltooltip=\"Response\",arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Response:
				return "[color=\"black:black\", edgetooltip=\"Alternate Response\",labeltooltip=\"Alternate Response\",dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Response:
				return "[color=\"black:black:black\", edgetooltip=\"Chain Response\", labeltooltip=\"Chain Response\", dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Precedence\", labeltooltip=\"Precedence\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Alternate Precedence\", labeltooltip=\"Alternate Precedence\", color=\"black:black\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Chain Precedence\", labeltooltip=\"Chain Precedence\",color=\"black:black:black\", label=\""+supp+"\","+penwidth+"]";
			case Succession:
				return "[dir=\"both\", edgetooltip=\"Succession\", labeltooltip=\"Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Succession:
				return "[dir=\"both\", edgetooltip=\"Alternate Succession\", labeltooltip=\"Alternate Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", color=\"black:black\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Succession:
				return "[dir=\"both\", edgetooltip=\"Chain Succession\", labeltooltip=\"Chain Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", color=\"black:black:black\", label=\""+supp+"\","+penwidth+"]";
			case CoExistence:
				return "[dir=\"both\", edgetooltip=\"CoExistence\", labeltooltip=\"CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Chain Precedence\", labeltooltip=\"Not Chain Precedence\", color=\"black:black:black\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Response:
				return "[dir=\"both\", edgetooltip=\"Not Chain Response\", labeltooltip=\"Not Chain Response\", arrowhead=\"normal\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Succession:
				return "[dir=\"both\", edgetooltip=\"Not Chain Succession\", labeltooltip=\"Not Chain Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", color=\"black:black:black\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]"; 
			case Not_CoExistence:
				return "[dir=\"both\", edgetooltip=\"Not CoExistence\", labeltooltip=\"Not CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Precedence\", labeltooltip=\"Not Precedence\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Responded_Existence:
				return "[dir=\"both\",arrowtail=\"dot\", arrowhead=\"none\", edgetooltip=\"Not Responded Existence\", labeltooltip=\"Not Responded Existence\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Response:
				return "[dir=\"both\", edgetooltip=\"Not Response\", labeltooltip=\"Not Response\", arrowhead=\"normal\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Succession:
				return "[dir=\"both\", edgetooltip=\"Not Succession\", labeltooltip=\"Not Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			default:
				return "";
		}
	}
	
	private static String getStyleForTemplate(String template, double s) {
		String supp = String.format("%.1f%%", s*100);
		String penwidth = "penwidth="+String.format("%.1f", 0.5+s);
		if(template.equals("Co-Existence")) {
			return "[dir=\"both\", edgetooltip=\"CoExistence\", labeltooltip=\"CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
		}
		if(template.equals("Not Co-Existence")) {
			return "[dir=\"both\", edgetooltip=\"Not CoExistence\", labeltooltip=\"Not CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
		}
		DeclareTemplate dt = DeclareTemplate.valueOf(template);
		switch(dt) {
			case Responded_Existence:
				return "[dir=\"both\", edgetooltip=\"Responded Existence\", labeltooltip=\"Responded Existence\",arrowhead=\"none\",arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Response:
				return "[dir=\"both\", edgetooltip=\"Response\", labeltooltip=\"Response\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Response:
				return "[color=\"black:black\", edgetooltip=\"Alternate Response\", labeltooltip=\"Alternate Response\", dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Response:
				return "[color=\"black:black:black\", edgetooltip=\"Chain Response\", labeltooltip=\"Chain Response\", dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Precedence\", labeltooltip=\"Precedence\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Alternate Precedence\", labeltooltip=\"Alternate Precedence\", color=\"black:black\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Chain Precedence\", labeltooltip=\"Chain Precedence\", color=\"black:black:black\", label=\""+supp+"\","+penwidth+"]";
			case Succession:
				return "[dir=\"both\", edgetooltip=\"Succession\", labeltooltip=\"Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Alternate_Succession:
				return "[dir=\"both\", edgetooltip=\"Alternate Succession\", labeltooltip=\"Alternate Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", color=\"black:black\", label=\""+supp+"\","+penwidth+"]";
			case Chain_Succession:
				return "[dir=\"both\", edgetooltip=\"Chain Succession\", labeltooltip=\"Chain Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", color=\"black:black:black\", label=\""+supp+"\","+penwidth+"]";
			case CoExistence:
				return "[dir=\"both\", edgetooltip=\"CoExistence\", labeltooltip=\"CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Chain Precedence\", labeltooltip=\"Not Chain Precedence\", color=\"black:black:black\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Response:
				return "[dir=\"both\", arrowhead=\"normal\", edgetooltip=\"Not Chain Response\", labeltooltip=\"Not Chain Response\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Chain_Succession:
				return "[dir=\"both\", arrowhead=\"dotnormal\", edgetooltip=\"Not Chain Succession\", labeltooltip=\"Not Chain Succession\",arrowtail=\"dot\", color=\"black:black:black\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]"; 
			case Not_CoExistence:
				return "[dir=\"both\", edgetooltip=\"Not CoExistence\", labeltooltip=\"Not CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Precedence:
				return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Precedence\", labeltooltip=\"Not Precedence\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Responded_Existence:
				return "[dir=\"both\", arrowtail=\"dot\", arrowhead=\"none\", edgetooltip=\"Not Responded Existence\", labeltooltip=\"Not Responded Existence\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Response:
				return "[dir=\"both\", edgetooltip=\"Not Response\", labeltooltip=\"Not Response\", arrowhead=\"normal\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			case Not_Succession:
				return "[dir=\"both\", edgetooltip=\"Not Succession\", labeltooltip=\"Not Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", style=\"dashed\", label=\""+supp+"\","+penwidth+"]";
			default:
				return "";
		}
	}
	
	private static String buildEdgeString(String nodeA, String nodeB, String template, double supp) {
		String style = getStyleForTemplate(template,supp);
		return nodeA + " -> " + nodeB + " " + style;
	}
	
	private static String buildEdgeString(String nodeA, String nodeB, String template, String label) {
		String style = getStyleForTemplate(template,label);
		return nodeA + " -> " + nodeB + " " + style;
	}
	
	public static String getDotRepresentation(ProcessModel pm) {
		OutputModelParameters outParams = new OutputModelParameters();
		File output_dot_file = new File("automaton.dot");
		outParams.fileToSaveDotFileForAutomaton = output_dot_file;
		
		new MinerFulOutputManagementLauncher().manageOutput(pm, outParams);
		Scanner s;
		try {
			s = new Scanner(output_dot_file);
			StringBuilder sb = new StringBuilder();
			while(s.hasNextLine()) {
				sb.append(s.nextLine());
			}
			s.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public static Browser browserify(Map<Integer,String> activitiesMap, Map<Integer,Double> actSuppMap, Map<Integer,String> templatesMap, Map<Integer,List<String>> constraintParametersMap, Map<Integer,Double> constraintSuppMap, Slider zoom, String view, String supportFor) {
		if(view.equals("Declare")) {
			System.out.println("Preparing Declare view...");
			Map<Integer,Boolean> isDrawnMap = new HashMap<Integer,Boolean>();
			Map<String,String> nodesMap = new HashMap<String,String>();
			constraintParametersMap.keySet().forEach(k -> isDrawnMap.put(k, false));
			StringBuilder sb = new StringBuilder("digraph \"\" {");
			//sb.append("size = \"6\"");
			//sb.append("ratio = \"fill\"");
			//sb.append("center = \"true\"");
			sb.append("ranksep = \"1\"");
			sb.append("nodesep = \".5\"");
			List<String> nodes = new ArrayList<String>();
			List<String> edges = new ArrayList<String>();
			constraintParametersMap.forEach((k,v) -> {
				if(!isDrawnMap.get(k) && v.size() == 2) {
					String a = v.get(0);
					String b = v.get(1);
					List<String> allUnaryForA = findAllUnaryFor(a,constraintParametersMap,templatesMap,isDrawnMap);
					List<String> allUnaryForB = findAllUnaryFor(b,constraintParametersMap,templatesMap,isDrawnMap);
					int ka = getKeyFor(a,activitiesMap);
					int kb = getKeyFor(b,activitiesMap);
					String nodeA = "node"+ka;
					String nodeB = "node"+kb;
					if(nodesMap.get(nodeA) == null) {
						nodesMap.put(nodeA, buildNodeString(nodeA,a,allUnaryForA,actSuppMap.get(ka)));
					}
					if(nodesMap.get(nodeB) == null) {
						nodesMap.put(nodeB, buildNodeString(nodeB,b,allUnaryForB,actSuppMap.get(kb)));
					}
					edges.add(buildEdgeString(nodeA,nodeB,templatesMap.get(k),constraintSuppMap.get(k)));
					isDrawnMap.put(k, true);
				}
				if(!isDrawnMap.get(k) && v.size() == 1) {
					String a = v.get(0);
					List<String> allUnaryForA = findAllUnaryFor(a,constraintParametersMap,templatesMap,isDrawnMap);
					int ka = getKeyFor(a,activitiesMap);
					String nodeA = "node"+ka;
					if(nodesMap.get(nodeA) == null) {
						nodesMap.put(nodeA, buildNodeString(nodeA,a,allUnaryForA,actSuppMap.get(ka)));
					}
					//nodes.add(buildNodeString(eA,a,allUnaryForA));
					isDrawnMap.put(k, true);
				}
			});
			sb.append("node [style=\"filled\", shape=box, fontsize=\"8\", fontname=\"Helvetica\"]");
			sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".5\"]");
			for(String s: nodesMap.values()) {
				sb.append(s);
			}
			for(String s: edges) {
				//System.out.println(s);
				sb.append(s);
			}
			sb.append("}");
			System.out.println("Dot string is ready...");
			Browser browser = new Browser(Screen.getPrimary().getVisualBounds().getHeight()*0.79,Screen.getPrimary().getVisualBounds().getWidth() * 0.75,sb.toString(),zoom);
			browser.setActivitiesMap(activitiesMap);
			browser.setActSuppMap(actSuppMap);
			browser.setTemplatesMap(templatesMap);
			browser.setConstraintParametersMap(constraintParametersMap);
			browser.setConstraintSuppMap(constraintSuppMap);
			return browser;
		}
		if(view.equals("Automaton")) {
			ProcessModel pm = ProcessModelGenerator.obtainProcessModel
					(activitiesMap, templatesMap, constraintParametersMap);
			
			String dotRep = getDotRepresentation(pm);
			Browser browser = new Browser(Screen.getPrimary().getVisualBounds().getHeight()*0.79,Screen.getPrimary().getVisualBounds().getWidth() * 0.75,dotRep,zoom);
			browser.setActivitiesMap(activitiesMap);
			browser.setActSuppMap(actSuppMap);
			browser.setTemplatesMap(templatesMap);
			browser.setConstraintParametersMap(constraintParametersMap);
			browser.setConstraintSuppMap(constraintSuppMap);
			return browser;
		}
		StringBuilder sbActivity = new StringBuilder();
		StringBuilder sbConstraint = new StringBuilder();
		int index = 1;
		List<Integer> la = activitiesMap.keySet().stream().sorted((i1,i2) -> {
			double s1 = actSuppMap.get(i1);
			double s2 = actSuppMap.get(i2);
			if(s2 > s1) return 1;
			else if(s1 > s2) return -1;
			else return 0;
		}).collect(Collectors.toList());
		for(int k:la) {
			double s = actSuppMap.get(k);
			String act = activitiesMap.get(k);
			String addIt = String.format("%d) %s : Exists in %.2f%% of traces in the log\\n", index, act, s*100);
			sbActivity.append(addIt);
			index++;
		}
		index = 1;
		List<Integer> lc = templatesMap.keySet().stream().sorted((i1,i2) -> {
			double s1 = constraintSuppMap.get(i1);
			double s2 = constraintSuppMap.get(i2);
			if(s2 > s1) return 1;
			else if(s1 > s2) return -1;
			else return 0;
		}).collect(Collectors.toList());
		for(int k:lc) {
			double s = constraintSuppMap.get(k);
			List<String> params = constraintParametersMap.get(k);
			String template = templatesMap.get(k);
			String[] p = (String[]) params.toArray(new String[params.size()]);
			String exp = TemplateDescription.get(templatesMap.get(k), p);
			if(template.startsWith("AtMostOne") || template.startsWith("Participation") || template.startsWith("Init") || template.startsWith("End")) {
				String addIt = String.format("%d) In %.2f%% of %s in the log, %s\\n", index, s*100, "traces", exp);
				sbConstraint.append(addIt);
			}
			else {
				String addIt = String.format("%d) In %.2f%% of %s in the log, %s\\n", index, s*100, supportFor, exp);
				sbConstraint.append(addIt);
			}
			index++;
		}
		
		Browser browser = new Browser(Screen.getPrimary().getVisualBounds().getHeight()*0.79,Screen.getPrimary().getVisualBounds().getWidth() * 0.75,sbActivity.toString(),sbConstraint.toString(),zoom);
		//System.out.println("Activities: "+sbActivity.toString());
		//System.out.println("Constraints: "+sbConstraint.toString());
		browser.setActivitiesMap(activitiesMap);
		browser.setActSuppMap(actSuppMap);
		browser.setTemplatesMap(templatesMap);
		browser.setConstraintParametersMap(constraintParametersMap);
		browser.setConstraintSuppMap(constraintSuppMap);
		return browser;
	}

}

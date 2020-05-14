package util;

import java.util.List;
import java.util.Map;

import minerful.concept.ProcessModel;

public class ModelExporter {
	
	public static String getTextString(Map<Integer,String> activitiesMap,
			Map<Integer,Double> actSuppMap,
			Map<Integer,String> templatesMap,
			Map<Integer,List<String>> constraintParametersMap,
			Map<Integer,Double> constraintSuppMap) {
		StringBuilder sb = new StringBuilder();
		sb.append("Activities:\n");
		int index = 1;
		for(int k:activitiesMap.keySet()) {
			double s = actSuppMap.get(k);
			String act = activitiesMap.get(k);
			String addIt = String.format("%d) %s : Exists in %.2f%% of traces in the log\n", index, act, s*100);
			sb.append(addIt);
			index++;
		}
		sb.append("Constraints:\n");
		index = 1;
		for(int k:templatesMap.keySet()) {
			double s = constraintSuppMap.get(k);
			List<String> params = constraintParametersMap.get(k);
			String[] p = (String[]) params.toArray(new String[params.size()]);
			String exp = TemplateDescription.get(templatesMap.get(k), p);
			String addIt = String.format("%d) In %.2f%% of traces in the log, %s\n", index, s*100, exp);
			sb.append(addIt);
			index++;
		}
		return sb.toString();
	}
	
	public static String getDotString(Map<Integer,String> activitiesMap,
			Map<Integer,String> templatesMap, Map<Integer,List<String>> constraintParametersMap) {
		ProcessModel pm = ProcessModelGenerator.obtainProcessModel
				(activitiesMap, templatesMap, constraintParametersMap);
		String dotRep = GraphGenerator.getDotRepresentation(pm);
		return dotRep;
	}
	
	public static String getDeclString(Map<Integer,String> activitiesMap,
			Map<Integer,String> templatesMap, Map<Integer,List<String>> constraintParametersMap) {
		StringBuilder sb = new StringBuilder();
		for(int k: activitiesMap.keySet()) {
			String addIt = String.format("activity %s\n", activitiesMap.get(k));
			sb.append(addIt);
		}
		for(int k: templatesMap.keySet()) {
			List<String> params = constraintParametersMap.get(k);
			if(params.size() == 1) {
				String temp = templatesMap.get(k).replace('_', ' ');
				String addIt = String.format("%s[%s] | |\n", temp, params.get(0));
				sb.append(addIt);
			}
			else if(params.size() == 2) {
				String temp = templatesMap.get(k).replace('_', ' ');
				String addIt = String.format("%s[%s, %s] | | |\n", temp, params.get(0), params.get(1));
				sb.append(addIt);
			}
		}
		return sb.toString();
	}

}

package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.ExactlyOne;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;

public class ProcessModelGenerator {
	
	public static ProcessModel obtainProcessModel(Map<Integer,String> activitiesMap, Map<Integer,String> templatesMap, Map<Integer,List<String>> constraintParametersMap) {
		List<String> allActivitiesInvolved = new ArrayList<String>(activitiesMap.values());
		
		TaskCharFactory tChFactory = new TaskCharFactory();
		
		List<TaskChar> tcList = allActivitiesInvolved.stream().map(activity -> tChFactory.makeTaskChar(activity)).collect(Collectors.toList());
		TaskChar[] tcArray = (TaskChar[]) tcList.toArray(new TaskChar[tcList.size()]); 
		TaskCharArchive taChaAr = new TaskCharArchive(tcArray);
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		Map<Integer,List<String>> constraintsMap = constraintParametersMap;
		for(int k:constraintsMap.keySet()) {
			String t = templatesMap.get(k);
			List<String> involvedActivities = constraintsMap.get(k);
			List<TaskChar> involved = involvedActivities.stream().map(activity -> taChaAr.getTaskChar(activity)).collect(Collectors.toList());
			Constraint constraint = getConstraint(t, involved);
			if(constraint != null) bag.add(constraint);
		}
		ProcessModel proMod = new ProcessModel(taChaAr, bag);
		return proMod;
	}
	
	private static Constraint getConstraint(String template, List<TaskChar> involved) {
		if(involved.size() == 1) {
			if(template.equals("Init")) {
				return new Init(involved.get(0));
			}
			else if(template.startsWith("Existence")) {
				return new Participation(involved.get(0));
			}
			else if(template.equals("Exactly1")) {
				return new ExactlyOne(involved.get(0));
			}
			else if(template.equals("Absence2")) {
				return new AtMostOne(involved.get(0));
			}
			else return null;
		}
		else {
			if(template.equals("Response")) {
				return new Response(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate_Response")) {
				return new AlternateResponse(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain_Response")) {
				return new ChainResponse(involved.get(0),involved.get(1));
			}
			if(template.equals("Precedence")) {
				return new Precedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate_Precedence")) {
				return new AlternatePrecedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain_Precedence")) {
				return new ChainPrecedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Succession")) {
				return new Succession(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate_Succession")) {
				return new AlternateSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain_Succession")) {
				return new ChainSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("CoExistence")) {
				return new CoExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Responded_Existence")) {
				return new RespondedExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Not_CoExistence")) {
				return new NotCoExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Not_Chain_Succession")) {
				return new NotChainSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("Not_Succession")) {
				return new NotSuccession(involved.get(0),involved.get(1));
			}
			return null;
		}
	}

}

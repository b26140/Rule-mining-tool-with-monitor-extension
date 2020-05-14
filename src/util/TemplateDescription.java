package util;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;

public class TemplateDescription {

	public static String get(String template, String... params) {
		try {
			if(template.equals("Co-Existence")) {
				return String.format("%s and %s occur together",params[0],params[1]);
			}
			if(template.equals("Not Co-Existence")) {
				return String.format("%s and %s never occur together",params[0],params[1]);
			}
			DeclareTemplate d = DeclareTemplate.valueOf(template);
			switch(d) {
				case Absence:
					return String.format("%s does not occur",params[0]);
				case Absence2:
					return String.format("%s occurs at most once",params[0]);
				case Absence3:
					return String.format("%s occurs at most twice",params[0]);
				case Exactly1:
					return String.format("%s occurs exactly once",params[0]);
				case Exactly2:
					return String.format("%s occurs exactly twice",params[0]);
				case Existence:
					return String.format("%s occurs at least once",params[0]);
				case Existence2:
					return String.format("%s occurs at least twice",params[0]);
				case Existence3:
					return String.format("%s occurs at least three times",params[0]);
				case Init:
					return String.format("%s occurs first",params[0]);
				case Responded_Existence:
					return String.format("If %s occurs then %s occurs as well",params[0],params[1]);
				case Response:
					return String.format("If %s occurs then %s occurs after %s",params[0],params[1],params[0]);
				case Alternate_Response:
					return String.format("Each time %s occurs, then %s occurs afterwards before %s recurs",params[0],params[1],params[0]);
				case Chain_Response:
					return String.format("Each time %s occurs, then %s occurs immediately afterwards",params[0],params[1]);
				case Precedence:
					return String.format("%s occurs if preceded by %s",params[1],params[0]);
				case Alternate_Precedence:
					return String.format("Each time %s occurs, it is preceded by %s and no other %s can recur in between",params[1],params[0],params[1]);
				case Chain_Precedence:
					return String.format("Each time %s occurs, then %s occurs immediately beforehand",params[1],params[0]);
				case CoExistence:
					return String.format("%s and %s occur together",params[0],params[1]);
				case Succession:
					return String.format("%s occurs if and only if it is followed by %s",params[0],params[1]);
				case Alternate_Succession:
					return String.format("%s and %s together if and only if the latter follows the former, and they alternate each other",params[0],params[1]);
				case Chain_Succession:
					return String.format("%s and %s together if and only if the latter immediately follows the former",params[0],params[1]);
				case Not_Chain_Succession:
					return String.format("%s and %s together if and only if the latter does not immediately follow the former",params[0],params[1]);
				case Not_Succession:
					return String.format("%s can never occur before %s",params[0],params[1]);
				case Not_CoExistence:
					return String.format("%s and %s never occur together",params[0],params[1]);
				case Not_Chain_Precedence:
					return String.format("Each time %s occurs, then %s does not occur immediately beforehand",params[1],params[0]);
				case Not_Chain_Response:
					return String.format("Each time %s occurs, then %s does not occur immediately afterwards",params[0],params[1]);
				case Not_Precedence:
					return String.format("%s occurs if it is not preceded by %s",params[1],params[0]);
				case Not_Response:
					return String.format("If %s occurs then %s does not occur after %s",params[0],params[1],params[0]);
				case Not_Responded_Existence:
					return String.format("If %s occurs then %s does not occur",params[0],params[1]);
				default:
					return "";
			}
		}catch(IllegalArgumentException e) {
			if(template.equals("Participation")) {
				return String.format("%s occurs at least once",params[0]);
			}
			else if(template.equals("AtMostOne")) {
				return String.format("%s occurs at most once",params[0]);
			}
			else if(template.equals("End")) {
				return String.format("%s is the last to occur",params[0]);
			}
			else {
				return "Not defined";
			}
		}
	}
}

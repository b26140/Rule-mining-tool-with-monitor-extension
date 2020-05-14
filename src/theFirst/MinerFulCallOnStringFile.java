package theFirst;

import java.io.Console;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.io.params.OutputModelParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to call MINERful to discover a process model out of strings saved on a file.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class MinerFulCallOnStringFile {

	public static void main(String[] args) {
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		OutputModelParameters outParams =
				new OutputModelParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters();
		
		inputParams.inputLogFile = new File("log.txt");
		inputParams.inputLanguage = InputEncoding.strings;
		postParams.supportThreshold = 0.0;
		postParams.confidenceThreshold = 0.0;
		postParams.interestFactorThreshold = 0.0;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		
		final ProcessModel processModel = miFuMiLa.mine();
		/*postParams.supportThreshold = 0.9;
		MinerFulMinerLauncher miFuMiLa2 = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		
		final ProcessModel processModel2 = miFuMiLa2.mine();*/
		//for(Constraint c: processModel.getAllConstraints()) {
			//System.out.println(c.getTemplateName()+" "+c.getSupport());
		//}
		System.out.println("\nSwitch to hierarchy...\n");
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHY;
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
		ProcessModel pm2 = miFuSiLa.simplify();
		
		MinerFulMinerLauncher miFuMiLa2 = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		final ProcessModel processModel2 = miFuMiLa2.mine();
		System.out.println(pm2.toString().equals(processModel2.toString()));
		//for(Constraint c: pm2.getAllConstraints()) {
		//	System.out.println(c.getTemplateName()+" "+c.getSupport());
		//}
		/*TaskCharArchive archive = new TaskCharArchive(processModel.getTaskCharArchive().getCopyOfTaskChars());
		ConstraintsBag bag = (ConstraintsBag) processModel.bag.clone();
		ProcessModel pm = new ProcessModel(archive,bag);
		
		for(Constraint c:processModel.getAllConstraints()) {
			//System.out.println(c.getTemplateName());
			if(c.getTemplateName().equals("Participation")) {
				System.out.println(c.toString()+": "+c.getSupport());
			}
		}
		postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.25;
		postParams.interestFactorThreshold = 0.125;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(pm, postParams);
		ProcessModel pm2 = miFuSiLa.simplify();
		System.out.println(pm2.toString());
		//outParams.fileToSaveAsXML = new File("/home/claudio/Desktop/Temp-MINERful/model.xml");
		
		//MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		//outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);
		
		/*outParams.fileToSaveConstraintsAsCSV = new File("logModel.csv");
		
		System.out.println("Saving...");
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);*/
		/*postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.6;
		postParams.interestFactorThreshold = 0.125;
		//postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		
		// Run the simplification algorithm
		System.out.println("Running the simplification algorithm...");
		
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(pm, postParams);
		miFuSiLa.simplify();
		
		index = 0;
		for(Constraint c : pm.getAllConstraints()) {
			Set<TaskChar> involved = c.getInvolvedTaskChars();
			List<String> list = involved.stream().map(i -> i.getName()).collect(Collectors.toList());
			System.out.println(++index + " " + list+ " -> " + c.getConfidence());
		}
		
		postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.25;
		postParams.interestFactorThreshold = 0.125;
		//postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		
		// Run the simplification algorithm
		System.out.println("Running the simplification algorithm...");
		
		miFuSiLa = new MinerFulSimplificationLauncher(pm, postParams);
		miFuSiLa.simplify();
		
		index = 0;
		for(Constraint c : pm.getAllConstraints()) {
			Set<TaskChar> involved = c.getInvolvedTaskChars();
			List<String> list = involved.stream().map(i -> i.getName()).collect(Collectors.toList());
			System.out.println(++index + " " + list+ " -> " + c.getConfidence());
		}*/
		
		
		System.exit(0);

	}
}
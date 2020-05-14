package task;

import controller.LogGenController;
import javafx.concurrent.Task;

public class LogGenTask extends Task<String> {
	
	private LogGenController controller;
	
	private String gen;
	
	public LogGenTask(LogGenController controller, String gen) {
		this.controller = controller;
		this.gen = gen;
	}

	@Override
	protected String call() throws Exception {
		// TODO Auto-generated method stub
		return controller.generateLog(gen);
	}

}

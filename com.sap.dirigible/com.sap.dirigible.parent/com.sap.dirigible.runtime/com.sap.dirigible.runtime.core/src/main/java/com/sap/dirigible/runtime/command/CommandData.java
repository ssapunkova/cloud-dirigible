package com.sap.dirigible.runtime.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandData {
	private String description;
	private String contentType;
	private boolean useContent;
	private String workDir;
	private List<CommandLineData> commands = new ArrayList<CommandLineData>();
	private Map<String, String> envAdd = new HashMap<String, String>();
	private List<String> envRemove = new ArrayList<String>();
	
	private CommandLineData targetCommand;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isUseContent() {
		return useContent;
	}

	public void setUseContent(boolean useContent) {
		this.useContent = useContent;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public List<CommandLineData> getCommands() {
		return commands;
	}

	public Map<String, String> getEnvAdd() {
		return envAdd;
	}

	public List<String> getEnvRemove() {
		return envRemove;
	}

	public CommandLineData getTargetCommand() {
		return targetCommand;
	}
	
	public void setTargetCommand(CommandLineData targetCommand) {
		this.targetCommand = targetCommand;
	}
}
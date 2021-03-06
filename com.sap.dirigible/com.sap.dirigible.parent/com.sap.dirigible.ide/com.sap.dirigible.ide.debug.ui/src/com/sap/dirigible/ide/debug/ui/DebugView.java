/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.debug.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;
import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.common.CommonUtils;
import com.sap.dirigible.ide.debug.model.DebugModel;
import com.sap.dirigible.ide.debug.model.DebugModelFacade;
import com.sap.dirigible.ide.debug.model.IDebugController;
import com.sap.dirigible.ide.editor.js.JavaScriptEditor;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.workspace.RemoteResourcesPlugin;
import com.sap.dirigible.ide.workspace.ui.commands.OpenHandler;
import com.sap.dirigible.repository.ext.debug.BreakpointMetadata;
import com.sap.dirigible.repository.ext.debug.BreakpointsMetadata;
import com.sap.dirigible.repository.ext.debug.DebugConstants;
import com.sap.dirigible.repository.ext.debug.DebugSessionMetadata;
import com.sap.dirigible.repository.ext.debug.DebugSessionsMetadata;
import com.sap.dirigible.repository.ext.debug.VariableValuesMetadata;

public class DebugView extends ViewPart implements IDebugController {
	private static final String THERE_IS_NO_ACTIVE_DEBUG_SESSION = "There is no active debug session";
	private static final String INTERNAL_ERROR_DEBUG_BRIDGE_IS_NOT_PRESENT = "Internal error - DebugBridge is not present";
	private static final String DEBUG_PROCESS_TITLE = "Debug Process";
	private static final String FILE = Messages.DebugView_FILE;
	private static final String SLASH = "/"; //$NON-NLS-1$
	private static final String SCRIPTING_SERVICES = "/ScriptingServices"; //$NON-NLS-1$
	private static final String SOURCE = Messages.DebugView_SOURCE;
	private static final String ROW = Messages.DebugView_ROW;
	private static final String VALUES = Messages.DebugView_VALUES;
	private static final String VARIABLES = Messages.DebugView_VARIABLES;
	private static final String SESSIONS = Messages.DebugView_SESSIONS;
	private static final String SKIP_BREAKPOINTS = Messages.DebugView_SKIP_BREAKPOINTS;
	private static final String CONTINUE = Messages.DebugView_CONTINUE;
	private static final String STEP_OVER = Messages.DebugView_STEP_OVER;
	private static final String STEP_INTO = Messages.DebugView_STEP_INTO;
	private static final String REFRESH = Messages.DebugView_REFRESH;

	private static final URL DIRIGIBLE_TERMINATE_ICON_URL = DebugView.class
			.getResource("/resources/terminate.png"); //$NON-NLS-1$
	private static final URL DIRIGIBLE_CONTINUE_ICON_URL = DebugView.class
			.getResource("/resources/resume.png"); //$NON-NLS-1$
	private static final URL DIRIGIBLE_STEP_OVER_ICON_URL = DebugView.class
			.getResource("/resources/step-out.png"); //$NON-NLS-1$
	private static final URL DIRIGIBLE_STEP_INTO_ICON_URL = DebugView.class
			.getResource("/resources/step-into.png"); //$NON-NLS-1$
	private static final URL DIRIGIBLE_REFRESH_ICON_URL = DebugView.class
			.getResource("/resources/refresh.png"); //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(DebugView.class);

	private final ResourceManager resourceManager;

	// private DebugModel debugModel;

	private TreeViewer sessionsTreeViewer;
	private SessionsViewContentProvider sessionsContentProvider;

	private TreeViewer variablesTreeViewer;
	private VariablesViewContentProvider variablesContentProvider;

	private TreeViewer breakpointsTreeViewer;
	private BreakpointViewContentProvider breakpointsContentProvider;

	private PropertyChangeSupport debuggerBridge;

	public DebugView() {
		super();
		this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		final Composite holder = new Composite(parent, SWT.NONE);
		holder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		holder.setLayout(new GridLayout(8, false));
		createButtonsRow(holder);

		SashForm sashFormSessions = new SashForm(parent, SWT.HORIZONTAL | SWT.BORDER);
		sashFormSessions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createSessionsTable(sashFormSessions);

		SashForm sashForm = new SashForm(sashFormSessions, SWT.HORIZONTAL | SWT.BORDER);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createVariablesTable(sashForm);
		createBreakpointsTable(sashForm);

		this.debuggerBridge = (PropertyChangeSupport) RWT.getRequest().getSession()
				.getAttribute(CommonParameters.DIRIGIBLE_DEBUGGER_BRIDGE);
		if (this.debuggerBridge != null) {
			this.debuggerBridge.addPropertyChangeListener(this);
		}
	}

	private void createButtonsRow(final Composite holder) {
		Button refreshButton = createButton(holder, REFRESH, DIRIGIBLE_REFRESH_ICON_URL);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1316287800753595995L;

			public void widgetSelected(SelectionEvent e) {
				sessionsTreeViewer.refresh(true);
				DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
				refresh(debugModel);
				waitForMetadata(debugModel);
			}

		});

		Button stepIntoButton = createButton(holder, STEP_INTO, DIRIGIBLE_STEP_INTO_ICON_URL);
		stepIntoButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -2027392635482495783L;

			public void widgetSelected(SelectionEvent e) {
				DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
				if (debugModel != null) {
					stepInto(debugModel);
					waitForMetadata(debugModel);
				}
			}

		});

		Button stepOverButton = createButton(holder, STEP_OVER, DIRIGIBLE_STEP_OVER_ICON_URL);
		stepOverButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 6512558201116618008L;

			public void widgetSelected(SelectionEvent e) {
				DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
				if (debugModel != null) {
					stepOver(debugModel);
					waitForMetadata(debugModel);
				}
			}

		});

		Button continueButton = createButton(holder, CONTINUE, DIRIGIBLE_CONTINUE_ICON_URL);
		continueButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -478646368834480614L;

			public void widgetSelected(SelectionEvent e) {
				DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
				if (debugModel != null) {
					continueExecution(debugModel);
					waitForMetadata(debugModel);
				}
			}

		});

		Button skipAllBreakpointsButton = createButton(holder, SKIP_BREAKPOINTS,
				DIRIGIBLE_TERMINATE_ICON_URL);
		skipAllBreakpointsButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 5141833336402908961L;

			public void widgetSelected(SelectionEvent e) {
				DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
				if (debugModel != null) {
					skipAllBreakpoints(debugModel);
				}
			}

		});
	}

	private Button createButton(Composite holder, String toolTipText, URL imageUrl) {
		Button button = new Button(holder, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, false, false));
		button.setToolTipText(toolTipText);
		button.setImage(createImage(imageUrl));
		return button;
	}

	private void createSessionsTable(final Composite holder) {
		sessionsTreeViewer = new TreeViewer(holder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		sessionsTreeViewer.getTree().setHeaderVisible(true);

		Tree tree = sessionsTreeViewer.getTree();

		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
		column.setText(SESSIONS);
		column.setWidth(150);

		sessionsContentProvider = new SessionsViewContentProvider();
		sessionsTreeViewer.setContentProvider(sessionsContentProvider);
		sessionsTreeViewer.setLabelProvider(new SessionsViewLabelProvider());
		sessionsTreeViewer.setInput(getViewSite());
		sessionsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()
						&& event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) event
							.getSelection();
					String sessionInfo = (String) structuredSelection.getFirstElement();
					StringTokenizer tokenizer = new StringTokenizer(sessionInfo,
							CommonParameters.DEBUG_SEPARATOR);
					String userId = tokenizer.nextToken();
					String sessionId = tokenizer.nextToken();
					String executionId = tokenizer.nextToken();
					DebugModel debugModel = DebugModelFacade.getInstance().getDebugModel(
							executionId);
					DebugModelFacade.setActiveDebugModel(debugModel);
					refresh(debugModel);
					waitForMetadata(debugModel);
				}

			}
		});
	}

	private void createVariablesTable(final Composite holder) {
		variablesTreeViewer = new TreeViewer(holder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		variablesTreeViewer.getTree().setHeaderVisible(true);

		Tree tree = variablesTreeViewer.getTree();

		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
		column.setText(VARIABLES);
		column.setWidth(150);

		column = new TreeColumn(tree, SWT.LEFT);
		column.setText(VALUES);
		column.setWidth(595);

		variablesContentProvider = new VariablesViewContentProvider();
		variablesTreeViewer.setContentProvider(variablesContentProvider);
		variablesTreeViewer.setLabelProvider(new VariablesViewLabelProvider());
		variablesTreeViewer.setInput(getViewSite());
	}

	private void createBreakpointsTable(final Composite holder) {
		breakpointsTreeViewer = new TreeViewer(holder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		breakpointsTreeViewer.getTree().setHeaderVisible(true);

		Tree tree = breakpointsTreeViewer.getTree();

		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
		column.setText(FILE);
		column.setWidth(150);

		column = new TreeColumn(breakpointsTreeViewer.getTree(), SWT.LEFT);
		column.setText(ROW);
		column.setWidth(50);

		column = new TreeColumn(breakpointsTreeViewer.getTree(), SWT.LEFT);
		column.setText(SOURCE);
		column.setWidth(350);

		breakpointsContentProvider = new BreakpointViewContentProvider();
		breakpointsTreeViewer.setContentProvider(breakpointsContentProvider);
		breakpointsTreeViewer.setLabelProvider(new BreakpointViewLabelProvider());
		breakpointsTreeViewer.setInput(getViewSite());
		breakpointsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()
						&& event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) event
							.getSelection();
					BreakpointMetadata breakpointMetadata = (BreakpointMetadata) structuredSelection
							.getFirstElement();
					openEditor(CommonUtils.formatToIDEPath(
							CommonParameters.SCRIPTING_CONTENT_FOLDER,
							breakpointMetadata.getFullPath()), breakpointMetadata.getRow());
				}
			}
		});
	}

	private void waitForMetadata(DebugModel debugModel) {
		if (debugModel == null) {
			// MessageDialog.openWarning(null, DEBUG_PROCESS_TITLE,
			// THERE_IS_NO_ACTIVE_DEBUG_SESSION);
			sessionsTreeViewer.refresh(true);
			return;
		}

		int wait = 0;
		final int maxWaits = 20;
		final int sleepTime = 500;

		while (wait < maxWaits) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			wait++;
			if (debugModel.getVariableValuesMetadata() != null) {
				variablesContentProvider.setVariablesMetaData(debugModel
						.getVariableValuesMetadata());
				variablesTreeViewer.refresh(true);
				wait = maxWaits;
			}
			if (debugModel.getBreakpointsMetadata() != null) {
				breakpointsContentProvider.setBreakpointMetadata(debugModel
						.getBreakpointsMetadata());
				breakpointsTreeViewer.refresh(true);
				wait = maxWaits;
			}

			if (debugModel.getCurrentLineBreak() != null) {
				openEditor(debugModel.getCurrentLineBreak().getFullPath(), debugModel
						.getCurrentLineBreak().getRow());
			}
		}

	}

	@Override
	public void setFocus() {
		//
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private Image createImage(URL imageURL) {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		return resourceManager.createImage(imageDescriptor);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String commandId = evt.getPropertyName();
		String clientId = (String) evt.getOldValue();
		String commandBody = (String) evt.getNewValue();
		logger.debug("DebugView.propertyChange() with commandId: " + commandId + ", clientId: "
				+ clientId + ", commandBody: " + commandBody);
		Gson gson = new Gson();

		if (commandId.startsWith(DebugConstants.VIEW)) {

			if (commandId.equals(DebugConstants.VIEW_REGISTER)) {
				DebugSessionMetadata debugSessionMetadata = gson.fromJson(commandBody,
						DebugSessionMetadata.class);
				DebugModel debugModel = createDebugModel(debugSessionMetadata.getSessionId(),
						debugSessionMetadata.getExecutionId(), debugSessionMetadata.getUserId());

			} else if (commandId.equals(DebugConstants.VIEW_FINISH)) {
				DebugSessionMetadata debugSessionMetadata = gson.fromJson(commandBody,
						DebugSessionMetadata.class);
				removeDebugModel(debugSessionMetadata.getSessionId(),
						debugSessionMetadata.getExecutionId(), debugSessionMetadata.getUserId());

			} else if (commandId.equals(DebugConstants.VIEW_SESSIONS)) {
				DebugSessionsMetadata debugSessionsMetadata = gson.fromJson(commandBody,
						DebugSessionsMetadata.class);
				reinitializeDebugModels(debugSessionsMetadata);

			} else if (commandId.equals(DebugConstants.VIEW_VARIABLE_VALUES)) {
				VariableValuesMetadata variableValuesMetadata = gson.fromJson(commandBody,
						VariableValuesMetadata.class);
				DebugModel debugModel = getDebugModel(variableValuesMetadata.getSessionId(),
						variableValuesMetadata.getExecutionId(), variableValuesMetadata.getUserId());
				debugModel.setVariableValuesMetadata(variableValuesMetadata);

			} else if (commandId.equals(DebugConstants.VIEW_BREAKPOINT_METADATA)) {
				BreakpointsMetadata breakpointsMetadata = gson.fromJson(commandBody,
						BreakpointsMetadata.class);
				DebugModel debugModel = getDebugModel(breakpointsMetadata.getSessionId(),
						breakpointsMetadata.getExecutionId(), breakpointsMetadata.getUserId());
				debugModel.setBreakpointsMetadata(breakpointsMetadata);

			} else if (commandId.equals(DebugConstants.VIEW_ON_LINE_CHANGE)) {
				BreakpointMetadata currentLineBreak = gson.fromJson(commandBody,
						BreakpointMetadata.class);
				DebugModel debugModel = getDebugModel(currentLineBreak.getSessionId(),
						currentLineBreak.getExecutionId(), currentLineBreak.getUserId());
				debugModel.setCurrentLineBreak(currentLineBreak);
				StringBuilder path = new StringBuilder(currentLineBreak.getFullPath());
				int lastIndex = path.lastIndexOf(SLASH);
				if (lastIndex != -1) {
					path.insert(lastIndex, SCRIPTING_SERVICES);
					currentLineBreak.setFullPath(path.toString());
				}
			}
		}
	}

	public DebugModel createDebugModel(String sessionId, String executionId, String userId) {
		return DebugModelFacade.getInstance()
				.createDebugModel(sessionId, executionId, userId, this);
	}

	public void reinitializeDebugModels(DebugSessionsMetadata debugSessionsMetadata) {
		DebugModelFacade debugModelFacade = DebugModelFacade.getInstance();
		debugModelFacade.clearDebugModels();
		for (DebugSessionMetadata debugSessionMetadata : debugSessionsMetadata
				.getDebugSessionsMetadata()) {
			debugModelFacade.createDebugModel(debugSessionMetadata.getSessionId(),
					debugSessionMetadata.getExecutionId(), debugSessionMetadata.getUserId(), this);
		}
	}

	public void removeDebugModel(String sessionId, String executionId, String userId) {
		DebugModelFacade.getInstance().removeDebugModel(executionId);
	}

	public DebugModel getDebugModel(String sessionId, String executionId, String userId) {
		DebugModel debugModel = null;
		if ((debugModel = DebugModelFacade.getInstance().getDebugModel(executionId)) == null) {
			debugModel = DebugModelFacade.getInstance().createDebugModel(sessionId, executionId,
					userId, this);
		}
		return debugModel;
	}

	private void sendCommand(final String commandId, final String clientId, String commandBody) {
		logger.debug("entering DebugView.sendCommand() with commandId: " + commandId
				+ ", clientId: " + clientId + ", commandBody: " + commandBody);
		DebugModel debugModel = DebugModelFacade.getActiveDebugModel();
		if (debugModel != null) {
			if (commandBody == null) {
				Gson gson = new Gson();
				commandBody = gson.toJson(new DebugSessionMetadata(debugModel.getSessionId(),
						debugModel.getExecutionId(), debugModel.getUserId()));
			}
			sendToBridge(commandId, clientId, commandBody);
		} else if (DebugConstants.DEBUG_REFRESH.equals(commandId)) {
			sendToBridge(commandId, clientId, commandBody);
		} else {
			logger.warn("sending in DebugView.sendCommand() failed - DebugModel is null for commandId: "
					+ commandId + ", and commandBody: " + commandBody);
		}
		logger.debug("exiting DebugView.sendCommand() with commandId: " + commandId
				+ ", and commandBody: " + commandBody);
	}

	private void sendToBridge(final String commandId, final String clientId, String commandBody) {
		if (this.debuggerBridge != null) {
			this.debuggerBridge.firePropertyChange(commandId, clientId, commandBody);
		} else {
			logger.debug("sending DebugView.sendCommand() failed - DebugBridge is not present - with commandId: "
					+ commandId + ", and commandBody: " + commandBody);
			MessageDialog.openError(null, DEBUG_PROCESS_TITLE,
					INTERNAL_ERROR_DEBUG_BRIDGE_IS_NOT_PRESENT);
		}
	}

	@Override
	public void refresh(DebugModel debugModel) {
		String clientId = (debugModel == null) ? "debug.global.manager" : debugModel
				.getExecutionId();
		final String commandId = DebugConstants.DEBUG_REFRESH;
		sendCommand(commandId, clientId, null);
	}

	@Override
	public void stepInto(DebugModel debugModel) {
		final String commandId = DebugConstants.DEBUG_STEP_INTO;
		sendCommand(commandId, debugModel.getExecutionId(), null);
	}

	@Override
	public void stepOver(DebugModel debugModel) {
		final String commandId = DebugConstants.DEBUG_STEP_OVER;
		sendCommand(commandId, debugModel.getExecutionId(), null);
	}

	@Override
	public void continueExecution(DebugModel debugModel) {
		final String commandId = DebugConstants.DEBUG_CONTINUE;
		sendCommand(commandId, debugModel.getExecutionId(), null);

	}

	@Override
	public void skipAllBreakpoints(DebugModel debugModel) {
		final String commandId = DebugConstants.DEBUG_SKIP_ALL_BREAKPOINTS;
		sendCommand(commandId, debugModel.getExecutionId(), null);
	}

	@Override
	public IEditorPart openEditor(String path, int row) {
		IPath location = new Path(path);
		IWorkspace workspace = RemoteResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(location);
		IEditorPart sourceCodeEditor = OpenHandler.open(file, row);
		if (sourceCodeEditor != null && sourceCodeEditor instanceof JavaScriptEditor) {
			((JavaScriptEditor) sourceCodeEditor).setDebugRow(row);
		}
		return sourceCodeEditor;
	}

	@Override
	public void setBreakpoint(DebugModel debugModel, String path, int row) {
		if (debugModel != null) {
			BreakpointMetadata breakpoint = new BreakpointMetadata(debugModel.getSessionId(),
					debugModel.getExecutionId(), debugModel.getUserId(), path, row);
			String commandBody = new Gson().toJson(breakpoint);
			final String commandId = DebugConstants.DEBUG_SET_BREAKPOINT;
			sendCommand(commandId, debugModel.getExecutionId(), commandBody);
		} else {
			// TODO
		}
	}

	@Override
	public void clearBreakpoint(DebugModel debugModel, String path, int row) {
		if (debugModel != null) {
			BreakpointMetadata breakpoint = new BreakpointMetadata(debugModel.getSessionId(),
					debugModel.getExecutionId(), debugModel.getUserId(), path, row);
			String commandBody = new Gson().toJson(breakpoint);
			final String commandId = DebugConstants.DEBUG_CLEAR_BREAKPOINT;
			sendCommand(commandId, debugModel.getExecutionId(), commandBody);
		} else {
			// TODO
		}
	}

	@Override
	public void clearAllBreakpoints(DebugModel debugModel) {
		final String commandId = DebugConstants.DEBUG_CLEAR_ALL_BREAKPOINTS;
		sendCommand(commandId, debugModel.getExecutionId(), null);
	}

	@Override
	public void clearAllBreakpoints(DebugModel debugModel, String path) {
		final String commandId = DebugConstants.DEBUG_CLEAR_ALL_BREAKPOINTS_FOR_FILE;
		sendCommand(commandId, debugModel.getExecutionId(), path);
	}

}

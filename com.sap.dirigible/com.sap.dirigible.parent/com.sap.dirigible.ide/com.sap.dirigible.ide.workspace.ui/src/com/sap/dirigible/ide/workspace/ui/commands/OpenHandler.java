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

package com.sap.dirigible.ide.workspace.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.repository.api.ContentTypeHelper;
import com.sap.dirigible.repository.api.ICommonConstants.ARTIFACT_TYPE;
import com.sap.rap.ui.shared.editor.EditorUtil;
import com.sap.rap.ui.shared.editor.SourceFileEditorInput;

public class OpenHandler extends AbstractHandler {

	private static final String OPEN_FAILURE2 = Messages.OpenHandler_OPEN_FAILURE2;

	private static final String VIEW_THEM_VIA_REGISTRY_AFTER_ACTIVATION = Messages.OpenHandler_VIEW_THEM_VIA_REGISTRY_AFTER_ACTIVATION;

	private static final String OPEN_FAILURE = Messages.OpenHandler_OPEN_FAILURE;

	private static final String BINARY_FILES_ARE_NOT_SUPPORTED = Messages.OpenHandler_BINARY_FILES_ARE_NOT_SUPPORTED;

	private static final Logger logger = Logger.getLogger(OpenHandler.class);

	private static final String COULD_NOT_OPEN_ONE_OR_MORE_FILES = Messages.OpenHandler_COULD_NOT_OPEN_ONE_OR_MORE_FILES;
	
	private static final String TEXT_EDITOR_ID = "com.sap.dirigible.ide.editor.SourceCodeEditor"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean successful = true;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Object element : structuredSelection.toArray()) {
				if (element instanceof IFile) {
					successful &= (openEditorFor(element) != null);
				} else {
					successful &= true; 
//					WorkspaceViewerUtils.expandElement(element);
				}
			}
		}
		if (!successful) {
			logger.error(COULD_NOT_OPEN_ONE_OR_MORE_FILES);
			MessageDialog.openError(null, OPEN_FAILURE2,
					COULD_NOT_OPEN_ONE_OR_MORE_FILES);
		}
		return null;
	}

	public static IEditorPart open(Object element, int row) {
		OpenHandler handler = new OpenHandler();
		
		if (element instanceof IFile) {
			return handler.openEditorForResource((IFile) element, row);
		}
		return null;
	}

	public IEditorPart openEditorFor(Object element) {
		if (element instanceof IFile) {
			return openEditorForResource((IFile) element, 0);
		}
		return null;
	}

	private IEditorPart openEditorForResource(IFile file, int row) {
		String editorId = EditorUtil.getEditorIdForExtension(file
				.getFileExtension());
		String contentType = ContentTypeHelper.getContentType(file
				.getFileExtension());
		if (editorId == null) {
			if (contentType != null && contentType.contains("text")) { //$NON-NLS-1$
				editorId = TEXT_EDITOR_ID;
			} else {
				logger.error(BINARY_FILES_ARE_NOT_SUPPORTED);
				MessageDialog.openError(null, OPEN_FAILURE,
						BINARY_FILES_ARE_NOT_SUPPORTED
								+ VIEW_THEM_VIA_REGISTRY_AFTER_ACTIVATION);
				return null;
			}
		}
		SourceFileEditorInput input = new SourceFileEditorInput(file);
		
		input.setRow(row);
		
		breakpointsSupported(file, contentType, input);
		
		readonlyEnabled(file, contentType, input);
		
		return openEditor(editorId, input);
	}

	protected void readonlyEnabled(IFile file, String contentType,
			SourceFileEditorInput input) {
//		input.setReadOnly(true);
	}

	protected void breakpointsSupported(IFile file, String contentType,
			SourceFileEditorInput input) {
		if (file.getRawLocation().toString().contains(ARTIFACT_TYPE.SCRIPTING_SERVICES)
				&& contentType != null
				&& contentType.contains("javascript")) {
			input.setBreakpointsEnabled(true);
		}
	}
	
	

	private IEditorPart openEditor(String id, IEditorInput input) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			IEditorPart editorPart = page.openEditor(input, id);
			return editorPart;
		} catch (PartInitException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}

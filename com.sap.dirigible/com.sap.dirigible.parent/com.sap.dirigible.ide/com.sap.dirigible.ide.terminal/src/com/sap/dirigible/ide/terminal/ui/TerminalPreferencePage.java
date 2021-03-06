package com.sap.dirigible.ide.terminal.ui;

import java.io.IOException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.repository.RepositoryGlobalPreferenceStore;

public class TerminalPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final long serialVersionUID = -877187045002896492L;
	
	private static final Logger logger = Logger.getLogger(TerminalPreferencePage.class);
	
	private static final String CONF_NAME_TERMINAL = "terminal";
	
	public static final String LIMIT_TIMEOUT = "limitTimeout";
	public static final String LIMIT_ENABLED = "limitEnabled";
	
	
	private RepositoryGlobalPreferenceStore repositoryPreferenceStore;

	private BooleanFieldEditor enableTimeLimitField;
	private IntegerFieldEditor limitTimeoutField;
	
	public static RepositoryGlobalPreferenceStore getTerminalPreferenceStore() {
		try {
			RepositoryGlobalPreferenceStore repositoryPreferenceStore = 
					new RepositoryGlobalPreferenceStore(CommonParameters.CONF_PATH_IDE, CONF_NAME_TERMINAL);
			repositoryPreferenceStore.load();
			return repositoryPreferenceStore;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public TerminalPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		this.repositoryPreferenceStore = 
				new RepositoryGlobalPreferenceStore(CommonParameters.CONF_PATH_IDE, CONF_NAME_TERMINAL);
		try {
			repositoryPreferenceStore.load();
			setPreferenceStore(repositoryPreferenceStore);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	protected void createFieldEditors() {
		
		try {
			repositoryPreferenceStore.load();
			if (repositoryPreferenceStore.preferenceNames().length == 0) {
				repositoryPreferenceStore.setValue(LIMIT_ENABLED, true);
				repositoryPreferenceStore.setValue(LIMIT_TIMEOUT, 30);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		enableTimeLimitField = new BooleanFieldEditor(
				LIMIT_ENABLED,
				"&Enable Time Limit",
		 		getFieldEditorParent());
			enableTimeLimitField.setPreferenceStore(getPreferenceStore());
			enableTimeLimitField.load();
		addField(enableTimeLimitField);
	
		limitTimeoutField = new IntegerFieldEditor(
				LIMIT_TIMEOUT,
				"&Limit Timeout (s):",
		 		getFieldEditorParent());
			limitTimeoutField.setPreferenceStore(getPreferenceStore());
			limitTimeoutField.load();
		addField(limitTimeoutField);

	}

	@Override
	public void init(IWorkbench workbench) {
		super.initialize();
	}
	
	private void storeValues() {
		try {
			enableTimeLimitField.store();
			limitTimeoutField.store();
			repositoryPreferenceStore.save();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void performApply() {
		super.performApply();
		storeValues();
	}

	@Override
	public boolean performOk() {
		storeValues();
		return super.performOk();
	}
	

	// TODO - Defaults?
	
}

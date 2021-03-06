/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package com.sap.dirigible.ide.ui.rap.layoutsets.fancy;

import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.rap.ui.interactiondesign.layout.model.ILayoutSetInitializer;
import org.eclipse.rap.ui.interactiondesign.layout.model.LayoutSet;

import com.sap.dirigible.ide.ui.rap.shared.LayoutSetConstants;

@SuppressWarnings("deprecation")
public class ConfigDialogInitializer implements ILayoutSetInitializer {

	public void initializeLayoutSet(final LayoutSet layoutSet) {
		layoutSet.addColor(LayoutSetConstants.CONFIG_BLACK,
				Graphics.getColor(0, 0, 0));
		layoutSet.addColor(LayoutSetConstants.CONFIG_WHITE,
				Graphics.getColor(255, 255, 255));
		layoutSet.addImagePath(LayoutSetConstants.CONFIG_DIALOG_CLOSE,
				LayoutSetConstants.IMAGE_PATH_FANCY + "close.png"); //$NON-NLS-1$
		layoutSet.addImagePath(LayoutSetConstants.CONFIG_DIALOG_ICON,
				LayoutSetConstants.IMAGE_PATH_FANCY + "conf_dialog_icon.png"); //$NON-NLS-1$
	}
}

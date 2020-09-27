/*******************************************************************************
 * Copyright (C) 2015, Max Hohenegger <eclipse@hohenegger.eu>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.egit.gitflow.ui.internal.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.egit.core.internal.job.JobUtil;
import org.eclipse.egit.core.settings.GitSettings;
import org.eclipse.egit.gitflow.GitFlowRepository;
import org.eclipse.egit.gitflow.op.FeatureStartOperation;
import org.eclipse.egit.gitflow.op.GitFlowOperation;
import org.eclipse.egit.gitflow.ui.internal.JobFamilies;
import org.eclipse.egit.gitflow.ui.internal.UIText;
import org.eclipse.egit.gitflow.ui.internal.validation.FeatureNameValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * git flow feature start
 */
public class FeatureStartHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final GitFlowRepository gfRepo = GitFlowHandlerUtil.getRepository(event);
		if (gfRepo == null) {
			return null;
		}

		InputDialog inputDialog = new StartDialog(
				HandlerUtil.getActiveShell(event),
				UIText.FeatureStartHandler_provideFeatureName,
				UIText.FeatureStartHandler_pleaseProvideANameForTheNewFeature,
				"", //$NON-NLS-1$
				new FeatureNameValidator(gfRepo));

		if (inputDialog.open() != Window.OK) {
			return null;
		}

		final String featureName = inputDialog.getValue();
		GitFlowOperation operation = new FeatureStartOperation(
				gfRepo, featureName,
				GitSettings.getRemoteConnectionTimeout());
		String fullBranchName = gfRepo.getConfig()
				.getFullFeatureBranchName(featureName);
		JobUtil.scheduleUserWorkspaceJob(operation,
				UIText.FeatureStartHandler_startingNewFeature,
				JobFamilies.GITFLOW_FAMILY,
				new PostBranchCheckoutUiTask(gfRepo, fullBranchName, operation));

		return null;
	}
}

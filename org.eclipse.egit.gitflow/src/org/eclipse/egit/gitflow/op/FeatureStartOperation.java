/*******************************************************************************
 * Copyright (C) 2018, Max Hohenegger <eclipse@hohenegger.eu>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.egit.gitflow.op;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.egit.gitflow.Activator;
import org.eclipse.egit.gitflow.GitFlowConfig;
import org.eclipse.egit.gitflow.GitFlowRepository;
import org.eclipse.egit.gitflow.internal.CoreText;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.osgi.util.NLS;

/**
 * git flow feature start
 */
public final class FeatureStartOperation extends AbstractFeatureOperation {
	/**
	 * @param repository
	 * @param featureName
	 */
	public FeatureStartOperation(GitFlowRepository repository,
			String featureName) {
		super(repository, featureName);
	}

	@Override
	public void execute(IProgressMonitor monitor) throws CoreException {
		GitFlowConfig config = repository.getConfig();
		handleDivergingDevelop(config);

		String branchName = config.getFeatureBranchName(featureName);
		RevCommit head = repository.findHead(config.getDevelop());
		if (head == null) {
			throw new IllegalStateException(NLS.bind(CoreText.StartOperation_unableToFindCommitFor, config.getDevelop()));
		}
		start(monitor, branchName, head);
	}

	private void handleDivergingDevelop(GitFlowConfig config)
			throws CoreException {
		try {
			BranchTrackingStatus developStatus = BranchTrackingStatus
					.of(repository.getRepository(), config.getDevelop());
			if (developStatus == null) {
				return;
			}

			if (developStatus.getBehindCount() > 0) {
				String message = NLS.bind(
						CoreText.FeatureStartOperation_divergingDevelop,
						config.getDevelop(), getOriginDevelopName(developStatus));
				if (developStatus.getAheadCount() == 0) {
					message += "\n" + NLS.bind( //$NON-NLS-1$
							CoreText.FeatureStartOperation_andBranchMayBeFastForwarded,
							config.getDevelop());
				}

				throw new CoreException(Activator.error(message));
			} else if (developStatus.getAheadCount() > 0) {
				String message = NLS.bind(
						CoreText.FeatureStartOperation_divergingDevelop,
						config.getDevelop(),
						getOriginDevelopName(developStatus));
				message += "\n" + NLS.bind( //$NON-NLS-1$
						CoreText.FeatureStartOperation_andLocalDevelopIsAheadOfOrigin,
						config.getDevelop(),
						getOriginDevelopName(developStatus));
				Activator.logInfo(message);
			}
		} catch (IOException e) {
			throw new CoreException(Activator.error(e));
		}
	}

	private String getOriginDevelopName(BranchTrackingStatus developStatus) {
		return developStatus.getRemoteTrackingBranch()
				.substring(Constants.R_REMOTES.length());
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return null;
	}
}

/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/
package org.elundstrom.e4.rcp.testbed.internal.lifecycle;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This is a stub implementation containing e4 LifeCycle annotated
 * methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references
 * this class.
 **/
@SuppressWarnings("restriction")
public class E4LifeCycle {

	@PostContextCreate
	void postContextCreate(final IEventBroker eventBroker, final IEclipseContext workbenchContext) {
		System.out.println("E4LifeCycle@PostContextCreate: " + workbenchContext + ". Adding an eventhandler for case UILifeCycle.APP_STARTUP_COMPLETE"); //$NON-NLS-1$

		eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("E4LifeCycle.handleEvent(): UILifeCycle.APP_STARTUP_COMPLETE. Run PerspectiveFactoryHandler."); //$NON-NLS-1$

				//PerspectiveFactoryHandler handler = ContextInjectionFactory.make(PerspectiveFactoryHandler.class, workbenchContext);
				//ContextInjectionFactory.invoke(handler, Execute.class, workbenchContext);
			}
		});
	}

	@PreSave
	void preSave(IEclipseContext workbenchContext, MApplication mapp) {
		System.out.println("E4LifeCycle@PreSave: " + workbenchContext+ ", performing nothing currently."); //$NON-NLS-1$
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext, MApplication mapp) {
		// The window exists by now.

		// Sure, it does, but is not configured with context etc.
		// MWindow window = null;
		// if ( mapp.getChildren() != null && !mapp.getChildren().isEmpty() ) {
		// window = mapp.getChildren().get( 0 );
		// IEclipseContext wContext = window.getContext();
		// }

		System.out.println("E4LifeCycle@ProcessAdditions: " + workbenchContext + ", performing nothing currently."); //$NON-NLS-1$
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext,
			MApplication application /*
										 * , @Optional @Named(
										 * "singleinstance.shutdown") Boolean
										 * shutdownTheApplication
										 */ ) {
		System.out.println("E4LifeCycle@ProcessRemovals: " + workbenchContext + ", performing nothing currently."); //$NON-NLS-1$

		boolean shutdown = false;
		// if ( shutdownTheApplication != null ) {
		// shutdown = shutdownTheApplication.booleanValue();
		// }
		if (shutdown) {
			// Not the best solution, but it shuts down the application
			// Another idea is to use a processor and inject a shutdown handler
			// that later can be called when a workbench is available
			// (Workbench.close())
			application.getChildren().clear();
		}
	}
}

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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.osgi.service.datalocation.Location;
import org.elundstrom.e4.rcp.testbed.internal.model.DefaultStateLoader;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This is a stub implementation containing e4 LifeCycle annotated
 * methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references
 * this class.
 **/
@SuppressWarnings( "restriction" )
public class E4LifeCycle
{

	@PostContextCreate
	void postContextCreate( final IEventBroker eventBroker, final IEclipseContext workbenchContext )
	{
		System.out.println( "E4LifeCycle@PostContextCreate: " + workbenchContext //$NON-NLS-1$
				+ ". Adding an eventhandler for case UILifeCycle.APP_STARTUP_COMPLETE" );

		eventBroker.subscribe( UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new EventHandler() {
			@Override
			public void handleEvent( Event event )
			{
				System.out.println( "E4LifeCycle.handleEvent(): UILifeCycle.APP_STARTUP_COMPLETE, performing nothing currently." ); //$NON-NLS-1$
				//System.out.println("E4LifeCycle.handleEvent(): UILifeCycle.APP_STARTUP_COMPLETE. Run PerspectiveFactoryHandler."); //$NON-NLS-1$

				//PerspectiveFactoryHandler handler = ContextInjectionFactory.make(PerspectiveFactoryHandler.class, workbenchContext);
				//ContextInjectionFactory.invoke(handler, Execute.class, workbenchContext);
			}
		} );
	}

	@PreSave
	void preSave( IEclipseContext workbenchContext, MApplication mapp )
	{
		System.out.println( "E4LifeCycle@PreSave: " + workbenchContext + ", performing nothing currently." ); //$NON-NLS-1$
	}

	@ProcessAdditions
	void processAdditions( IEclipseContext workbenchContext, MApplication mapp )
	{
		// The window exists by now.

		// Sure, it does, but is not configured with context etc.
		// MWindow window = null;
		// if ( mapp.getChildren() != null && !mapp.getChildren().isEmpty() ) {
		// window = mapp.getChildren().get( 0 );
		// IEclipseContext wContext = window.getContext();
		// }

		System.out.println( "E4LifeCycle@ProcessAdditions: " + workbenchContext + ", performing nothing currently." ); //$NON-NLS-1$
	}

//	private File getWorkbenchSaveLocation()
//	{
//		File workbenchData = new File( getBaseLocation(), "workbench.xmi" ); //$NON-NLS-1$
//		return workbenchData;
//	}

	private File getBaseLocation( Location instanceLocation )
	{
		File baseLocation;
		try {
			baseLocation = new File( URIUtil.toURI( instanceLocation.getURL() ) );
		}
		catch ( URISyntaxException e ) {
			throw new RuntimeException( e );
		}
		baseLocation = new File( baseLocation, ".metadata" ); //$NON-NLS-1$
		baseLocation = new File( baseLocation, ".plugins" ); //$NON-NLS-1$
		baseLocation = new File( baseLocation, "org.eclipse.e4.workbench" ); //$NON-NLS-1$
		return baseLocation;
	}

	@ProcessRemovals
	void processRemovals( @Named( E4Workbench.INSTANCE_LOCATION ) @Optional Location instanceLocation,
			@Named( E4Workbench.INITIAL_WORKBENCH_MODEL_URI ) @Optional URI applicationDefinitionInstance,
			@Named( IWorkbench.PERSIST_STATE ) boolean saveAndRestore,
			@Named( IWorkbench.CLEAR_PERSISTED_STATE ) boolean clearPersistedState, IEclipseContext workbenchContext,
			MApplication application, EModelService modelService /*
																	* , @Optional @Named(
																	* "singleinstance.shutdown") Boolean
																	* shutdownTheApplication
																	*/ )
	{
		System.out.println( "E4LifeCycle@ProcessRemovals" ); //$NON-NLS-1$

		//Use this opportunity to check whether there are some garbage/excessive placeholders floating around. 
		//Decide whether a "restore" has actually happened. (Influenced by method in ResourceHandler)
		boolean wasRestored = false;
		if ( saveAndRestore && !clearPersistedState && instanceLocation != null
				&& applicationDefinitionInstance != null && workbenchContext != null ) {
			File workbenchData = new File( getBaseLocation( instanceLocation ), "workbench.xmi" ); //$NON-NLS-1$
			if ( workbenchData != null && workbenchData.exists() ) {
				URI restoreLocation = URI.createFileURI( workbenchData.getAbsolutePath() );
				// last stored time-stamp
				long restoreLastModified = restoreLocation == null ? 0L
						: new File( restoreLocation.toFileString() ).lastModified();
				wasRestored = restoreLastModified > 0;
			}
		}

		if ( wasRestored ) {
			System.out.println( "E4LifeCycle@ProcessRemovals: removing potentially excessive placeholders" );

			//Get a clone of the App def instance (incl fragments). Just used for comparisons; to track the default
			//positions (i.e. container ids) of the Placeholders
			DefaultStateLoader loader = new DefaultStateLoader( workbenchContext );
			Resource applicationResource = loader.loadResource( applicationDefinitionInstance );

			//Cloned app
			MApplication referenceApplication = (MApplication)applicationResource.getContents().get( 0 );

			//Let's take one perspective at the time. 
			List<MPerspective> perspectives = modelService.findElements( application, MPerspective.class, EModelService.ANYWHERE, PERSPECTIVE_SELECTOR );
			for ( MPerspective perspective : perspectives ) {
				String perspId = perspective.getElementId();
				//TODO: Maybe switch this query to using something more "safe"? Imagine two distinct MUIElement (probably of different type)
				//having the same ids. This API method does not feel that safe - may retrieve an unexpected object (of unwanted type). 
				MPerspective referencePerspective = (MPerspective)modelService.find( perspId, referenceApplication );
				if ( referencePerspective != null ) {
					this.checkPlaceholders( perspective, referencePerspective, modelService );
				}
				else {
					System.out.println( "Could not find a perspective with the id " + perspId
							+ ". Maybe a custom perspective?" );
				}
			}

			//Drop the EMF Resource clone (we have picked what we need from it and moved to the "correct" perspective stack)
			loader.unload();
			loader = null;
		}

		boolean shutdown = false;
		// if ( shutdownTheApplication != null ) {
		// shutdown = shutdownTheApplication.booleanValue();
		// }
		if ( shutdown ) {
			// Not the best solution, but it shuts down the application
			// Another idea is to use a processor and inject a shutdown handler
			// that later can be called when a workbench is available
			// (Workbench.close())
			application.getChildren().clear();
		}
	}

	private void checkPlaceholders( MPerspective perspective, MPerspective referencePerspective,
			EModelService modelService )
	{
		//This method assumes that any given placeholder (id) only occurs once in a certain perspective.
		//TODO make more rigorous when several PHs may be defined with same id. Also, keep in mind that things may be
		//more difficult when/if PartDescriptors will be used. 

		//Using the referencePerspective as the starting point. 
		List<MPlaceholder> referencePlaceholders = modelService.findElements( referencePerspective, MPlaceholder.class, EModelService.ANYWHERE, PLACEHOLDER_SELECTOR );
		for ( MPlaceholder referencePlaceholder : referencePlaceholders ) {
			String phId = referencePlaceholder.getElementId();

			//This is the important thing: See if we can find >1 placeholder in the perspective with the same id.
			List<MPlaceholder> placeholders = modelService.findElements( perspective, phId, MPlaceholder.class, null );
			if ( placeholders.size() > 1 ) {
				//This means that the restore (of workbench.xmi) created one instance in the "non-default" container, i.e. the
				//user moved the placeholder. The other was created during ModelAssembler operation (a placeholder coming from a fragment - buggy stuff there?)
				System.out.println( "the case" );

				MElementContainer<MUIElement> parent1 = referencePlaceholder.getParent();
				String defaultContainerId = parent1 != null ? parent1.getElementId() : "";

				MPlaceholder inUserIntendedPlace = null;  //as deserialized during ResourceLoader "restore"
				MPlaceholder inDefaultPlace = null;  //as generated with ModelAssembler "delta"
				for ( MPlaceholder mPlaceholder : placeholders ) {
					MElementContainer<MUIElement> parent2 = mPlaceholder.getParent();
					if ( parent2 != null && Objects.equals( defaultContainerId, parent2.getElementId() ) ) {
						inDefaultPlace = mPlaceholder;
					}
					else if ( parent2 != null && !Objects.equals( defaultContainerId, parent2.getElementId() ) ) {
						inUserIntendedPlace = mPlaceholder;
					}
				}
				if ( inUserIntendedPlace != null && inDefaultPlace != null ) {
					//Maybe a bit over-cautions, since its only about Placeholders. But why not be meticulous! What we
					//do is to move the PH generated during ModelAssembler procedure to the intended place. Reason: maybe new version
					//of (fragment) app model has added something to the object...(not very likely, but still)
					EObject root = EcoreUtil.getRootContainer( (EObject)inUserIntendedPlace );
//					// Replacing the object in the container
					EcoreUtil.replace( (EObject)inUserIntendedPlace, (EObject)inDefaultPlace );
//					// Replacing the object in other references than the container.
					Collection<Setting> settings = UsageCrossReferencer.find( (EObject)inUserIntendedPlace, root );
					for ( Setting setting : settings ) {
						setting.set( inDefaultPlace );
					}
				}
			}
			//else if 0 or 1 - ok.
		}
	}

	private static Selector PERSPECTIVE_SELECTOR = new Selector() {

		@Override
		public boolean select( MApplicationElement element )
		{
			return element instanceof MPerspective;
		}
	};

	private static Selector PLACEHOLDER_SELECTOR = new Selector() {

		@Override
		public boolean select( MApplicationElement element )
		{
			return element instanceof MPlaceholder;
		}
	};
}

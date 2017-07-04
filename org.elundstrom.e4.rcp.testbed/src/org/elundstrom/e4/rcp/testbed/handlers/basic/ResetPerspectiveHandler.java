package org.elundstrom.e4.rcp.testbed.handlers.basic;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.widgets.Shell;
import org.elundstrom.e4.rcp.testbed.internal.model.DefaultStateLoader;

/**
 * <!-- begin-user-doc --> The <b>Reset Perspective</b> handler class,
 * registered for "Reset Perspective" command. A (handled) menu item exists in
 * the App model (Window/Main Menu/Window Menu). <br>
 * 
 * What this handler performs is:
 * <ul>
 * <li>Creates a pristine, cloned Resource from the App Model + its fragments.
 * (This represents the intended default layouts of all perspectives.)</li>
 * <li>For the cloned Perspective of interest: make sure that all placeholders
 * refer Parts existing in the "Shared Elements" of the existing Window.</li>
 * <li>Make the rendering engine destroy the UI of the active perspective.</li>
 * <li>Remove the active perspective from the runtime model.</li>
 * <li>Add the cloned perspective to the runtime model.</li>
 * <li>Switch to the cloned perspective (this will also create the necessary UI
 * elements).</li>
 * <li>Unload the "clone" EMF Resource created in the first step.</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @author elundstrom
 *
 */
@SuppressWarnings( "restriction" )
public class ResetPerspectiveHandler
{

	@Inject
	@Named( E4Workbench.INITIAL_WORKBENCH_MODEL_URI )
	private URI applicationDefinitionInstance;

	// @Inject
	// private Logger logger;

	@Inject
	private IEclipseContext context;

	private static Selector PLACEHOLDER_SELECTOR = new Selector() {

		@Override
		public boolean select( MApplicationElement element )
		{
			return element instanceof MPlaceholder;
		}
	};

	@Execute
	public void execute( @Named( IServiceConstants.ACTIVE_SHELL ) Shell shell, EModelService modelService,
			MApplication app, EPartService partService,
			MWindow window /* , IEclipseContext workbenchContext */ )
	{

		MPerspective activePerspective = (modelService != null && window != null)
				? modelService.getActivePerspective( window ) : null;

		if ( activePerspective == null || activePerspective.getElementId() == null ) {
			System.out.println( "Warn: ResetPerspectiveHandler@Execute: No active perspective!" );
			return;
		}

		String perspId = activePerspective.getElementId();
		System.out.println( "ResetPerspectiveHandler@Execute, persp is: " + perspId );

		// Get a cloned Resource from the App Model + its fragments, representing the default state of the Perspective(s).
		// This would be the only way, since the current MPerspective represents
		// the "user current state", and we do not employ snippets currently.
		System.out.println( "ResetPerspectiveHandler@Execute:Cloning original app definition, incl fragments..." );
		DefaultStateLoader loader = new DefaultStateLoader( this.context );
		Resource applicationResource = loader.loadResource( applicationDefinitionInstance );

		//Cloned app (this will be dropped/unloaded, once we have "cherry-picked" the Perspective clone from it)
		MApplication appElement = (MApplication)applicationResource.getContents().get( 0 );

		MUIElement clone = modelService.find( perspId, appElement );
		if ( clone instanceof MPerspective ) {
			MPerspective clonedPerspective = (MPerspective)clone;
			// clonedPerspective.setToBeRendered(true);  //No need, it will be "true" here

			//Make the (cloned) Placeholders refer the "shared elements" parts of the Window we want (not of the clone, that we will not use)
			List<MPlaceholder> placeholders = modelService.findElements( clonedPerspective, MPlaceholder.class, EModelService.ANYWHERE, PLACEHOLDER_SELECTOR );
			for ( MPlaceholder mPlaceholder : placeholders ) {
				//We can assume two possible id conventions for placeholders: 1) Same id as the part it will refer. 2) Some decided-upon syntax (like "<PartId>.ph")
				String phId = mPlaceholder.getElementId();
				MPart part = this.findSharedPart( modelService, window, phId ); //Try option 1.
				if ( part == null ) {
					String[] splitted = phId.split( ".ph" ); //Try option 2.
					if ( splitted != null && splitted.length == 1 ) {
						part = this.findSharedPart( modelService, window, splitted[0] );
					}
				}
				if ( part != null ) {
					mPlaceholder.setRef( part );
				}
				else {
					System.out.println( "PlaceholderUtility.redirectPlaceholderReferences: Could not find a (shared element) Part with the ID: "
							+ phId );
				}
			}

			MElementContainer<MUIElement> parent = activePerspective.getParent();
			//Make the rendering engine call "removeGui" -> disposal/detroy. POJOs backing the MParts will destroy here. 
			activePerspective.setToBeRendered( false );

			// Remove old perspective
			parent.getChildren().remove( activePerspective );
			activePerspective = null;

			// Add new, cloned perspective
			parent.getChildren().add( clonedPerspective );

			//Make the rendering engine create new POJOs etc. The MPart will have its "setObject()" invoked with those instances.
			partService.switchPerspective( clonedPerspective );

			System.out.println( "ResetPerspectiveHandler@Execute  Reset DONE. Removed old, added cloned and switched to it." );
		}
		else {
			System.out.println( "Cannot reset perspective " + activePerspective.getElementId() //$NON-NLS-1$
					+ ". Could not find a perspective defined with that id." ); //$NON-NLS-1$
		}

		//Drop the EMF Resource clone (we have picked what we need from it and moved to the "correct" perspective stack)
		loader.unload();
	}

	protected MPart findSharedPart( EModelService modelService, MWindow window, String partId )
	{
		MPart part = null;

		//Approach1: (Scrapped, due to non-proper merging of app fragments (?)) 
		// part = (MPart) modelService.find(partId, window); //May find
		// an MPart contained in a StringModelFragmentImpl..we do not
		// want that.

		//Approach2: (Scrapped, due to that it will detect the "wrong" part, in the temporary/cloned window) 
		//We make sure to find the MPart we're looking for in the "shared
		//elements" containment reference of the MWindow only:
//		Class<?> clazz = MUIElement.class;
//		Selector sharedElemMatcher = new SharedElementMatcher( partId, clazz, null, window );
//		List<?> result = modelService.findElements( window, clazz, EModelService.ANYWHERE, sharedElemMatcher );
//		if ( result != null && !result.isEmpty() && result.get( 0 ) instanceof MPart ) {
//			part = (MPart)result.get( 0 );
//		}

		//Approach3: Keep it simple. We know where it is!
		for ( MUIElement sharedElement : window.getSharedElements() ) {
			if ( sharedElement instanceof MPart && Objects.equals( sharedElement.getElementId(), partId ) ) {
				if ( part != null ) {
					System.out.println( "Warning: Several parts exist in the 'shared elements' with the same id: "
							+ partId );
				}
				part = (MPart)sharedElement;
			}
		}

		return part;
	}
}

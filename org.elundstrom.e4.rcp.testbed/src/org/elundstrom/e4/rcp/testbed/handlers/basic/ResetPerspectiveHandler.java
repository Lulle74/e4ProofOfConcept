package org.elundstrom.e4.rcp.testbed.handlers.basic;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.widgets.Shell;
import org.elundstrom.e4.rcp.testbed.internal.PlaceholderUtility;
import org.elundstrom.e4.rcp.testbed.internal.model.DefaultStateLoader;

@SuppressWarnings("restriction")
public class ResetPerspectiveHandler {

	@Inject
	@Named(E4Workbench.INITIAL_WORKBENCH_MODEL_URI)
	private URI applicationDefinitionInstance;

	// @Inject
	// private Logger logger;

	@Inject
	private IEclipseContext context;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, EModelService modelService,
			MApplication app, EPartService partService,
			MWindow window /* , IEclipseContext workbenchContext */) {

		MPerspective activePerspective = modelService.getActivePerspective(window);

		if (activePerspective == null || activePerspective.getElementId() == null) {
			return;
		}

		String perspId = activePerspective.getElementId();
		System.out.println("ResetPerspectiveHandler@Execute, persp is: " + perspId);

		// Idea: Clone the "pristine" (default) state of the Perspective, as
		// defined in the e4xmi (plus the fragments additions/deltas "on top").
		// This
		// would be the only way, since the current MPerspective represents the
		// "user current state", and we do not employ snippets currently.
		System.out.println("ResetPerspectiveHandler@Execute:Cloning original app definition, incl fragments)...");
		DefaultStateLoader loader = new DefaultStateLoader(this.context);
		Resource applicationResource = loader.loadResource(applicationDefinitionInstance);

		// Add model items described in the model extension point
		// This has to be done before commands are put into the context
		MApplication appElement = (MApplication) applicationResource.getContents().get(0);

		MUIElement clone = modelService.find(perspId, appElement);

		// You must clone the perspective as snippet, otherwise the running
		// application would break, because the saving process of the resource
		// removes the element from the running application model
		// MUIElement clonedPerspective =
		// modelService.cloneElement(activePerspective, null /* window */);

		IEclipseContext appContext = app.getContext();

		if (clone instanceof MPerspective) {
			MPerspective clonedPerspective = (MPerspective) clone;
			// When cloned, this will already be "true".
			// clonedPerspective.setToBeRendered(true);

			PlaceholderUtility.redirectPlaceholderReferences(clonedPerspective, window, modelService);

			// Set the old one toBeRendered=false. This will cause the rendering
			// engine to call "removeGui" - disposal/destroys. Note that the
			// POJOs "backing" the MParts
			// will be destroyed here, and MPart.setObject() will be invoked
			// with null. That is fine.
			MElementContainer<MUIElement> parent = activePerspective.getParent();
			activePerspective.setToBeRendered(false);

			// Remove the old
			parent.getChildren().remove(activePerspective);
			activePerspective = null;

			// Add new
			parent.getChildren().add(clonedPerspective);

			// This "switch" will invoke the rendering, and thus create new POJO
			// instances, and MPart.setObject() will
			// be invoked with such a POJO instance.
			partService.switchPerspective(clonedPerspective);

			// Run our "perspective factory"
			// PerspectiveFactoryHandler handler =
			// ContextInjectionFactory.make(PerspectiveFactoryHandler.class,
			// appContext);
			// ContextInjectionFactory.invoke(handler, Execute.class,
			// appContext);
			// Now the new perspective copy should be ok!

			System.out.println(
					"ResetPerspectiveHandler@Execute  Reset DONE. Removed old, added cloned and switched to it.");
		} else {
			System.out.println("Cannot reset perspective " + activePerspective.getElementId() //$NON-NLS-1$
					+ ". Could not find a perspective defined with that id."); //$NON-NLS-1$
		}

		loader.unload();
	}

}

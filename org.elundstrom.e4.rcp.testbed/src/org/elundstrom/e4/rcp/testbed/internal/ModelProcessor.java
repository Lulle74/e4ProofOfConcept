package org.elundstrom.e4.rcp.testbed.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/**
 * This processor was used when I was trying out to define both Window and
 * Perspective(s) as "Snippets". Does not seem as a viable way though...
 * 
 */
public class ModelProcessor {
	@Execute
	public void execute(EModelService modelService, EPartService partService, MApplication application,
			IEclipseContext context) {
		System.out.println("ModelProcessor@Execute");

		MWindow existingFromLoad = (MWindow) modelService.find(AppModel_IDConstants.WINDOW, application);
		if (existingFromLoad != null) {
			System.out.println(
					"ModelProcessor.execute(): not performing; Window already exists. Either model reconciliation (= load former user state) did the job, or Window exists from the get go in the app e4xmi.");
			return;
		}

		// The window will not exist as we get here - it is defined as a
		// "snippet".
		MWindow window = (MWindow) modelService.cloneSnippet(application, AppModel_IDConstants.WINDOW, null); // $NON-NLS-1$
		if (window == null) {
			System.out.println("Could not clone the main window 'as a snippet'. Make sure it is defined as a snippet!");
			return;
		}

		MPerspectiveStack perspectiveStack = (MPerspectiveStack) modelService.find(AppModel_IDConstants.PERSP_STACK,
				window); // $NON-NLS-1$

		// clone each snippet that is a perspective and add the cloned
		// perspective into the main PerspectiveStack
		MPerspective perspToSelect = null;
		for (MUIElement snippet : application.getSnippets()) {
			if (snippet instanceof MPerspective) {
				MPerspective perspectiveClone = (MPerspective) modelService.cloneSnippet(application,
						snippet.getElementId(), null);
				perspectiveStack.getChildren().add(perspectiveClone);
				if (perspToSelect == null) {
					perspToSelect = perspectiveClone;
				}
			}
		}

		application.getChildren().add(window);
		// modelService.bringToTop( window );

		if (perspToSelect != null) {
			perspectiveStack.setSelectedElement(perspToSelect);
			// or: ?
			// partService.switchPerspective(perspToSelect);

			// Run our "perspective factory"
			// PerspectiveFactoryHandler handler =
			// ContextInjectionFactory.make(PerspectiveFactoryHandler.class,
			// context);
			// ContextInjectionFactory.invoke(handler, Execute.class, context);
			// Now the new perspective copy should be ok!
		}
	}

}
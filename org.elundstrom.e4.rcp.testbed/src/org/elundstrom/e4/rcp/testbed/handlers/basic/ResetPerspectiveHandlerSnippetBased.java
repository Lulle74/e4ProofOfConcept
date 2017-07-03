package org.elundstrom.e4.rcp.testbed.handlers.basic;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

/**
 * This impl of a "reset perspective" requires that perspectives be defined as "snippets". Not currently used. 
 * 
 * @author elundstrom
 *
 */
public class ResetPerspectiveHandlerSnippetBased {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, EModelService modelService,
			MApplication app, EPartService partService, MWindow window) {

		// MWindow window = (MWindow)modelService.find( "Main", app );

		final MPerspective perspective = modelService.getActivePerspective(window);

		final MUIElement snippet = modelService.cloneSnippet(app, perspective.getElementId(), window);

		if (snippet != null) {
			snippet.setToBeRendered(true);
			MElementContainer<MUIElement> parent = perspective.getParent();
			perspective.setToBeRendered(false);

			List<MWindow> existingDetachedWindows = new ArrayList<MWindow>();
			existingDetachedWindows.addAll(perspective.getWindows());

			MPerspective dummyPerspective = (MPerspective) snippet;
			while (dummyPerspective.getWindows().size() > 0) {
				MWindow detachedWindow = dummyPerspective.getWindows().remove(0);
				perspective.getWindows().add(detachedWindow);
			}

			parent.getChildren().remove(perspective);

			parent.getChildren().add(snippet);
			System.out.println(parent.getChildren().get(0).getElementId());
			// String idden = snippet.getElementId();
			// partService.switchPerspective( idden );
			partService.switchPerspective((MPerspective) snippet);
		} else {
			System.out.println("Cannot reset perspective " + perspective.getElementId() //$NON-NLS-1$
					+ ". You need to define it as an E4 snippet."); //$NON-NLS-1$
		}
	}

}

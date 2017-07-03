package org.elundstrom.e4.rcp.testbed.internal;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class PlaceholderUtility {

	public static void redirectPlaceholderReferences(MPerspective clonedPerspective, MWindow window,
			EModelService modelService) {
		// All the existing placeholders in the cloned perspective. They are
		// "alright", except for that
		// they will (in their "ref" reference) refer to an MPart instance that
		// resides in the cloned MWindow.
		// Remember, what we are after is to get a pristine clone of the
		// MPerspective, but with "intact" MWindow
		// and its SharedElements.

		// TODO: Is there a better query to be made than this? Seems to work
		// alright, though, but feels a bit clumsy.
		List<MPlaceholder> placeholders = modelService.findElements(clonedPerspective, MPlaceholder.class,
				EModelService.ANYWHERE, new Selector() {

					@Override
					public boolean select(MApplicationElement element) {
						return element instanceof MPlaceholder;
					}
				});

		for (MPlaceholder mPlaceholder : placeholders) {
			// TODO maybe check whether the "ref" of the MPlaceholder is "set"
			// already. Does not
			// really harm to reset though...
			String phId = mPlaceholder.getElementId();
			String[] splitted = phId.split(AppModel_IDConstants.PLACEHOLDER_SUFFIX);
			if (splitted != null && splitted.length == 1) {
				String partId = splitted[0];

				// This is important: Get/find the part from the current window
				// (i.e. the shared elements of the window)
				MPart part = (MPart) modelService.find(partId, window);
				if (part == null) {
					System.out.println(
							"PlaceholderUtility.redirectPlaceholderReferences: Could not find a (shared element) Part with the ID: "
									+ partId);
				}

				// And set the ref of the Placeholder to the pre-existing one.
				// We do not want the placeholder referring
				// to the cloned MWindow.sharedElements part
				if (part != null) {
					mPlaceholder.setRef(part);
					//mPlaceholder.setToBeRendered(true);
				}

			}
		}

	}
}

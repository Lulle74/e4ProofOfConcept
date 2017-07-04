package org.elundstrom.e4.rcp.testbed.internal;

import java.util.Objects;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ElementMatcher;
import org.eclipse.emf.ecore.EObject;

/**
 * Note: Class not in use - the query (using EModelService) where this Selector implementation was used did not give the results 
 * sought for. Maybe I don't understand how to query for "shared elements" properly - resorting to plain EMF API traversals seems easier...
 * @author elundstrom
 *
 */
public class SharedElementMatcher extends ElementMatcher {
	private MWindow window = null;

	public SharedElementMatcher(String id, Class<?> clazz, String tag, MWindow sharedElementContainer) {
		super(id, clazz, tag);
		this.window = sharedElementContainer;
	}

	@Override
	public boolean select(MApplicationElement element) {
		boolean match = false;
		if (super.select(element)) {
			// The parent (eContainer) of a "shared element" is the MWindow.
			if (this.window != null && element instanceof EObject
					&& Objects.equals(this.window, ((EObject) element).eContainer())) {
				match = true;
			}
		}
		return match;
	}
}

package org.elundstrom.e4.rcp.testbed.internal.model;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

@SuppressWarnings("restriction")
public class DefaultStateLoader {

	private IEclipseContext context;
	private ResourceSetImpl resourceSetImpl;

	public DefaultStateLoader(IEclipseContext eContext) {
		this.context = eContext;
	}

	private void init() {
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());

		resourceSetImpl.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI, ApplicationPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI, CommandsPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI, BasicPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI, AdvancedPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

	}

	public void unload() {
		for (Resource resource : this.resourceSetImpl.getResources()) {
			resource.unload();
		}
		this.resourceSetImpl.getResources().clear();
		this.resourceSetImpl = null;
	}

	public Resource loadResource(URI applicationDefinitionInstance) {
		if (this.resourceSetImpl != null) {
			this.unload();
		}

		this.init();

		if (applicationDefinitionInstance == null) {
			System.out.println("Cannot load with null URI");
			return null;
		}

		Resource resource;
		try {
			resource = getResource(applicationDefinitionInstance);
		} catch (Exception e) {
			// TODO We could use diagnostics for better analyzing the error
			System.out.println("Unable to load resource " + applicationDefinitionInstance.toString() + ", exc: " + e);
			// logger.error(e, "Unable to load resource " +
			// applicationDefinitionInstance.toString()); //$NON-NLS-1$
			return null;
		}

		// TODO once we switch from deltas, we only need this once on the
		// default model?
		String contributorURI = URIHelper.EMFtoPlatform(applicationDefinitionInstance);
		if (contributorURI != null) {
			TreeIterator<EObject> it = EcoreUtil.getAllContents(resource.getContents());
			while (it.hasNext()) {
				EObject o = it.next();
				if (o instanceof MApplicationElement) {
					((MApplicationElement) o).setContributorURI(contributorURI);
				}
			}
		}

		// Add model items described in the model extension point
		// This has to be done before commands are put into the context
		MApplication appElement = (MApplication) resource.getContents().get(0);

		// this.context.set(MApplication.class, appElement);

		// This will run processors and fragments
		// NOTE: See the doc in the CustomModelAssembler...it is a copy of the
		// org.eclipse.e4.ui.internal.workbench.ModelAssembler. The sole reason
		// for
		// copying it is that I need to provide another MApplication model
		// (which represents the "pristine" default state). The reason to invoke
		// the code below
		// is to apply the "pristine" default fragment state as well).
		CustomModelAssembler contribProcessor = ContextInjectionFactory.make(CustomModelAssembler.class, context);
		contribProcessor.processModel(true, appElement);

		return resource;

	}

	private Resource getResource(URI uri) throws Exception {
		Resource resource = resourceSetImpl.getResource(uri, true);
		return resource;
	}
}

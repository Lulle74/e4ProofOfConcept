package org.elundstrom.e4.rcp.testbed.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TodoDetailsPart {

	private static String NONE = "<NONE>";

	private Text activePartPresentationText;
	private Text activeSelectionPresentationText;

	public TodoDetailsPart() {
		// System.out.println("TodoDetailsPart alive.");
	}

	@PostConstruct
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		label.setText("Active part: ");

		this.activePartPresentationText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		this.activePartPresentationText.setMessage("Mess");
		this.activePartPresentationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		this.activePartPresentationText.setText(NONE);

		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		label.setText("Active selection: ");

		this.activeSelectionPresentationText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
		// this.activePartPresentationText.s
		this.activeSelectionPresentationText.setMessage("Mess");
		this.activeSelectionPresentationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		this.activeSelectionPresentationText.setText(NONE);
	}

	// tracks the active part
	@Inject
	@Optional
	public void receiveActivePart(@Named(IServiceConstants.ACTIVE_PART) MPart activePart, IEclipseContext context) {
		String activePartId = activePart != null ? activePart.getElementId() : NONE;
		if (this.activePartPresentationText != null && !this.activePartPresentationText.isDisposed()) {
			this.activePartPresentationText.setText(activePartId);
		}
	}

	// tracks the active shell
	@Inject
	@Optional
	public void receiveActiveShell(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		if (shell != null) {
			// System.out.println("Active shell (Window) changed");
		}
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection,
			@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		// System.out.println("SetSelection. From active part: "
		// + (activePart != null ? activePart.getElementId() : "null") + " ,
		// selection is : " + selection);

		String selectionString = selection != null ? selection.toString() : NONE;
		if (this.activeSelectionPresentationText != null && !this.activeSelectionPresentationText.isDisposed()) {
			this.activeSelectionPresentationText.setText(selectionString);
		}
	}
}

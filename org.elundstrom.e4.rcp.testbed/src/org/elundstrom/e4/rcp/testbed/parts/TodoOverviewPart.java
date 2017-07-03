package org.elundstrom.e4.rcp.testbed.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class TodoOverviewPart {
	@Inject
	protected ESelectionService selectionService;

	public TodoOverviewPart() {
		System.out.println("TodoOverviewPart() constructor");
	}

	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService) {
		System.out.println("TodoOverviewPart@PostConstruct");
		// more code...
		TableViewer viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);

		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});

		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				List<String> elements = new ArrayList<>();
				for (int i = 0; i < 50; i++) {
					String element = "Number " + i;
					elements.add(element);
				}

				return elements.toArray();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection viewerSelection = event.getSelection();
				Object selectionToPost = "<NONE>";
				if (viewerSelection instanceof StructuredSelection) {
					List<Object> allSelected = new ArrayList<>();
					allSelected.addAll(((StructuredSelection) viewerSelection).toList());
					selectionToPost = allSelected;
				}
				
				//Post selection to service
				TodoOverviewPart.this.selectionService.setSelection(selectionToPost);
			}
		});

		viewer.setInput(new Object());

		// register context menu on the table
		menuService.registerContextMenu(viewer.getControl(), "com.example.e4.rcp.todo.popupmenu.table");
	}
	
	@PreDestroy
	public void destroy()
	{
		System.out.println("TodoOverviewPart@PreDestroy");
	}
}

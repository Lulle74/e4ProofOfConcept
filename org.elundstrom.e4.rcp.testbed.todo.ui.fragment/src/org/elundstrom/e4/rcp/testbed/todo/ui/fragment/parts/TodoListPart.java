package org.elundstrom.e4.rcp.testbed.todo.ui.fragment.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
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
import org.eclipse.swt.widgets.Label;
import org.elundstrom.e4.rcp.testbed.todo.model.ITodoService;
import org.elundstrom.e4.rcp.testbed.todo.model.Todo;

public class TodoListPart
{
	@Inject
	protected ESelectionService selectionService;

	@Inject
	@Named( "org.elundstrom.e4.rcp.testbed.todo.model.ITodoService" )
	ITodoService todoService;

	private TableViewer viewer = null;

	public TodoListPart()
	{
		System.out.println( "TodoListPart() constructor" );
	}

	/**
	 * If service changes, be able to react.
	 * 
	 * @param todoService1
	 */
	@Inject
	public void setTodoService(
			@Named( "org.elundstrom.e4.rcp.testbed.todo.model.ITodoService" ) @Optional ITodoService todoService1 )
	{
		System.out.println( "setTodoService" );

		if ( this.viewer != null && this.viewer.getControl() != null && !this.viewer.getControl().isDisposed() )
			this.viewer.setInput( todoService1 );

	}

	@PostConstruct
	public void createControls( Composite parent )
	{
		System.out.println( "TodoListPart@PostConstruct" );
		Label lbl = new Label( parent, SWT.NONE );
		lbl.setText( "The content in this part (presented below) comes from the active ITodoService. \n"
				+ "The default ITodoService provider comes via a 'context function', an OSGi service which has access to the IEclipseContext. I was inspired by \n"
				+ "the Vogella resources (http://www.vogella.com/tutorials/Eclipse4ContextFunctions/article.html) and https://github.com/vogellacompany/eclipse4book to get going with this. \n\n"
				+ "The default ITodoService can be substituted with another one, pressing the Android-like button in the main toolbar.\n"
				+ "This part reacts to the (re)setting of the service, and updates the UI." );
		// more code...
		viewer = new TableViewer( parent, SWT.FULL_SELECTION | SWT.MULTI );

		viewer.setLabelProvider( new LabelProvider() {
			@Override
			public String getText( Object element )
			{
				return (String)element;
			}
		} );

		viewer.setContentProvider( new IStructuredContentProvider() {

			@Override
			public Object[] getElements( Object inputElement )
			{
				List<String> elements = new ArrayList<>();
				if ( inputElement instanceof ITodoService ) {
					List<Todo> todos = ((ITodoService)inputElement).getTodos();
					for ( Todo todo : todos ) {
						elements.add( todo.getId() + ": " + todo.getSummary() + " (" + todo.getDescription() + ")" );
					}
				}
				return elements.toArray();
			}
		} );

		viewer.addSelectionChangedListener( new ISelectionChangedListener() {

			@Override
			public void selectionChanged( SelectionChangedEvent event )
			{
				ISelection viewerSelection = event.getSelection();
				Object selectionToPost = "<NONE>";
				if ( viewerSelection instanceof StructuredSelection ) {
					List<Object> allSelected = new ArrayList<>();
					allSelected.addAll( ((StructuredSelection)viewerSelection).toList() );
					selectionToPost = allSelected;
				}

				//Post selection to service
				TodoListPart.this.selectionService.setSelection( selectionToPost );
			}
		} );

		viewer.setInput( this.todoService );

		// register context menu on the table
		//menuService.registerContextMenu(viewer.getControl(), "com.example.e4.rcp.todo.popupmenu.table");
	}

	@PreDestroy
	public void destroy()
	{
		System.out.println( "TodoListPart@PreDestroy" );
	}
}

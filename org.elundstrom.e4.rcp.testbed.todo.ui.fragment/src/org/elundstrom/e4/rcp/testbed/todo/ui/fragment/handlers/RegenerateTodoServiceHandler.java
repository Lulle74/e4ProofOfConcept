
package org.elundstrom.e4.rcp.testbed.todo.ui.fragment.handlers;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.elundstrom.e4.rcp.testbed.todo.model.ITodoService;

public class RegenerateTodoServiceHandler
{
	@Execute
	public void execute( MApplication app )
	{
		IEclipseContext context = app.getContext();
		ITodoService todoService = ContextInjectionFactory.make( MySpecialTodoServiceImpl.class, context );

		IEclipseContext appCtx = app.getContext();
		appCtx.set( ITodoService.class, todoService );
	}

}
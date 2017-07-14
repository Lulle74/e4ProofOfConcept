package org.elundstrom.e4.rcp.testbed.todo.ui.fragment.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elundstrom.e4.rcp.testbed.todo.model.ITodoService;
import org.elundstrom.e4.rcp.testbed.todo.model.Todo;

public class MySpecialTodoServiceImpl implements ITodoService
{

	private static int current = 1;
	private List<Todo> todos;

	public MySpecialTodoServiceImpl()
	{
		todos = createInitialModel();
	}

	// always return a new copy of the data
	@Override
	public List<Todo> getTodos()
	{
		List<Todo> list = new ArrayList<Todo>();
		for ( Todo todo : todos ) {
			list.add( todo.copy() );
		}
		return list;
	}

	// create or update an existing instance of Todo
	@Override
	public synchronized boolean saveTodo( Todo newTodo )
	{
		boolean created = false;
		Todo updateTodo = findById( newTodo.getId() );
		if ( updateTodo == null ) {
			created = true;
			updateTodo = new Todo( current++ );
			todos.add( updateTodo );
		}
		updateTodo.setSummary( newTodo.getSummary() );
		updateTodo.setDescription( newTodo.getDescription() );
		updateTodo.setDone( newTodo.isDone() );
		updateTodo.setDueDate( newTodo.getDueDate() );

		// configure the event

		// send out events
//			if (created) {
//				broker.post(MyEventConstants.TOPIC_TODO_NEW,
//						createEventData(MyEventConstants.TOPIC_TODO_NEW, 
//								String.valueOf(updateTodo.getId())));
//			} else {
//				broker.
//				post(MyEventConstants.TOPIC_TODO_UPDATE,
//				 createEventData(MyEventConstants.TOPIC_TODO_UPDATE, 
//						 String.valueOf(updateTodo.getId())));
//			}
		return true;
	}

	@Override
	public Todo getTodo( long id )
	{
		Todo todo = findById( id );

		if ( todo != null ) {
			return todo.copy();
		}
		return null;
	}

	@Override
	public boolean deleteTodo( long id )
	{
		Todo deleteTodo = findById( id );

		if ( deleteTodo != null ) {
			todos.remove( deleteTodo );
			// configure the event
//				broker.
//					post(MyEventConstants.TOPIC_TODO_DELETE,
//					 createEventData(MyEventConstants.TOPIC_TODO_DELETE, 
//							 String.valueOf(deleteTodo.getId())));
			return true;
		}
		return false;
	}

	// Example data, change if you like
	private List<Todo> createInitialModel()
	{
		List<Todo> list = new ArrayList<Todo>();
		list.add( createTodo( "You need to fix this", "It is very important" ) );
		list.add( createTodo( "This also needs seeing to", "Now don't be lazy" ) );
		list.add( createTodo( "A small reminder", "The beer after work is worth the effort" ) );
		return list;
	}

	private Todo createTodo( String summary, String description )
	{
		return new Todo( current++, summary, description, false, new Date() );
	}

	private Todo findById( long id )
	{
		for ( Todo todo : todos ) {
			if ( id == todo.getId() ) {
				return todo;
			}
		}
		return null;
	}

	private Map<String, String> createEventData( String topic, String todoId )
	{
		Map<String, String> map = new HashMap<String, String>();
		// in case the receiver wants to check the topic
		//map.put(MyEventConstants.TOPIC_TODO, topic);
		// which todo has changed
		map.put( Todo.FIELD_ID, todoId );
		return map;
	}
}
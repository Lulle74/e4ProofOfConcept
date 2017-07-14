package org.elundstrom.e4.rcp.testbed.todo.model;

import java.util.List;

public interface ITodoService {
	
	List<Todo> getTodos();

	boolean saveTodo(Todo todo);

	Todo getTodo(long id);

	boolean deleteTodo(long id);

}

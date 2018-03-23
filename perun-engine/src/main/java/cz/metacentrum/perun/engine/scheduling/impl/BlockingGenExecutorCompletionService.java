package cz.metacentrum.perun.engine.scheduling.impl;


import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.BlockingBoundedMap;
import cz.metacentrum.perun.engine.scheduling.BlockingCompletionService;
import cz.metacentrum.perun.engine.scheduling.EngineWorker;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.*;

/**
 * Implementation of BlockingCompletionService<Task> for generating Tasks in Engine.
 * Tasks are managed by separate threads GenPlanner and GenCollector.
 *
 * @see BlockingCompletionService
 * @see GenWorker
 * @see GenWorkerImpl
 * @see cz.metacentrum.perun.engine.runners.GenPlanner
 * @see cz.metacentrum.perun.engine.runners.GenCollector
 *
 * @author David Šarman
 */
public class BlockingGenExecutorCompletionService implements BlockingCompletionService<Task> {

	private final static Logger log = LoggerFactory.getLogger(BlockingGenExecutorCompletionService.class);
	private CompletionService<Task> completionService;
	@Autowired
	@Qualifier("generatingTasks")
	private BlockingBoundedMap<Integer, Task> executingTasks;

	/**
	 * Create new blocking CompletionService for GEN Tasks with specified limit
	 *
	 * @param limit Limit for processing GEN Tasks
	 */
	public BlockingGenExecutorCompletionService(int limit) {
		completionService = new ExecutorCompletionService<Task>(Executors.newFixedThreadPool(limit), new LinkedBlockingQueue<Future<Task>>());
	}

	@Override
	public Future<Task> blockingSubmit(EngineWorker<Task> taskWorker) throws InterruptedException {
		GenWorker genWorker = (GenWorker) taskWorker;
		executingTasks.blockingPut(genWorker.getTaskId(), genWorker.getTask());
		return completionService.submit(genWorker);
	}

	@Override
	public Task blockingTake() throws InterruptedException, TaskExecutionException {
		Future<Task> taskFuture = completionService.take();
		try {
			Task taskResult = taskFuture.get();
			Task removed = executingTasks.remove(taskResult.getId());
			if (removed == null) {
				String errorStr = "Task " + taskResult + " could not be removed from completion services pool " + completionService;
				log.error(errorStr);
				throw new TaskExecutionException(taskResult.getId(), errorStr);
			}
			return taskResult;
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause.getClass().equals(TaskExecutionException.class)) {
				TaskExecutionException castedCause = (TaskExecutionException) cause;
				executingTasks.remove((Integer) castedCause.getId());
				throw castedCause;
			} else {
				String errorMsg = "Unexpected exception occurred during Task execution";
				log.error(errorMsg, e);
				throw new RuntimeException(errorMsg, e);
			}
		}
	}
}
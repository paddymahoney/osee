/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.workitem;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.task.AbstractAtsTaskService;
import org.eclipse.osee.ats.api.task.JaxAtsTask;
import org.eclipse.osee.ats.api.task.NewTaskData;
import org.eclipse.osee.ats.api.task.NewTaskDatas;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.ats.rest.IAtsServer;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;

/**
 * @author Donald G. Dunne
 */
public class AtsTaskService extends AbstractAtsTaskService {

   private final IAtsServer atsServer;

   public AtsTaskService(IAtsServer atsServer) {
      super(atsServer.getServices());
      this.atsServer = atsServer;
   }

   @Override
   public Collection<IAtsTask> createTasks(NewTaskData newTaskData) {
      IAtsChangeSet changes =
         atsServer.getStoreService().createAtsChangeSet(newTaskData.getCommitComment(), AtsCoreUsers.SYSTEM_USER);
      return createTasks(newTaskData, changes);
   }

   @Override
   public Collection<IAtsTask> createTasks(NewTaskData newTaskData, IAtsChangeSet changes) {
      CreateTasksOperation operation = new CreateTasksOperation(newTaskData, atsServer, new XResultData());
      XResultData results = operation.validate();
      operation.run(changes);
      if (results.isErrors()) {
         throw new OseeStateException("Error creating tasks - " + results.toString());
      }
      List<IAtsTask> tasks = new LinkedList<>();
      for (JaxAtsTask task : operation.getTasks()) {
         tasks.add(atsServer.getWorkItemFactory().getTask(atsServer.getArtifact(task.getUuid())));
      }
      return tasks;
   }

   @Override
   public Collection<IAtsTask> createTasks(NewTaskDatas newTaskDatas) {
      CreateTasksOperation operation = new CreateTasksOperation(newTaskDatas, atsServer, new XResultData());
      XResultData results = operation.validate();

      if (results.isErrors()) {
         throw new OseeStateException("Error validating task creation - " + results.toString());
      }
      operation.run();
      if (results.isErrors()) {
         throw new OseeStateException("Error creating tasks - " + results.toString());
      }
      List<IAtsTask> tasks = new LinkedList<>();
      for (JaxAtsTask task : operation.getTasks()) {
         tasks.add(atsServer.getWorkItemFactory().getTask(atsServer.getArtifact(task.getUuid())));
      }
      return tasks;
   }

   @Override
   public Collection<IAtsTask> createTasks(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<Object>> attributes, IAtsChangeSet changes) {
      NewTaskData tasks = atsServer.getTaskService().getNewTaskData(teamWf, titles, assignees, createdDate, createdBy,
         relatedToState, taskWorkDef, attributes, changes.getComment());
      return createTasks(tasks, changes);
   }

}

/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.project.ws;

import org.sonar.api.utils.Paging;
import org.sonar.api.web.UserRole;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.ComponentDto;
import org.sonarqube.ws.client.project.SearchMyProjectsWsRequest;

import java.util.List;

import static org.sonar.api.utils.Paging.forPageIndex;
import static org.sonar.server.project.ws.SearchMyProjectsData.newBuilder;

public class SearchMyProjectsDataLoader {
  private final DbClient dbClient;

  public SearchMyProjectsDataLoader(DbClient dbClient) {
    this.dbClient = dbClient;
  }

  SearchMyProjectsData load(SearchMyProjectsWsRequest request) {
    DbSession dbSession = dbClient.openSession(false);
    try {
      SearchMyProjectsData.Builder data = newBuilder();
      int projectsCount = countProjects(dbSession, request);
      List<ComponentDto> projects = searchProjects(dbSession, request, paging(request, projectsCount));

      data.projects(projects)
        .paging(paging(request, projectsCount));

      return data.build();
    } finally {
      dbClient.closeSession(dbSession);
    }
  }

  private static Paging paging(SearchMyProjectsWsRequest request, int total) {
    return forPageIndex(request.getPage())
      .withPageSize(request.getPageSize())
      .andTotal(total);
  }

  private int countProjects(DbSession dbSession, SearchMyProjectsWsRequest request) {
    return dbClient.permissionDao().countProjectsByUser(dbSession, request.getUserId(), UserRole.ADMIN);
  }

  private List<ComponentDto> searchProjects(DbSession dbSession, SearchMyProjectsWsRequest request, Paging paging) {
    return dbClient.permissionDao().selectProjectsByUser(dbSession, request.getUserId(), UserRole.ADMIN, paging);
  }
}

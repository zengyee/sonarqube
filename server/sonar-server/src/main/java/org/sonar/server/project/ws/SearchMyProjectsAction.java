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

import java.util.List;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.Paging;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.ComponentLinkDto;
import org.sonar.server.user.UserSession;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.WsProjects.SearchMyProjectsWsResponse;
import org.sonarqube.ws.WsProjects.SearchMyProjectsWsResponse.Project;
import org.sonarqube.ws.client.project.SearchMyProjectsWsRequest;

import static org.sonar.server.ws.WsUtils.writeProtobuf;

public class SearchMyProjectsAction implements ProjectsWsAction {
  private final DbClient dbClient;
  private final SearchMyProjectsDataLoader dataLoader;
  private final UserSession userSession;

  public SearchMyProjectsAction(DbClient dbClient, SearchMyProjectsDataLoader dataLoader, UserSession userSession) {
    this.dbClient = dbClient;
    this.dataLoader = dataLoader;
    this.userSession = userSession;
  }

  @Override
  public void define(WebService.NewController context) {
    context.createAction("search_my_projects")
      .setDescription("Return list of projects for which the current user has 'Administer' permission.")
      .setResponseExample(getClass().getResource("search_my_projects-example.json"))
      .setSince("6.0")
      .setInternal(true)
      .addPagingParams(100)
      .setHandler(this);
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    SearchMyProjectsWsResponse searchMyProjectsWsResponse = doHandle(toSearchMyProjectsWsRequest(request));
    writeProtobuf(searchMyProjectsWsResponse, request, response);
  }

  private SearchMyProjectsWsResponse doHandle(SearchMyProjectsWsRequest request) {
    checkAuthenticated();
    DbSession dbSession = dbClient.openSession(false);
    try {
      SearchMyProjectsData data = dataLoader.load(request);
      return buildResponse(data);
    } finally {
      dbClient.closeSession(dbSession);
    }
  }

  private SearchMyProjectsWsRequest toSearchMyProjectsWsRequest(Request request) {
    return new SearchMyProjectsWsRequest()
      .setUserId(userSession.getUserId())
      .setPage(request.mandatoryParamAsInt(WebService.Param.PAGE))
      .setPageSize(request.mandatoryParamAsInt(WebService.Param.PAGE_SIZE));
  }

  private SearchMyProjectsWsResponse buildResponse(SearchMyProjectsData data) {
    SearchMyProjectsWsResponse.Builder response = SearchMyProjectsWsResponse.newBuilder();

    Project.Builder projectBuilder = Project.newBuilder();
    for (ComponentDto project : data.projects()) {
      projectBuilder
        .clear()
        .setId(project.uuid())
        .setKey(project.key())
        .setName(project.name());
      if (project.description() != null) {
        projectBuilder.setDescription(project.description());
      }
      List<ComponentLinkDto> componentLinkDtos = data.projectLinksFor(project.uuid());
      for (ComponentLinkDto componentLinkDto : componentLinkDtos) {
        projectBuilder.addLinksBuilder()
          .setType(componentLinkDto.getType())
          .setName(componentLinkDto.getName())
          .setHref(componentLinkDto.getHref())
          .build();
      }

      response.addProjects(projectBuilder);
    }

    Paging paging = data.paging();
    response.setPaging(
      Common.Paging.newBuilder()
        .setPageIndex(paging.pageIndex())
        .setPageSize(paging.pageSize())
        .setTotal(paging.total()));

    return response.build();
  }

  private void checkAuthenticated() {
    userSession.checkLoggedIn();
  }
}

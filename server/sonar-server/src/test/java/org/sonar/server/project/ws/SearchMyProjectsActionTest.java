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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.System2;
import org.sonar.api.web.UserRole;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.DbTester;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.user.UserDto;
import org.sonar.db.user.UserRoleDto;
import org.sonar.server.exceptions.UnauthorizedException;
import org.sonar.server.tester.UserSessionRule;
import org.sonar.server.ws.WsActionTester;

import javax.annotation.Nullable;

import static org.sonar.db.component.ComponentTesting.*;
import static org.sonar.db.user.UserTesting.newUserDto;
import static org.sonar.test.JsonAssert.assertJson;

public class SearchMyProjectsActionTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public UserSessionRule userSession = UserSessionRule.standalone();
  @Rule
  public DbTester db = DbTester.create(System2.INSTANCE);

  private final static String USER_LOGIN = "TESTER";

  WsActionTester ws;
  DbClient dbClient = db.getDbClient();
  final DbSession dbSession = db.getSession();
  SearchMyProjectsDataLoader dataLoader;
  UserDto userDto;

  SearchMyProjectsAction underTest;

  @Before
  public void setUp() {
    userDto = newUserDto().setLogin(USER_LOGIN);
    insertUser(userDto);
    userSession.login(userDto.getLogin()).setUserId(userDto.getId().intValue());

    dataLoader = new SearchMyProjectsDataLoader(dbClient);
    underTest = new SearchMyProjectsAction(dbClient, dataLoader, userSession);
    ws = new WsActionTester(underTest);
  }

  @Test
  public void should_return_my_projects() {
    ComponentDto jdk7 = insertJdk7();
    insertClang();

    insertUserRole(UserRole.ADMIN, userDto.getId(), jdk7.getId());

    commit();

    String result = ws.newRequest().execute().getInput();
    assertJson(result)
      .isSimilarTo(getClass().getResource("SearchMyProjectsActionTest/search.json"));
  }

  @Test
  public void should_return_only_admin_projects() {
    ComponentDto jdk7 = insertJdk7();
    ComponentDto clang = insertClang();

    insertUserRole(UserRole.ADMIN, userDto.getId(), jdk7.getId());
    insertUserRole(UserRole.ISSUE_ADMIN, userDto.getId(), clang.getId());

    commit();

    String result = ws.newRequest().execute().getInput();
    assertJson(result)
      .isSimilarTo(getClass().getResource("SearchMyProjectsActionTest/search.json"));
  }

  @Test
  public void should_return_only_projects() {
    ComponentDto jdk7 = insertJdk7();
    ComponentDto dev = insertDeveloper();
    ComponentDto view = insertView();

    insertUserRole(UserRole.ADMIN, userDto.getId(), jdk7.getId());
    insertUserRole(UserRole.ADMIN, userDto.getId(), dev.getId());
    insertUserRole(UserRole.ADMIN, userDto.getId(), view.getId());

    commit();

    String result = ws.newRequest().execute().getInput();
    assertJson(result)
      .isSimilarTo(getClass().getResource("SearchMyProjectsActionTest/search.json"));
  }

  @Test
  public void should_return_empty_list() {
    String result = ws.newRequest().execute().getInput();
    assertJson(result)
      .isSimilarTo(getClass().getResource("SearchMyProjectsActionTest/empty.json"));
  }

  @Test
  public void should_fail_for_anonymous() {
    userSession.anonymous();
    expectedException.expect(UnauthorizedException.class);
    ws.newRequest().execute();
  }

  private ComponentDto insertClang() {
    return insertComponent(newProjectDto("project-uuid-2")
      .setName("Clang")
      .setKey("clang")
      .setUuid("ce4c03d6-430f-40a9-b777-ad877c00aa4d"));
  }

  private ComponentDto insertJdk7() {
    return insertComponent(newProjectDto("project-uuid-1")
      .setName("JDK 7")
      .setKey("net.java.openjdk:jdk7")
      .setUuid("0bd7b1e7-91d6-439e-a607-4a3a9aad3c6a"))
      .setDescription("JDK");
  }

  private ComponentDto insertView() {
    return insertComponent(newView()
      .setUuid("752d8bfd-420c-4a83-a4e5-8ab19b13c8fc")
      .setName("Java")
      .setKey("Java"));
  }

  private ComponentDto insertDeveloper() {
    return insertComponent(newDeveloper("Joda")
      .setUuid("4e607bf9-7ed0-484a-946d-d58ba7dab2fb")
      .setKey("joda"));
  }

  private ComponentDto insertComponent(ComponentDto component) {
    dbClient.componentDao().insert(dbSession, component.setEnabled(true));
    return dbClient.componentDao().selectOrFailByUuid(dbSession, component.uuid());
  }

  private UserDto insertUser(UserDto user) {
    return dbClient.userDao().insert(dbSession, user.setActive(true));
  }

  private void insertUserRole(String permission, long userId, @Nullable Long resourceId) {
    dbClient.roleDao().insertUserRole(dbSession, new UserRoleDto()
      .setRole(permission)
      .setUserId(userId)
      .setResourceId(resourceId));
  }

  private void commit() {
    dbSession.commit();
  }
}
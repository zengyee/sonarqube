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

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import org.sonar.api.utils.Paging;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.ComponentLinkDto;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;

class SearchMyProjectsData {
  private final List<ComponentDto> projects;
  private final ListMultimap<String, ComponentLinkDto> projectLinksByProjectUuid;
  private final Paging paging;

  private SearchMyProjectsData(Builder builder) {
    this.projects = copyOf(builder.projects);
    this.paging = builder.paging;
    ImmutableListMultimap.Builder<String, ComponentLinkDto> projectLinksBuilder = ImmutableListMultimap.builder();
    for (ComponentLinkDto projectLink : builder.projectLinks) {
      projectLinksBuilder.put(projectLink.getComponentUuid(), projectLink);
    }
    this.projectLinksByProjectUuid = projectLinksBuilder.build();
  }

  static Builder newBuilder() {
    return new Builder();
  }

  List<ComponentDto> projects() {
    return projects;
  }

  List<ComponentLinkDto> projectLinksFor(String projectUuid) {
    return projectLinksByProjectUuid.get(projectUuid);
  }

  Paging paging() {
    return paging;
  }

  static class Builder {
    private List<ComponentDto> projects;
    private List<ComponentLinkDto> projectLinks;
    private Paging paging;

    private Builder() {
      // prevents instantiation outside main class
    }

    Builder setProjects(List<ComponentDto> projects) {
      this.projects = projects;
      return this;
    }

    public Builder setProjectLinks(List<ComponentLinkDto> projectLinks) {
      this.projectLinks = projectLinks;
      return this;
    }

    Builder setPaging(Paging paging) {
      this.paging = paging;
      return this;
    }

    SearchMyProjectsData build() {
      checkState(projects != null);

      return new SearchMyProjectsData(this);
    }
  }
}

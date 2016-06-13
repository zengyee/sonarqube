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
import React from 'react';
import Header from './Header';
import List from './List';
import Search from './Search';
import ApplyTemplateView from '../views/ApplyTemplateView';
import ListFooter from '../../../components/controls/ListFooter';
import { TooltipsContainer } from '../../../components/mixins/tooltips-mixin';
import {
    getProjectPermissions,
    getPermissionTemplates
} from '../../../api/permissions';
import '../../permission-templates/styles.css';
import {
    sortPermissions,
    mergePermissions
} from '../../permission-templates/utils';

export default class App extends React.Component {
  static propTypes = {
    component: React.PropTypes.object,
    rootQualifiers: React.PropTypes.arrayOf(React.PropTypes.string).isRequired
  };

  state = {
    ready: false,
    permissions: [],
    projects: [],
    total: 0,
    filter: '__ALL__'
  };

  componentWillMount () {
    this.search = this.search.bind(this);
    this.refresh = this.refresh.bind(this);
    this.loadMore = this.loadMore.bind(this);
    this.handleFilter = this.handleFilter.bind(this);
    this.bulkApplyTemplate = this.bulkApplyTemplate.bind(this);
  }

  componentDidMount () {
    this.mounted = true;
    this.requestPermissions();
  }

  componentWillUnmount () {
    this.mounted = false;
  }

  requestPermissions (page = 1, query = '', filter = this.state.filter) {
    this.setState({ ready: false });

    let data = { p: page, q: query };
    if (filter !== '__ALL__') {
      data.qualifier = filter;
    }
    if (this.props.component) {
      data = { projectId: this.props.component.id };
    }

    Promise.all([
      getProjectPermissions(data),
      getPermissionTemplates()
    ]).then(responses => {
      if (this.mounted) {
        const [r1, r2] = responses;
        const permissions = sortPermissions(r1.permissions);
        let projects = mergePermissions(r1.projects, permissions);
        if (page > 1) {
          projects = [].concat(this.state.projects, projects);
        }
        this.setState({
          projects,
          permissions,
          query,
          filter,
          total: r1.paging.total,
          page: r1.paging.pageIndex,
          permissionTemplates: r2.permissionTemplates,
          ready: true
        });
      }
    });
  }

  loadMore () {
    this.requestPermissions(this.state.page + 1, this.state.query);
  }

  search (query) {
    this.requestPermissions(1, query);
  }

  handleFilter (filter) {
    this.requestPermissions(1, this.state.query, filter);
  }

  refresh () {
    this.requestPermissions(1, this.state.query);
  }

  bulkApplyTemplate () {
    new ApplyTemplateView({
      query: this.state.query,
      filter: this.state.filter,
      permissionTemplates: this.state.permissionTemplates,
      refresh: () => this.requestPermissions(1, this.state.query,
          this.state.filter)
    }).render();
  }

  render () {
    const singleProject = this.props.component != null;

    return (
        <TooltipsContainer>
          <div className="page page-limited">
            <Header
                singleProject={singleProject}
                ready={this.state.ready}
                refresh={this.refresh}
                onBulkApplyTemplate={this.bulkApplyTemplate}/>

            {!singleProject && (
                <Search
                    rootQualifiers={this.props.rootQualifiers}
                    filter={this.state.filter}
                    onSearch={this.search}
                    onFilter={this.handleFilter}/>
            )}

            <List
                singleProject={singleProject}
                ready={this.state.ready}
                projects={this.state.projects}
                permissions={this.state.permissions}
                permissionTemplates={this.state.permissionTemplates}
                refresh={this.refresh}/>

            {!singleProject && (
                <ListFooter
                    ready={this.state.ready}
                    count={this.state.projects.length}
                    total={this.state.total}
                    loadMore={this.loadMore}/>
            )}
          </div>
        </TooltipsContainer>
    );
  }
}

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
import PermissionCell from '../../permission-templates/components/PermissionCell';
import UsersView from '../views/UsersView';
import GroupsView from '../views/GroupsView';
import ApplyTemplateView from '../views/ApplyTemplateView';
import { getComponentUrl } from '../../../helpers/urls';
import QualifierIcon from '../../../components/shared/qualifier-icon';

export default class ListItem extends React.Component {
  static propTypes = {
    project: React.PropTypes.object.isRequired,
    permissionTemplates: React.PropTypes.arrayOf(
        React.PropTypes.object
    ).isRequired,
    refresh: React.PropTypes.func.isRequired,
    displayFirstColumn: React.PropTypes.bool
  };

  static defaultProps = {
    displayFirstColumn: true
  };

  componentWillMount () {
    this.handleShowUsers = this.handleShowUsers.bind(this);
    this.handleShowGroups = this.handleShowGroups.bind(this);
    this.applyTemplate = this.applyTemplate.bind(this);
  }

  handleShowGroups (permission) {
    new GroupsView({
      permission: permission.key,
      project: this.props.project.id,
      projectName: this.props.project.name,
      refresh: this.props.refresh
    }).render();
  }

  handleShowUsers (permission) {
    new UsersView({
      permission: permission.key,
      project: this.props.project.id,
      projectName: this.props.project.name,
      refresh: this.props.refresh
    }).render();
  }

  applyTemplate (e) {
    e.preventDefault();
    new ApplyTemplateView({
      permissionTemplates: this.props.permissionTemplates,
      project: this.props.project,
      refresh: this.props.refresh
    }).render();
  }

  render () {
    const permissions = this.props.project.permissions.map(p => (
        <PermissionCell
            key={p.key}
            permission={p}
            onShowUsers={this.handleShowUsers}
            onShowGroups={this.handleShowGroups}/>
    ));

    return (
        <tr data-key={this.props.project.key}>
          {this.props.displayFirstColumn && (
              <td className="js-project">
                <span className="little-spacer-right">
                  <QualifierIcon qualifier={this.props.project.qualifier}/>
                </span>
                <a href={getComponentUrl(this.props.project.key)}>
                  {this.props.project.name}
                </a>
              </td>
          )}

          {permissions}

          <td className="thin nowrap text-right">
            <button onClick={this.applyTemplate} className="js-apply-template">
              Apply Template
            </button>
          </td>
        </tr>
    );
  }
}

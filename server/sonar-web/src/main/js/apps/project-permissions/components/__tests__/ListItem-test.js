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
import { expect } from 'chai';
import { shallow } from 'enzyme';
import sinon from 'sinon';
import React from 'react';
import ListItem from '../ListItem';
import PermissionCell from '../../../permission-templates/components/PermissionCell';

const SAMPLE_PROJECT = {
  key: 'key',
  name: 'name',
  qualifier: 'TRK',
  permissions: [
    {
      key: 'user',
      name: 'user',
      description: '',
      usersCount: 0,
      groupsCount: 0
    },
    {
      key: 'admin',
      name: 'admin',
      description: '',
      usersCount: 0,
      groupsCount: 0
    }
  ]
};

describe('Project Permissions :: ListItem', () => {
  it('should render project', () => {
    const output = shallow(
        <ListItem
            project={SAMPLE_PROJECT}
            permissionTemplates={[]}
            refresh={sinon.stub().throws()}/>
    );
    expect(output.find('.js-project')).to.have.length(1);
  });

  it('should render permission cells', () => {
    const output = shallow(
        <ListItem
            project={SAMPLE_PROJECT}
            permissionTemplates={[]}
            refresh={sinon.stub().throws()}/>
    );
    expect(output.find(PermissionCell)).to.have.length(2);
  });

  it('should not render project', () => {
    const output = shallow(
        <ListItem
            project={SAMPLE_PROJECT}
            permissionTemplates={[]}
            refresh={sinon.stub().throws()}
            displayFirstColumn={false}/>
    );
    expect(output.find('.js-project')).to.have.length(0);
  });
});

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
import List from '../List';
import ListHeader from '../../../permission-templates/components/ListHeader';
import ListItem from '../ListItem';

describe('Project Permissions :: List', () => {
  it('should render ListHeader', () => {
    const output = shallow(
        <List
            singleProject={false}
            projects={[]}
            permissions={[]}
            permissionTemplates={[]}
            refresh={() => sinon.stub().throws()}/>
    );
    expect(output.find(ListHeader)).to.have.length(1);
  });

  it('should render ListItem', () => {
    const projects = [{ key: 'key1' }, { key: 'key2' }];
    const output = shallow(
        <List
            singleProject={false}
            projects={projects}
            permissions={[]}
            permissionTemplates={[]}
            refresh={() => sinon.stub().throws()}/>
    );
    expect(output.find(ListItem)).to.have.length(2);
  });
});

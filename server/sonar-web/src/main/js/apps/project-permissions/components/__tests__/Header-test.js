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
import Header from '../Header';

describe('Project Permissions :: Header', () => {
  it('should render', () => {
    const output = shallow(
        <Header
            ready={true}
            singleProject={false}
            refresh={() => true}
            onBulkApplyTemplate={() => true}/>
    );
    expect(output.find('.page-title')).to.have.length(1);
    expect(output.find('.page-description')).to.have.length(1);
    expect(output.find('.spinner')).to.have.length(0);
    expect(output.find('.js-bulk-apply-template')).to.have.length(1);
  });

  it('should render spinner', () => {
    const output = shallow(
        <Header
            ready={false}
            singleProject={false}
            refresh={() => true}
            onBulkApplyTemplate={() => true}/>
    );
    expect(output.find('.spinner')).to.have.length(1);
  });

  it('should not render "Bulk Apply Template" button', () => {
    const output = shallow(
        <Header
            ready={true}
            singleProject={true}
            refresh={() => true}
            onBulkApplyTemplate={() => true}/>
    );
    expect(output.find('.js-bulk-apply-template')).to.have.length(0);
  });

  it('should bulk apply template', () => {
    const onBulkApplyTemplate = sinon.spy();
    const output = shallow(
        <Header
            ready={true}
            singleProject={false}
            refresh={() => true}
            onBulkApplyTemplate={onBulkApplyTemplate}/>
    );

    output.find('.js-bulk-apply-template')
        .simulate('click', { preventDefault () {} });
    expect(onBulkApplyTemplate.called).to.equal(true);
  });
});

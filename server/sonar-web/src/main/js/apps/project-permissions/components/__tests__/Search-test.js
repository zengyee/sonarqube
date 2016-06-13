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
import Search from '../Search';
import QualifierFilter from '../QualifierFilter';

const ROOT_QUALIFIERS = ['TRK', 'VW'];

describe('Project Permissions :: Search', () => {
  it('should render QualifierFilter', () => {
    const onFilter = sinon.stub().throws();
    const output = shallow(
        <Search
            rootQualifiers={ROOT_QUALIFIERS}
            filter="TRK"
            onSearch={sinon.stub().throws()}
            onFilter={onFilter}/>
    ).find(QualifierFilter);
    expect(output).to.have.length(1);
    expect(output.prop('filter')).to.equal('TRK');
    expect(output.prop('onFilter')).to.equal(onFilter);
    expect(output.prop('rootQualifiers')).to.equal(ROOT_QUALIFIERS);
  });

  it('should not render QualifierFilter', () => {
    const onFilter = sinon.stub().throws();
    const output = shallow(
        <Search
            rootQualifiers={['VW']}
            filter="TRK"
            onSearch={sinon.stub().throws()}
            onFilter={onFilter}/>
    ).find(QualifierFilter);
    expect(output).to.have.length(0);
  });
});

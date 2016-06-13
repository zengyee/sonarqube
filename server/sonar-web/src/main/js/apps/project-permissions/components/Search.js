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
import debounce from 'lodash/debounce';
import QualifierFilter from './QualifierFilter';
import { translate } from '../../../helpers/l10n';

export default class Search extends React.Component {
  static propTypes = {
    rootQualifiers: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
    filter: React.PropTypes.string.isRequired,
    onSearch: React.PropTypes.func.isRequired,
    onFilter: React.PropTypes.func.isRequired
  };

  componentWillMount () {
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleSearch = debounce(this.handleSearch.bind(this), 250);
  }

  handleSubmit (e) {
    e.preventDefault();
    this.handleSearch();
  }

  handleSearch () {
    const q = this.refs.input.value;
    this.props.onSearch(q);
  }

  render () {
    if (this.props.componentId) {
      return null;
    }
    return (
        <div className="spacer-bottom">
          {this.props.rootQualifiers.length > 1 && (
              <QualifierFilter
                  filter={this.props.filter}
                  rootQualifiers={this.props.rootQualifiers}
                  onFilter={this.props.onFilter}/>
          )}

          <form className="search-box display-inline-block text-top"
                onSubmit={this.handleSubmit}>
            <button className="search-box-submit button-clean">
              <i className="icon-search"/>
            </button>

            <input
                ref="input"
                className="search-box-input"
                type="search"
                placeholder={translate('search_verb')}
                onChange={this.handleSearch}/>
          </form>
        </div>
    );
  }
}

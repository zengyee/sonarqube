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
package org.sonar.server.plugins.ws;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.core.platform.PluginInfo;
import org.sonar.server.plugins.PluginDownloader;
import org.sonar.server.plugins.ServerPluginRepository;
import org.sonar.server.plugins.UpdateCenterMatrixFactory;
import org.sonar.updatecenter.common.Plugin;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.io.Resources.getResource;
import static org.sonar.server.plugins.ws.PluginWSCommons.compatiblePluginsByKey;

/**
 * Implementation of the {@code pending} action for the Plugins WebService.
 */
public class PendingAction implements PluginsWsAction {

  private static final String ARRAY_INSTALLING = "installing";
  private static final String ARRAY_REMOVING = "removing";
  private static final String ARRAY_UPDATING = "updating";

  private final PluginDownloader pluginDownloader;
  private final ServerPluginRepository installer;
  private final PluginWSCommons pluginWSCommons;
  private final UpdateCenterMatrixFactory updateCenterMatrixFactory;

  public PendingAction(PluginDownloader pluginDownloader,
    ServerPluginRepository installer,
    PluginWSCommons pluginWSCommons, UpdateCenterMatrixFactory updateCenterMatrixFactory) {
    this.pluginDownloader = pluginDownloader;
    this.installer = installer;
    this.pluginWSCommons = pluginWSCommons;
    this.updateCenterMatrixFactory = updateCenterMatrixFactory;
  }

  @Override
  public void define(WebService.NewController controller) {
    controller.createAction("pending")
      .setDescription("Get the list of plugins which will either be installed or removed at the next startup of the SonarQube instance, sorted by plugin name")
      .setSince("5.2")
      .setHandler(this)
      .setResponseExample(getResource(this.getClass(), "example-pending_plugins.json"));
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    ImmutableMap<String, Plugin> compatiblePluginsByKey = compatiblePluginsByKey(updateCenterMatrixFactory);

    JsonWriter jsonWriter = response.newJsonWriter();

    jsonWriter.beginObject();
    writePlugins(jsonWriter, compatiblePluginsByKey);
    jsonWriter.endObject();
    jsonWriter.close();
  }

  private void writePlugins(JsonWriter json, Map<String, Plugin> compatiblePluginsByKey) {
    Collection<PluginInfo> uninstalledPlugins = installer.getUninstalledPlugins();
    Collection<PluginInfo> downloadedPlugins = pluginDownloader.getDownloadedPlugins();
    Collection<PluginInfo> installedPlugins = installer.getPluginInfos();
    MatchPluginKeys matchPluginKeys = new MatchPluginKeys(from(installedPlugins).transform(PluginInfoToKey.INSTANCE).toSet());

    Collection<PluginInfo> newPlugins = new ArrayList<>();
    Collection<PluginInfo> updatedPlugins = new ArrayList<>();
    for (PluginInfo pluginInfo : downloadedPlugins) {
      if (matchPluginKeys.apply(pluginInfo)) {
        updatedPlugins.add(pluginInfo);
      } else {
        newPlugins.add(pluginInfo);
      }
    }

    pluginWSCommons.writePluginInfoList(json, newPlugins, compatiblePluginsByKey, ARRAY_INSTALLING);
    pluginWSCommons.writePluginInfoList(json, updatedPlugins, compatiblePluginsByKey, ARRAY_UPDATING);
    pluginWSCommons.writePluginInfoList(json, uninstalledPlugins, compatiblePluginsByKey, ARRAY_REMOVING);
  }

  private enum PluginInfoToKey implements Function<PluginInfo, String> {
    INSTANCE;

    @Override
    public String apply(@Nonnull PluginInfo input) {
      return input.getKey();
    }
  }

  private static class MatchPluginKeys implements Predicate<PluginInfo> {
    private final Set<String> pluginKeys;

    private MatchPluginKeys(Collection<String> pluginKeys) {
      this.pluginKeys = copyOf(pluginKeys);
    }

    @Override
    public boolean apply(@Nonnull PluginInfo input) {
      return pluginKeys.contains(input.getKey());
    }
  }
}

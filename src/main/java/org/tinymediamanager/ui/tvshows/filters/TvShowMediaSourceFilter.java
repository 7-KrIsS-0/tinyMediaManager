/*
 * Copyright 2012 - 2015 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a media source filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaSourceFilter extends AbstractTvShowUIFilter {
  private JComboBox<MediaSource> combobox;

  @Override
  public String getId() {
    return "tvShowMediaSource";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return ((MediaSource) combobox.getSelectedItem()).name();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof MediaSource) {
      combobox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      MediaSource mediaSource = MediaSource.valueOf((String) value);
      if (mediaSource != null) {
        combobox.setSelectedItem(mediaSource);
      }
    }
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    // search for media source in episodes
    for (TvShowEpisode episode : episodes) {
      if (episode.getMediaSource() == combobox.getSelectedItem()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    combobox = new JComboBox<>(MediaSource.values());
    return combobox;
  }
}
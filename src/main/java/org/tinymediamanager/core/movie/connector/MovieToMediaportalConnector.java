/*
 *
 *  * Copyright 2012 - 2016 Manuel Laggner
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.tinymediamanager.core.movie.connector;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.w3c.dom.Element;

/**
 * the class MovieToMediaportalConnector is used to write a classic Mediaportal 1.x compatible NFO file
 */
public class MovieToMediaportalConnector extends MovieGenericXmlConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieToMediaportalConnector.class);

  public MovieToMediaportalConnector(Movie movie) {
    super(movie);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void addOwnTags() {
  }

  /**
   * the media portal fanart style<br />
   * <fanart><thumb>xxx</thumb></fanart>
   */
  @Override
  protected void addFanart() {
    Element fanart = document.createElement("fanart");

    String fanarUrl = movie.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isNotBlank(fanarUrl)) {
      Element thumb = document.createElement("thumb");
      thumb.setTextContent(fanarUrl);
      fanart.appendChild(thumb);
    }

    root.appendChild(fanart);
  }

  /**
   * the mpaa style in <mpaa>xxx</mpaa>
   */
  @Override
  protected void addMpaa() {
    Element mpaa = document.createElement("certification");
    if (movie.getCertification() != null) {
      mpaa.setTextContent(CertificationStyle.formatCertification(movie.getCertification(), MovieModuleManager.SETTINGS.getCertificationStyle()));
    }
    root.appendChild(mpaa);
  }

  /**
   * no certification tag
   */
  @Override
  protected void addCertification() {
  }

  /**
   * genres are nested in a genre tag<br />
   * <genres><genre>xxx</genre></genres>
   */
  @Override
  protected void addGenres() {
    Element genres = document.createElement("genres");

    for (MediaGenres mediaGenre : movie.getGenres()) {
      Element genre = document.createElement("genre");
      genre.setTextContent(mediaGenre.getName());
      genres.appendChild(genre);
    }

    root.appendChild(genres);
  }

  /**
   * studios are concatenated in a single <studio>xxx</studio> tag
   */
  @Override
  protected void addStudios() {
    Element studio = document.createElement("studio");
    studio.setTextContent(movie.getProductionCompany());
    root.appendChild(studio);
  }

  /**
   * credits are concatenated in a single <credits>xxx</credits> tag
   */
  @Override
  protected void addCredits() {
    Element credits = document.createElement("credits");
    credits.setTextContent(movie.getWriter());
    root.appendChild(credits);
  }

  /**
   * directors are concatenated in a single <director>xxx</director> tag
   */
  @Override
  protected void addDirectors() {
    Element director = document.createElement("director");
    director.setTextContent(movie.getDirector());
    root.appendChild(director);
  }
}
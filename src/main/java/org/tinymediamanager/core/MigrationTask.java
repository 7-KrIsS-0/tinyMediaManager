/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.ui.MainWindow;

/**
 * UpdaterTasks checks if there's a new update for TMM
 * 
 * @author Myron BOyle
 */
public class MigrationTask extends SwingWorker<Boolean, Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MigrationTask.class);

  /**
   * Instantiates a new updater task.
   */
  public MigrationTask() {
  }

  @Override
  public Boolean doInBackground() {
    if (ReleaseInfo.isGitBuild()) {
      return false;
    }

    try {
      // check, if v3 already there
      String gdUrl = getV3UrlFromGD();
      Url u = new Url(gdUrl);
      InputStream is = u.getInputStream(true);
      if (is != null && !u.isFault()) {
        return true;
      }
    }
    catch (IOException e) {
      LOGGER.warn("Could not check for V3: {}", e.getMessage());
    }
    catch (InterruptedException e) {
      LOGGER.warn("Could not check for V3: {}", e.getMessage());
    }

    return false;
  }

  /**
   * Starts the v2-to-v3 migration<br>
   * Abort on any exception
   * 
   * @throws IOException
   *           if we couldn't migrate all the needed files - you need to restart TMM
   */
  public void migrateToV3() throws IOException {

    // backup datasources to plain text file for easier xml->JSON migration
    try {
      LOGGER.info("MIG: backing up datasources");
      Path ds = Paths.get("cache", "migv3movies.ds");
      Files.write(ds, Settings.getInstance().getMovieSettings().getMovieDataSource(), StandardCharsets.UTF_8);
      ds = Paths.get("cache", "migv3shows.ds");
      Files.write(ds, Settings.getInstance().getTvShowSettings().getTvShowDataSource(), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      LOGGER.warn("MIG: could not backup datasources");
    }

    if (ReleaseInfo.isGitBuild()) {
      throw new IOException("Update cannot be executed inside IDE!");
    }

    // download fresh V3 getdown file (should fail in eclipse on purpose)
    LOGGER.info("MIG: downloading fresh updater file...");
    String gdUrl = getV3UrlFromGD();
    Path gd = Paths.get("getdown.txt");
    Url u = new Url(gdUrl);
    boolean ok = u.download(gd);
    if (!ok) {
      LOGGER.info("MIG: ...failed - write new minimum file");
      Utils.writeStringToFile(gd, "appbase=" + gdUrl);
    }

    LOGGER.info("MIG: close databases");
    TmmModuleManager.getInstance().shutDown();

    LOGGER.info("MIG: removing old settings and databases");
    Utils.moveDirectorySafe(Paths.get("data"), Paths.get("data_old_v2"));

    LOGGER.info("MIG: Starting update...");
    MainWindow.getActiveInstance().closeTmmAndStart(Utils.getPBforTMMupdate());
  }

  private String getV3UrlFromGD() throws IOException {
    String gdUrl = "";
    Path gd = Paths.get("getdown.txt");
    List<String> cont = Files.readAllLines(gd);

    for (String line : cont) {
      String[] kv = line.split("=");
      if ("appbase".equals(kv[0].trim())) {
        gdUrl = kv[1].trim();
      }
    }
    if (!gdUrl.isEmpty()) {
      gdUrl = gdUrl.replace("/v2", "/v3");
      gdUrl += "/getdown.txt";
    }
    else {
      throw new IOException("no appbase found");
    }
    return gdUrl;
  }

}

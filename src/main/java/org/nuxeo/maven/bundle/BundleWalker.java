/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Arnaud Kervern
 */

package org.nuxeo.maven.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.nuxeo.maven.runtime.MojoRuntime;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.nuxeo.runtime.model.impl.ComponentDescriptorReader;

public class BundleWalker {

    private Path basePath;

    private Log log;

    private ComponentDescriptorReader reader;

    public BundleWalker() {
        reader = new ComponentDescriptorReader();
    }

    public BundleWalker(String basePath) {
        this(new File(basePath));
    }

    public BundleWalker(File basePath) {
        this();
        setBasePath(basePath);
    }

    private Path findFile(String filePath) {
        try {
            return Files.walk(basePath).filter(s -> s.endsWith(filePath)).findFirst().orElse(null);
        } catch (IOException e) {
            getLog().debug(e);
            getLog().warn(filePath + ":" + e.getMessage());
            return null;
        }
    }

    public Stream<Path> getComponents() throws IOException {
        Path manifestPath = getManifest();
        if (manifestPath == null) {
            getLog().info(String.format("%s do no contains MANIFEST.MF file", basePath.toAbsolutePath().toString()));
            return Stream.empty();
        }

        Manifest manifest;
        try (FileInputStream fis = new FileInputStream(manifestPath.toFile())) {
            manifest = new Manifest(fis);
        }

        String components = manifest.getMainAttributes().getValue("Nuxeo-Component");
        if (StringUtils.isBlank(components)) {
            return Stream.empty();
        }

        return Arrays.stream(components.split("[, \t\n\r\f]"))
                     .filter(StringUtils::isNotBlank)
                     .map(this::findFile)
                     .filter(Objects::nonNull);
    }

    public Path getManifest() {
        return findFile("META-INF/MANIFEST.MF");
    }

    private Log getLog() {
        if (this.log == null) {
            this.log = new SystemStreamLog();
        }

        return this.log;
    }

    public Stream<RegistrationInfo> getRegistrationInfos() throws IOException {
        return getComponents().map(this::read).filter(Objects::nonNull);
    }

    public RegistrationInfo read(Path component) {
        try (InputStream is = new FileInputStream(component.toFile())) {
            return reader.read(MojoRuntime.instance, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBasePath(File basePath) {
        if (basePath != null) {
            this.basePath = basePath.toPath();
        } else {
            this.basePath = null;
        }
    }
}

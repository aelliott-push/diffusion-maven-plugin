/*
 * Copyright 2014 Push Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pushtechnology.diffusion.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

/**
 * Unit tests for {@link com.pushtechnology.diffusion.maven.plugin.AbstractDiffusionMojo}.
 *
 * @author Philip Aston
 * @author Andy Piper
 */
public class DiffusionMojoTest extends AbstractMojoTestCase {

    private static File testBaseDirectory =
            new File(getBasedir(), "target/mojo-test");

    private File buildDirectory;
    private File simplePom;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testBaseDirectory.mkdirs();
        buildDirectory = File.createTempFile("build", "", testBaseDirectory);
        buildDirectory.delete();
        buildDirectory.mkdirs();

        simplePom = getTestFile("src/test/resources/unit/basic-test/pom.xml");
        assertTrue(simplePom.exists());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private DiffusionStartMojo getStartMojo(final File buildDirectory) throws Exception {
        final DiffusionStartMojo mojo = (DiffusionStartMojo) lookupMojo("start", simplePom);

        setVariableValueToObject(mojo, "logDirectory", buildDirectory.getAbsoluteFile());
        setVariableValueToObject(mojo, "execution",
                new DiffusionExecutionStub(null, "start", "boot"));

        return mojo;
    }

    public void testBasicInvalid() throws Exception {

        final DiffusionStartMojo mojo = getStartMojo(buildDirectory);
        setVariableValueToObject(mojo, "project",
                new DiffusionProjectStub(buildDirectory, simplePom));
        setVariableValueToObject(mojo, "diffusionConfigDir",
                "some missing directory");
        try {
            mojo.execute();
            assertTrue(false);
        }
        catch (MojoExecutionException mee) {
            assertEquals(mee.getMessage(), "Invalid diffusionConfigDir");
        }
    }

    public void testBasicStart() throws Exception {

        final DiffusionStartMojo mojo = getStartMojo(buildDirectory);
        setVariableValueToObject(mojo, "project",
                new DiffusionProjectStub(buildDirectory, simplePom));

        mojo.execute();
        assertTrue(mojo.getServer().isStarted());
        // Clean up
        mojo.stopDiffusion();
    }

    public void testBasicStop() throws Exception {

        final DiffusionStopMojo mojo = (DiffusionStopMojo) lookupMojo("stop", simplePom);
        final DiffusionStartMojo startmojo = getStartMojo(buildDirectory);
        MavenProject project = new DiffusionProjectStub(buildDirectory, simplePom);
        setVariableValueToObject(startmojo, "project", project);
        startmojo.execute();
        // Now stop it
        setVariableValueToObject(mojo, "execution",
                new DiffusionExecutionStub(null, "stop", "shutdown"));

        project.getProperties().put("startedServerInstance", startmojo.server);
        setVariableValueToObject(mojo, "project", project);
        mojo.execute();
        assertTrue(mojo.getServer().isStopped());
    }
}
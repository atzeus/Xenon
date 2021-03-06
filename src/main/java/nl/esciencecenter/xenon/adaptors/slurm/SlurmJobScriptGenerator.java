/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.slurm;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import nl.esciencecenter.xenon.engine.util.CommandLineUtils;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.JobDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Script generated is a Unix script.")
public final class SlurmJobScriptGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmJobScriptGenerator.class);

    private SlurmJobScriptGenerator() {
        //DO NOT USE
    }

    private static String getWorkingDirPath(JobDescription description, RelativePath fsEntryPath) {
        String path;
        if (description.getWorkingDirectory().startsWith("/")) {
            path = description.getWorkingDirectory();
        } else {
            //make relative path absolute
            RelativePath workingDirectory = fsEntryPath.resolve(description.getWorkingDirectory());
            path = workingDirectory.getAbsolutePath();
        }

        return path;
    }
    
    public static String[] generateInteractiveArguments(JobDescription description, RelativePath fsEntryPath, UUID tag) {
        ArrayList<String> arguments = new ArrayList<>();

        //suppress printing of status messages
        arguments.add("--quiet");

        //add a tag so we can find the job back in the queue later
        arguments.add("--comment=" + tag.toString());
        
        //set working directory
        if (description.getWorkingDirectory() != null) {
            String path = getWorkingDirPath(description, fsEntryPath);
            arguments.add("--chdir=" + path);
        }

        if (description.getQueueName() != null) {
            arguments.add("--partition=" + description.getQueueName());
        }

        //number of nodes
        arguments.add("--nodes=" + description.getNodeCount());

        //number of processer per node
        arguments.add("--ntasks-per-node=" +description.getProcessesPerNode());

        //add maximum runtime
        arguments.add("--time=" +description.getMaxTime());

        arguments.add(description.getExecutable());
        arguments.addAll(description.getArguments());
        
        return arguments.toArray(new String[arguments.size()]);
    }

    @SuppressWarnings("PMD.NPathComplexity")
    public static String generate(JobDescription description, RelativePath fsEntryPath) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set name of job to xenon
        script.format("#SBATCH --job-name xenon\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            String path = getWorkingDirPath(description, fsEntryPath);
            script.format("#SBATCH --workdir='%s'\n", path);
        }

        if (description.getQueueName() != null) {
            script.format("#SBATCH --partition=%s\n", description.getQueueName());
        }

        //number of nodes
        script.format("#SBATCH --nodes=%d\n", description.getNodeCount());

        //number of processer per node
        script.format("#SBATCH --ntasks-per-node=%d\n", description.getProcessesPerNode());

        //add maximum runtime
        script.format("#SBATCH --time=%d\n", description.getMaxTime());

        if (description.getStdin() != null) {
            script.format("#SBATCH --input='%s'\n", description.getStdin());
        }

        if (description.getStdout() == null) {
            script.format("#SBATCH --output=/dev/null\n");
        } else {
            script.format("#SBATCH --output='%s'\n", description.getStdout());
        }

        if (description.getStderr() == null) {
            script.format("#SBATCH --error=/dev/null\n");
        } else {
            script.format("#SBATCH --error='%s'\n", description.getStderr());
        }

        for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
            script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
        }

        script.format("\n");

        if (!description.isStartSingleProcess()) {
            //run commands through srun
            script.format("srun ");
        }

        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
        }
        script.format("\n");

        script.close();

        LOGGER.debug("Created job script:\n{} from description {}", stringBuilder, description);

        return stringBuilder.toString();
    }
}

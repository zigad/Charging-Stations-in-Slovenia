package si.deisinger.business.controller;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@ApplicationScoped
public class GitController {
    private static final Logger LOG = LoggerFactory.getLogger(GitController.class);

    @ConfigProperty(name = "is.push.enabled")
    private boolean isPushEnabled;

    @ConfigProperty(name = "git.username")
    private String gitUsername;

    @ConfigProperty(name = "git.password")
    private String gitPassword;

    /**
     * Commits the current information about the charging stations for the given provider.
     *
     * @param provider
     *         the provider
     * @param timeStamp
     *         the time stamp
     *
     * @return the commit URL, or `null` if push is disabled via configuration
     */
    public String gitCommit(Providers provider, String timeStamp) {
        Config config = new Config();
        config.unset("gpg", null, "format");
        String commitUrl = null;
        try (Git git = Git.open(new File(""))) {
            LOG.info("Adding files to commit");
            git.add().addFilepattern("currentInfoPerProvider.json").addFilepattern("Stations/" + provider.getProviderName() + "/" + provider.getProviderName() + "_" + timeStamp + ".json").call();
            LOG.info("Committing file to git");
            git.commit().setMessage("Updated List Of Charging Stations for " + provider.getProviderName()).setGpgConfig(new GpgConfig(config)).call();
            if (isPushEnabled) {
                LOG.info("Pushing to origin");
                git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword)).call();
                commitUrl = getCommitUrl(git);
            } else {
                LOG.info("Pushing to origin disabled via configuration, skipping this step");
            }
        } catch (IOException | GitAPIException e) {
            LOG.error("Error while committing and pushing changes to Git repository", e);
        }
        LOG.info("Commit successful");
        return commitUrl;
    }

    /**
     * Returns the commit URL from the given Git instance.
     *
     * @param git
     *         the Git instance
     *
     * @return the commit URL
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public String getCommitUrl(Git git) throws IOException {
        // Get the commit hash using git.getRepository().resolve()
        ObjectId commitHash = git.getRepository().resolve("HEAD");
        // Use the git show command to get the commit URL
        String command = "git show --pretty=format:%H -s " + commitHash.getName();
        String[] commands = {"sh", "-c", command};
        Process process = new ProcessBuilder(commands).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        return "https://github.com/zigad/Charging-Stations-in-Slovenia/commit/" + line;
    }
}

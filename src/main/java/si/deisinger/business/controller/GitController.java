package si.deisinger.business.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.configuration.ConfigUtils;
import si.deisinger.providers.enums.Providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitController {
	private static final Logger LOG = LoggerFactory.getLogger(GitController.class);

	public String gitCommit(Providers provider, String timeStamp) {
		Config config = new Config();
		config.unset("gpg", null, "format");
		String commitUrl = null;
		try (Git git = Git.open(new File(""))) {
			LOG.info("Adding files to commit");
			git.add().addFilepattern("currentInfoPerProvider.json").addFilepattern(provider.getProviderName() + "/" + provider.getProviderName() + "_" + timeStamp + ".json").call();
			LOG.info("Committing file to git");
			git.commit().setMessage("Updated List Of Charging Stations for " + provider.getProviderName()).setGpgConfig(new GpgConfig(config)).call();
			LOG.info("Pushing to origin");
			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(ConfigUtils.getJgitUsername(), ConfigUtils.getJgitPassword())).call();
			// Get the commit hash using git.getRepository().resolve()
			ObjectId commitHash = git.getRepository().resolve("HEAD");
			// Use the git show command to get the commit URL
			String command = "git show --pretty=format:%H -s " + commitHash.getName();
			String[] commands = { "sh", "-c", command };
			Process process = new ProcessBuilder(commands).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = reader.readLine();
			commitUrl = "https://github.com/zigad/Charging-Stations-in-Slovenia/commit/" + line;

		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		LOG.info("Commit successful");
		return commitUrl;
	}
}

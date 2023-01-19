package si.deisinger.business.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import java.io.File;
import java.io.IOException;

public class GitController {
	private static final Logger LOG = LoggerFactory.getLogger(GitController.class);

	public static void gitCommit(Providers provider, String timeStamp) {
		Config config = new Config();
		config.unset("gpg", null, "format");
		try (Git git = Git.open(new File(""))) {
			LOG.info("Adding files to commit");
			git.add().addFilepattern("currentInfoPerProvider.json").addFilepattern(provider.getProviderName() + "/" + provider.getProviderName() + "_" + timeStamp + ".json").call();
			LOG.info("Committing file to git");
			git.commit().setMessage("Updated List Of Charging Stations for " + provider.getProviderName()).setGpgConfig(new GpgConfig(config)).call();
			LOG.info("Pushing to origin");
			git.push().call();
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		LOG.info("Commit successful");
	}
}

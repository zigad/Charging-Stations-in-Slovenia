package si.deisinger.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public interface CheckForNewStationsInterface {

	void checkGremoNaElektriko() throws IOException, GitAPIException;

	void checkPetrol() throws IOException;
}

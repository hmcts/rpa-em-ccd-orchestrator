package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class LocalConfigurationLoaderTest {
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(new ObjectMapper(new YAMLFactory()));

    @Test
    public void load() {
        BundleConfiguration config = loader.load("example.yaml");

        assertEquals(config.title, "New bundle");
        assertEquals(config.hasTableOfContents, true);
        assertEquals(config.hasCoversheets, true);
        assertEquals(config.hasFolderCoversheets, false);
        assertEquals(config.folders.get(0).name, "Folder 1");
        assertEquals(config.folders.get(0).folders.get(0).name, "Folder 1.a");
        assertEquals(config.folders.get(0).folders.get(1).name, "Folder 1.b");
        assertEquals(config.folders.get(1).name, "Folder 2");
        assertEquals(config.filename, "stitched.pdf");
    }

    @Test(expected = BundleConfigurationException.class)
    public void loadMissingConfig() {
        loader.load("does-not-exist.yaml");
    }

    @Test
    public void filename() {
        BundleConfiguration config = loader.load("example-with-filename.yaml");

        assertEquals(config.title, "Bundle with filename");
        assertEquals(config.filename, "bundle.pdf");
        assertEquals(config.hasTableOfContents, false);
        assertEquals(config.hasCoversheets, false);
        assertEquals(config.hasFolderCoversheets, true);
    }

    @Test(expected = BundleConfigurationException.class)
    public void fileContainsIncorrectFieldname() {
        loader.load("example-incorrect-key.yaml");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fileContainsIncorrectValueForField() {
        thrown.expect(BundleConfigurationException.class);
        thrown.expectMessage("Invalid configuration file entry in: example-incorrect-value-for-key.yaml"
                + "; Configuration file parameter(s) and/or parameter value(s)");
        loader.load("example-incorrect-value-for-key.yaml");
    }

    @Test
    public void documentSet() {
        BundleConfiguration config = loader.load("example-with-documents.yaml");

        assertEquals(2, config.documents.size());
        assertEquals(0, config.folders.get(0).documents.size());
        assertEquals(2, config.folders.get(1).documents.size());
    }

    @Test
    public void enableEmailNotificationNull() {
        BundleConfiguration config = loader.load("example-with-documents.yaml");

        assertEquals(null, config.enableEmailNotification);
    }
}

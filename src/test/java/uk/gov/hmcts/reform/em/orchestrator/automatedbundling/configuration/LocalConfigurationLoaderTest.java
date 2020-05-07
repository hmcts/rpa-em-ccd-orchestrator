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

        assertEquals("New bundle", config.title);
        assertEquals(true, config.hasTableOfContents);
        assertEquals(true, config.hasCoversheets);
        assertEquals(false, config.hasFolderCoversheets);
        assertEquals("Folder 1", config.folders.get(0).name);
        assertEquals("Folder 1.a", config.folders.get(0).folders.get(0).name);
        assertEquals("Folder 1.b", config.folders.get(0).folders.get(1).name);
        assertEquals("Folder 2", config.folders.get(1).name);
        assertEquals("stitched.pdf",config.filename);
    }

    @Test(expected = BundleConfigurationException.class)
    public void loadMissingConfig() {
        loader.load("does-not-exist.yaml");
    }

    @Test
    public void filename() {
        BundleConfiguration config = loader.load("example-with-filename.yaml");

        assertEquals("Bundle with filename", config.title);
        assertEquals("bundle.pdf", config.filename);
        assertEquals(false, config.hasTableOfContents);
        assertEquals(false, config.hasCoversheets);
        assertEquals(true, config.hasFolderCoversheets);
    }

    @Test
    public void bundleWithNoFileName() {
        BundleConfiguration config = loader.load("example.yaml");

        assertEquals("New bundle", config.title);
        assertEquals("stitched.pdf", config.filename);
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

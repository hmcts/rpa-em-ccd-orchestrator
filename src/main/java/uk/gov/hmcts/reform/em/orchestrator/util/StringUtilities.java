package uk.gov.hmcts.reform.em.orchestrator.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


public class StringUtilities {

    private StringUtilities() {
    }

    public static String ensurePdfExtension(String fileName) {
        if (StringUtils.isNoneBlank(fileName)) {
            if (!FilenameUtils.getExtension(fileName).isEmpty()) {
                return fileName;
            } else {
                return fileName.concat(".pdf");
            }
        } else {
            return "stitched.pdf";
        }

    }
}

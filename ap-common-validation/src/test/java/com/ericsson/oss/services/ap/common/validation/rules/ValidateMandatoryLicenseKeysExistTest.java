/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.rules;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseAccessException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseValidationException;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.ValidationFailure;

/**
 * Unit tests for {@link ValidateMandatoryLicenseKeysExist}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateMandatoryLicenseKeysExistTest {

    private static final String AUTOMATIC_LICENSE_REQUEST_OPTION = "automaticLicenseRequest";
    private static final String AUTOMATIC_LICENSE_REQUEST_FILE_NAME = "automaticLicenseRequest.xml";
    private static final String LICENSE_KEY_ID = "key1";
    private static final String FINGERPRINT_LICENSE_OPTION = "fingerprint";
    private static final String FINGERPRINT_LICENSE_OPTION_VALUE = "fingerprint_value";
    private static final String MANDATORY_LICENSE_KEYS_LICENSE_OPTION = "mandatoryLicenseKeys";
    private static final String MANDATORY_LICENSE_KEYS_FILE_NAME = "mandatoryLicenseKeys.xml";
    private static final String MANDATORY_LICENSE_KEYS_CONTENT = "<licenseKeys><licenseKey>" + LICENSE_KEY_ID + "</licenseKey></licenseKeys>";
    private static final String NODE_FDN = "Project=Project1,Node=Node1";

    @Mock
    private LicenseFileManagerService licenseFileManagerService;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private ManagedObject licenseOptionsMo;

    @Mock
    private RawArtifactHandler rawArtifactHandler;

    @Mock
    private DataBucket liveBucket;

    @InjectMocks
    @Spy
    private ValidateMandatoryLicenseKeysExist validationRule;

    private ValidationContext context;

    private final Collection<ArtifactDetails> rawLicenseKeyFiles = new ArrayList<>();

    @Before
    public void setUp() {
        final ArtifactDetails artifactDetails;
        final List<String> licenseKeyFiles = new ArrayList<>();
        licenseKeyFiles.add("file1");
        final Map<String, Object> contextTarget = new HashMap<>();
        contextTarget.put("nodeFdn", NODE_FDN);

        context = new ValidationContext("", contextTarget);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn("Project=Project1,Node=Node1,LicenseOptions=1")).thenReturn(licenseOptionsMo);

        artifactDetails = new ArtifactBuilder().artifactContent(MANDATORY_LICENSE_KEYS_CONTENT).build();
        final Map<String, Object> allLicenseOptionsMoAttributes = new HashMap<>();
        allLicenseOptionsMoAttributes.put(MANDATORY_LICENSE_KEYS_LICENSE_OPTION, "licenseKeys.xml");
        when(licenseOptionsMo.getAllAttributes()).thenReturn(allLicenseOptionsMoAttributes);
        when(licenseOptionsMo.getAttribute(MANDATORY_LICENSE_KEYS_LICENSE_OPTION)).thenReturn("licenseKeys.xml");
        when(rawArtifactHandler.readAllOfType(NODE_FDN, "MandatoryLicenseKeys")).thenReturn(rawLicenseKeyFiles);
        rawLicenseKeyFiles.add(artifactDetails);
    }

    @Test
    public void whenValidateMandatoryLicenseKeysAndNoMandatoryLicenseKeyFilesExistsThenThereIsNoCallToShmAndValidationPasses()
        throws LicenseAccessException, LicenseValidationException {
        when(licenseOptionsMo.getAttribute(MANDATORY_LICENSE_KEYS_LICENSE_OPTION)).thenReturn(null);

        final boolean result = validationRule.execute(context);

        assertTrue(result);
        verify(licenseFileManagerService, never()).validateLicenseKeys(anyString(), anyListOf(String.class));
    }

    @Test
    public void whenValidateMandatoryLicenseKeysAndShmReturnsNoErrorThenValidationPasses()
        throws LicenseAccessException, LicenseValidationException {
        when(licenseOptionsMo.getAttribute(MANDATORY_LICENSE_KEYS_LICENSE_OPTION)).thenReturn(MANDATORY_LICENSE_KEYS_FILE_NAME);

        final boolean result = validationRule.execute(context);

        assertTrue(result);
        final List<String> keyList = new ArrayList<>();
        keyList.add(LICENSE_KEY_ID);
        verify(licenseFileManagerService).validateLicenseKeys("Node1", keyList);
    }

    @Test
    public void whenValidateMandatoryLicenseKeysAndFingerprintAndShmReturnsNoErrorThenValidationPasses()
        throws LicenseAccessException, LicenseValidationException {

        final Map<String, Object> allLicenseOptionsMoAttributes = new HashMap<>();
        allLicenseOptionsMoAttributes.put(MANDATORY_LICENSE_KEYS_LICENSE_OPTION, "licenseKeys.xml");
        allLicenseOptionsMoAttributes.put(FINGERPRINT_LICENSE_OPTION, FINGERPRINT_LICENSE_OPTION_VALUE);
        when(licenseOptionsMo.getAllAttributes()).thenReturn(allLicenseOptionsMoAttributes);

        final boolean result = validationRule.execute(context);

        assertTrue(result);
        final List<String> keyList = new ArrayList<>();
        keyList.add(LICENSE_KEY_ID);
        verify(licenseFileManagerService).validateLicenseKeys(FINGERPRINT_LICENSE_OPTION_VALUE, keyList);
    }

    @Test
    public void whenValidateMandatoryLicenseKeysAndShmReturnsAccessErrorThenValidationFailsAndErrorMessageIsTakenFromShmException()
        throws LicenseAccessException, LicenseValidationException {
        when(licenseOptionsMo.getAttribute(MANDATORY_LICENSE_KEYS_LICENSE_OPTION)).thenReturn(MANDATORY_LICENSE_KEYS_FILE_NAME);
        doThrow(new LicenseAccessException("accessError")).when(licenseFileManagerService).validateLicenseKeys(anyString(), anyListOf(String.class));

        validationRule.execute(context);

        assertTrue(context.getValidationErrors().get(0).contains("accessError"));
    }

    @Test
    public void whenValidateMandatoryLicenseKeysAndShmValidationErrorThenValidationFailsAndErrorMessageIsBuiltFromResponse()
        throws LicenseAccessException, LicenseValidationException {
        final Map<String, Object> licenseOptions = new HashMap<>();
        licenseOptions.put(MANDATORY_LICENSE_KEYS_LICENSE_OPTION, MANDATORY_LICENSE_KEYS_FILE_NAME);

        final Set<String> missingKeys = new HashSet<>();
        missingKeys.add("fakeLicenseKey");
        final EnumMap<ValidationFailure, Set<String>> failingLicenseKeys = new EnumMap<>(ValidationFailure.class);
        failingLicenseKeys.put(ValidationFailure.KEY_DOES_NOT_EXIST, missingKeys);

        doThrow(new LicenseValidationException(failingLicenseKeys)).when(licenseFileManagerService).validateLicenseKeys(anyString(),
            anyListOf(String.class));

        validationRule.execute(context);

        assertTrue(context.getValidationErrors().get(0).contains(ValidationFailure.KEY_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void whenAutomaticLicenseRequestAttributeExistThenMandatoryLicenseKeysCheckIsSkipped() throws LicenseAccessException, LicenseValidationException {
        final Map<String, Object> allLicenseOptionsMoAttributes = new HashMap<>();
        allLicenseOptionsMoAttributes.put(AUTOMATIC_LICENSE_REQUEST_OPTION, AUTOMATIC_LICENSE_REQUEST_FILE_NAME);
        when(licenseOptionsMo.getAllAttributes()).thenReturn(allLicenseOptionsMoAttributes);
        when(licenseOptionsMo.getAttribute(AUTOMATIC_LICENSE_REQUEST_OPTION)).thenReturn(AUTOMATIC_LICENSE_REQUEST_FILE_NAME);

        final boolean result = validationRule.execute(context);

        assertTrue(result);
        verify(licenseFileManagerService, never()).validateLicenseKeys(anyString(), anyListOf(String.class));
    }

    @Test
    public void whenAutomaticLicenseRequestAttributeReturnsNullThenMandatoryLicenseKeysCheckIsExecuted() throws LicenseAccessException, LicenseValidationException {
        when(licenseOptionsMo.getAttribute(AUTOMATIC_LICENSE_REQUEST_OPTION)).thenReturn(null);

        validationRule.execute(context);

        verify(licenseFileManagerService, times(1)).validateLicenseKeys(anyString(), anyListOf(String.class));
    }
}

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.shm
import javax.inject.Inject

import org.apache.http.entity.ContentType
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.core.rest.client.shm.util.NodeMoHelper

import spock.lang.Subject

class ShmRestClientSpec extends CdiSpecification {

    @Subject
    @Inject
    ShmRestClient shmRestClient

    private ShmMockResponseDataProvider shmMockResponseDataProvider = new ShmMockResponseDataProvider()
    private static String expectedPackageName = "CXP9024418_6_R5B17"
    private static String expectedSoftwareVersion = "CXP9024418_6_R5B17"
    private static String backupId = "RadioNode_R5B17_release_upgrade_package_22032022_023213_READY_FOR_SERVICE"
    private static String nodeName = "LTE01dg00002"
    private static String nodeType = "RadioNode"
    private static String packageName = null
    private static String softwareVersion = null
    private static String protocol = "http"
    private static String hostname = "localhost"
    private static int port = 1081
    private static String upgradePackagePath = "/oss/shm/rest/softwarePackage/search"
    private static String softeareBackupVersionPath = "/oss/shm/rest/inventory/backupSoftwareVersions"

    @MockedImplementation
    private NodeMoHelper nodeMoHelper;

    static {
        System.setProperty("INTERNAL_URL",protocol + "://" + hostname + ":" + port)
    }

    def "When valid upgrade package name from response then upgrade package is what expected and no exception is thrown"() {
        given: "valid software version from MO"
            nodeMoHelper.getSoftwareVersionFromMO(nodeName) >> expectedSoftwareVersion

        and: "POST request to retrieve upgrade packages are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonidealResponse
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType);

        then: "The object mapped from the response is what is expected"
            packageName == expectedPackageName

       cleanup:
           clientAndServer.stop()
    }

    def "When valid upgrade package name from response but no software version identified then no exception is thrown"() {
        given: "POST request to retrieve upgrade packages are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonidealResponse
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType,null);

        then: "Exception is thrwon"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for no software version is identified.");

       cleanup:
           clientAndServer.stop()
    }

    def "When responded with 200 but no upgrade package name from response then exception is thrown"() {
        given: "No upgrade package name from response, POST request to retrieve upgrade packages are mocked to respond with status OK and no error"
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponsePackageNotfound
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType,expectedPackageName);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

        cleanup:
            clientAndServer.stop()
    }

    def "When responded with 200 but error JSON response then exception is thrwon"() {
        given: "No upgrade package name from response, POST request to retrieve upgrade packages are mocked to respond with status OK"
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponsePackageerror
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType,expectedPackageName);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

        cleanup:
            clientAndServer.stop()
    }

    def "When request to get upgrade package name is responded with 503, then exception is thrown"() {
        given: "POST request to retrieve upgrade packages are mocked to respond with status 503 "
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
            ).respond(
                HttpResponse.response()
                        .withStatusCode(503)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.TEXT_HTML.getMimeType()),
                ).withBody(
                        "503 Service Unavailable"
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType,expectedPackageName);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

        cleanup:
            clientAndServer.stop()
    }

    def "When request to get upgrade package name is responded with 500 with json, then exception is thrown"() {
        given: "POST request to retrieve upgrade packages are mocked to respond with status 503 "
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
            ).respond(
                HttpResponse.response()
                        .withStatusCode(500)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponsePackageNotfound
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType,expectedPackageName);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

        cleanup:
            clientAndServer.stop()
    }

    def "When valid backup software version from response then the backup software version is what expected and no exception is thrown"() {
        given: "valid backup software version from response"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseBackupSoftwareVersionPackage
                )
            )

        when: "The http request has been sent and the response has been processed"
            softwareVersion = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "The object mapped from the response is what is expected"
            softwareVersion == expectedSoftwareVersion

       cleanup:
           clientAndServer.stop()
    }

    def "When responded with 200 but no backup version from response then exception is thrown"() {
        given: "POST request to retrieve backup software version are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseBackupSoftwareVersionNotFoundPackage
                )
            )

        when: "The http request has been sent and the response has been processed"
            softwareVersion = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "The object mapped from the response is what is expected"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName));

       cleanup:
           clientAndServer.stop()
    }

    def "When request to get software backup version is responded with 503, then exception is thrown"() {
        given: "POST request to retrieve software backup version are mocked to respond with status 503 "
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
            ).respond(
                HttpResponse.response()
                        .withStatusCode(503)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.TEXT_HTML.getMimeType()),
                ).withBody(
                        "503 Service Unavailable"
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName));

        cleanup:
            clientAndServer.stop()
    }

    def "When duplicated upgrade package name from response then exception is thrown"() {
        given: "valid upgrade package name from response"
            nodeMoHelper.getSoftwareVersionFromMO(nodeName) >> expectedPackageName

        and: "POST request to retrieve upgrade packages are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseSamePackage
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType);

        then: "Exception thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

       cleanup:
           clientAndServer.stop()
    }

    def "When no upgrade package name matched from response then exception is thrown"() {
        given: "valid upgrade package name from response"
            nodeMoHelper.getSoftwareVersionFromMO(nodeName) >> expectedPackageName

        and: "POST request to retrieve upgrade packages are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(upgradePackagePath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponsePackageNotMatch
                )
            )

        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getUpgradePackageName(nodeName,nodeType);

        then: "Exception thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get upgrade package for CXP9024418_6_R5B17");

       cleanup:
           clientAndServer.stop()
    }

    def "When responded with 200 but invalid backup version from response then exception is thrown"() {
        given: "POST request to retrieve backup software version are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseBackupSoftwareVersionInvalidPackage
                )
            )

        when: "The http request has been sent and the response has been processed"
            softwareVersion = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "The object mapped from the response is what is expected"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName));

       cleanup:
           clientAndServer.stop()
    }

    def "When responded with 200 but invalid json body from response then exception is thrown"() {
        given: "POST request to retrieve backup software version are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseBackupSoftwareVersionerror
                )
            )

        when: "The http request has been sent and the response has been processed"
            softwareVersion = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "The object mapped from the response is what is expected"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName));

       cleanup:
           clientAndServer.stop()
    }

    def "When responded with 200 but mutilple backup version from response then exception is thrown"() {
        given: "POST request to retrieve backup software version are mocked to respond with status OK and no error"

            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(softeareBackupVersionPath)
                        .withHeader(Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()))
            ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                        Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                ).withBody(
                        shmMockResponseDataProvider.jsonResponseMultipleBackupSoftwareVersionPackage
                )
            )

        when: "The http request has been sent and the response has been processed"
            softwareVersion = shmRestClient.getBackupSoftwareVersion(nodeName,backupId);

        then: "The object mapped from the response is what is expected"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName));

       cleanup:
           clientAndServer.stop()
    }

    def "When backupid is null to get software backup version then exception is thrown"() {
        when: "The http request has been sent and the response has been processed"
            packageName = shmRestClient.getBackupSoftwareVersion(nodeName,null);

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().startsWith("Failed to get backup software version for no backupid is identified.");
    }
}

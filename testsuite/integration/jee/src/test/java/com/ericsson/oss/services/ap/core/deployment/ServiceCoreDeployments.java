/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.deployment;

import static com.ericsson.oss.services.ap.core.deployment.DeploymentHelper.getVersion;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.DescriptorImporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;

/**
 * This class is responsible for the deployment of the service-core ear that will be tested by arquillian.
 */
@ArquillianSuiteDeployment
public class ServiceCoreDeployments {

    private static final String SERVICE_CORE_EAR = "com.ericsson.oss.services.autoprovisioning:ap-service-core-ear:ear:?";

    private static final String CORE_REST_WAR = "com.ericsson.oss.services.autoprovisioning:ap-core-rest-war:war:?";

    private static final String AP_TESTWARE_INTEGRATION_ALLURE_JAR = "com.ericsson.oss.services.autoprovisioning:ap-testware-integration-allure:jar:?";
    private static final String AP_TESTWARE_INTEGRATION_ASPECTS_JAR = "com.ericsson.oss.services.autoprovisioning:ap-testware-integration-aspects:jar:?";
    private static final String AP_TESTWARE_INTEGRATION_UTIL_JAR = "com.ericsson.oss.services.autoprovisioning:ap-testware-integration-util:jar:?";
    private static final String AP_TESTWARE_INTEGRATION_CUCUMBER_JAR = "com.ericsson.oss.services.autoprovisioning:ap-testware-integration-cucumber:jar:?";
    private static final String AP_TESTWARE_INTEGRATION_TESTNG_JAR = "com.ericsson.oss.services.autoprovisioning:ap-testware-integration-testng:jar:?";

    private static final String TEST_DEPENDENCY_FREEMARKER = "org.freemarker:freemarker:?";
    private static final String TEST_DEPENDENCY_GUAVA = "com.google.guava:guava:25.0.0.redhat-1";
    private static final String TEST_DEPENDENCY_ASSERTJ = "org.assertj:assertj-core:?";
    private static final String TEST_DEPENDENCY_ZIP4J = "net.lingala.zip4j:zip4j:?";
    private static final String TEST_DEPENDENCY_SHRINKWRAP_DESCRIPTORS = "org.jboss.shrinkwrap.descriptors:shrinkwrap-descriptors-spi:?";
    private static final String TEST_DEPENDENCY_REST_ASSURED = "com.jayway.restassured:rest-assured:jar:?";
    private static final String TEST_DEPENDENCY_ORG_JSON = "org.json:json:?";
    private static final String TEST_DEPENDENCY_HTTP_CORE = "org.apache.httpcomponents:httpcore:jar:?";
    private static final String TEST_DEPENDENCY_MOCK_SERVER = "org.mock-server:mockserver-netty:?";

    private ServiceCoreDeployments() {
    }

    @Deployment(testable = true)
    public static Archive<?> generate() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        final EnterpriseArchive archiveWithCoreEar = getServiceCoreEar();
        final EnterpriseArchive finalArchive = updateResourceFilesForTest(archiveWithCoreEar);
        final WebArchive war = addRestWar();

        finalArchive.addAsModule(war);
        finalArchive.addAsLibrary(createTestJar());
        return modifyApplicationXML(finalArchive);
    }

    /**
     * Adds the ap-service-core-rest-war to the test archive.
     */
    private static WebArchive addRestWar() {
        final WebArchive webArchive = DeploymentHelper.getWebArchive("ap-core-rest.war", CORE_REST_WAR);
        final String version = getVersion("sdk_service_version");
        final String path = "/WEB-INF/lib/ap-common-model-" + version + ".jar";
        webArchive.delete(path);
        return webArchive;
    }

    private static JavaArchive createTestJar() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-test.jar").addPackages(true, ServiceCoreTest.class.getPackage())
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        .merge(DeploymentHelper.getArchiveByType(JavaArchive.class, AP_TESTWARE_INTEGRATION_UTIL_JAR));
    }

    private static EnterpriseArchive getServiceCoreEar() {
        return DeploymentHelper.getArchive("test.ear", SERVICE_CORE_EAR)
                .addAsLibrary(DeploymentHelper.fromMaven(AP_TESTWARE_INTEGRATION_TESTNG_JAR))
                .addAsLibrary(DeploymentHelper.fromMaven(AP_TESTWARE_INTEGRATION_ALLURE_JAR))
                .addAsLibrary(DeploymentHelper.fromMaven(AP_TESTWARE_INTEGRATION_CUCUMBER_JAR))
                .addAsLibrary(DeploymentHelper.fromMaven(AP_TESTWARE_INTEGRATION_ASPECTS_JAR))
                .addAsLibrary(DeploymentHelper.createContentsArchive())
                .addAsApplicationResource("jboss-deployment-structure.xml")
                .addAsApplicationResource("beans.xml");
    }

    private static EnterpriseArchive updateResourceFilesForTest(final EnterpriseArchive archive) {
        // First add the common test libraries.
        archive.addAsLibraries(
            DeploymentHelper.getGavsFiles(TEST_DEPENDENCY_FREEMARKER, TEST_DEPENDENCY_GUAVA, TEST_DEPENDENCY_ASSERTJ, TEST_DEPENDENCY_ZIP4J,
                TEST_DEPENDENCY_SHRINKWRAP_DESCRIPTORS, TEST_DEPENDENCY_REST_ASSURED, TEST_DEPENDENCY_ORG_JSON, TEST_DEPENDENCY_HTTP_CORE,
                TEST_DEPENDENCY_MOCK_SERVER));


        return archive;
    }

    private static EnterpriseArchive modifyApplicationXML(final EnterpriseArchive ear) {
        final Node node = ear.get("META-INF/application.xml");

        final DescriptorImporter<ApplicationDescriptor> importer = Descriptors.importAs(ApplicationDescriptor.class,
            CORE_REST_WAR);
        ApplicationDescriptor desc = importer.fromStream(node.getAsset().openStream());

        String xml = desc.exportAsString();
        xml = xml.replaceAll("<library-directory>.*<\\/library-directory>", "");

        desc = importer.fromString(xml);
        desc.webModule("ap-core-rest.war", "/auto-provisioning");
        final Asset asset = new StringAsset(desc.exportAsString());

        ear.delete(node.getPath());
        ear.setApplicationXML(asset);
        return ear;
    }

}

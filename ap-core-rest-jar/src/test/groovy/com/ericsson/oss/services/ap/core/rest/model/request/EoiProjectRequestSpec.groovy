/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package groovy.com.ericsson.oss.services.ap.core.rest.model.request

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.core.rest.model.request.EoiNetworkElement
import com.ericsson.oss.services.ap.core.rest.model.request.EoiProjectRequest

class EoiProjectRequestSpec extends CdiSpecification{

    @ObjectUnderTest
    private EoiProjectRequest eoiProjectRequest
    def "Test EoiProjectRequest"(){
        given:
        eoiProjectRequest.setNetworkUsecaseType("networkUsecase")
        eoiProjectRequest.getNetworkUsecaseType()
        EoiNetworkElement eoiNetworkElement=new EoiNetworkElement()
        eoiNetworkElement.setNodeName("name")
        ArrayList<EoiNetworkElement> aleoi =new ArrayList<>()
        aleoi.add(eoiNetworkElement)
        eoiProjectRequest.setNetworkElements(aleoi)
        eoiProjectRequest.getNetworkElements()
        eoiProjectRequest.toString()
        when:
        eoiProjectRequest.toString()
        then:
        return

    }
}

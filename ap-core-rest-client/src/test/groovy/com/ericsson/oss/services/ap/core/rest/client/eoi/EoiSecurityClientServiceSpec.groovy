package com.ericsson.oss.services.ap.core.rest.client.eoi


import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

import org.spockframework.util.Assert

class EoiSecurityClientServiceSpec extends CdiSpecification{

    @ObjectUnderTest
    EoiSecurityClientService eoiSecurityClientService

    def "Test EoiSecurityClientService with False"(){
        given:

        when:
        eoiSecurityClientService= eoiSecurityClientService.getHttpClient(false,"iPlanetdirectoryPro")
        then:
        Assert.notNull(eoiSecurityClientService)
        Assert.notNull(eoiSecurityClientService.getHttpCookieStore())
        Assert.notNull(eoiSecurityClientService.getHttpClient())

    }

    def "Test EoiSecurityClientService with True"(){
        given:

        when:
        eoiSecurityClientService= eoiSecurityClientService.getHttpClient(true,"iPlanetdirectoryPro")
        then:
        Assert.notNull(eoiSecurityClientService)
        Assert.notNull(eoiSecurityClientService.getHttpCookieStore())
        Assert.notNull(eoiSecurityClientService.getHttpClient())

    }

}

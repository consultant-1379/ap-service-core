package groovy.com.ericsson.oss.services.ap.api.exception

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApSecurityException

class ApSecurityExceptionSpec  extends CdiSpecification{

    def "Check exception message sets correctly for new exception with message"() {
        when: "Exception created with message"
        Exception e = new ApSecurityException("Exception message test")


        then: "Exception has expected content"
        e.getMessage() == "Exception message test"
    }

    def "Check exception message and parent exception sets correctly for new exception with message and exception"() {
        given: "Create parent exception for test"
        Exception parentException = new Exception("Parent exception message test");

        when: "Exception created with message and exception"
        Exception e = new ApSecurityException("Exception message test", parentException)

        then: "Exception has expected content"
        e.getMessage() == "Exception message test"
        e.getCause().getMessage() == "Parent exception message test"
    }

    def "When Security Constructor is called"(){
        given:
        when:
         new ApSecurityException()
        then:
        return
    }

    def "When Super Security Constructor is called"() {
        when:
        new ApSecurityException("When super constructor is called")
        then:
        return
    }
}

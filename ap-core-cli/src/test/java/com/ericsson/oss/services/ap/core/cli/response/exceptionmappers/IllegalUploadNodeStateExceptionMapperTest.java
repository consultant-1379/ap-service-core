package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IllegalUploadNodeStateExceptionMapperTest {

    @Test
    public void whenToCommandResponse_solution_is_formatted_with_valid_states_from_display_name() {
        final List<String> validStates = Arrays.asList("READY_FOR_ORDER","ORDER_FAILED","ORDER_CANCELLED");
        final String expected = "Ensure node is in correct state before executing the command. Valid state(s) are [Ready for Order, Order Failed, Order Cancelled]";
        final IllegalUploadNodeStateException illegalUploadNodeStateException = new IllegalUploadNodeStateException("message","ORDER_STARTED",validStates);
        final CommandResponseDto commandResponseDto =new IllegalUploadNodeStateExceptionMapper().toCommandResponse("order",illegalUploadNodeStateException);

        assertEquals(expected,commandResponseDto.getSolution());
    }

}

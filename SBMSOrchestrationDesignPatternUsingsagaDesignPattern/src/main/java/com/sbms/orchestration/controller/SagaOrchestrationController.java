package com.sbms.orchestration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sbms.orchestration.dto.BookingDto;
import com.sbms.orchestration.dto.ResponseMessagedto;
import com.sbms.orchestration.service.SagaOrchestrationService;
import com.sbms.orchestration.service.SagaOrchestrationServiceUsingFeign;

@RestController
@RequestMapping("/orchestrator")
public class SagaOrchestrationController {

    @Autowired
    private SagaOrchestrationService sagaService;
    
    @Autowired
    private SagaOrchestrationServiceUsingFeign sagaServiceFeign;

    @PostMapping("/start")
    public ResponseEntity<ResponseMessagedto<Object>> startSaga(@RequestBody BookingDto booking) {
        return sagaService.startSaga(booking);
    }
    
    @PostMapping("/startFeign")
    public ResponseEntity<ResponseMessagedto<Object>> startSagaFeign(@RequestBody BookingDto booking) {
    	return sagaServiceFeign.startSaga(booking);
    }
}

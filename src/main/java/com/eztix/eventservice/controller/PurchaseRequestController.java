package com.eztix.eventservice.controller;

import com.eztix.eventservice.dto.PurchaseRequestCreation;
import com.eztix.eventservice.dto.PurchaseRequestDTO;
import com.eztix.eventservice.dto.confirmation.EventConfirmationDTO;
import com.eztix.eventservice.dto.prretrieval.PurchaseRequestRetrievalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.eztix.eventservice.model.PurchaseRequest;
import com.eztix.eventservice.service.PurchaseRequestService;

import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    /**
     * Create a purchase request.
     * 
     * @param purchaseRequest a PurchaseRequestDTO object containing the info of the purchase request to be created.
     * @param authentication an Authentication object containing user details.
     * @return a ResponseEntity containing a PurchaseRequestCreation containing the details of the new purchase request and an OK status.
     */
    @CrossOrigin
    @PostMapping("/api/v1/purchase-request")
    public ResponseEntity<PurchaseRequestCreation> addPurchaseRequest (@RequestBody PurchaseRequestDTO purchaseRequest, Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseRequestService.addNewPurchaseRequest(purchaseRequest, authentication.getName()));
    }

    /**
     * Retrieve a event confirmation dto.
     * 
     * @param id a long value representing the unique identifier of the purchase request.
     * @return a ResponseEntity containing a EventConfirmationDTO containing details about the purchase request and an OK status.
     */
    @GetMapping ("/api/v1/purchase-request/{id}/confirmation")
    public ResponseEntity<EventConfirmationDTO> getPurchaseRequestConfirmation (@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(purchaseRequestService.getPurchaseRequestConfirmation(id));
    }

    /**
     * Retrieve a list of purchase requests filtered by userId.
     * 
     * @param authentication an Authentication object containing user details.
     * @return a ResponseEntity containing a list of PurchaseRequestRetrievalDTO related to the userId and an OK status.
     */
    @GetMapping("/api/v1/purchase-request")
    public ResponseEntity<List<PurchaseRequestRetrievalDTO>> getPurchaseRequestByUserId(Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK)
                .body(purchaseRequestService.getPurchaseRequestByUserId(authentication.getName()));
    }

    /**
     * Retrieve a purchase request.
     * 
     * @param id a long value representing the unique identifier of the purchase request.
     * @return a ResponseEntity containing the PurchaseRequest and an OK status.
     */
    @GetMapping ("/api/v1/purchase-request/{id}")
    public ResponseEntity<PurchaseRequest> getPurchaseRequestById (@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(purchaseRequestService.getPurchaseRequestById(id));
    }

    /**
     * Update a purchase request.
     * 
     * @param id a long value representing the unique identifier of the purchase request.
     * @param purchaseRequest a PurchaseRequest object containing the PurchaseRequest info to be updated.
     * @return a ResponseEntity containing the updated PurchaseRequest and an OK status.
     */
    @PutMapping("/api/v1/purchase-request/{id}")
    public ResponseEntity<PurchaseRequest> updatePurchaseRequest (@PathVariable Long id, @RequestBody PurchaseRequest purchaseRequest) {
        purchaseRequest.setId(id);
        return ResponseEntity.status(HttpStatus.OK)
              .body(purchaseRequestService.updatePurchaseRequest(purchaseRequest));
    }

}
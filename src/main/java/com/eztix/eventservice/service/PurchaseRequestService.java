package com.eztix.eventservice.service;

import com.eztix.eventservice.dto.PurchaseRequestDTO;
import com.eztix.eventservice.exception.RequestValidationException;
import com.eztix.eventservice.exception.ResourceNotFoundException;
import com.eztix.eventservice.model.PurchaseRequest;
import com.eztix.eventservice.model.PurchaseRequestItem;
import com.eztix.eventservice.model.SalesRound;
import com.eztix.eventservice.model.TicketType;
import com.eztix.eventservice.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SalesRoundService salesRoundService;
    private final TicketTypeService ticketTypeService;

    // Add new PurchaseRequest
    public PurchaseRequest addNewPurchaseRequest(PurchaseRequestDTO purchaseRequestDTO) {

        if (purchaseRequestDTO.getSalesRoundId() == null) {
            throw new RequestValidationException("sales round id cannot be null.");
        }

        if (purchaseRequestDTO.getPurchaseRequestItems().isEmpty()) {
            throw new RequestValidationException("there cannot be 0 item in the purchase request.");
        }

        // Get Sales Round
        SalesRound salesRound = salesRoundService.getSalesRoundById(purchaseRequestDTO.getSalesRoundId());

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Singapore"));

        if (salesRound.getRoundEnd().isAfter(now) || salesRound.getRoundEnd().isBefore(now)) {
            throw new RequestValidationException("request rejected due to sales round not ongoing.");
        }

        // New Purchase Request
        PurchaseRequest newPurchaseRequest =
                PurchaseRequest.builder().status("pending").customerId("Default TODO").salesRound(salesRound).build();

        List<PurchaseRequestItem> newPurchaseRequestItemList = createNewPrItemList(purchaseRequestDTO,
                newPurchaseRequest);
        newPurchaseRequest.setPurchaseRequestItems(newPurchaseRequestItemList);

        return purchaseRequestRepository.save(newPurchaseRequest);
    }

    // Get PurchaseRequest by id
    public PurchaseRequest getPurchaseRequestById(Long id) {

        return purchaseRequestRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("purchase request with id %d does not exist.", id))
        );

    }

    // Update PurchaseRequest
    @Transactional
    public PurchaseRequest updatePurchaseRequest(PurchaseRequest purchaseRequest) {
        if (purchaseRequest.getId() == null) {
            throw new RequestValidationException("purchase request id cannot be null.");
        }

        PurchaseRequest currentPurchaseRequest = this.getPurchaseRequestById(purchaseRequest.getId());

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Singapore"));

        if (purchaseRequest.getSalesRound().getRoundEnd().isAfter(now) || purchaseRequest.getSalesRound().getRoundEnd().isBefore(now)) {
            throw new RequestValidationException("request rejected due to sales round not ongoing.");
        }

        int sum = 0;
        List<PurchaseRequestItem> newPurchaseRequestItemList = new ArrayList<>();

        for (PurchaseRequestItem temp : purchaseRequest.getPurchaseRequestItems()) {

            if (temp.getTicketType() == null) {
                throw new RequestValidationException("ticket type cannot be null.");
            }

            PurchaseRequestItem purchaseRequestItem = new PurchaseRequestItem();
            purchaseRequestItem.setQuantityApproved(0);
            purchaseRequestItem.setQuantityRequested(temp.getQuantityRequested());
            purchaseRequestItem.setTicketType(temp.getTicketType());
            purchaseRequestItem.setPurchaseRequest(currentPurchaseRequest);
            sum += temp.getQuantityRequested();

            newPurchaseRequestItemList.add(purchaseRequestItem);

        }

        if (sum > 4) {
            throw new RequestValidationException("purchase request exceed 4 ticket limit.");
        } else if (sum < 0) {
            throw new RequestValidationException("purchase request must have at least 1 ticket.");
        }

        currentPurchaseRequest.setPurchaseRequestItems(newPurchaseRequestItemList);

        return purchaseRequestRepository.save(currentPurchaseRequest);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void processPurchaseRequests(Long salesRoundId) {
        // Get the total count of items for the given sales round
        long totalItemCount = purchaseRequestRepository.countBySalesRoundId(salesRoundId);

        // Algorithm
        List<Long> rng = new ArrayList<>();
        for (long i = 1; i <= totalItemCount; i++) {
            rng.add(i);
        }
        Collections.shuffle(rng, new SecureRandom());

        // Create an iterator for shuffled indices
        Iterator<Long> rngIterator = rng.iterator();

        // Stream through all the PRs under a sales round to assign queue numbers
//        Stream<PurchaseRequest> prStream =
                purchaseRequestRepository.findBySalesRoundId(salesRoundId)
                        .peek(pr -> pr.setQueueNumber(rngIterator.next())) // Set Queue Number for each
                        .sorted((a, b) -> Math.toIntExact(a.getQueueNumber() - b.getQueueNumber()))
                        .forEach(purchaseRequestRepository::save); // Sort them by

    }
    // Delete all PurchaseRequest
    public void deleteAllPurchaseRequests() {
        purchaseRequestRepository.deleteAll();
    }

    private List<PurchaseRequestItem> createNewPrItemList(PurchaseRequestDTO purchaseRequestDTO,
                                                          PurchaseRequest newPurchaseRequest) {
        AtomicInteger sum = new AtomicInteger();

        List<PurchaseRequestItem> newPurchaseRequestItemList =
                purchaseRequestDTO.getPurchaseRequestItems().stream().map(prItem -> {
                    if (prItem.getTicketTypeId() == null) {
                        throw new RequestValidationException("ticket type id cannot be null.");
                    }
                    TicketType ticketType = ticketTypeService.getTicketTypeById(prItem.getTicketTypeId());
                    sum.addAndGet(prItem.getQuantityRequested());

                    return PurchaseRequestItem.builder()
                            .quantityApproved(0)
                            .quantityRequested(prItem.getQuantityRequested())
                            .ticketType(ticketType)
                            .purchaseRequest(newPurchaseRequest)
                            .build();
                }).toList();

        checkTicketLimit(sum);

        return newPurchaseRequestItemList;
    }

    private List<PurchaseRequestItem> createNewPrItemList(PurchaseRequest purchaseRequest,
                                                          PurchaseRequest currentPurchaseRequest) {
        AtomicInteger sum = new AtomicInteger();
        List<PurchaseRequestItem> newPurchaseRequestItemList =
                purchaseRequest.getPurchaseRequestItems().stream().map(prItem -> {
                    if (prItem.getTicketType() == null) {
                        throw new RequestValidationException("ticket type cannot be null.");
                    }
                    sum.addAndGet(prItem.getQuantityRequested());

                    return PurchaseRequestItem.builder()
                            .quantityApproved(0)
                            .quantityRequested(prItem.getQuantityRequested())
                            .ticketType(prItem.getTicketType())
                            .purchaseRequest(currentPurchaseRequest).build();
                }).toList();

        checkTicketLimit(sum);

        return newPurchaseRequestItemList;
    }

    private void checkTicketLimit(AtomicInteger sum) {
        if (sum.get() > 4) {
            throw new RequestValidationException("purchase request exceed 4 ticket limit.");
        } else if (sum.get() < 0) {
            throw new RequestValidationException("purchase request must have at least 1 ticket.");
        }
    }
}

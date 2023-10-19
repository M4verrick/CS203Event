package com.eztix.eventservice.service;

import com.eztix.eventservice.dto.request.NewAdmissionPolicy;
import com.eztix.eventservice.exception.RequestValidationException;
import com.eztix.eventservice.exception.ResourceNotFoundException;
import com.eztix.eventservice.model.AdmissionPolicy;
import com.eztix.eventservice.model.Event;
import com.eztix.eventservice.repository.AdmissionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmissionPolicyService {
    private final AdmissionPolicyRepository admissionPolicyRepository;

    public List<AdmissionPolicy> getAllAdmissionPolicyByEventId(Long eventId){
        return admissionPolicyRepository.findAllByEventId(eventId).orElseThrow(() ->
                    new ResourceNotFoundException(String.format("event with id %d does not have admission policy", eventId))
                );
    }

    public AdmissionPolicy addNewAdmissionPolicy(Event event, AdmissionPolicy admissionPolicy){
        admissionPolicy.setEvent(event);
        return admissionPolicyRepository.save(admissionPolicy);
    }

    @Transactional
    public AdmissionPolicy updateAdmissionPolicy(AdmissionPolicy admissionPolicy){
        if (admissionPolicy.getId() == null){
            throw new RequestValidationException("admission policy id cannot be null.");
        }

        admissionPolicyRepository.findById(admissionPolicy.getId()).orElseThrow(() ->
                new ResourceNotFoundException(String.format("admission policy with id %d does not exist", admissionPolicy.getId())));

        return admissionPolicyRepository.save(admissionPolicy);
    }

    public void deleteAdmissionPolicy(Long admissionPolicyId) {
        if (admissionPolicyId == null) {
            throw new RequestValidationException("admission policy id cannot be null.");
        }

        admissionPolicyRepository.deleteById(admissionPolicyId);
    }

}
package com.example.profile_service.consumer;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableKafka
public class ProfileConsumer {
    private final ProfileService profileService;

    @KafkaListener(topics = "create-profile", groupId = "profile-group")
    public void handleCreateProfile(String profileEvent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProfileRequest profileRequest = null;
        try {
            profileRequest = objectMapper.readValue(profileEvent, ProfileRequest.class);
        } catch (JsonMappingException e)
        {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
        profileService.createProfile(profileRequest);
    }
}

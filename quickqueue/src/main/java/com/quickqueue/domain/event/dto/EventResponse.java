package com.quickqueue.domain.event.dto;

import com.quickqueue.domain.event.entity.EventStatus;

public record EventResponse(
        Long id,
        String name,
        String publicId,
        EventStatus status
){
}

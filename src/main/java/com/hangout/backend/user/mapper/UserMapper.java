package com.hangout.backend.user.mapper;

import com.hangout.backend.user.dto.UserSummaryResponse;
import com.hangout.backend.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Hand-written entity<->DTO mapping. Swap for MapStruct later if the mapping
 * surface grows large enough to justify the codegen/annotation-processor
 * setup - kept manual for now to minimize moving parts on a brand-new
 * Spring Boot 4 toolchain.
 */
@Component
public class UserMapper {

    public UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getProfilePicture()
        );
    }
}

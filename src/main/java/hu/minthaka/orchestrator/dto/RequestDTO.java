package hu.minthaka.orchestrator.dto;

import hu.minthaka.common.dto.DoctorDataCreateDTO;
import hu.minthaka.common.enums.Status;
import java.util.UUID;

public record RequestDTO(UUID uuid, Status status, DoctorDataCreateDTO payload) {
}

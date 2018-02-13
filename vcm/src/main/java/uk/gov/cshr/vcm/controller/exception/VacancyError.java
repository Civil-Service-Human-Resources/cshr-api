package uk.gov.cshr.vcm.controller.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyError {

    private HttpStatus status;
    private String message;
    private List<String> errors;
}

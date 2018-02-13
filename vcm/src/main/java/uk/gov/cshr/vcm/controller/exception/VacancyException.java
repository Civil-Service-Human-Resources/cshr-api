package uk.gov.cshr.vcm.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VacancyException extends RuntimeException {

//    private Long vacancyID;
//    private HttpStatus status;
//    private String message;
//    private List<String> errors;
    private VacancyError vacancyError;
}

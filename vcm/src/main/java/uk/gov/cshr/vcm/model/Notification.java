package uk.gov.cshr.vcm.model;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @NotNull
    private String email;

    @NotNull
    private String templateID;

    @NotNull
    private String notifyCode;

}

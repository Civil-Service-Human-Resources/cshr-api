package uk.gov.cshr.vcm.controller;

import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.vcm.model.Notification;
import uk.gov.cshr.vcm.service.NotifyService;
import uk.gov.service.notify.NotificationClientException;

@RestController
@RequestMapping(value = "/notify", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "notifyservice")
@RolesAllowed("NOTIFY_ROLE")
public class NotifyController {
    
    private static final Logger log = LoggerFactory.getLogger(NotifyController.class);

    @Autowired
    private NotifyService notifyService;

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Sent notification code", nickname = "notify")
    public ResponseEntity<Notification> notify(@RequestBody Notification notification)
            throws NotificationClientException {

        notifyService.notify(notification);
        return ResponseEntity.ok().build();
    }
}

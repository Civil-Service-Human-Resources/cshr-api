package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/wcnDataFeed", consumes = MediaType.TEXT_XML_VALUE)
public class WcnHrXmlController {

    private static final Logger log = LoggerFactory.getLogger(WcnHrXmlController.class);


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Accept HR-XML push from WCN", nickname = "wcnDataFeed")
    public ResponseEntity<?> wcnDataFeed(@RequestBody String hrXml) {
        log.debug(hrXml);
        return ResponseEntity.ok().build();
    }

}

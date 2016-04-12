/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import to.xss.httprequestlog.domain.RequestEntity;
import to.xss.httprequestlog.domain.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static to.xss.httprequestlog.util.IpUtil.ipToHostName;

@Controller
public class RequestLogController {

    private static final Logger log = LoggerFactory.getLogger(RequestLogController.class);

    private static final String VALID_LOGGED_PATH_REGEX = "^/(?!_)[\\p{IsAlphabetic}\\p{IsDigit}\\p{Punct} ]{2,128}$";
    private static final Pattern VALID_LOGGED_PATH = Pattern.compile(VALID_LOGGED_PATH_REGEX);

    @Autowired
    RequestRepository requestRepository;


    @RequestMapping(value = {"/"}, method = RequestMethod.GET, produces = "text/html")
    public String showLandingPage() {
        log.info("homepage request made");
        return "_view/index.html";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json", consumes = {"application/json"})
    @ResponseBody
    public List<RequestEntity> recentRequests(@RequestBody Map<String, String> jsonParams) {
        String path = jsonParams.getOrDefault("path", "");
        log.info("recent logged requests for path={} made", path);

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(0, 25, sort);

        return path.isEmpty() ?
                requestRepository.findAll(pageable).getContent() :
                requestRepository.findByPath(path, pageable).getContent();
    }

    @RequestMapping(value = "/{partialPath:(?!_view).+}/**", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> loggedRequest(HttpServletRequest request) {

        String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        log.info("request logging controller called path={}", path);

        if (!VALID_LOGGED_PATH.matcher(path).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("logged paths must match regex: " + VALID_LOGGED_PATH_REGEX);
        }

        String remoteIp = request.getRemoteAddr();

        RequestEntity requestEntity = new RequestEntity(new Date(), request.getMethod(), path);
        requestEntity.setRemoteIp(remoteIp);
        requestEntity.setRemoteHost(ipToHostName(remoteIp));
        requestEntity.setRemotePort(request.getRemotePort());

        Enumeration allHeaderNames = request.getHeaderNames();
        while (allHeaderNames.hasMoreElements()) {
            String name = (String) allHeaderNames.nextElement();
            requestEntity.addRequestHeader(name, request.getHeader(name));
        }

        for (Map.Entry<String, String[]> param : request.getParameterMap().entrySet()) {
            String key = param.getKey();
            String[] values = param.getValue();
            for (String value : values) {
                requestEntity.addRequestParam(key, value);
            }
        }

        requestRepository.save(requestEntity);

        return ResponseEntity.status(HttpStatus.OK).body("logged");
    }
}

/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import to.xss.httprequestlog.domain.RequestEntity;
import to.xss.httprequestlog.domain.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class UserViewController {

    private static final Logger log = LoggerFactory.getLogger(UserViewController.class);

    @Autowired
    RequestRepository requestRepository;

    // Some brain-dead bots and people manipulating request headers would send the location fragment to the server,
    // so I mapped our single-page app locations (/#/**) to the homepage so the requests are not logged publicly.
    @RequestMapping(
            value = {"/", "/#/**"},
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public String showLandingPage(HttpServletResponse response) {
        log.info("homepage request made");
        response.addHeader("X-Frame-Options", "deny");
        response.addHeader(
                "Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline';"
        );
        return "_view/index.html";
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
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

}

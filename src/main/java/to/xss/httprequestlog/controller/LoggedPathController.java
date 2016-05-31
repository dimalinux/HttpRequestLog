/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import to.xss.httprequestlog.domain.RequestEntity;
import to.xss.httprequestlog.domain.RequestRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static to.xss.httprequestlog.util.IpUtil.ipToHostName;

@Controller
@RequestMapping(value = "/{partialPath:(?!_view)(?!_webjars)(?!favicon\\.ico).+}/**")
@CrossOrigin
class LoggedPathController {

    private static final Logger log = LoggerFactory.getLogger(LoggedPathController.class);

    private static final String VALID_LOGGED_PATH_REGEX = "^/(?!_)[\\p{IsAlphabetic}\\p{IsDigit}\\p{Punct} ]{2,128}$";
    private static final Pattern VALID_LOGGED_PATH = Pattern.compile(VALID_LOGGED_PATH_REGEX);

    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABAQMAAAAl21bKAAAAA1BMVEUAAACnej3aAAAAAXRSTlMAQObYZgAAAApJREFUCNdjYAAAAAIAAeIhvDMAAAAASUVORK5CYII="
    );

    private static final CacheControl DO_NOT_CACHE = CacheControl.noStore().mustRevalidate().sMaxAge(0, TimeUnit.SECONDS);

    @Autowired
    private RequestRepository requestRepository;


    @Value("${production.host:xss.to}")
    private String productionHost;

    @Value("${behindReverseProxy:false}")
    private boolean behindReverseProxy;


    private static class BadLoggedPathException extends RuntimeException {}

    @ExceptionHandler(BadLoggedPathException.class)
    public ResponseEntity handleBadLoggedPathException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                "Logged paths must match regex: " + VALID_LOGGED_PATH_REGEX
        );
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity logRequestNonJsonBody(HttpServletRequest request) {

        RequestEntity requestEntity = requestRepository.save(buildBaseRequestEntity(request));

        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.OK).cacheControl(DO_NOT_CACHE);

        String path = requestEntity.getPath().toLowerCase();

        if (path.endsWith(".png")) {
            return response.contentType(MediaType.IMAGE_PNG).body(ONE_PIXEL_PNG);
        } else if (path.equals("/robots.txt")) {
            return createRobotsDotTextResponse(request);
        } else if (path.equals("/sitemap.xml")) {
            return createSiteMapDotXmlResponse();
        } else {
            return response.contentType(MediaType.TEXT_PLAIN).body("logged");
        }
    }


    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity logRequestWithJsonBody(HttpServletRequest request, @RequestBody Map<String, Object> jsonBody) {

        RequestEntity requestEntity = buildBaseRequestEntity(request);

        for (Map.Entry<String, Object> jsonParam : jsonBody.entrySet()) {
            String key = jsonParam.getKey();
            String value = jsonParam.getValue().toString();
            requestEntity.addRequestParam(key, value);
        }

        requestRepository.save(requestEntity);

        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.OK).cacheControl(DO_NOT_CACHE);

        return response.contentType(MediaType.APPLICATION_JSON_UTF8).body("{\"logged\": true}");
    }


    /*
     * Creates a robots.txt that denies all paths if our server is not the production server.
     */
    private ResponseEntity<String> createRobotsDotTextResponse(HttpServletRequest request) {

        boolean allowRobotCrawl = false;
        String requestHost = request.getHeader("Host");

        if (requestHost != null) {
            requestHost = requestHost.replaceFirst(":.*", "").toLowerCase(); // strip port numbers
            allowRobotCrawl = requestHost.equals(productionHost);
        }

        log.debug("RobotsController called, reqHost={} canCrawl={}", requestHost, allowRobotCrawl);
        return ResponseEntity.status(HttpStatus.OK).body(
                "User-agent: *\n" +
                        "Disallow: " + (allowRobotCrawl ? "_view/\n" : "/\n")
        );
    }

    /*
     *  I should create a sitemap.xml eventually.  For now we return a 404 response to let the
     *  (logged) requesting robot know that we don't have one.
     */
    private ResponseEntity<String> createSiteMapDotXmlResponse() {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.NOT_FOUND);
        return response.contentType(MediaType.TEXT_PLAIN).body("404 - NOT FOUND (logged)");
    }


    private RequestEntity buildBaseRequestEntity(HttpServletRequest request) {

        String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();

        log.info("request logging controller called path={}", path);

        if (!VALID_LOGGED_PATH.matcher(path).matches()) {
            throw new BadLoggedPathException();
        }

        RequestEntity requestEntity = new RequestEntity(new Date(), request.getMethod(), path);
        String remoteIp = request.getRemoteAddr();
        String connectionHeaderVal = null;

        Enumeration allHeaderNames = request.getHeaderNames();
        while (allHeaderNames.hasMoreElements()) {
            String name = (String) allHeaderNames.nextElement();
            // X-Real-IP is a header added by our nginx reverse proxy.  If it's set, it overrides
            // the servlet client ip address.  X-Real-Connection is also added by our nginx config.
            // It contains the original Connection header value when the reverse proxy changes
            // the original value to "close".
            if (behindReverseProxy) {
                switch (name) {
                    case "X-Real-IP":
                        remoteIp = request.getHeader(name);
                        continue;
                    case "X-Real-Connection":
                        connectionHeaderVal = request.getHeader(name);
                        continue;
                    case "Connection":
                        // don't reset the connection value if it was already fixated by X-Real-Connection
                        if (connectionHeaderVal == null) {
                            connectionHeaderVal = request.getHeader(name);
                        }
                        continue;
                }
            }
            requestEntity.addRequestHeader(name, request.getHeader(name));
        }

        if (connectionHeaderVal != null) {
            requestEntity.addRequestHeader("Connection", connectionHeaderVal);
        }

        requestEntity.setRemoteIp(remoteIp);
        requestEntity.setRemoteHost(ipToHostName(remoteIp));
        requestEntity.setRemotePort(request.getRemotePort());

        for (Map.Entry<String, String[]> param : request.getParameterMap().entrySet()) {
            String key = param.getKey();
            String[] values = param.getValue();
            for (String value : values) {
                requestEntity.addRequestParam(key, value);
            }
        }

        return requestEntity;
    }

}

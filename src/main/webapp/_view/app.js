/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
(function () {

    'use strict';

    angular.module("requestLoggerApp", ['ngRoute', 'ui.router', 'ui.bootstrap'])
        .config(RequestLoggerRoutes)
        .service('SearchPathService', SearchPathService)
        .controller('NavigationController', NavigationController)
        .controller('RequestsController', RequestsController);


    RequestLoggerRoutes.$scope = ['$routeProvider'];

    function RequestLoggerRoutes($routeProvider) {
        $routeProvider
            .when('/', {
                controller: 'RequestsController',
                templateUrl: '/_view/requests.html'
            })
            .when('/path/:path', {
                controller: 'RequestsController',
                templateUrl: '/_view/requests.html'
            })

            .when('/about', {
                templateUrl: '/_view/about.html'
            })
            .otherwise({
                redirectTo: '/'
            });
    }

    function SearchPathService() {
        this.path = '';
    }


    NavigationController.$scope = ['$scope', '$log', '$location', 'SearchPathService'];

    function NavigationController($scope, $log, $location, SearchPathService) {
        $log.debug('navigationController called');
        $scope.search = SearchPathService;

        $scope.limitPathResults = function () {
            var path = SearchPathService.path;
            if (path) {
                $location.path("/path/" + path.replace(/^\/*/, ''));
            } else {
                $location.path("/");
            }
        }
    }


    RequestsController.$scope = ['$scope', '$http', '$routeParams', '$log', 'SearchPathService'];

    function RequestsController($scope, $http, $routeParams, $log, SearchPathService) {

        var postData = {};

        if ($routeParams.path) {
            var path = $routeParams.path.replace(/^\/*/, "/"); // Make sure there is exactly one leading slash
            SearchPathService.path = path;
            postData.path = path;
            $scope.path = path;
            $log.debug('RequestsController called, path=' + path);
        } else {
            $log.debug('RequestsController called (any path)');
        }

        $http.post('/', postData).then(
            function successCallback(response) {
                $scope.recentRequests = response.data;
            },
            function errorCallback(response) {
                $log.error('unable to load recent requests');
            });
    };

})();
/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */

angular.module("requestLoggerApp", ['ngRoute', 'ui.bootstrap'])

    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                controller: 'recentRequestsController',
                templateUrl: '/_view/requests.html'
            })
            .when('/path/:path', {
                controller: 'recentRequestsController',
                templateUrl: '/_view/requests.html'
            })

            .when('/about', {
                templateUrl: '/_view/about.html'
            })
            .otherwise({
                redirectTo: '/'
            });
    })

    .service('searchPathService', function () {
        this.path = '';
    })

    .controller('navigationController', ['$scope', '$log', '$location', 'searchPathService', function ($scope, $log, $location, searchPathFactory) {
        $log.debug('navigationController called');
        $scope.searchPath = searchPathFactory;

        $scope.go = function (hash) {
            $location.path(hash);
        }
    }])

    .controller('recentRequestsController', ['$scope', '$http', '$routeParams', '$log', 'searchPathService', function ($scope, $http, $routeParams, $log, searchPathService) {

        var postData = {};

        if ($routeParams.path) {
            searchPathService.path = $routeParams.path;
            postData.path = $routeParams.path;
            $scope.path = $routeParams.path;
            $log.debug('recentRequestsController called, path=' + $routeParams.path);
        } else {
            $log.debug('recentRequestsController called (any path)');
        }

        $http.post('/', postData).then(
            function successCallback(response) {
                $scope.recentRequests = response.data;
            },
            function errorCallback(response) {
                $log.error('unable to load recent requests');
            });
    }]);
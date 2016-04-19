/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
(function () {

    'use strict';

    angular.module("requestLoggerApp", ['ui.bootstrap', 'ui.router'])
        .config(ConfigRoutes)
        .service('SearchPathService', SearchPathService)
        .controller('NavigationController', NavigationController)
        .controller('RequestsController', RequestsController);


    ConfigRoutes.$inject = ['$stateProvider', '$urlRouterProvider'];

    function ConfigRoutes($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/");

        $stateProvider
            .state('Home', {
                url: '/',
                templateUrl: '/_view/requests.html',
                controller: 'RequestsController as vm'
            })
            .state('RequestsForPath', {
                url: '/path/{path:.*}',
                templateUrl: '/_view/requests.html',
                controller: 'RequestsController as vm'
            })
            .state('About', {
                url: '/about',
                templateUrl: '/_view/about.html'
            });
    }


    function SearchPathService() {
        this.path = '';
    }


    NavigationController.$inject = ['$log', '$location', 'SearchPathService'];

    function NavigationController($log, $location, SearchPathService) {

        var vm = this;

        $log.debug('navigationController called');
        vm.search = SearchPathService;

        vm.limitPathResults = function () {
            $log.debug('search for path initiated');
            var path = vm.search.path;
            if (path) {
                $location.path("/path/" + path.replace(/^\/*/, ''));
            } else {
                $location.path("/");
            }
        }
    }


    RequestsController.$inject = ['$http', '$stateParams', '$log', 'SearchPathService'];

    function RequestsController($http, $stateParams, $log, SearchPathService) {

        var vm = this;
        var postData = {};

        if ($stateParams.path) {
            var path = $stateParams.path.replace(/^\/*/, "/"); // Make sure there is exactly one leading slash
            SearchPathService.path = path;
            postData.path = path;
            vm.path = path;
            $log.debug('RequestsController called, path=' + path);
        } else {
            $log.debug('RequestsController called (any path)');
        }

        $http.post('/', postData).then(
            function successCallback(response) {
                vm.requests = response.data;
            },
            function errorCallback(response) {
                $log.error('unable to load recent requests');
            });
    }

})();
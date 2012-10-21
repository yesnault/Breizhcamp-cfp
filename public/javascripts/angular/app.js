'use strict';


// Declare app level module which depends on filters, and services
var App = angular.module('breizhCampCFP', ['breizhCampCFP.services']); // , ['breizhCampCFP.filters', 'breizhCampCFP.services', 'breizhCampCFP.directives']).


// Configuration des routes
App.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {templateUrl: '/assets/pages/partials/login.html', controller: LoginController});
    $routeProvider.when('/dashboard', {templateUrl: '/assets/pages/partials/dashboard.html'});
    $routeProvider.when('/submittalk', {templateUrl: '/assets/pages/partials/submittalk.html', controller: TalkController});
    $routeProvider.when('/managetalk', {templateUrl: '/assets/pages/partials/managetalks.html', controller: TalkController});
    $routeProvider.otherwise({redirectTo: '/login'});
  }]);


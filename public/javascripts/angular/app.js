'use strict';


// Declare app level module which depends on filters, and services
angular.module('breizhCampCFP', ['breizhCampCFP.filters', 'breizhCampCFP.services', 'breizhCampCFP.directives']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {templateUrl: '/assets/pages/partials/login.html', controller: MyCtrl1});
    $routeProvider.when('/view2', {templateUrl: '/assets/pages/partials/partial2.html', controller: MyCtrl2});
    $routeProvider.otherwise({redirectTo: '/login'});
  }]);

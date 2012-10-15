'use strict';


// Declare app level module which depends on filters, and services
var App = angular.module('breizhCampCFP', []); // , ['breizhCampCFP.filters', 'breizhCampCFP.services', 'breizhCampCFP.directives']).


// Configuration des routes
App.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {templateUrl: '/assets/pages/partials/login.html'});
    $routeProvider.when('/dashboard', {templateUrl: '/assets/pages/partials/dashboard.html'});
    $routeProvider.otherwise({redirectTo: '/login'});
  }]);


// Fonctions et données partagées par l'ensemble de l'application
App.run(['$rootScope', function ($rootScope) {
	// permet de savoir si l'utilisation est authentifié
	$rootScope.authenticaded = null;

	$rootScope.admin = null;
	
	$rootScope.loggeduser = null;
}])

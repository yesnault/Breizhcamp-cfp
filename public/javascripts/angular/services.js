'use strict';

/* Services */

angular.module('breizhCampCFP.services', [])
	.factory('userService', ['$http', '$log', '$location', function(http, logger, location) {
		// Service pour gérer les utilisateurs
		function UserService(http, logger) {
			var userdata = null;
			var authenticated = null;
			var admin = false;
		
			// Fonction de login
			this.login = function(user, route) {
		
				logger.info("Tentative d'authentification de " + user.email);
		
				http({
					method : 'POST',
					url : '/login',
					data : user
				}).success(function(data, status, headers, config) {

					logger.info(status);
					logger.info(data);
					
					userdata = data;
					authenticated = true;
					if (userdata.admin) admin = true;
					logger.info('routage vers le dashboard');
					location.url(route);
					
				}).error(function(data, status, headers, config) {
					logger.info('code http de la réponse : ' + status);
					logger.info(data);
					});
			   };
			
				// Getters
				this.getUserData = function() {
					return userdata;
				};
				
				this.isAuthenticated = function() {
					return authenticated;
				};
				
				this.isAdmin = function() {
					return admin;
				};
		};
		// instanciation du service
		return new UserService(http, logger);
	}]);

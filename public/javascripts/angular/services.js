'use strict';

/* Services */

var Services = angular.module('breizhCampCFP.services', ['ngResource', 'ngCookies']);

Services.factory('UserService', ['$http', '$log', '$location', '$cookieStore', function(http, logger, location, $cookieStore) {
		// Service pour gérer les utilisateurs
		function UserService(http, logger) {
			var userdata = null;
			var authenticated = false;
			var admin = null;

            this.logout = function() {
                var user = this.getUserData();
                logger.info("deconnexion de " + user.email);


                http({
                    method : 'GET',
                    url : '/logout'
                }).success(
                    function(data, status, headers, config) {
                        // Suppression du cookie.
                        $cookieStore.remove('userData');
                        userdata = null;
                        authenticated = false;
                        admin = null;
                        location.url("/");
                    })

            };
		
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
					$cookieStore.put('userData', data);

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
                    if (userdata == null) {
                        if ($cookieStore.get('userData') !== undefined) {
                            userdata = $cookieStore.get('userData');
                        }
                    }
					return userdata;
				};
				
				this.isAuthenticated = function() {
                    if (authenticated == false) {
                        if (this.getUserData() != null) {
                            authenticated = true;
                        }
                    }
					return authenticated;
				};
				
				this.isAdmin = function() {
                    if (admin == null && this.getUserData() != null) {
                        admin = this.getUserData().admin;
                    }
					return admin;
				};
		}
		// instanciation du service
		return new UserService(http, logger);
	}]);

Services.factory('AccountService', function($resource) {
    function AccountService($resource) {
        this.getUser = function (idUser) {
            return $resource('/settings/user/:id').get({id:idUser});
        }
    }

    return new AccountService($resource);
});

Services.factory('TalkService', function($resource) {
        return $resource('/talk/:id', {});
    });

Services.factory('AllTalkService', function($resource) {
    return $resource('/talk/all', {});
});

Services.factory('ManageUsersService', function($resource) {
        return $resource('/admin/users/get', {});
    });
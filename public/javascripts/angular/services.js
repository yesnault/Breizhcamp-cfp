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
			this.login = function(user, route, failledCallBack) {
		
				logger.info("Tentative d'authentification de " + user);
		
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
                    failledCallBack(data);
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

Services.factory('ProfilService', function($resource) {
    function ProfilService($resource) {
        this.getTalks = function (userId) {
            return $resource('/user/:userId/talks').query({userId:userId});
        }
    }

    return new ProfilService($resource);
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



Services.factory('PasswordService', function() {

    function PasswordService() {

        this.getPasswordStrength = function(H) {
            var A, B, C, D, E, F, G, I;
            D = H.length;
            if (D > 5) {
                D = 5;
            }
            F = H.replace(/[0-9]/g, "");
            G = H.length - F.length;
            if (G > 3) {
                G = 3;
            }
            A = H.replace(/\W/g, "");
            C = H.length - A.length;
            if (C > 3) {
                C = 3;
            }
            B = H.replace(/[A-Z]/g, "");
            I = H.length - B.length;
            if (I > 3) {
                I = 3;
            }
            E = ((D * 10) - 20) + (G * 10) + (C * 15) + (I * 10);
            if (E < 0) {
                E = 0;
            }
            if (E > 100) {
                E = 100;
            }
            return E;
        };

        this.randomPassword = function() {
            var $max, $num, $temp, chars, i, ret, size;
            chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$_+";
            size = 15;
            i = 1;
            ret = "";
            while (i <= size) {
                $max = chars.length - 1;
                $num = Math.floor(Math.random() * $max);
                $temp = chars.substr($num, 1);
                ret += $temp;
                i++;
            }
            return ret;
        };

    }

    return new PasswordService();

});
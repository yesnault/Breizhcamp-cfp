'use strict';

/* Services */

var Services = angular.module('breizhCampCFP.services', ['ngResource', 'ngCookies']);

Services.factory('UserService', ['$http', '$log', '$location','$resource', '$cookieStore', function(http, logger, location,$resource, $cookieStore) {

        // Service pour gérer les utilisateurs
        function UserService(http, logger) {
            var authenticated;
            var admin;
            var superAdmin;
            var currentEvent;

            this.logout = function() {
                var user = this.getUserData();
                logger.info("deconnexion de " + user.email);

                http({
                    method: 'GET',
                    url: '/logout'
                }).success(
                        function(data, status, headers, config) {
                            // Suppression du cookie.
                            authenticated = false;
                            admin = null;
                            // Force page reload - this will redirect to login page
                            location.reload(true);
                        })

            };

            // Getters
            this.getUserData = function() {
                return window.userData;
            };

            this.getEvent = function() {
                if(currentEvent == null){
                    currentEvent = $resource('/user/event').get();
                }
                return currentEvent;
            };


            this.isAuthenticated = function() {
                if (!authenticated) {
                    // Conversion en booleén (double not)
                    authenticated = !!this.getUserData();
                }
                return authenticated;
            };

            this.isSuperAdmin = function() {
                if (superAdmin == null && this.getUserData() != null) {
                    superAdmin = this.getUserData().admin;
                }
                return superAdmin;
            };

            this.isAdmin = function() {
                if (admin == null && this.getUserData() != null) {
                    this.getEvent();
                    angular.forEach(this.getUserData().events, function (event) {

                        if (event.id === currentEvent.id) {
                            admin = true;
                        }
                    });
                }
                return admin;
            };
        }
        // instanciation du service
        return new UserService(http, logger);
    }]);

Services.factory('AccountService', function($resource) {
    function AccountService($resource) {
        this.getUser = function() {
            //return $resource('/settings/user/:id').get({id:idUser});
            return $resource('/userLogged').get();
        }

        this.getLinkType = function() {
            return $resource('/settings/link/types').query();
        }
    }

    return new AccountService($resource);
});

Services.factory('ProfilService', function($resource) {
    function ProfilService($resource) {
        this.getProposals = function(userId) {
            return $resource('/user/:userId/proposals').query({userId: userId});
        };

        this.getAcceptedProposals = function(userId) {
            return $resource('/user/:userId/proposals/A').query({userId: userId});
        };

        this.getUser = function(idUser) {
            return $resource('/user/:id').get({id: idUser});
        }
    }

    return new ProfilService($resource);
});

Services.factory('ProposalService', function($resource) {
    return $resource('/proposal/:id', {});
});

Services.factory('TrackProposalService', function($resource) {
    return $resource('/track/proposals/:id', {});
});

Services.factory('SubmitProposalService', function($resource) {
    return $resource('/proposal/submit/:id', {});
});


Services.factory('AllProposalService', function($resource) {
    return $resource('/proposal/all', {});
});

Services.factory('CreneauxService', function($resource) {
    return $resource('/format/:id', {});
});

Services.factory('DynamicFieldsService', function($resource) {
    return $resource('/dynamicfield/:id', {});
});


Services.factory('EventService', function($resource) {
      return $resource('/event/:id', {});
});

Services.factory('EventOrganizersService', function($resource) {
    return $resource('/event/:id/organizers', {});
});



Services.factory('TrackService', function($resource) {
    return $resource('/track/:id', {});
});

Services.factory('VoteService', function($resource, $http, $log) {
    function VoteService($resource, $http, $log) {

        this.getVote = function() {
            return $resource('/admin/vote', {}).get();
        }
    }
    return new VoteService($resource, $http, $log);
});

Services.factory('StatService', function($resource) {
    function StatService($resource) {
        this.getProposalStat = function() {
            return $resource('/proposalStat').get();
        }
    }
    return new StatService($resource);
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
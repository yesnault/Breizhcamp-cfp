'use strict';

/* Controllers */
function RootController($scope, UserService, $log, $location) {
	$scope.userService = UserService;

    $scope.logout = function() {
        UserService.logout();

    };

    $scope.checkloc = function(mustBeAdmin) {
        var user = UserService.getUserData();
        if (user == null) {
            $location.url("/login");
        } else {
            if (mustBeAdmin && !UserService.isAdmin()) {
                $location.url("/");
            }
        }
    }
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
RootController.$inject = ['$scope', 'UserService', '$log', '$location'];

function DashboardController($scope) {
    $scope.checkloc(false);
}
DashboardController.$inject = ['$scope'];

function LoginController($scope, $log, UserService, PasswordService, $http, $location) {

    // Si l'utilisateur est déjà loggué, on le redirige vers /
    var user = UserService.getUserData();
    if (user != null) {
        $location.url("/");
    }


	// Fonction de login appelée sur le bouton de formulaire
	$scope.login = function() {
		$log.info($scope.user);
        $log.info($scope);
		// TODO Trouver un moyen pour que le routage ne soit pas fait dans le callback du XHR ?
		UserService.login($scope.user, '/dashboard', function(data){
            $scope.errors = data;
        });
	};

    $scope.generatePassword = function() {
        $scope.generatedPassword = PasswordService.randomPassword();
        $scope.changeStrength($scope.generatedPassword, '#passwordStrengthDiv2')
    };

    $scope.changeStrength = function(password, divSelected) {

        var strength = PasswordService.getPasswordStrength(password);

        var percent = Math.floor(strength / 10) * 10;

        $(divSelected).removeClass('is10');
        $(divSelected).removeClass('is20');
        $(divSelected).removeClass('is30');
        $(divSelected).removeClass('is40');
        $(divSelected).removeClass('is50');
        $(divSelected).removeClass('is60');
        $(divSelected).removeClass('is70');
        $(divSelected).removeClass('is80');
        $(divSelected).removeClass('is90');
        $(divSelected).removeClass('is100');

        $(divSelected).addClass('is' + percent);
    };

    $scope.signup = function() {
        var newUser = $scope.new;

        $http({
            method : 'POST',
            url : 'signup',
            data : newUser
        }).success(function(data, status, headers, config) {
              $location.url("signup");
            }).error(function(data, status, headers, config) {
                $scope.newerrors = data;
            });
    }
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = ['$scope', '$log', 'UserService', 'PasswordService', '$http', '$location'];



function NewTalkController($scope, $log, $location, TalkService) {

    $scope.checkloc(false);

	$scope.$location = $location;
	
	$scope.isNew = true;

	$scope.saveTalk = function() {
		$log.info("Soummission du nouveau talk");
		
		TalkService.save($scope.talk, function(data) {	
			$log.info("Soummission du talk ok");
			$location.url('/managetalk');
		}, function (err) {
			$log.info("Soummission du talk ko");
            $log.info(err.data);
            $scope.errors = err.data;
		});	
	}
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
NewTalkController.$inject = ['$scope', '$log', '$location', 'TalkService'];

function EditTalkController($scope, $log, $location, $routeParams, TalkService, http) {

    $scope.checkloc(false);

	$scope.talk = TalkService.get({id:$routeParams.talkId});
	$scope.$location = $location;
	
	$scope.isNew = false;
	
	$scope.saveTalk = function() {
		$log.info("Sauvegarde du talk : " + $routeParams.talkId);
		
		TalkService.save($scope.talk, function(data) {	
			$log.info("Soummission du talk ok");
			$location.url('/managetalk');
		}, function (err) {
            $log.info("Soummission du talk ko");
            $log.info(err.data);
            $scope.errors = err.data;
		});
		
	};

    $scope.addTag = function() {
        $log.info("Ajout de tags " + $scope.tags);

        var data = {'tags' : $scope.talk.tags,'idTalk' : $scope.talk.id};

        http({
            method : 'POST',
            url : '/talk/' + $scope.talk.id + '/tags/'+$scope.talk.tags,
            data : data
        }).success(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.talk = TalkService.get({id:$routeParams.talkId});
            }).error(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    }
	
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
EditTalkController.$inject = ['$scope', '$log', '$location', '$routeParams', 'TalkService', '$http'];


function ManageTalkController($scope, $log, $location, TalkService) {

    $scope.checkloc(false);
	
	$scope.talks = TalkService.query();
	
	$scope.deleteTalk = function(talk) {
		$log.info("Delete du talk " + talk.id);
		TalkService.delete({'id': talk.id}, function(data) {
			$log.info("Delete du talk ok");
			$location.url('/managetalk');
	    }, function(err) {
	    	  $log.info("Delete du talk ko : " + err);
	    });
	}
	
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
ManageTalkController.$inject = ['$scope', '$log', '$location', 'TalkService'];


function ManageUsersController($scope, $log, $location, ManageUsersService, http) {

    $scope.checkloc(true);

    $scope.users = ManageUsersService.query();

    $scope.submitUsers = function() {

        var data = new Object();


        $.each($scope.users, function(index, value) {
            data[value.email] = value.admin;
        });


        http({
            method : 'POST',
            url : '/admin/submitusers',
            data : data
        }).success(function(data, status, headers, config) {
                $('#messageSuccess').text('Utilisateurs sauvegardés');
                $('#messageSuccess').removeClass('hide');
                $('#messageError').addClass('hide');
            }).error(function(data, status, headers, config) {
                $log.info('code http de la réponse : ' + status);
                $('#messageError').text('Une erreur a eu lieu pendant la sauvegarde des utilisateurs (' + status + ')');
                $('#messageSuccess').addClass('hide');
                $('#messageError').removeClass('hide');
            });
    };

}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
ManageUsersController.$inject = ['$scope', '$log', '$location', 'ManageUsersService', '$http'];

function ListTalksController($scope, $log, AllTalkService) {

    $scope.checkloc(true);

    $scope.talks = AllTalkService.query();
}
ListTalksController.$inject = ['$scope', '$log', 'AllTalkService'];

function SeeTalksController($scope, $log, $routeParams, TalkService, http) {

    $scope.checkloc(false);

    $scope.talk = TalkService.get({id:$routeParams.talkId});

    $scope.postComment = function() {
        $log.info("Sauvegarde du commentaire " + $scope.comment);

        var data = {'comment' : $scope.comment};

        http({
            method : 'POST',
            url : '/talks/' + $scope.talk.id + '/comment',
            data : data
        }).success(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.talk = TalkService.get({id:$routeParams.talkId});
            }).error(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.postStatus = function() {
        $log.info("postStatus");

        var data = {'status' : $scope.talk.statusTalk};

        http({
            method : 'POST',
            url : '/talks/' + $scope.talk.id + '/status',
            data : data
        }).success(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.talk = TalkService.get({id:$routeParams.talkId});
            }).error(function(data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    }
}
SeeTalksController.$inject = ['$scope', '$log', '$routeParams', 'TalkService', '$http'];

function ProfilController($scope, $log, $routeParams, AccountService, ProfilService, http) {

    $scope.checkloc(false);

    var idUSer = $routeParams.userId;
    $scope.user = AccountService.getUser(idUSer);

    $scope.talks = ProfilService.getTalks(idUSer);


}
ProfilController.$inject = ['$scope', '$log','$routeParams', 'AccountService', 'ProfilService', '$http'];



function SettingsAccountController($scope, $log, AccountService, UserService, http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.removeLink = function (lien) {
        if (confirm('Êtes vous sûr de vouloir supprimer le lien ' + lien.label + '?')) {
            $log.info("Suppression du lien "  + lien.label + '(' + lien.id + ')');
            http({
                method : 'GET',
                url : '/settings/lien/remove/' + lien.id
            }).success(function(data, status, headers, config) {
                    $scope.errors = undefined;
                    var idUser = UserService.getUserData().id;
                    $scope.user = AccountService.getUser(idUser);
                }).error(function(data, status, headers, config) {
                    $log.info(status);
                    $scope.errors = data;
                });
        }
    };

    $scope.saveSettings = function() {
        var user = jQuery.extend(true, {}, $scope.user);
        if ($scope.lien !== undefined) {
            user.liens.push($scope.lien);
        }
        http({
            method : 'POST',
            url : '/settings/account',
            data : user
        }).success(function(data, status, headers, config) {
                $scope.errors = undefined;
                var idUser = UserService.getUserData().id;
                $scope.lien = undefined;
                $scope.user = AccountService.getUser(idUser);
            }).error(function(data, status, headers, config) {
                $scope.errors = data;
                $log.info(status);
            });

    }
}
SettingsAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http'];

function NotifsAccountController($scope, $log, AccountService, UserService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.saveSettings = function() {
        var user = jQuery.extend(true, {}, $scope.user);
        $http({
            method : 'POST',
            url : '/settings/notifs',
            data : user
        }).success(function(data, status, headers, config) {
                $('#messageError').addClass('hide');
                $('#messageSuccess').text('Settings sauvegardés');
                $('#messageSuccess').removeClass('hide');
                var idUser = UserService.getUserData().id;
                $scope.user = AccountService.getUser(idUser);
            }).error(function(data, status, headers, config) {
                $('#messageError').text('Une erreur a eu lieu pendant la sauvegarde des settings (' + status + ')');
                $('#messageError').removeClass('hide');
                $('#messageSuccess').addClass('hide');
                $log.info(status);
            });
    };
}

NotifsAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http'];

function PasswordAccountController($scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.resetPassword = function() {
        $http( {
            method : 'POST',
            url : '/settings/password'
        }).success(function(data, status, headers, config) {
                $('#messageError').addClass('hide');
                $('#messageSuccess').text('Un mail a été envoyé. Merci de vérifier vos mails.');
                $('#messageSuccess').removeClass('hide');
            }).error(function(data, status, headers, config) {
                $('#messageError').text('Une erreur a eu lieu pendant le reset du password (' + status + ')');
                $('#messageError').removeClass('hide');
                $('#messageSuccess').addClass('hide');
                $log.info(status);
            });
    }

}

PasswordAccountController.$inject = ['$scope', '$log', 'UserService', 'AccountService', '$http'];

function EmailAccountController($scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.changeEmail = function() {
        $http( {
            method : 'POST',
            url : '/settings/email',
            data: $scope.user
        }).success(function(data, status, headers, config) {
                $('#messageSuccess').text('Un mail a été envoyé. Merci de vérifier vos mails.');
                $('#messageSuccess').removeClass('hide');
                $scope.errors = undefined;
            }).error(function(data, status, headers, config) {
                $('#messageSuccess').addClass('hide');
                $scope.errors = data;
                $log.info(status);
            });
    }
}
EmailAccountController.$inject = ['$scope', '$log', 'UserService', 'AccountService', '$http'];

function MacAccountController($scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.changeMac = function() {
        $http( {
            method : 'POST',
            url : '/settings/mac',
            data: $scope.user
        }).success(function(data, status, headers, config) {
                $('#messageSuccess').text('Votre adresse mac a été enregistrée.');
                $('#messageSuccess').removeClass('hide');
                $scope.errors = undefined;
            }).error(function(data, status, headers, config) {
                $('#messageSuccess').addClass('hide');
                $scope.errors = data;
                $log.info(status);
            });
    }
}
MacAccountController.$inject = ['$scope', '$log', 'UserService', 'AccountService', '$http'];

function ResetPasswordController($scope, $log, $http) {

    $scope.resetPassword = function () {
        var data = new Object();
        data.email = $scope.email;
        $http({
            method:'POST',
            url:'/reset/ask',
            data:data
        }).success(function (data, status, headers, config) {
                $('#fieldEmail').addClass('hide');
                $('#valider').addClass('hide');
                $('#messageError').addClass('hide');
                $('#messageSuccess').text('Un mail a été envoyé. Merci de vérifier vos mails.');
                $('#messageSuccess').removeClass('hide');
            }).error(function (data, status, headers, config) {
                $('#messageError').text('Une erreur a eu lieu pendant le reset du password (' + status + ')');
                $('#messageError').removeClass('hide');
                $('#messageSuccess').addClass('hide');
                $log.info(status);
            });
    }

}

ResetPasswordController.$inject = ['$scope', '$log', '$http'];

function ConfirmSignupController($scope, $log, $http, $routeParams) {
    var token = $routeParams.token;

    $http({
        method:'GET',
        url:'/confirm/' + token
    }).success(function (data, status, headers, config) {
            $scope.successMessage = 'Votre compte est validé';
            $scope.showSuccess = true;
        }).error(function (data, status, headers, config) {
            $scope.errorMessage = 'Une erreur a eu lieu pendant la confirmation (' + status + ')';
            $scope.showError = true;
        });
}

ConfirmSignupController.$inject = ['$scope', '$log', '$http', '$routeParams'];

function ConfirmResetPasswordController($scope, $log, $http, $routeParams, PasswordService) {



    $scope.generatePassword = function() {
        $scope.generatedPassword = PasswordService.randomPassword();
        $scope.changeStrength($scope.generatedPassword, '#passwordStrengthDiv2')
    };

    $scope.changeStrength = function(password, divSelected) {

        var strength = PasswordService.getPasswordStrength(password);

        var percent = Math.floor(strength / 10) * 10;

        $(divSelected).removeClass('is10');
        $(divSelected).removeClass('is20');
        $(divSelected).removeClass('is30');
        $(divSelected).removeClass('is40');
        $(divSelected).removeClass('is50');
        $(divSelected).removeClass('is60');
        $(divSelected).removeClass('is70');
        $(divSelected).removeClass('is80');
        $(divSelected).removeClass('is90');
        $(divSelected).removeClass('is100');

        $(divSelected).addClass('is' + percent);
    };

    $scope.resetPassword = function() {
        var data = new Object();
        data.inputPassword = $scope.inputPassword;
        var token = $routeParams.token;
        $http({
            method:'POST',
            url:'/reset/' + token,
            data: data
        }).success(function (data, status, headers, config) {
                $scope.successMessage = 'Votre nouveau mot de passe est enregistré.';
                $('#valider').addClass('hide');
                $scope.showSuccess = true;
            }).error(function (data, status, headers, config) {
                $scope.errorMessage = 'Une erreur a eu lieu pendant le changement de mot de passe (' + status + ')';
                $scope.showError = true;
            });
    }
}

ConfirmResetPasswordController.$inject = ['$scope', '$log', '$http', '$routeParams', 'PasswordService'];

function ConfirmEmailController($scope, $log, $http, $routeParams, UserService, AccountService) {
    var token = $routeParams.token;
    if (UserService.getUserData() != null) {
        var idUser = UserService.getUserData().id;
        $scope.user = AccountService.getUser(idUser);
    }

    $http({
        method:'GET',
        url:'/email/' + token
    }).success(function (data, status, headers, config) {
            // TODO ajouter la nouvelle adresse email dans le message, une fois la vue scale supprimée.
            $scope.successMessage = 'Votre nouvelle adresse mail est validée';
            $scope.showSuccess = true;
        }).error(function (data, status, headers, config) {
            $scope.errorMessage = "Une erreur a eu lieu pendant le changement d'adresse (" + status + ')';
            $scope.showError = true;
        });
}

ConfirmEmailController.$inject = ['$scope', '$log', '$http', '$routeParams', 'UserService', 'AccountService'];






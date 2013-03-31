'use strict';

Array.prototype.contains = function ( needle ) {
    var i;
    for (i in this) {
        if (this[i] == needle) return true;
    }
    return false;
}

/* Controllers */

// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
RootController.$inject = ['$scope', 'UserService', '$log', '$location'];
function RootController($scope, UserService, $log, $location) {
    $scope.userService = UserService;

    $scope.logout = function() {
        UserService.logout();
    };

    $scope.verifyUser = function(user, mustBeAdmin) {
        if (!user) {
            $location.url("/login");
        } else {
            if (mustBeAdmin && !user.admin) {
                $location.url("/");
            }
        }
    };

    $scope.checkloc = function(mustBeAdmin) {
        var user = UserService.getUserData();
        if (!user) {
            UserService.isLogged(function(){
                $scope.verifyUser(UserService.getUserData(), mustBeAdmin);
            },
            function(){
                $scope.verifyUser(UserService.getUserData(), mustBeAdmin);
            })
        } else {
            $scope.verifyUser(user, mustBeAdmin);
        }
    };
}


DashboardController.$inject = ['$rootScope', '$scope', 'ProfilService', 'AccountService', 'UserService', '$log'];
function DashboardController($rootScope, $scope, ProfilService, AccountService, UserService, $log) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $rootScope.user = UserService.getUserData();
    $scope.talks = ProfilService.getTalks(idUser);
    $scope.talksok = ProfilService.getTalksAccepted(idUser);
    $scope.talksko = ProfilService.getTalksRefused(idUser);
    $scope.talks_w = ProfilService.getTalksWait(idUser);


}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = ['$scope', '$log', 'UserService', 'PasswordService', '$http', '$location', '$cookies', '$cookieStore'];
function LoginController($scope, $log, UserService, PasswordService, $http, $location, $cookies) {

    // Si l'utilisateur est déjà loggué, on le redirige vers /
//    var user = UserService.getUserData();
//    if (user != null) {
//        $location.url("/");
//    }

    $log.info("appel a isLogged");
    $scope.userlogged = UserService.isLogged(function(){
        $location.url("/dashboard")
    }, function() {
        $location.url("/login");
    });

    $scope.debug = function() {
        //playCookie = $cookieStore.get('PLAY_SESSION');
        $log.info('cookie play : ' + $scope.cookieValue);
    };

    // Fonction de login appelée sur le bouton de formulaire
    $scope.login = function() {
        $log.info($scope.user);
        $log.info($scope);
        // TODO Trouver un moyen pour que le routage ne soit pas fait dans le callback du XHR ?
        UserService.login($scope.user, '/dashboard', function(data) {
            $scope.errors = data;
        });
    };
    
    $scope.createAccount = function () {
        $log.info($scope.email);
        $http({method: 'POST', url: '/signup', data: '{email: "' + $scope.email+'"}'}).
                success(function(data, status){
                    $log.info('createAccount XHR status : ' + status);
                }).
                error(function(data, status){
                    $log.error('createAccount XHR error - status : ' + status);
                });
    };

}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
NewTalkController.$inject = ['$scope', '$log', '$location', 'TalkService', 'CreneauxService'];
function NewTalkController($scope, $log, $location, TalkService, CreneauxService) {

    $scope.checkloc(false);

    $scope.$location = $location;

    $scope.isNew = true;

    $scope.creneaux = CreneauxService.query();

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveTalk = function() {
        $log.info("Soummission du nouveau talk");

        TalkService.save($scope.talk, function(data) {
            $log.info("Soummission du talk ok");
            $location.url('/managetalk');
        }, function(err) {
            $log.info("Soummission du talk ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
EditTalkController.$inject = ['$scope', '$log', '$location', '$routeParams', 'TalkService', '$http', 'CreneauxService'];
function EditTalkController($scope, $log, $location, $routeParams, TalkService, http, CreneauxService) {

    $scope.checkloc(false);

    $scope.talk = TalkService.get({id: $routeParams.talkId});

    $scope.$location = $location;

    $scope.isNew = false;

    $scope.creneaux = CreneauxService.query();

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveTalk = function() {
        $log.info("Sauvegarde du talk : " + $routeParams.talkId);
        // Contournement pour ne pas soumettre l'objet speaker dans le POST JSON
        $scope.talk.speaker = null;

        TalkService.save($scope.talk, function(data) {
            $log.info("Soummission du talk ok");
            $location.url('/managetalk');
        }, function(err) {
            $log.info("Soummission du talk ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });

    };

    $scope.addTag = function() {
        $log.info("Ajout de tags " + $scope.tags);

        var data = {'tags': $scope.talk.tagsname, 'idTalk': $scope.talk.id};

        http({
            method: 'POST',
            url: '/talk/' + $scope.talk.id + '/tags/' + $scope.talk.tagsname,
            data: data
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    }

}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
ManageTalkController.$inject = ['$scope', '$log', '$location', 'TalkService'];
function ManageTalkController($scope, $log, $location, TalkService) {

    $scope.checkloc(false);

    $scope.talks = TalkService.query();

    $scope.deleteTalk = function(talk) {
        var confirmation = confirm('Êtes vous sûr de vouloir supprimer le talk "' + talk.title + '" ?');
        if (confirmation) {
            TalkService.delete({'id': talk.id}, function(data) {
                $scope.talks = TalkService.query();
                $scope.errors = undefined;
            }, function(err) {
                $log.info("Delete du talk ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }

}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
ManageUsersController.$inject = ['$scope', '$log', '$location', 'ManageUsersService', '$http'];
function ManageUsersController($scope, $log, $location, ManageUsersService, http) {

    $scope.checkloc(true);

    $scope.users = ManageUsersService.query();

    $scope.submitUsers = function() {

        var data = {};


        $.each($scope.users, function(index, value) {
            data[value.email] = value.admin;
        });


        http({
            method: 'POST',
            url: '/admin/submitusers',
            data: data
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

    $scope.deleteCompte = function(id) {
        var data = {"id":id};
        $log.info("suppression du compte " + id);
        http({
            method: 'POST',
            url: '/admin/deleteuser/'+id,
            data: data
        }).success(function(data, status, headers, config) {
                $('#messageSuccess').text('Utilisateur supprimer');
                $('#messageSuccess').removeClass('hide');
                $('#messageError').addClass('hide');
                $('#deleteCompte'+id).modal('hide');
                $scope.users = ManageUsersService.query();
            })
    }
 }

ListTalksController.$inject = ['$scope', '$log', 'AllTalkService', 'VoteService'];
function ListTalksController($scope, $log, AllTalkService, VoteService) {

    $scope.checkloc(true);

    $scope.status = ['ACCEPTE','ATTENTE','REJETE', 'NULL'];

    $scope.talks = AllTalkService.query();

    $scope.vote = VoteService.getVote();

    $scope.predicate = 'moyenne';

    $scope.reverse = true;

    $scope.talksAcceptes = function(talk){
        return talk.statusTalk =='ACCEPTE';
    };

    $scope.doStatus = function(talk){
        $log.info('call doStatus');
        return (talk.statusTalk == undefined && $scope.status.contains("NULL")) || $scope.status.contains(talk.statusTalk);
    };
}


VoteController.$inject = ['$scope', '$log', 'VoteService', '$http'];
function VoteController($scope, $log, VoteService, $http) {

    $scope.checkloc(true);

    $scope.vote = VoteService.getVote();

    $log.info($scope.vote);

    $scope.submitVote = function() {
        var vote = $scope.vote;
        $http({
            method: 'POST',
            url: '/admin/vote/' + vote.status
        }).success(function() {
            $scope.error = undefined;
            $scope.success = "Le changement de status du votes a bien été pris en compte."
        }).error(function() {
            $scope.error = "Une erreur est survenue pendant le changement de status des votes";
            $scope.success = undefined;
        });
    }
}


SeeTalksController.$inject = ['$scope', '$log', '$routeParams', 'TalkService', '$http', 'VoteService'];
function SeeTalksController($scope, $log, $routeParams, TalkService, http, VoteService) {

    $scope.checkloc(false);

    $scope.talk = TalkService.get({id: $routeParams.talkId});

    $scope.voteStatus = VoteService.getVote();

    $scope.commentE = Array();

    $scope.converter = new Markdown.getSanitizingConverter();

    $scope.getSafeDescription = function() {
        if ($scope.talk.description) {
            return $scope.converter.makeHtml($scope.talk.description);
        }
    }

    $scope.postComment = function() {
        $log.info("Sauvegarde du commentaire " + $scope.comment);

        var data = {'comment': $scope.comment, 'private': $scope.private};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/comment',
            data: data
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = null;
            $scope.comment = null;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.postReponse = function(id) {
        $log.info("Sauvegarde de la reponse " + $scope.commentR);

        var commentId = id;
        var dataR = {'comment': $scope.commentR, 'private': $scope.privateR};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/comment/' + commentId + '/response',
            data: dataR
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.commentR = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.editComment = function(id) {
        $log.info("modification du commentaire " + $scope.commentE[id]);

        var commentId = id;
        var dataR = {'comment': $scope.commentE[id]};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/comment/' + commentId + '/edit',
            data: dataR
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.commentE = Array();
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.postCloseComment = function(id) {
        $log.info("cloture du commentaire " + id);

        var data = {};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/comment/' + id + '/close',
            data: data
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.deleteComment = function(id) {
        $log.info("suppression du commentaire " + id);

        var data = {};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/comment/' + id + '/delete',
            data: data
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.postStatus = function() {
        $log.info("postStatus");

        var data = {'status': $scope.talk.statusTalk};

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/status',
            data: data
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    };

    $scope.note = function(talk) {
        $.fn.raty.defaults.path = '/assets/img/';
        $log.info("talk : " + talk.note);
        $('#star').raty({
            score: talk.note != undefined ? talk.note : 1,
            click: function(score, evt) {
                $scope.note = score;
                $('#note').val(score);
            }
        });
    };

    $scope.postVote = function() {
        $log.info("postVote");
        $log.info($scope.talk);

        http({
            method: 'POST',
            url: '/talks/' + $scope.talk.id + '/vote/' + $scope.note
        }).success(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = undefined;
            $scope.talk = TalkService.get({id: $routeParams.talkId});
        }).error(function(data, status, headers, config) {
            $log.info(status);
            $scope.errors = data;
        });
    }
}

ProfilController.$inject = ['$scope', '$log', '$routeParams', 'AccountService', 'ProfilService', 'UserService', '$http'];
function ProfilController($scope, $log, $routeParams, AccountService, ProfilService, UserService, http) {

    $scope.checkloc(false);

    $scope.converter = new Markdown.getSanitizingConverter();

    var idUSer = $routeParams.userId;
    $scope.pUser = ProfilService.getUser(idUSer);
    $scope.talks = ProfilService.getTalks(idUSer);
    $scope.talksok = ProfilService.getTalksAccepted(idUSer);

    $scope.getSafeDescription = function() {
        if ($scope.pUser.description) {
            return $scope.converter.makeHtml($scope.pUser.description);
        }
    }
}


SettingsAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http', '$location'];
function SettingsAccountController($scope, $log, AccountService, UserService, http, $location) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();
    
    $scope.removeLink = function(lien) {
        if (confirm('Êtes vous sûr de vouloir supprimer le lien ' + lien.label + '?')) {
            $log.info("Suppression du lien " + lien.label + '(' + lien.id + ')');
            http({
                method: 'GET',
                url: '/settings/lien/remove/' + lien.id
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
            method: 'POST',
            url: '/settings/account',
            data: user
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

    $scope.appercu = function() {
        var user = jQuery.extend(true, {}, $scope.user);
        if ($scope.lien !== undefined) {
            user.liens.push($scope.lien);
        }
        http({
            method: 'POST',
            url: '/settings/account',
            data: user
        }).success(function(data, status, headers, config) {
            var idUser = UserService.getUserData().id;
            $location.path('/profil/' + idUser)
        }).error(function(data, status, headers, config) {
            $scope.errors = data;
            $log.info(status);
        });
    }
}

NotifsAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http'];
function NotifsAccountController($scope, $log, AccountService, UserService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.saveSettings = function() {
        var user = jQuery.extend(true, {}, $scope.user);
        $http({
            method: 'POST',
            url: '/settings/notifs',
            data: user
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


EmailAccountController.$inject = ['$rootScope','$scope', '$log', 'UserService', 'AccountService', '$http'];
function EmailAccountController($rootScope,$scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id; 
    $scope.user = $rootScope.user;

    $scope.changeEmail = function() {
        $http({
            method: 'POST',
            url: '/settings/email',
            data: $scope.user
        }).success(function(data, status, headers, config) {
            $('#messageSuccess').text('Merci. Cet email nous servira à vous contacter.');
            $('#messageSuccess').removeClass('hide');
            $scope.errors = undefined;
            $rootScope.user.email = $scope.user.email;
        }).error(function(data, status, headers, config) {
            $('#messageSuccess').addClass('hide');
            $scope.errors = data;
            $log.info(status);
        });
    }
}

MacAccountController.$inject = ['$scope', '$log', 'UserService', 'AccountService', '$http'];
function MacAccountController($scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.changeMac = function() {
        $http({
            method: 'POST',
            url: '/settings/mac',
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

ResetPasswordController.$inject = ['$scope', '$log', '$http'];
function ResetPasswordController($scope, $log, $http) {

    $scope.resetPassword = function() {
        var data = new Object();
        data.email = $scope.email;
        $http({
            method: 'POST',
            url: '/reset/ask',
            data: data
        }).success(function(data, status, headers, config) {
            $('#fieldEmail').addClass('hide');
            $('#valider').addClass('hide');
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


ConfirmSignupController.$inject = ['$scope', '$log', '$http', '$routeParams'];
function ConfirmSignupController($scope, $log, $http, $routeParams) {
    var token = $routeParams.token;

    $http({
        method: 'GET',
        url: '/confirm/' + token
    }).success(function(data, status, headers, config) {
        $scope.successMessage = 'Votre compte est validé';
        $scope.showSuccess = true;
    }).error(function(data, status, headers, config) {
        $scope.errorMessage = 'Une erreur a eu lieu pendant la confirmation (' + status + ')';
        $scope.showError = true;
    });
}


ConfirmResetPasswordController.$inject = ['$scope', '$log', '$http', '$routeParams', 'PasswordService'];
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
            method: 'POST',
            url: '/reset/' + token,
            data: data
        }).success(function(data, status, headers, config) {
            $scope.successMessage = 'Votre nouveau mot de passe est enregistré.';
            $('#valider').addClass('hide');
            $scope.showSuccess = true;
        }).error(function(data, status, headers, config) {
            $scope.errorMessage = 'Une erreur a eu lieu pendant le changement de mot de passe (' + status + ')';
            $scope.showError = true;
        });
    }
}


ConfirmEmailController.$inject = ['$scope', '$log', '$http', '$routeParams', 'UserService', 'AccountService'];
function ConfirmEmailController($scope, $log, $http, $routeParams, UserService, AccountService) {
    var token = $routeParams.token;
    if (UserService.getUserData() != null) {
        var idUser = UserService.getUserData().id;
        $scope.user = AccountService.getUser(idUser);
    }

    $http({
        method: 'GET',
        url: '/email/' + token
    }).success(function(data, status, headers, config) {
        // TODO ajouter la nouvelle adresse email dans le message, une fois la vue scale supprimée.
        $scope.successMessage = 'Votre nouvelle adresse mail est validée';
        $scope.showSuccess = true;
    }).error(function(data, status, headers, config) {
        $scope.errorMessage = "Une erreur a eu lieu pendant le changement d'adresse (" + status + ')';
        $scope.showError = true;
    });
}


CreneauxController.$inject = ['$scope', '$log', 'CreneauxService'];
function CreneauxController($scope, $log, CreneauxService) {
    $scope.checkloc(true);

    $scope.creneaux = CreneauxService.query();

    $scope.deleteCreneau = function(creneauToDelete) {
        var confirmation = confirm('Êtes vous sûr de vouloir supprimer le creneau ' + creneauToDelete.libelle + '?');
        if (confirmation) {
            CreneauxService.delete({id: creneauToDelete.id}, function(data) {
                $scope.creneaux = CreneauxService.query();
                $scope.errors = undefined;
            }, function(err) {
                $log.info("Delete du creneau ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }
}


NewCreneauController.$inject = ['$scope', '$log', 'CreneauxService', '$location'];
function NewCreneauController($scope, $log, CreneauxService, $location) {
    $scope.checkloc(true);

    $scope.isNew = true;

    $scope.converter = new Markdown.Converter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveCreneau = function() {
        $log.info("Creneau à sauvegarder");
        $log.info($scope.creneau);

        CreneauxService.save($scope.creneau, function(data) {
            $log.info("Soummission du creneau ok");
            $location.url('/admin/creneaux');
        }, function(err) {
            $log.info("Soummission du creneau ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


EditCreneauController.$inject = ['$scope', '$log', 'CreneauxService', '$location', '$routeParams'];
function EditCreneauController($scope, $log, CreneauxService, $location, $routeParams) {
    $scope.checkloc(true);

    var idCreneau = $routeParams.creneauId;

    $scope.creneau = CreneauxService.get({id: idCreneau});

    $scope.isNew = false;

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveCreneau = function() {
        $log.info("Creneau à sauvegarder");
        $log.info($scope.creneau);

        CreneauxService.save($scope.creneau, function(data) {
            $log.info("Soummission du creneau ok");
            $location.url('/admin/creneaux');
        }, function(err) {
            $log.info("Soummission du creneau ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


DynamicFieldsController.$inject = ['$scope', '$log', 'DynamicFieldsService'];
function DynamicFieldsController($scope, $log, DynamicFieldsService) {
    $scope.checkloc(true);

    $scope.dynamicFields = DynamicFieldsService.query();

    $scope.deleteDynamicField = function(dynamicFieldToDelete) {
        var confirmation = confirm('Êtes vous sûr de vouloir supprimer le champ ' + dynamicFieldToDelete.name + '?');
        if (confirmation) {
            DynamicFieldsService.delete({id: dynamicFieldToDelete.id}, function(data) {
                $scope.dynamicFields = DynamicFieldsService.query();
                $scope.errors = undefined;
            }, function(err) {
                $log.info("Delete du champ dynamique ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }
}


NewDynamicFieldController.$inject = ['$scope', '$log', 'DynamicFieldsService', '$location'];
function NewDynamicFieldController($scope, $log, DynamicFieldsService, $location) {
    $scope.checkloc(true);

    $scope.isNew = true;

    $scope.saveDynamicField = function() {
        $log.info("Champ dynamique à sauvegarder");
        $log.info($scope.dynamicField);

        DynamicFieldsService.save($scope.dynamicField, function(data) {
            $log.info("Soummission du champ dynamique ok");
            $location.url('/admin/dynamicfields');
        }, function(err) {
            $log.info("Soummission du champ dynamique ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


EditDynamicFieldController.$inject = ['$scope', '$log', 'DynamicFieldsService', '$location', '$routeParams'];
function EditDynamicFieldController($scope, $log, DynamicFieldsService, $location, $routeParams) {
    $scope.checkloc(true);

    var idDynamicField = $routeParams.dynamicFieldId;

    $scope.dynamicField = DynamicFieldsService.get({id: idDynamicField});

    $scope.isNew = false;

    $scope.saveDynamicField = function() {
        $log.info("Champ dynamique à sauvegarder");
        $log.info($scope.dynamicField);

        DynamicFieldsService.save($scope.dynamicField, function(data) {
            $log.info("Soummission du champ dynamique ok");
            $location.url('/admin/dynamicfields');
        }, function(err) {
            $log.info("Soummission du champ dynamique ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}

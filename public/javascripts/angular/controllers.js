'use strict';

Array.prototype.contains = function (needle) {
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

    $scope.logout = function () {
        UserService.logout();
    };

    $scope.verifyUser = function (user, mustBeAdmin) {
        if (mustBeAdmin && !user.admin) {
            $location.url("/");
        } else if (!user.isInfoValid) {
            $location.url("/settings/account");
        }
    };

    $scope.checkloc = function (mustBeAdmin) {
        $log.info("check user is logged");
        var user = UserService.getUserData();
        $scope.verifyUser(UserService.getUserData(), mustBeAdmin);
    };
}


DashboardController.$inject = ['$rootScope', '$scope', '$http', 'ProfilService', 'AccountService', 'UserService', 'StatService','ProposalService', '$log'];
function DashboardController($rootScope, $scope, $http, ProfilService, AccountService, UserService, StatService,ProposalService, $log) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $rootScope.user = UserService.getUserData();

    $scope.proposals = ProfilService.getProposals(idUser);

    if (UserService.isAdmin()) {
        $scope.proposalstats = StatService.getProposalStat();
    }

    $scope.deleteProposal = function (proposal) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir supprimer le proposal "' + proposal.title + '" ?');
        if (confirmation) {
            ProposalService.delete({'id':proposal.id}, function (data) {
                $scope.proposals = ProposalService.query();
                $scope.errors = undefined;
            }, function (err) {
                $log.info("Delete du proposal ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }

    $scope.submitProposal = function (proposal) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir soumettre le proposal "' + proposal.title + '" a l\'équipe ?');
        if (confirmation) {
            $http({
                method:'POST',
                url:'/proposal/submit/' + proposal.id,
                data:{}
            }).success(function (data, status, headers, config) {
                    $scope.proposals = ProfilService.getProposals(idUser);
                    $scope.errors = undefined;
                }).error(function (data, status, headers, config) {
                    $log.info("soumission du proposal ko");
                    $log.info(status);
                    $scope.errors = status;
                });
        }
    }
}

// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
NewProposalController.$inject = ['$scope', '$log', '$location', 'ProposalService', 'CreneauxService', 'TrackService', '$http'];
function NewProposalController($scope, $log, $location, ProposalService, CreneauxService, TrackService, $http) {

    $scope.checkloc(false);

    $scope.$location = $location;

    $http.get("/user/cospeakers").success(function (data) {
        $scope.coSpeakers = data;
    });

    $scope.addSelectedCoSpeaker = function () {
        if ($scope.proposal === undefined) {
            $scope.proposal = {};
        }
        if ($scope.proposal.coSpeakers === undefined) {
            $scope.proposal.coSpeakers = [];
        }
        if ($scope.coSpeakerSelected !== undefined) {

            var found = false;

            angular.forEach($scope.proposal.coSpeakers, function (coSpeaker) {
                if (coSpeaker.id === $scope.coSpeakerSelected.id) {
                    found = true;
                }
            });

            if (!found) {
                $scope.proposal.coSpeakers.push($scope.coSpeakerSelected);
            }
            $scope.coSpeakerSelected = undefined;
        }
    };

    $scope.removeCoSpeaker = function (coSpeaker) {
        $scope.proposal.coSpeakers.splice($scope.proposal.coSpeakers.indexOf(coSpeaker), 1);
    };


    $scope.isNew = true;

    $scope.formats = CreneauxService.query();

    $scope.tracks = TrackService.query();

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.editorIndication = new Markdown.Editor($scope.converter, '-indications');
    $scope.editorIndication.run();

    $scope.saveProposal = function () {
        $log.info("Soummission du nouveau proposal");

        ProposalService.save($scope.proposal, function (data) {
            $log.info("Soummission du proposal ok");
            $location.url('/dashboard');
        }, function (err) {
            $log.info("Soummission du proposal ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    };

    $scope.changeFormat = changeFormat;
}


function changeFormat(newId, proposal, formats) {

    var found = false;
    angular.forEach(formats, function (format) {
        if (format.id === newId) {
            proposal.format = format;
            found = true;
        }
    });
    if (!found) {
        proposal.format = undefined;
    }
}

// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
EditProposalController.$inject = ['$scope', '$log', '$location', '$routeParams', 'ProposalService', '$http', 'CreneauxService', 'TrackService'];
function EditProposalController($scope, $log, $location, $routeParams, ProposalService, http, CreneauxService, TrackService) {

    $scope.checkloc(false);

    $scope.proposal = ProposalService.get({id:$routeParams.proposalId});

    http.get("/user/cospeakers").success(function (data) {
        $scope.coSpeakers = data;
    });

    $scope.addSelectedCoSpeaker = function () {
        if ($scope.proposal === undefined) {
            $scope.proposal = {};
        }
        if ($scope.proposal.coSpeakers === undefined) {
            $scope.proposal.coSpeakers = [];
        }
        if ($scope.coSpeakerSelected !== undefined) {

            var found = false;

            angular.forEach($scope.proposal.coSpeakers, function (coSpeaker) {
                if (coSpeaker.id === $scope.coSpeakerSelected.id) {
                    found = true;
                }
            });

            if (!found) {
                $scope.proposal.coSpeakers.push($scope.coSpeakerSelected);
            }
            $scope.coSpeakerSelected = undefined;
        }
    };

    $scope.removeCoSpeaker = function (coSpeaker) {
        $scope.proposal.coSpeakers.splice($scope.proposal.coSpeakers.indexOf(coSpeaker), 1);
    };

    $scope.$location = $location;

    $scope.isNew = false;

    $scope.formats = CreneauxService.query();
    $scope.tracks = TrackService.query();

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.editorIndication = new Markdown.Editor($scope.converter, '-indications');
    $scope.editorIndication.run();

    $scope.saveProposal = function () {
        $log.info("Sauvegarde du sujet : " + $routeParams.proposalId);
        // Contournement pour ne pas soumettre l'objet speaker dans le POST JSON
        $scope.proposal.speaker = null;
        $scope.proposal.comments = null;

        ProposalService.save($scope.proposal, function (data) {
            $log.info("Soummission du sujet ok");
            $location.url('/dashboard');
        }, function (err) {
            $log.info("Soummission du sujet ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });

    };

    $scope.addTag = function () {
        $log.info("Ajout de tags " + $scope.tags);

        var data = {'tags':$scope.proposal.tagsname, 'idProposal':$scope.proposal.id};

        http({
            method:'POST',
            url:'/proposal/' + $scope.proposal.id + '/tags/' + $scope.proposal.tagsname,
            data:data
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.changeFormat = changeFormat;
}


// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
ManageUsersController.$inject = ['$scope', '$log', '$location', 'ManageUsersService', '$http'];
function ManageUsersController($scope, $log, $location, ManageUsersService, http) {

    $scope.checkloc(true);

    $scope.users = ManageUsersService.query();

    $scope.submitUsers = function () {

        var data = {};


        $.each($scope.users, function (index, value) {
            data[value.email] = value.admin;
        });


        http({
            method:'POST',
            url:'/admin/submitusers',
            data:data
        }).success(function (data, status, headers, config) {
                $('#messageSuccess').text('Utilisateurs sauvegard\u00e9s');
                $('#messageSuccess').removeClass('hide');
                $('#messageError').addClass('hide');
            }).error(function (data, status, headers, config) {
                $log.info('code http de la réponse : ' + status);
                $('#messageError').text('Une erreur a eu lieu pendant la sauvegarde des utilisateurs (' + status + ')');
                $('#messageSuccess').addClass('hide');
                $('#messageError').removeClass('hide');
            });
    };

    $scope.deleteCompte = function (id) {
        var data = {"id":id};
        $log.info("suppression du compte " + id);
        http({
            method:'POST',
            url:'/admin/deleteuser/' + id,
            data:data
        }).success(function (data, status, headers, config) {
                $('#messageSuccess').text('Utilisateur supprim\u00e9');
                $('#messageSuccess').removeClass('hide');
                $('#messageError').addClass('hide');
                $('#deleteCompte' + id).modal('hide');
                $scope.users = ManageUsersService.query();
            })
    }
}

ListProposalsController.$inject = ['$scope', '$log', '$http', 'AllProposalService', 'VoteService', 'ProposalService', 'UserService', 'EventService'];
function ListProposalsController($scope, $log, http, AllProposalService, VoteService, ProposalService, UserService, EventService) {

    $scope.checkloc(true);

    $scope.status = ['ACCEPTED', 'WAITING', 'REJECTED', 'NULL'];

    $scope.proposals = AllProposalService.query();

    $scope.vote = VoteService.getVote();

    $scope.events = EventService.query();

    $scope.predicate = 'moyenne';

    $scope.reverse = true;

    $scope.proposalsAcceptes = function (proposal) {
        return proposal.statusProposal == 'ACCEPTED';
    };

    $scope.doStatus = function (proposal) {
        $log.info('call doStatus');
        return (proposal.statusProposal == undefined && $scope.status.contains("NULL")) || $scope.status.contains(proposal.statusProposal);
    };
    $scope.doEvent = function (proposal) {
        $log.info('call doEvent ' + $scope.event + ' ' + proposal.event);
        return (proposal.event == undefined ) || $scope.event == proposal.event.id;
    };

    $scope.deleteProposal = function (proposal) {
        $log.info('deleteProposal ' + proposal.title);
        ProposalService.delete({'id':proposal.id}, function (data) {
            $scope.proposals = ProposalService.query();
            $scope.errors = undefined;
            $('#messageSuccess').text('Proposal supprim\u00e9');
            $('#messageSuccess').removeClass('hide');
            $('#messageError').addClass('hide');
            $('#deleteProposal' + proposal.id).modal('hide');
        }, function (err) {
            $log.info("Delete du proposal ko");
            $log.info(err);
            $scope.errors = err.data;
        });
    };

    $scope.getProposalDetails = function (idProposal) {
        $scope.proposalModal = ProposalService.get({id:idProposal}, function success(data) {
            $scope.proposalModal = data;

        });
    };


    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.getSafeHtml = function (value) {
        if (value) {
            return $scope.converter.makeHtml(value);
        }
    }

    $scope.postVote = function (proposal) {
        $log.info("postVote");
        $log.info(proposal);

        http({
            method:'POST',
            url:'/proposals/' + proposal.id + '/vote/' + proposal.note
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    }

    $scope.rejeterRestant = function () {

        http({
            method:'POST',
            url:'/proposals/rejectall'
        }).success(function (data, status, headers, config) {
                $scope.proposals = ProposalService.query();
            });

    }
}


VoteController.$inject = ['$scope', '$log', 'VoteService', '$http'];
function VoteController($scope, $log, VoteService, $http) {

    $scope.checkloc(true);

    $scope.vote = VoteService.getVote();

    $log.info($scope.vote);

    $scope.submitVote = function () {
        var vote = $scope.vote;
        $http({
            method:'POST',
            url:'/admin/vote/' + vote.status
        }).success(function () {
                $scope.error = undefined;
                $scope.success = "Le changement de status du votes a bien \u00e9t\u00e9 pris en compte."
            }).error(function () {
                $scope.error = "Une erreur est survenue pendant le changement de status des votes";
                $scope.success = undefined;
            });
    }
}


SeeProposalsController.$inject = ['$scope', '$log', '$routeParams', 'ProposalService', '$http', 'VoteService', 'UserService', 'CreneauxService'];
function SeeProposalsController($scope, $log, $routeParams, ProposalService, http, VoteService, UserService, CreneauxService) {

    $scope.checkloc(false);

    $scope.proposal = ProposalService.get({id:$routeParams.proposalId}, function success(data) {
        $scope.proposal = data;
    });

    $scope.voteStatus = VoteService.getVote();

    $scope.formats = CreneauxService.query();

    $scope.commentE = Array();

    $scope.converter = new Markdown.getSanitizingConverter();

    $scope.getSafeDescription = function () {
        if ($scope.proposal.description) {
            return $scope.converter.makeHtml($scope.proposal.description);
        }
    }

    $scope.getSafeIndications = function () {
        if ($scope.proposal.indicationsOrganisateurs) {
            return $scope.converter.makeHtml($scope.proposal.indicationsOrganisateurs);
        }
    }

    $scope.postComment = function () {
        $log.info("Sauvegarde du commentaire " + $scope.comment);

        var data = {'comment':$scope.comment, 'private':$scope.private};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/comment',
            data:data
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = null;
                $scope.comment = null;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.postReponse = function (id) {
        $log.info("Sauvegarde de la reponse " + $scope.commentR);

        var commentId = id;
        var dataR = {'comment':$scope.commentR, 'private':$scope.privateR};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/comment/' + commentId + '/response',
            data:dataR
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.commentR = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.editComment = function (id) {
        $log.info("modification du commentaire " + $scope.commentE[id]);

        var commentId = id;
        var dataR = {'comment':$scope.commentE[id]};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/comment/' + commentId + '/edit',
            data:dataR
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.commentE = Array();
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.postCloseComment = function (id) {
        $log.info("cloture du commentaire " + id);

        var data = {};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/comment/' + id + '/close',
            data:data
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.hi=function()
    {
        alert('parent');
    }

    $scope.data = {name :'Bob'};

    $scope.deleteComment = function (id) {
        $log.info("suppression du commentaire " + id);

        var data = {};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/comment/' + id + '/delete',
            data:data
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.postStatus = function () {
        $log.info("postStatus");

        var data = {'status':$scope.proposal.statusProposal};

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/status',
            data:data
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    };

    $scope.postVote = function () {
        $log.info("postVote");
        $log.info($scope.proposal);

        http({
            method:'POST',
            url:'/proposals/' + $scope.proposal.id + '/vote/' + $scope.proposal.note
        }).success(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = undefined;
                $scope.proposal = ProposalService.get({id:$routeParams.proposalId});
            }).error(function (data, status, headers, config) {
                $log.info(status);
                $scope.errors = data;
            });
    }
}

ProfilController.$inject = ['$scope', '$log', '$routeParams', 'AccountService', 'ProfilService', 'UserService', '$http'];
function ProfilController($scope, $log, $routeParams, AccountService, ProfilService, UserService, http) {

    $scope.checkloc(false);

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    var idUSer = $routeParams.userId;
    $scope.pUser = ProfilService.getUser(idUSer);
    $scope.proposals = ProfilService.getProposals(idUSer);
    $scope.proposalsok = ProfilService.getAcceptedProposals(idUSer);

    $scope.getSafeDescription = function () {
        if ($scope.pUser.description) {
            $scope.descriptionE = $scope.pUser.description;
            return $scope.converter.makeHtml($scope.pUser.description);
        }
    }

    $scope.editProfil = function (id) {

        var data = {'description':$scope.descriptionE};

        http({
            method:'POST',
            url:'/admin/profil/' + id + '/edit',
            data:data
        }).success(function (data, status, headers, config) {
                $scope.errors = undefined;
                $scope.pUser = ProfilService.getUser(idUSer);
            }).error(function (data, status, headers, config) {
                $scope.errors = data;
                $log.info(status);
            });
    }
}


LinksAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http', '$location'];
function LinksAccountController($scope, $log, AccountService, UserService, http, $location) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.linkTypes = AccountService.getLinkType();
    $scope.addLink = false;
    $scope.addOther = false;


    $scope.linkUsername = function (link) {
        $scope.link.url = $scope.linkType.url + link.username;
    };


    $scope.addTypeLink = function () {
        $scope.addLink = false;
        $scope.addOther = false;

        if ($scope.typeLink != undefined) {
            $log.info("addTypeLink  " + $scope.typeLink);
            if ($scope.typeLink == 'OTHER') {
                $scope.addOther = true;
            } else {
                $scope.addLink = true;
            }

            $scope.linkTypes.forEach(function (entry) {
                if (entry.id == $scope.typeLink) {
                    if ($scope.link == undefined) {
                        $scope.link = {};
                    }
                    $scope.link.url = entry.url;
                    $scope.linkType = entry;
                }
            });


        }

    };

    $scope.removeLink = function (link) {
        if (confirm('\u00cates vous s\u00fbr de vouloir supprimer le lien ' + link.label + '?')) {
            $log.info("Suppression du link " + link.label + '(' + link.id + ')');
            http({
                method:'GET',
                url:'/settings/link/remove/' + link.id
            }).success(function (data, status, headers, config) {
                    $scope.errors = undefined;
                    var idUser = UserService.getUserData().id;
                    $scope.user = AccountService.getUser(idUser);
                }).error(function (data, status, headers, config) {
                    $log.info(status);
                    $scope.errors = data;
                });
        }
    };

    $scope.saveLinks = function () {
        var user = jQuery.extend(true, {}, $scope.user);
        if ($scope.link !== undefined) {

            if ($scope.typeLink != 'OTHER') {
                $scope.link.label = $scope.linkType.label;
                $scope.link.type = $scope.linkType.id;
            }

            user.links.push($scope.link);
        }

        http({
            method:'POST',
            url:'/settings/links',
            data:user
        }).success(function (data, status, headers, config) {
                $scope.errors = undefined;
                var idUser = UserService.getUserData().id;
                $scope.link = undefined;
                $scope.user = AccountService.getUser(idUser);
            }).error(function (data, status, headers, config) {
                $scope.errors = data;
                $log.info(status);
            });

    };
}

SettingsAccountController.$inject = ['$scope', '$log', 'AccountService', 'UserService', '$http', '$location'];
function SettingsAccountController($scope, $log, AccountService, UserService, http, $location) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = AccountService.getUser(idUser);

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    //$('.selectpicker').selectpicker();


    $scope.saveSettings = function () {
        var user = jQuery.extend(true, {}, $scope.user);

        http({
            method:'POST',
            url:'/settings/account',
            data:user
        }).success(function (data, status, headers, config) {
                $scope.errors = undefined;
                var idUser = UserService.getUserData().id;
                $scope.user = AccountService.getUser(idUser);
            }).error(function (data, status, headers, config) {
                $scope.errors = data;
                $log.info(status);
            });

    }

    $scope.appercu = function () {
        var user = jQuery.extend(true, {}, $scope.user);
        if ($scope.link !== undefined) {
            user.links.push($scope.link);
        }
        http({
            method:'POST',
            url:'/settings/account',
            data:user
        }).success(function (data, status, headers, config) {
                var idUser = UserService.getUserData().id;
                $location.path('/profil/' + idUser)
            }).error(function (data, status, headers, config) {
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

    $scope.saveSettings = function () {
        var user = jQuery.extend(true, {}, $scope.user);
        $http({
            method:'POST',
            url:'/settings/notifs',
            data:user
        }).success(function (data, status, headers, config) {
                $('#messageError').addClass('hide');
                $('#messageSuccess').text('Settings sauvegard\u00e9s');
                $('#messageSuccess').removeClass('hide');
                var idUser = UserService.getUserData().id;
                $scope.user = AccountService.getUser(idUser);
            }).error(function (data, status, headers, config) {
                $('#messageError').text('Une erreur a eu lieu pendant la sauvegarde des settings (' + status + ')');
                $('#messageError').removeClass('hide');
                $('#messageSuccess').addClass('hide');
                $log.info(status);
            });
    };
}


EmailAccountController.$inject = ['$rootScope', '$scope', '$log', 'UserService', 'AccountService', '$http'];
function EmailAccountController($rootScope, $scope, $log, UserService, AccountService, $http) {

    $scope.checkloc(false);

    var idUser = UserService.getUserData().id;
    $scope.user = $rootScope.user;

    $scope.changeEmail = function () {
        $http({
            method:'POST',
            url:'/settings/email',
            data:$scope.user
        }).success(function (data, status, headers, config) {
                $('#messageSuccess').text('Merci. Cet email nous servira \u00e0 vous contacter.');
                $('#messageSuccess').removeClass('hide');
                $scope.errors = undefined;
                $rootScope.user.email = $scope.user.email;
            }).error(function (data, status, headers, config) {
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

    $scope.changeMac = function () {
        $http({
            method:'POST',
            url:'/settings/mac',
            data:$scope.user
        }).success(function (data, status, headers, config) {
                $('#messageSuccess').text('Votre adresse mac a \u00e9t\u00e9 enregistr\u00e9e.');
                $('#messageSuccess').removeClass('hide');
                $scope.errors = undefined;
            }).error(function (data, status, headers, config) {
                $('#messageSuccess').addClass('hide');
                $scope.errors = data;
                $log.info(status);
            });
    }
}

ResetPasswordController.$inject = ['$scope', '$log', '$http'];
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
                $('#messageSuccess').text('Un mail a \u00e9t\u00e9 envoy\u00e9. Merci de v\u00e9rifier vos mails.');
                $('#messageSuccess').removeClass('hide');
            }).error(function (data, status, headers, config) {
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
        method:'GET',
        url:'/confirm/' + token
    }).success(function (data, status, headers, config) {
            $scope.successMessage = 'Votre compte est valid\u00e9';
            $scope.showSuccess = true;
        }).error(function (data, status, headers, config) {
            $scope.errorMessage = 'Une erreur a eu lieu pendant la confirmation (' + status + ')';
            $scope.showError = true;
        });
}


ConfirmResetPasswordController.$inject = ['$scope', '$log', '$http', '$routeParams', 'PasswordService'];
function ConfirmResetPasswordController($scope, $log, $http, $routeParams, PasswordService) {


    $scope.generatePassword = function () {
        $scope.generatedPassword = PasswordService.randomPassword();
        $scope.changeStrength($scope.generatedPassword, '#passwordStrengthDiv2')
    };

    $scope.changeStrength = function (password, divSelected) {

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

    $scope.resetPassword = function () {
        var data = new Object();
        data.inputPassword = $scope.inputPassword;
        var token = $routeParams.token;
        $http({
            method:'POST',
            url:'/reset/' + token,
            data:data
        }).success(function (data, status, headers, config) {
                $scope.successMessage = 'Votre nouveau mot de passe est enregistr\u00e9.';
                $('#valider').addClass('hide');
                $scope.showSuccess = true;
            }).error(function (data, status, headers, config) {
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
        method:'GET',
        url:'/email/' + token
    }).success(function (data, status, headers, config) {
            // TODO ajouter la nouvelle adresse email dans le message, une fois la vue scale supprimée.
            $scope.successMessage = 'Votre nouvelle adresse mail est valid\u00e9e';
            $scope.showSuccess = true;
        }).error(function (data, status, headers, config) {
            $scope.errorMessage = "Une erreur a eu lieu pendant le changement d'adresse (" + status + ')';
            $scope.showError = true;
        });
}


CreneauxController.$inject = ['$scope', '$log', 'CreneauxService'];
function CreneauxController($scope, $log, CreneauxService) {
    $scope.checkloc(true);

    $scope.formats = CreneauxService.query();

    $scope.deleteCreneau = function (creneauToDelete) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir supprimer le creneau ' + creneauToDelete.libelle + '?');
        if (confirmation) {
            CreneauxService.delete({id:creneauToDelete.id}, function (data) {
                $scope.formats = CreneauxService.query();
                $scope.errors = undefined;
            }, function (err) {
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

    $scope.saveCreneau = function () {
        $log.info("Format \u00e0 sauvegarder");
        $log.info($scope.format);

        CreneauxService.save($scope.format, function (data) {
            $log.info("Soummission du format ok");
            $location.url('/admin/formats');
        }, function (err) {
            $log.info("Soummission du format ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


EditCreneauController.$inject = ['$scope', '$log', 'CreneauxService', '$location', '$routeParams'];
function EditCreneauController($scope, $log, CreneauxService, $location, $routeParams) {
    $scope.checkloc(true);

    var idCreneau = $routeParams.creneauId;

    $scope.format = CreneauxService.get({id:idCreneau});

    $scope.isNew = false;

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveCreneau = function () {
        $log.info("Format \u00e0 sauvegarder");
        $log.info($scope.format);

        CreneauxService.save($scope.format, function (data) {
            $log.info("Soummission du format ok");
            $location.url('/admin/formats');
        }, function (err) {
            $log.info("Soummission du format ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


DynamicFieldsController.$inject = ['$scope', '$log', 'DynamicFieldsService'];
function DynamicFieldsController($scope, $log, DynamicFieldsService) {
    $scope.checkloc(true);

    $scope.dynamicFields = DynamicFieldsService.query();

    $scope.deleteDynamicField = function (dynamicFieldToDelete) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir supprimer le champ ' + dynamicFieldToDelete.name + '?');
        if (confirmation) {
            DynamicFieldsService.delete({id:dynamicFieldToDelete.id}, function (data) {
                $scope.dynamicFields = DynamicFieldsService.query();
                $scope.errors = undefined;
            }, function (err) {
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

    $scope.saveDynamicField = function () {
        $log.info("Champ dynamique \u00e0 sauvegarder");
        $log.info($scope.dynamicField);

        DynamicFieldsService.save($scope.dynamicField, function (data) {
            $log.info("Soummission du champ dynamique ok");
            $location.url('/admin/dynamicfields');
        }, function (err) {
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

    $scope.dynamicField = DynamicFieldsService.get({id:idDynamicField});

    $scope.isNew = false;

    $scope.saveDynamicField = function () {
        $log.info("Champ dynamique \u00e0 sauvegarder");
        $log.info($scope.dynamicField);

        DynamicFieldsService.save($scope.dynamicField, function (data) {
            $log.info("Soummission du champ dynamique ok");
            $location.url('/admin/dynamicfields');
        }, function (err) {
            $log.info("Soummission du champ dynamique ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}

MailingController.$inject = [ '$scope', '$http', '$log', '$location'];
function MailingController($scope, $http, $log, $location) {
    $scope.checkloc(true);

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();


    $scope.sendMail = function () {
        if ($scope.status !== undefined && $scope.mail !== undefined && $scope.subject !== undefined) {
            $http({
                method:'POST',
                url:'/admin/mailing/' + $scope.status,
                data:{
                    subject:$scope.subject,
                    mail:$scope.mail
                }
            }).
                success(function (data, status) {
                    $location.url('/admin/proposals/list');
                }).
                error(function (data, status) {
                    $log.error(data);
                });

        }
    }

}

EventController.$inject = ['$scope', '$log', 'EventService', '$location'];
function EventController($scope, $log, EventService, $location) {
    $scope.checkloc(true);

    $scope.events = EventService.query();

    $scope.deleteEvent = function (eventToDelete) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir supprimer l\'événement ' + eventToDelete.name + '?');
        if (confirmation) {
            EventService.delete({id:eventToDelete.id}, function (data) {
                $scope.events = EventService.query();
                $scope.errors = undefined;
            }, function (err) {
                $log.info("Delete de l'événenemt ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }

}


NewEventController.$inject = ['$scope', '$log', 'EventService', '$location'];
function NewEventController($scope, $log, EventService, $location) {
    $scope.checkloc(true);

    $scope.isNew = true;

    $scope.converter = new Markdown.Converter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveEvent = function () {
        $log.info("Evénement \u00e0 sauvegarder");
        $log.info($scope.event);

        EventService.save($scope.event, function (data) {
            $log.info("Soummission de l'événement ok");
            $location.url('/admin/events');
        }, function (err) {
            $log.info("Soummission de l'événement ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


EditEventController.$inject = ['$scope', '$log', 'EventService', '$location', '$routeParams'];
function EditEventController($scope, $log, EventService, $location, $routeParams) {
    $scope.checkloc(true);

    var idEvent = $routeParams.eventId;

    $scope.event = EventService.get({id:idEvent});

    $scope.isNew = false;

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.editorCgu = new Markdown.Editor($scope.converter,'-cgu');
    $scope.editorCgu.run();

    $scope.saveEvent = function () {
        $log.info("Evénement \u00e0 sauvegarder");
        $log.info($scope.event);

        EventService.save($scope.event, function (data) {
            $log.info("Soummission de l'événement ok");
            $location.url('/admin/events');
        }, function (err) {
            $log.info("Soummission de l'événement ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}

SeeEventController.$inject = ['$scope', '$log','$http', 'EventService','EventOrganizersService', '$location', '$routeParams'];
function SeeEventController($scope, $log,$http, EventService,EventOrganizersService, $location, $routeParams) {
    $scope.checkloc(true);

    var idEvent = $routeParams.id;

    $scope.event = EventService.get({id:idEvent});
    $scope.organizersEvent = EventOrganizersService.query({id:idEvent});

    $scope.converter = new Markdown.getSanitizingConverter();

    $scope.getSafeDescription = function () {
        if ($scope.event.description) {
            return $scope.converter.makeHtml($scope.event.description);
        }
    }

    $scope.getSafeCGU = function () {
        if ($scope.event.cgu) {
            return $scope.converter.makeHtml($scope.event.cgu);
        }
    }

    $http.get("/admin/users/get").success(function (data) {
        $scope.organizers = data;
    });

    $scope.addOrganizer = function () {

        $log.info('orga '+$scope.organizersEvent.length);
        if ($scope.organiserNew !== undefined) {

            var found = false;

            angular.forEach($scope.organizersEvent, function (organizerItem) {

                if (organizerItem.id === $scope.organiserNew.id) {

                    found = true;
                }
            });

            if (!found) {
                $log.info('item '+ $scope.organiserNew.fullname);
                $scope.organizersEvent.push($scope.organiserNew);
                $scope.event.organizers= $scope.organizersEvent;
            }
            $scope.organiserNew = undefined;
            $log.info('orga '+$scope.event.organizers.length);
        }
    };

    $scope.removeOrganizer = function (organizer) {
        $log.info('remove '+ organizer.fullname);
        $scope.organizersEvent.splice($scope.organizersEvent.indexOf(organizer), 1);
        $scope.event.organizers= $scope.organizersEvent;
    };

    $scope.saveEvent = function () {
        $log.info("Sauvegarde de l'event : " + $routeParams.id);

        EventService.save($scope.event, function (data) {
            $log.info("Soummission de l'event ok");
            $location.url('/event/'+$routeParams.id);
        }, function (err) {
            $log.info("Soummission de l'event ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    };


}



TrackController.$inject = ['$scope', '$log', 'TrackService', '$location'];
function TrackController($scope, $log, TrackService, $location) {
    $scope.checkloc(true);

    $scope.tracks = TrackService.query();

    $scope.deleteTrack = function (trackToDelete) {
        var confirmation = confirm('\u00cates vous s\u00fbr de vouloir supprimer le track ' + trackToDelete.name + '?');
        if (confirmation) {
            TrackService.delete({id:trackToDelete.id}, function (data) {
                $scope.tracks = TrackService.query();
                $scope.errors = undefined;
            }, function (err) {
                $log.info("Delete track ko");
                $log.info(err);
                $scope.errors = err.data;
            });
        }
    }


}


NewTrackController.$inject = ['$scope', '$log', 'TrackService', '$location'];
function NewTrackController($scope, $log, TrackService, $location) {
    $scope.checkloc(true);

    $scope.isNew = true;

    $scope.converter = new Markdown.Converter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveTrack = function () {
        $log.info("Track \u00e0 sauvegarder");
        $log.info($scope.track);

        TrackService.save($scope.track, function (data) {
            $log.info("Soummission du track ok");
            $location.url('/admin/tracks');
        }, function (err) {
            $log.info("Soummission du Track ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}


EditTrackController.$inject = ['$scope', '$log', 'TrackService', '$location', '$routeParams'];
function EditTrackController($scope, $log, TrackService, $location, $routeParams) {
    $scope.checkloc(true);

    var idTrack = $routeParams.id;

    $scope.track = TrackService.get({id:idTrack});

    $scope.isNew = false;

    $scope.converter = new Markdown.getSanitizingConverter();
    $scope.editor = new Markdown.Editor($scope.converter);
    $scope.editor.run();

    $scope.saveTrack = function () {
        $log.info("Track \u00e0 sauvegarder");
        $log.info($scope.track);

        TrackService.save($scope.track, function (data) {
            $log.info("Soummission du Track ok");
            $location.url('/admin/tracks');
        }, function (err) {
            $log.info("Soummission du Track ko");
            $log.info(err.data);
            $scope.errors = err.data;
        });
    }
}

SeeTrackController.$inject = ['$scope', '$log', 'TrackService', 'TrackProposalService', '$location', '$routeParams'];
function SeeTrackController($scope, $log, TrackService, TrackProposalService, $location, $routeParams) {
    $scope.checkloc(true);

    var idTrack = $routeParams.id;

    $scope.track = TrackService.get({id:idTrack});

    $scope.proposals = TrackProposalService.query({id:idTrack});

    $scope.converter = new Markdown.getSanitizingConverter();

    $scope.getSafeDescription = function () {
        if ($scope.track.description) {
            return $scope.converter.makeHtml($scope.track.description);
        }
    }

}

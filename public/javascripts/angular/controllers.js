'use strict';

/* Controllers */
function RootController($scope, UserService) {

	$scope.userService = UserService;
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
RootController.$inject = ['$scope', 'UserService'];



function LoginController($scope, $log, UserService) {

	// Fonction de login appelée sur le bouton de formulaire
	$scope.login = function() {
		$log.info($scope.user.email);
		// TODO Trouver un moyen pour que le routage ne soit pas fait dans le callback du XHR ?
		UserService.login($scope.user, '/dashboard');
	}
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = ['$scope', '$log', 'UserService'];



function NewTalkController($scope, $log, $location, TalkService) {

	$scope.talk;
	$scope.$location = $location;
	
	$scope.isNew = true;

	$scope.saveTalk = function() {
		$log.info("Soummission du nouveau talk");
		
		TalkService.save($scope.talk, function(data) {	
			$log.info("Soummission du talk ok");
			$location.url('/managetalk');
		}, function (err) {
			$log.info("Soummission du talk ko : " + err);
		});	
	}
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
NewTalkController.$inject = ['$scope', '$log', '$location', 'TalkService'];

function EditTalkController($scope, $log, $location, $routeParams, TalkService) {

	$scope.talk = TalkService.get({id:$routeParams.talkId});
	$scope.$location = $location;
	
	$scope.isNew = false;
	
	$scope.saveTalk = function() {
		$log.info("Sauvegarde du talk : " + $routeParams.talkId);
		
		TalkService.save($scope.talk, function(data) {	
			$log.info("Soummission du talk ok");
			$location.url('/managetalk');
		}, function (err) {
			$log.info("Soummission du talk ko : " + err);
		});
		
	}
	
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
EditTalkController.$inject = ['$scope', '$log', '$location', '$routeParams', 'TalkService'];


function ManageTalkController($scope, $log, $location, TalkService) {
	
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
    $scope.talks = AllTalkService.query();
}
ListTalksController.$inject = ['$scope', '$log', 'AllTalkService'];
'use strict';

/* Controllers */
function RootController($scope, UserService) {

	$scope.userService = UserService;

}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
RootController.$inject = ['$scope', 'UserService'];



function LoginController($scope, $log, UserService) {

	// Fonction de login appelée sur le bouton de formulaire
	$scope.login = function(user) {
		$log.info($scope.user.email);

		// TODO Trouver un moyen pour que le routage ne soit pas fait dans le callback du XHR ?
		UserService.login($scope.user, '/dashboard');

	}
}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = ['$scope', '$log', 'UserService'];



function TalkController($scope, $log, TalkService) {

	$scope.talk = null;
	
	$scope.talks = TalkService.query();
	
	$scope.submitTalk = function() {
		$log.info("Soummission d'un talk");
		TalkService.save($scope.talk);
	}

}
// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
TalkController.$inject = ['$scope', '$log', 'TalkService'];
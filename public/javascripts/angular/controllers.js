'use strict';

/* Controllers */
function LoginController($scope, $log, userService) {

	// Fonction de login appelée sur le bouton de formulaire
	$scope.login = function(user) {
		$log.info($scope.user.email);

		// TODO Trouver un moyen pour que le routage ne soit pas fait dans le callback du XHR ?
		userService.login($scope.user, '/dashboard');

	}
}

// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = ['$scope', '$log', 'userService'];


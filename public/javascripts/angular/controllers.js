'use strict';

/* Controllers */
function LoginController($scope, $rootScope, $log, $http, $location) {

	// Fonction de login appelée sur le bouton de formulaire
	$scope.login = function(user) {
		$log.info($scope.user.email);

		$http({method : 'POST',	url : '/login',	data : $scope.user}).
		  success(function(data, status, headers, config) {
			// this callback will be called asynchronously
			// when the response is available
			$log.info(status);
			$log.info(data);
			
			// Mise à jour des variables de l'appli
			$rootScope.authenticaded = true;
			if (data.admin) $rootScope.admin=true;
			$rootScope.loggeduser= data;
			
			// Routage vers le dashboard
			$location.url('/dashboard');
			
		 }).
		  error(function(data, status, headers, config) {
			// called asynchronously if an error occurs
			// or server returns response with status
			// code outside of the <200, 400) range
			$log.info(status);
			$log.info(data);
		});
	}
}

// Pour que l'injection de dépendances fonctionne en cas de 'minifying'
LoginController.$inject = [$scope, $rootScope, $log, $http, $location];


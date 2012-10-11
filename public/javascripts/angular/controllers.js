'use strict';

/* Controllers */

function LoginCtrl($scope, $log, $http, $rootScope, $location) {

	$rootScope.loggeduser = null;
	$rootScope.islogged = false;
	$rootScope.isadmin = false;
	
	$scope.login = function(user) {
		$log.info($scope.user.email);

		$http({method : 'POST',	url : '/login',	data : $scope.user}).
		  success(function(data, status, headers, config) {
			// this callback will be called asynchronously
			// when the response is available
			$log.info(status);
			$log.info(data);
			$rootScope.loggeduser = data;
			$rootScope.islogged = true;
			$rootScope.isadmin = data.admin;
			//$location.url('/dashboard');
			
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

// LoginCtrl.$inject = [$scope];


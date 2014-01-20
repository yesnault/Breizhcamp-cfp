'use strict';


// Declare app level module which depends on filters, and services
var App = angular.module('breizhCampCFP', ['breizhCampCFP.services', 'breizhCampCFP.directives', 'ui.bootstrap','angular-table']);


// Configuration des routes
App.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {templateUrl: '/assets/pages/partials/login.html', controller: LoginController});
    $routeProvider.when('/signup', {templateUrl: '/assets/pages/partials/signup.html'});
    $routeProvider.when('/dashboard', {templateUrl: '/assets/pages/partials/dashboard.html', controller: DashboardController});
    $routeProvider.when('/submitproposal/new', {templateUrl: '/assets/pages/partials/submitproposal.html', controller: NewProposalController});
    $routeProvider.when('/editproposal/:proposalId', {templateUrl: '/assets/pages/partials/submitproposal.html', controller: EditProposalController});
    $routeProvider.when('/manageproposal', {templateUrl: '/assets/pages/partials/manageproposals.html', controller: ManageProposalController});
    $routeProvider.when('/admin/users', {templateUrl: '/assets/pages/partials/admin/users.html', controller: ManageUsersController});
    $routeProvider.when('/admin/proposals/list', {templateUrl: '/assets/pages/partials/admin/listproposals.html', controller: ListProposalsController});
    $routeProvider.when('/admin/vote', {templateUrl: 'assets/pages/partials/admin/vote.html', controller: VoteController});
    $routeProvider.when('/admin/formats', {templateUrl: 'assets/pages/partials/admin/formats.html', controller: CreneauxController});
    $routeProvider.when('/admin/format/new', {templateUrl: 'assets/pages/partials/admin/submitcreneau.html', controller: NewCreneauController});
    $routeProvider.when('/admin/format/edit/:creneauId', {templateUrl: 'assets/pages/partials/admin/submitcreneau.html', controller: EditCreneauController});

    $routeProvider.when('/admin/events', {templateUrl: 'assets/pages/partials/event/events.html', controller: EventController});
    $routeProvider.when('/admin/event/new', {templateUrl: 'assets/pages/partials/event/submit.html', controller: NewEventController});
    $routeProvider.when('/admin/event/edit/:eventId', {templateUrl: 'assets/pages/partials/event/submit.html', controller: EditEventController});

    $routeProvider.when('/admin/tracks', {templateUrl: 'assets/pages/partials/track/tracks.html', controller: TrackController});
    $routeProvider.when('/admin/track/new', {templateUrl: 'assets/pages/partials/track/submit.html', controller: NewTrackController});
    $routeProvider.when('/admin/track/edit/:id', {templateUrl: 'assets/pages/partials/track/submit.html', controller: EditTrackController});


    $routeProvider.when('/admin/dynamicfields', {templateUrl: 'assets/pages/partials/admin/dynamicfields.html', controller: DynamicFieldsController});
    $routeProvider.when('/admin/dynamicfield/new', {templateUrl: 'assets/pages/partials/admin/submitdynamicfield.html', controller: NewDynamicFieldController});
    $routeProvider.when('/admin/dynamicfield/edit/:dynamicFieldId', {templateUrl: 'assets/pages/partials/admin/submitdynamicfield.html', controller: EditDynamicFieldController});
    $routeProvider.when('/admin/mailing', {templateUrl: 'assets/pages/partials/admin/mailing.html', controller: MailingController});

    $routeProvider.when('/proposals/see/:proposalId', {templateUrl: '/assets/pages/partials/seeproposal.html', controller: SeeProposalsController});

    $routeProvider.when('/profil/:userId', {templateUrl: '/assets/pages/partials/profil.html', controller: ProfilController});

    $routeProvider.when('/settings/account', {templateUrl: '/assets/pages/partials/settings/account.html', controller: SettingsAccountController});
    $routeProvider.when('/settings/notifs', {templateUrl: '/assets/pages/partials/settings/notifs.html', controller: NotifsAccountController});
    $routeProvider.when('/settings/email', {templateUrl: '/assets/pages/partials/settings/email.html', controller: EmailAccountController});
    $routeProvider.when('/settings/mac', {templateUrl: '/assets/pages/partials/settings/mac.html', controller: MacAccountController});
    $routeProvider.when('/reset/ask', {templateUrl: 'assets/pages/partials/resetpassword.html', controller: ResetPasswordController});
    $routeProvider.when('/reset/:token', {templateUrl: 'assets/pages/partials/confirmresetpassword.html', controller: ConfirmResetPasswordController});
    $routeProvider.when('/confirm/:token', {templateUrl: 'assets/pages/partials/confirmsignup.html', controller: ConfirmSignupController});
    $routeProvider.when('/email/:token', {templateUrl: 'assets/pages/partials/settings/confirmemail.html', controller: ConfirmEmailController});
    $routeProvider.otherwise({redirectTo: '/dashboard'}); 
  }]);

// Intercepteur HTTP pour gérer le timeout de session
// Inspiré de http://www.espeo.pl/2012/02/26/authentication-in-angularjs-application
App.config(function($httpProvider) {
      
    var authentInterceptor = ['$rootScope', '$q', '$log', function (scope, $q, $log) {
        function success(response) {
            return response;
        }
        function error(response) {
            var status = response.status;
            if (status == 401) {
                $log.info('401 -> event:unauthorized');
                scope.$broadcast('event:unauthorized');
            }
            return $q.reject(response);
        }
        return function(promise) {
            return promise.then(success, error);
        };
    }];
    $httpProvider.responseInterceptors.push(authentInterceptor);
});


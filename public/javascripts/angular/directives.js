'use strict';

/* Directives */

var directives = angular.module('breizhCampCFP.directives', []);

directives.directive('sessionhandler', function($location, $log) {
    // Directive pour gérer la fin de session
    // Evenement envoyé par un intercepteur HTTP
    return function(scope, element, attrs) {
        scope.$on('event:unauthorized', function() {
            location.reload(true);
        });
    };
});

directives.directive('pagedownInit', function($log) {
    // Utilisé pour rafraîchir la zone de preview MarkDown
    // editor.run() doit avoir été appelé dans le contrôleur auparavant
    return function(scope, element, attrs) {
        scope.$watch(attrs.pagedownInit, function(value) {
            scope.editor.refreshPreview();
        });
    };
});


directives.directive('star', function factory() {
    var directiveDefinitionObject = {
        template: '<div id="star"> </div>',
        restrict: 'E',
        replace: true,
        require: 'ngModel',
        transclude: true,
        link: function postLink(scope, iElement, iAttr,ngModel) {
            $.fn.raty.defaults.path = '/assets/img/';
            $('#star').raty({
                click: function(score, evt) {
                    scope.$parent.proposal = ngModel.$modelValue;
                    scope.$parent.proposal.note = score;
                    scope.$digest();
                }
            });

            scope.$watch(function () {
                return ngModel.$modelValue.note;
            }, function(value) {
                $('#star').raty('score', value != undefined ? value : 1);
            });

        }

    };
    return directiveDefinitionObject;
});

directives.directive('profil', function factory() {
    var directiveDefinitionObject = {
        templateUrl: 'assets/pages/templates/profil.html',
        restrict: 'E',
        replace: true,
        require: 'ngModel',
        transclude: true,
        link: function (scope, element, attrs) {
            scope.settings= attrs.settings;
            scope.publicView = attrs.publicView;
        }
    };
    return directiveDefinitionObject;
});


directives.directive('reponse', function factory() {
    var directiveDefinitionObject = {
        templateUrl: 'assets/pages/templates/commentaireReponse.html',
        restrict: 'E',
        replace: true,
        require: 'ngModel',
        transclude: true,
        link: function (scope, element, attrs) {

        }
    };
    return directiveDefinitionObject;
});


directives.directive('comment', function ($compile) {
    var directiveDefinitionObject = {
        templateUrl: 'assets/pages/templates/comment.html',
        restrict: 'E',
        replace: true,
        require: 'ngModel',
        transclude: true,
        link: function (scope, element, attrs,controller) {

        }
    };
    return directiveDefinitionObject;
});


//directives.directive('stars', function factory() {
//    var directiveDefinitionObject = {
//        restrict: 'E',
//        replace: true,
//        require: 'ngModel',
//        scope: {
//            proposalid: '@'
//        },
//        transclude: true,
//        template: '<div id="star{{proposalid}}" > </div>',
//        link: function postLink(scope, iElement, iAttr,ngModel) {
//            $.fn.raty.defaults.path = '/assets/img/';
 //          $('#star'+scope.proposalid).raty({
//                click: function(score, evt) {
//                    scope.$parent.proposalModal = ngModel.$modelValue;
//                    scope.$parent.proposalModal.note = score;
//                    scope.$digest();
  //              }
//            });
//
//            scope.$watch(function () {
//                return ngModel.$modelValue!= undefined ?  ngModel.$modelValue.note:1;
//            }, function(value) {
//                console.debug('#star'+scope.proposalid+'= '+value);
//                $('#star'+scope.proposalid).raty('score',  value);
//            });

//        }

//    };
//    return directiveDefinitionObject;
//});




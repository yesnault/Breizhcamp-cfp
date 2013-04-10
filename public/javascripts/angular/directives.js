'use strict';

/* Directives */

var directives = angular.module('breizhCampCFP.directives', []);

directives.directive('sessionhandler', function($location, $log) {
    // Directive pour gérer la fin de session
    // Evenement envoyé par un intercepteur HTTP
    return function(scope, element, attrs) {
        scope.$on('event:unauthorized', function() {
            $location.path('/login');
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
        transclude: true,
        link: function postLink(scope, iElement, iAttrs) {
            $.fn.raty.defaults.path = '/assets/img/';
            $('#star').raty({
                click: function(score, evt) {
                    scope.talk.note = score;
                    scope.$digest();
                }
            });

            scope.$watch('talk.note', function(value) {
                $('#star').raty('score', value != undefined ? value : 1);
            });
        }

    };
    return directiveDefinitionObject;
});
